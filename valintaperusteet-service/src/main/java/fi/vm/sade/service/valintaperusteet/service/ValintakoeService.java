package fi.vm.sade.service.valintaperusteet.service;

import fi.vm.sade.service.valintaperusteet.dto.ValintakoeCreateDTO;
import fi.vm.sade.service.valintaperusteet.dto.ValintakoeDTO;
import fi.vm.sade.service.valintaperusteet.model.ValinnanVaihe;
import fi.vm.sade.service.valintaperusteet.model.Valintakoe;

import java.util.List;

/**
 * User: kwuoti
 * Date: 15.4.2013
 * Time: 16.05
 */
public interface ValintakoeService {

    void deleteByOid(String oid);

    Valintakoe readByOid(String oid);

    List<Valintakoe> findValintakoeByValinnanVaihe(String oid);

    Valintakoe lisaaValintakoeValinnanVaiheelle(String valinnanVaiheOid, ValintakoeCreateDTO koe);

    Valintakoe update(String oid, ValintakoeDTO valintakoe);

    void kopioiValintakokeetMasterValinnanVaiheeltaKopiolle(ValinnanVaihe valinnanVaihe, ValinnanVaihe masterValinnanVaihe);
}
