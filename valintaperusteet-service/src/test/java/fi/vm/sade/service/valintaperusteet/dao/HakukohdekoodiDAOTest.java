package fi.vm.sade.service.valintaperusteet.dao;

import fi.vm.sade.dbunit.annotation.DataSetLocation;
import fi.vm.sade.dbunit.listener.JTACleanInsertTestExecutionListener;
import fi.vm.sade.service.valintaperusteet.model.Hakukohdekoodi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * User: wuoti
 * Date: 8.5.2013
 * Time: 14.14
 */
@ContextConfiguration(locations = "classpath:test-context.xml")
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, JTACleanInsertTestExecutionListener.class })
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@DataSetLocation("classpath:test-data.xml")
public class HakukohdekoodiDAOTest {

    @Autowired
    private HakukohdekoodiDAO hakukohdekoodiDAO;

    @Test
    public void testFindByKoodiUri() {
        final String koodiUri = "hakukohdekoodiuri1";
        Hakukohdekoodi koodi = hakukohdekoodiDAO.findByKoodiUri(koodiUri);

        final String valintaryhmaOid = "oid36";
        final String hakukohdeOid = "oid11";

        assertEquals(koodiUri, koodi.getUri());
        assertEquals(koodi.getValintaryhma().getOid(), valintaryhmaOid);
        assertEquals(koodi.getHakukohde().getOid(), hakukohdeOid);

        assertNull(hakukohdekoodiDAO.findByKoodiUri("not-exists"));
    }
}