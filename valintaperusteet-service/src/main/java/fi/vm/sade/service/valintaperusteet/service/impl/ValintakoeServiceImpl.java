package fi.vm.sade.service.valintaperusteet.service.impl;

import fi.vm.sade.service.valintaperusteet.dao.LaskentakaavaDAO;
import fi.vm.sade.service.valintaperusteet.dao.ValintakoeDAO;
import fi.vm.sade.service.valintaperusteet.dto.ValintakoeDTO;
import fi.vm.sade.service.valintaperusteet.model.*;
import fi.vm.sade.service.valintaperusteet.service.OidService;
import fi.vm.sade.service.valintaperusteet.service.ValinnanVaiheService;
import fi.vm.sade.service.valintaperusteet.service.ValintakoeService;
import fi.vm.sade.service.valintaperusteet.service.exception.*;
import fi.vm.sade.service.valintaperusteet.util.LinkitettavaJaKopioitavaUtil;
import fi.vm.sade.service.valintaperusteet.util.ValintakoeKopioija;
import fi.vm.sade.service.valintaperusteet.util.ValintakoeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * User: kwuoti
 * Date: 15.4.2013
 * Time: 16.13
 */
@Transactional
@Service
public class ValintakoeServiceImpl extends AbstractCRUDServiceImpl<Valintakoe, Long, String> implements ValintakoeService {

    @Autowired
    private ValintakoeDAO valintakoeDAO;

    @Autowired
    private ValinnanVaiheService valinnanVaiheService;

    @Autowired
    private LaskentakaavaDAO laskentakaavaDAO;

    @Autowired
    private OidService oidService;

    private static ValintakoeKopioija kopioija = new ValintakoeKopioija();

    @Autowired
    public ValintakoeServiceImpl(ValintakoeDAO dao) {
        super(dao);
        this.valintakoeDAO = dao;
    }

    @Override
    public Valintakoe update(String s, Valintakoe entity) {
        throw new UnsupportedOperationException("not supported");
    }

