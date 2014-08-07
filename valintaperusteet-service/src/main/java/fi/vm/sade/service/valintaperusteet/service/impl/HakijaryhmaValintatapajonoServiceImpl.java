package fi.vm.sade.service.valintaperusteet.service.impl;

import fi.vm.sade.service.valintaperusteet.dao.HakijaryhmaDAO;
import fi.vm.sade.service.valintaperusteet.dao.HakijaryhmaValintatapajonoDAO;
import fi.vm.sade.service.valintaperusteet.dto.HakijaryhmaCreateDTO;
import fi.vm.sade.service.valintaperusteet.dto.HakijaryhmaValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.dto.HakijaryhmaValintatapajonoUpdateDTO;
import fi.vm.sade.service.valintaperusteet.dto.mapping.ValintaperusteetModelMapper;
import fi.vm.sade.service.valintaperusteet.model.Hakijaryhma;
import fi.vm.sade.service.valintaperusteet.model.HakijaryhmaValintatapajono;
import fi.vm.sade.service.valintaperusteet.model.HakukohdeViite;
import fi.vm.sade.service.valintaperusteet.model.Valintatapajono;
import fi.vm.sade.service.valintaperusteet.service.*;
import fi.vm.sade.service.valintaperusteet.service.exception.HakijaryhmaEiOleOlemassaException;
import fi.vm.sade.service.valintaperusteet.service.exception.HakijaryhmaOidListaOnTyhjaException;
import fi.vm.sade.service.valintaperusteet.service.exception.HakijaryhmaaEiVoiPoistaaException;
import fi.vm.sade.service.valintaperusteet.util.HakijaryhmaValintatapajonoKopioija;
import fi.vm.sade.service.valintaperusteet.util.LinkitettavaJaKopioitavaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * User: jukais
 * Date: 1.10.2013
 * Time: 16.23
 */
@Service
@Transactional
public class HakijaryhmaValintatapajonoServiceImpl implements HakijaryhmaValintatapajonoService {

    @Autowired
    private HakijaryhmaDAO hakijaryhmaDAO;

    @Autowired
    private HakijaryhmaValintatapajonoDAO hakijaryhmaValintatapajonoDAO;

    @Autowired
    private OidService oidService;

    @Autowired
    private ValintaryhmaService valintaryhmaService;

    @Autowired
    private HakukohdeService hakukohdeService;

    @Autowired
    private ValintatapajonoService valintapajonoService;

    @Autowired
    private LaskentakaavaService laskentakaavaService;

    @Autowired
    private ValintaperusteetModelMapper modelMapper;

    private static HakijaryhmaValintatapajonoKopioija kopioija = new HakijaryhmaValintatapajonoKopioija();

    private HakijaryhmaValintatapajono haeHakijaryhmaValintatapajono(String oid) {
        HakijaryhmaValintatapajono hakijaryhma = hakijaryhmaValintatapajonoDAO.readByOid(oid);

        if (hakijaryhma == null) {
            throw new HakijaryhmaEiOleOlemassaException("Hakijaryhmavalintatapajono (" + oid + ") ei ole olemassa", oid);
        }

        return hakijaryhma;
    }

    @Override
    public List<HakijaryhmaValintatapajono> findHakijaryhmaByJono(String oid) {
        return hakijaryhmaValintatapajonoDAO.findByValintatapajono(oid);
    }

    @Override
    public HakijaryhmaValintatapajono readByOid(String oid) {
        return haeHakijaryhmaValintatapajono(oid);
    }

    @Override
    public List<HakijaryhmaValintatapajono> findByHakijaryhma(String hakijaryhmaOid) {
        return hakijaryhmaValintatapajonoDAO.findByHakijaryhma(hakijaryhmaOid);
    }

    @Override
    public Hakijaryhma lisaaHakijaryhmaValintatapajonolle(String valintatapajonoOid, HakijaryhmaCreateDTO dto) {

        Hakijaryhma hakijaryhma = modelMapper.map(dto, Hakijaryhma.class);
        Valintatapajono valintatapajono = valintapajonoService.readByOid(valintatapajonoOid);
        HakijaryhmaValintatapajono edellinenHakijaryhma = hakijaryhmaValintatapajonoDAO.haeValintatapajononViimeinenHakijaryhma(valintatapajonoOid);

        hakijaryhma.setOid(oidService.haeHakijaryhmaOid());
        hakijaryhma.setKaytaKaikki(dto.isKaytaKaikki());
        hakijaryhma.setTarkkaKiintio(dto.isTarkkaKiintio());
        hakijaryhma.setLaskentakaava(laskentakaavaService.haeMallinnettuKaava(dto.getLaskentakaavaId()));

        Hakijaryhma lisatty = hakijaryhmaDAO.insert(hakijaryhma);

        HakijaryhmaValintatapajono jono = new HakijaryhmaValintatapajono();
        jono.setOid(oidService.haeValintatapajonoHakijaryhmaOid());
        jono.setHakijaryhma(hakijaryhma);
        jono.setValintatapajono(valintatapajono);
        jono.setAktiivinen(true);
        jono.setEdellinen(edellinenHakijaryhma);

        hakijaryhmaValintatapajonoDAO.insert(jono);

        LinkitettavaJaKopioitavaUtil.asetaSeuraava(edellinenHakijaryhma, jono);

        return lisatty;
    }

