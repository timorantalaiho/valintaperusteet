package fi.vm.sade.service.valintaperusteet.dao;

import fi.vm.sade.generic.dao.JpaDAO;
import fi.vm.sade.service.valintaperusteet.model.Hakijaryhma;
import fi.vm.sade.service.valintaperusteet.model.HakijaryhmaValintatapajono;
import org.springframework.stereotype.Repository;

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
}