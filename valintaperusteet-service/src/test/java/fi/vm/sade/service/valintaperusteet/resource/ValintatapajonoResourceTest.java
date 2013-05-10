package fi.vm.sade.service.valintaperusteet.resource;

import fi.vm.sade.dbunit.annotation.DataSetLocation;
import fi.vm.sade.dbunit.listener.JTACleanInsertTestExecutionListener;
import fi.vm.sade.service.valintaperusteet.model.Jarjestyskriteeri;
import fi.vm.sade.service.valintaperusteet.model.JsonViews;
import fi.vm.sade.service.valintaperusteet.model.Valintatapajono;
import fi.vm.sade.service.valintaperusteet.service.exception.ValintatapajonoEiOleOlemassaException;
import fi.vm.sade.service.valintaperusteet.util.TestUtil;
import org.codehaus.jettison.json.JSONObject;
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

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: jukais
 * Date: 23.1.2013
 * Time: 10.58
 * To change this template use File | Settings | File Templates.
 */
@ContextConfiguration(locations = "classpath:test-context.xml")
@TestExecutionListeners(listeners = {JTACleanInsertTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@RunWith(SpringJUnit4ClassRunner.class)
@DataSetLocation("classpath:test-data.xml")
public class ValintatapajonoResourceTest {
    private ValintatapajonoResource resource = new ValintatapajonoResource();
    private ValinnanVaiheResource vaiheResource = new ValinnanVaiheResource();

    @Autowired
    private ApplicationContext applicationContext;
    private TestUtil testUtil = new TestUtil(ValintatapajonoResource.class);

    @Before
    public void setUp() {
        applicationContext.getAutowireCapableBeanFactory().autowireBean(resource);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(vaiheResource);
    }

    @Test
    public void testUpdate() throws Exception {
        Valintatapajono jono = resource.readByOid("1");
        jono.setNimi("muokattu");
        resource.update(jono.getOid(), jono);

        jono = resource.readByOid("1");
        assertEquals("muokattu" , jono.getNimi());
        testUtil.lazyCheck(JsonViews.Basic.class, jono);
    }

    @Test
    public void testFindAll() throws Exception {
        List<Valintatapajono> jonos = resource.findAll();

        assertEquals(63, jonos.size());
        testUtil.lazyCheck(JsonViews.Basic.class, jonos);
    }

    @Test
    public void testJarjestykriteeri() throws Exception {
        List<Jarjestyskriteeri> jarjestyskriteeri = resource.findJarjestyskriteeri("6");

        testUtil.lazyCheck(JsonViews.Basic.class, jarjestyskriteeri, true);
        assertEquals(3, jarjestyskriteeri.size());

    }

    @Test(expected = RuntimeException.class)
    public void testDeleteOidNotFound() {
        resource.delete("");
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteInherited() {
        // objekti on peritty
        resource.delete("27");
    }

    @Test
    public void testChildrenAreDeleted() {
        Valintatapajono valintatapajono = resource.readByOid("30");
        assertNotNull(valintatapajono);
        // objekti on peritty
        resource.delete("26");
        try {
            valintatapajono = resource.readByOid("30");
            assertNull(valintatapajono);
        } catch(ValintatapajonoEiOleOlemassaException e) {

        }

    }

    @Test
    public void testDelete() {
        Response delete = resource.delete("12");
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), delete.getStatus());
    }

    @Test
    public void testInsertJK() throws Exception {

        JSONObject jk = new JSONObject();
        jk.put("metatiedot", "mt1");
        jk.put("laskentakaava_id", 1);
        jk.put("aktiivinen", "true");
        Response insert = resource.insertJarjestyskriteeri("1", jk);

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), insert.getStatus());

        Jarjestyskriteeri entity = (Jarjestyskriteeri)insert.getEntity();

        testUtil.lazyCheck(JsonViews.Basic.class, entity, true);

    }


    @Test
    public void testJarjesta() {
        List<Valintatapajono> valintatapajonoList = vaiheResource.listJonos("1");
        List<String> oids = new ArrayList<String>();

        for (Valintatapajono valintatapajono : valintatapajonoList) {
            oids.add(valintatapajono.getOid());
        }

        assertEquals("1", oids.get(0));
        assertEquals("5", oids.get(4));
        Collections.reverse(oids);

        List<Valintatapajono> jarjesta = resource.jarjesta(oids);
        assertEquals("5", jarjesta.get(0).getOid());
        assertEquals("1", jarjesta.get(4).getOid());

        jarjesta = vaiheResource.listJonos("1");
        assertEquals("5", jarjesta.get(0).getOid());
        assertEquals("1", jarjesta.get(4).getOid());
    }
}