    @Override
    public Valintakoe insert(Valintakoe entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteByOid(String oid) {
        Valintakoe valintakoe = haeValintakoeOidilla(oid);
        if (valintakoe.getMaster() != null) {
            throw new ValintakoettaEiVoiPoistaaException("Valintakoe on peritty.");
        }

        removeValintakoe(valintakoe);
    }

    private void removeValintakoe(Valintakoe valintakoe) {
        for (Valintakoe koe : valintakoe.getKopiot()) {
            removeValintakoe(koe);
        }

        valintakoeDAO.remove(valintakoe);
    }

    @Override
    public Valintakoe readByOid(String oid) {
        return haeValintakoeOidilla(oid);
    }

    private Valintakoe haeValintakoeOidilla(String oid) {
        Valintakoe valintakoe = valintakoeDAO.readByOid(oid);
        if (valintakoe == null) {
            throw new ValintakoettaEiOleOlemassaException("Valintakoetta (oid " + oid + ") ei ole olemassa");
        }

        return valintakoe;
    }

    @Override
    public List<Valintakoe> findValintakoeByValinnanVaihe(String oid) {
        return valintakoeDAO.findByValinnanVaihe(oid);
    }

    @Override
    public Valintakoe lisaaValintakoeValinnanVaiheelle(String valinnanVaiheOid, ValintakoeDTO koe) {
        ValinnanVaihe valinnanVaihe = valinnanVaiheService.readByOid(valinnanVaiheOid);
        if (!ValinnanVaiheTyyppi.VALINTAKOE.equals(valinnanVaihe.getValinnanVaiheTyyppi())) {
            throw new ValintakoettaEiVoiLisataException("Valintakoetta ei voi lisätä valinnan vaiheelle, jonka " +
                    "tyyppi on " + valinnanVaihe.getValinnanVaiheTyyppi().name());
        }

        Valintakoe valintakoe = new Valintakoe();
        valintakoe.setOid(oidService.haeValintakoeOid());
        valintakoe.setTunniste(koe.getTunniste());
        valintakoe.setNimi(koe.getNimi());
        valintakoe.setKuvaus(koe.getKuvaus());
        valintakoe.setValinnanVaihe(valinnanVaihe);
        valintakoe.setAktiivinen(koe.getAktiivinen());

        if (koe.getLaskentakaavaId() != null) {
            valintakoe.setLaskentakaava(haeLaskentakaavaValintakokeelle(koe.getLaskentakaavaId()));
        }
        Valintakoe lisatty = valintakoeDAO.insert(valintakoe);
        for (ValinnanVaihe kopio : valinnanVaihe.getKopiot()) {
            lisaaValinnanVaiheelleKopioMasterValintakokeesta(kopio, lisatty);
        }

        return lisatty;
    }

    private void lisaaValinnanVaiheelleKopioMasterValintakokeesta(ValinnanVaihe valinnanVaihe,
                                                                  Valintakoe masterValintakoe) {
        Valintakoe kopio = ValintakoeUtil.teeKopioMasterista(masterValintakoe);
        kopio.setValinnanVaihe(valinnanVaihe);
        kopio.setOid(oidService.haeValintakoeOid());

        Valintakoe lisatty = valintakoeDAO.insert(kopio);

        for (ValinnanVaihe vaihekopio : valinnanVaihe.getKopioValinnanVaiheet()) {
            lisaaValinnanVaiheelleKopioMasterValintakokeesta(vaihekopio, lisatty);
        }
    }

    private Laskentakaava haeLaskentakaavaValintakokeelle(Long laskentakaavaId) {
        Laskentakaava laskentakaava = laskentakaavaDAO.getLaskentakaava(laskentakaavaId);
        if (laskentakaava == null) {
            throw new LaskentakaavaEiOleOlemassaException("Laskentakaavaa (" + laskentakaavaId + ") ei ole " +
                    "olemassa", laskentakaavaId);
        } else if (!Funktiotyyppi.TOTUUSARVOFUNKTIO.equals(laskentakaava.getTyyppi())) {
            throw new VaaranTyyppinenLaskentakaavaException("Valintakokeen laskentakaavan tulee olla tyyppiä " +
                    Funktiotyyppi.TOTUUSARVOFUNKTIO.name());
        } else if (laskentakaava.getOnLuonnos()) {
            throw new ValintakokeeseenLiitettavaLaskentakaavaOnLuonnosException("Valintakokeeseen liitettävä " +
                    "laskentakaava on LUONNOS-tilassa");
        }

        return laskentakaava;
    }

    @Override
    public Valintakoe update(String oid, ValintakoeDTO valintakoe) {
        Valintakoe incoming = new Valintakoe();
        incoming.setAktiivinen(valintakoe.getAktiivinen());
        incoming.setKuvaus(valintakoe.getKuvaus());
        incoming.setNimi(valintakoe.getNimi());
        incoming.setTunniste(valintakoe.getTunniste());

        Valintakoe managedObject = haeValintakoeOidilla(oid);
        Long laskentakaavaOid = valintakoe.getLaskentakaavaId();

        if (laskentakaavaOid != null) {
            Laskentakaava laskentakaava = haeLaskentakaavaValintakokeelle(laskentakaavaOid);
            incoming.setLaskentakaava(laskentakaava);
        } else {
            incoming.setLaskentakaava(null);
        }

        return LinkitettavaJaKopioitavaUtil.paivita(managedObject, incoming, kopioija);
    }

    @Override
    public void kopioiValintakokeetMasterValinnanVaiheeltaKopiolle(ValinnanVaihe valinnanVaihe, ValinnanVaihe masterValinnanVaihe) {
        List<Valintakoe> kokeet = valintakoeDAO.findByValinnanVaihe(masterValinnanVaihe.getOid());
        for (Valintakoe master : kokeet) {
            Valintakoe kopio = ValintakoeUtil.teeKopioMasterista(master);
            kopio.setOid(oidService.haeValintakoeOid());
            valinnanVaihe.addValintakoe(kopio);

            valintakoeDAO.insert(kopio);
        }
    }
}
