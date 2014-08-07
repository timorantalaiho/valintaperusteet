package fi.vm.sade.service.valintaperusteet.service;

import fi.vm.sade.service.valintaperusteet.dto.HakijaryhmaCreateDTO;
import fi.vm.sade.service.valintaperusteet.dto.HakijaryhmaValintatapajonoUpdateDTO;
import fi.vm.sade.service.valintaperusteet.model.Hakijaryhma;
import fi.vm.sade.service.valintaperusteet.model.HakijaryhmaValintatapajono;

import java.util.List;

public interface HakijaryhmaValintatapajonoService {

    void deleteByOid(String oid, boolean skipInheritedCheck);

    List<HakijaryhmaValintatapajono> findHakijaryhmaByJono(String oid);

    HakijaryhmaValintatapajono readByOid(String oid);

    List<HakijaryhmaValintatapajono> findByHakijaryhma(String hakijaryhmaOid);

    HakijaryhmaValintatapajono insert(HakijaryhmaValintatapajono entity);

    // CRUD
    HakijaryhmaValintatapajono update(String oid, HakijaryhmaValintatapajonoUpdateDTO dto);

    void liitaHakijaryhmaValintatapajonolle(String valintatapajonoOid, String hakijaryhmaOid);

    Hakijaryhma lisaaHakijaryhmaValintatapajonolle(String valintatapajonoOid, HakijaryhmaCreateDTO dto);

    Hakijaryhma lisaaHakijaryhmaHakukohteelle(String hakukohdeOid, HakijaryhmaCreateDTO hakijaryhma);

    void delete(HakijaryhmaValintatapajono entity);

    List<HakijaryhmaValintatapajono> jarjestaHakijaryhmat(String hakijaryhmaValintatapajonoOid, List<String> oids);

    List<HakijaryhmaValintatapajono> findByHakukohde(String oid);
}
