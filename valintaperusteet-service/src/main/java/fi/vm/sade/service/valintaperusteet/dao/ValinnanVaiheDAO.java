package fi.vm.sade.service.valintaperusteet.dao;

import fi.vm.sade.generic.dao.JpaDAO;
import fi.vm.sade.service.valintaperusteet.model.ValinnanVaihe;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: jukais
 * Date: 15.1.2013
 * Time: 17.20
 * To change this template use File | Settings | File Templates.
 */
public interface ValinnanVaiheDAO extends JpaDAO<ValinnanVaihe, Long> {

    ValinnanVaihe readByOid(String oid);

    List<ValinnanVaihe> findByHakukohde(String oid);

    ValinnanVaihe haeValintaryhmanViimeinenValinnanVaihe(String oid);

    List<ValinnanVaihe> findByValintaryhma(String oid);

    List<ValinnanVaihe> readByOids(Set<String> oids);

    ValinnanVaihe haeHakukohteenViimeinenValinnanVaihe(String hakukohdeOid);

    Set<String> findValinnanVaiheOidsByValintaryhma(String valintaryhmaOid);

    Set<String> findValinnanVaiheOidsByHakukohde(String hakukohdeOid);

    List<ValinnanVaihe> haeKopiot(String oid);

    boolean kuuluuSijoitteluun(String oid);
}
