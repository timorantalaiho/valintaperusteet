package fi.vm.sade.service.valintaperusteet.dao;

import fi.vm.sade.service.valintaperusteet.model.HakijaryhmaValintatapajono;

import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jukais
 * Date: 15.1.2013
 * Time: 17.20
 * To change this template use File | Settings | File Templates.
 */
public interface HakijaryhmaValintatapajonoDAO extends JpaDAO<HakijaryhmaValintatapajono, Long> {
    HakijaryhmaValintatapajono readByOid(String oid);

    List<HakijaryhmaValintatapajono> findByValintatapajono(String oid);

    List<HakijaryhmaValintatapajono> findByHakijaryhma(String hakijaryhmaOid);

    List<HakijaryhmaValintatapajono> findByHakukohde(String oid);

    List<HakijaryhmaValintatapajono> findByHakukohteet(Collection<String> oids);

    List<HakijaryhmaValintatapajono> findByHaku(String hakuOid);

    HakijaryhmaValintatapajono haeHakukohteenViimeinenHakijaryhma(String hakukohdeOid);

    HakijaryhmaValintatapajono haeValintatapajononViimeinenHakijaryhma(String valintatapajonoOid);
}
