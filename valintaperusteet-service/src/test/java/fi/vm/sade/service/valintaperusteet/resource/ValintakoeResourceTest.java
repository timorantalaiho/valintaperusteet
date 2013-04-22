package fi.vm.sade.service.valintaperusteet.resource;

import fi.vm.sade.dbunit.annotation.DataSetLocation;
import fi.vm.sade.dbunit.listener.JTACleanInsertTestExecutionListener;
import fi.vm.sade.service.valintaperusteet.ObjectMapperProvider;
import fi.vm.sade.service.valintaperusteet.dto.ValintakoeDTO;
import fi.vm.sade.service.valintaperusteet.model.JsonViews;
import fi.vm.sade.service.valintaperusteet.model.Valintakoe;
import fi.vm.sade.service.valintaperusteet.util.TestUtil;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * User: kwuoti
 * Date: 15.4.2013
 * Time: 18.17
 */
@ContextConfiguration(locations = "classpath:test-context.xml")
@TestExecutionListeners(listeners = {JTACleanInsertTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@RunWith(SpringJUnit4ClassRunner.class)
@DataSetLocation("classpath:test-data.xml")
public class ValintakoeResourceTest {

    private ValintakoeResource valintakoeResource = new ValintakoeResource();
    private ObjectMapper mapper = new ObjectMapperProvider().getContext(ValintakoeResource.class);
    private TestUtil testUtil = new TestUtil(this.getClass());


    @Autowired
    private ApplicationContext applicationContext;

    @Before
    public void setUp() {
        applicationContext.getAutowireCapableBeanFactory().autowireBean(valintakoeResource);
    }

    @Test
    public void testReadByOid() throws Exception {
        final String oid = "oid1";
        Valintakoe valintakoe = valintakoeResource.readByOid(oid);
        testUtil.lazyCheck(JsonViews.Basic.class, valintakoe);
    }

    @Test
    public void testUpdate() {
        final String oid = "oid1";
        final Long laskentakaavaId = 102L;

        Valintakoe saved = valintakoeResource.readByOid(oid);

        ValintakoeDTO koe = new ValintakoeDTO();
        koe.setKuvaus("uusi kuvaus");
        koe.setNimi("uusi nimi");
        koe.setTunniste("uusi tunniste");
        koe.setLaskentakaavaId(laskentakaavaId);

        assertFalse(saved.getNimi().equals(koe.getNimi()));
        assertFalse(saved.getKuvaus().equals(koe.getKuvaus()));
        assertFalse(saved.getTunniste().equals(koe.getTunniste()));
        assertFalse(saved.getLaskentakaavaId().equals(koe.getLaskentakaavaId()));

        valintakoeResource.update(oid, koe);

        saved = valintakoeResource.readByOid(oid);

        assertEquals(koe.getKuvaus(), saved.getKuvaus());
        assertEquals(koe.getNimi(), saved.getNimi());
        assertEquals(koe.getTunniste(), saved.getTunniste());
        assertEquals(koe.getLaskentakaavaId(), saved.getLaskentakaavaId());

    }

}
