package fi.vm.sade.service.valintaperusteet.service;

import fi.vm.sade.service.valintaperusteet.model.*;

import java.util.List;

public interface HakijaryhmaService extends CRUDService<Hakijaryhma, Long, String> {

    void deleteByOid(String oid, boolean skipInheritedCheck);

    List<Hakijaryhma> findByHakukohde(String oid);

    List<Hakijaryhma> findByValintaryhma(String oid);

    Hakijaryhma readByOid(String oid);

    void liitaHakijaryhmaValintatapajonolle(String valintatapajonoOid, String hakijaryhmaOid);

    Hakijaryhma lisaaHakijaryhmaValintaryhmalle(String valintaryhmaOid, Hakijaryhma hakijaryhma);

    Hakijaryhma lisaaHakijaryhmaHakukohteelle(String hakukohdeOid, Hakijaryhma hakijaryhma);

    List<Hakijaryhma> jarjestaHakijaryhmat(List<String> oids);

    void kopioiHakijaryhmatParentilta(Valintaryhma inserted, Valintaryhma parent);
    void kopioiHakijaryhmatParentilta(HakukohdeViite inserted, Valintaryhma parent);

}