    @Override
    public Hakijaryhma lisaaHakijaryhmaHakukohteelle(String hakukohdeOid, HakijaryhmaCreateDTO dto) {
        Hakijaryhma hakijaryhma = modelMapper.map(dto, Hakijaryhma.class);
        HakukohdeViite hakukohde = hakukohdeService.readByOid(hakukohdeOid);
        HakijaryhmaValintatapajono edellinenHakijaryhma = hakijaryhmaValintatapajonoDAO.haeHakukohteenViimeinenHakijaryhma(hakukohdeOid);

        hakijaryhma.setOid(oidService.haeHakijaryhmaOid());
        hakijaryhma.setKaytaKaikki(dto.isKaytaKaikki());
        hakijaryhma.setTarkkaKiintio(dto.isTarkkaKiintio());
        hakijaryhma.setLaskentakaava(laskentakaavaService.haeMallinnettuKaava(dto.getLaskentakaavaId()));

        Hakijaryhma lisatty = hakijaryhmaDAO.insert(hakijaryhma);



        HakijaryhmaValintatapajono jono = new HakijaryhmaValintatapajono();
        jono.setOid(oidService.haeValintatapajonoHakijaryhmaOid());
        jono.setHakijaryhma(hakijaryhma);
        jono.setHakukohdeViite(hakukohde);
        jono.setAktiivinen(true);
        jono.setEdellinen(edellinenHakijaryhma);

        hakijaryhmaValintatapajonoDAO.insert(jono);

        LinkitettavaJaKopioitavaUtil.asetaSeuraava(edellinenHakijaryhma, jono);

        return lisatty;
    }


    @Override
    public void deleteByOid(String oid, boolean skipInheritedCheck) {

        HakijaryhmaValintatapajono hakijaryhmaValintatapajono = hakijaryhmaValintatapajonoDAO.readByOid(oid);


        if (!skipInheritedCheck && hakijaryhmaValintatapajono.getMaster() != null) {
            throw new HakijaryhmaaEiVoiPoistaaException("hakijaryhma on peritty.");
        }

        delete(hakijaryhmaValintatapajono);
    }

    // CRUD
    @Override
    public HakijaryhmaValintatapajono update(String oid, HakijaryhmaValintatapajonoUpdateDTO dto) {
        HakijaryhmaValintatapajono managedObject = haeHakijaryhmaValintatapajono(oid);
        HakijaryhmaValintatapajono entity = modelMapper.map(dto, HakijaryhmaValintatapajono.class);

        return LinkitettavaJaKopioitavaUtil.paivita(managedObject, entity, kopioija);
    }

    @Override
    public void liitaHakijaryhmaValintatapajonolle(String valintatapajonoOid, String hakijaryhmaOid) {

    }

    @Override
    public List<HakijaryhmaValintatapajono> jarjestaHakijaryhmat(String hakijaryhmaValintatapajonoOid, List<String> oids) {
        if (oids.isEmpty()) {
            throw new HakijaryhmaOidListaOnTyhjaException("Valintatapajonon Hakijaryhma sOID-lista on tyhjä");
        }

        LinkedHashMap<String, HakijaryhmaValintatapajono> alkuperainenJarjestys = LinkitettavaJaKopioitavaUtil.
                teeMappiOidienMukaan(LinkitettavaJaKopioitavaUtil.jarjesta(hakijaryhmaValintatapajonoDAO.findByValintatapajono(hakijaryhmaValintatapajonoOid)));

        LinkedHashMap<String, HakijaryhmaValintatapajono> jarjestetty = LinkitettavaJaKopioitavaUtil.jarjestaOidListanMukaan(alkuperainenJarjestys, oids);

        return new ArrayList<>(jarjestetty.values());
    }

    @Override
    public List<HakijaryhmaValintatapajono> findByHakukohde(String oid) {
        return hakijaryhmaValintatapajonoDAO.findByHakukohde(oid);
    }

    @Override
    public HakijaryhmaValintatapajono insert(HakijaryhmaValintatapajono entity) {
        return hakijaryhmaValintatapajonoDAO.insert(entity);
    }

    @Override
    public void delete(HakijaryhmaValintatapajono entity) {
        for (HakijaryhmaValintatapajono hakijaryhma : entity.getKopiot()) {
            delete(hakijaryhma);
        }

        if (entity.getSeuraava() != null) {
            HakijaryhmaValintatapajono seuraava = entity.getSeuraava();
            seuraava.setEdellinen(entity.getEdellinen());
        }

        hakijaryhmaValintatapajonoDAO.remove(entity);
    }
}
