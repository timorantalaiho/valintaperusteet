package fi.vm.sade.service.valintaperusteet.resource;

import fi.vm.sade.dbunit.annotation.DataSetLocation;
import fi.vm.sade.dbunit.listener.JTACleanInsertTestExecutionListener;
import fi.vm.sade.service.valintaperusteet.ObjectMapperProvider;
import fi.vm.sade.service.valintaperusteet.dto.ValintakoeDTO;
import fi.vm.sade.service.valintaperusteet.model.JsonViews;
import fi.vm.sade.service.valintaperusteet.model.Tasapistesaanto;
import fi.vm.sade.service.valintaperusteet.model.ValinnanVaihe;
import fi.vm.sade.service.valintaperusteet.model.Valintatapajono;
import fi.vm.sade.service.valintaperusteet.service.exception.ValinnanVaiheEiOleOlemassaException;
import fi.vm.sade.service.valintaperusteet.service.exception.ValinnanVaihettaEiVoiPoistaaException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: tommiha
 * Date: 1/23/13
 * Time: 1:03 PM
 */
@ContextConfiguration(locations = "classpath:test-context.xml")
@TestExecutionListeners(listeners = {JTACleanInsertTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class})
@RunWith(SpringJUnit4ClassRunner.class)
@DataSetLocation("classpath:test-data.xml")
public class ValinnanVaiheResourceTest {

    private ObjectMapper mapper = new ObjectMapperProvider().getContext(ValinnanVaiheResource.class);
    private ValinnanVaiheResource vaiheResource = new ValinnanVaiheResource();
    private HakukohdeResource hakuResource = new HakukohdeResource();

    @Autowired
    private ApplicationContext applicationContext;

    @Before
    public void setUp() throws Exception {
        applicationContext.getAutowireCapableBeanFactory().autowireBean(vaiheResource);
        applicationContext.getAutowireCapableBeanFactory().autowireBean(hakuResource);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testRead() throws IOException {
        ValinnanVaihe vaihe = vaiheResource.read("1");

        mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(vaihe);
    }

    @Test
    public void testQuery() throws IOException {
        List<Valintatapajono> jonos = vaiheResource.listJonos("1");

        mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(jonos);
    }

    @Test
    public void testUpdate() throws IOException {
        ValinnanVaihe vaihe = vaiheResource.read("1");

        ValinnanVaihe vaihe1 = vaiheResource.update(vaihe.getOid(), vaihe);
        mapper.writerWithView(JsonViews.Basic.class).writeValueAsString(vaihe1);
    }

    @Test
    public void testInsertValintatapajono() {
        Valintatapajono jono = new Valintatapajono();
        Response insert = vaiheResource.addJonoToValinnanVaihe("1", jono);
        assertEquals(500, insert.getStatus());

        jono = newJono();
        insert = vaiheResource.addJonoToValinnanVaihe("1", jono);
        assertEquals(201, insert.getStatus());
    }

    @Test
    public void testInsertValintakoe() {
        final String valinnanVaiheOid = "83";
        final Long laskentakaavaId = 101L;

        ValintakoeDTO valintakoe = new ValintakoeDTO();
        valintakoe.setTunniste("tunniste");
        valintakoe.setLaskentakaavaId(laskentakaavaId);

        Response response = vaiheResource.addValintakoeToValinnanVaihe(valinnanVaiheOid, valintakoe);
        assertEquals(201, response.getStatus());

    }

    @Test
    public void testDelete() {
        Response delete = vaiheResource.delete("4");
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), delete.getStatus());
    }

    @Test(expected = ValinnanVaiheEiOleOlemassaException.class)
    public void testDeleteOidNotFound() {
        vaiheResource.delete("");
    }

    @Test(expected = ValinnanVaihettaEiVoiPoistaaException.class)
    public void testDeleteInherited() {
        vaiheResource.delete("32");
    }

    @Test
    public void testChildrenAreDeleted() {

        ValinnanVaihe read = vaiheResource.read("79");

        assertNotNull(read);
        // objekti on peritty
        Response delete = vaiheResource.delete("75");
        assertEquals(Response.Status.ACCEPTED.getStatusCode(), delete.getStatus());

        try {
            ValinnanVaihe read1 = vaiheResource.read("79");
            assertNull(read1);
        } catch (ValinnanVaiheEiOleOlemassaException e) {

        }
    }

    @Test
    public void testJarjesta() {
        List<ValinnanVaihe> valinnanVaiheList = hakuResource.valinnanVaihesForHakukohde("oid6");
        List<String> oids = new ArrayList<String>();

        for (ValinnanVaihe valinnanVaihe : valinnanVaiheList) {
            oids.add(valinnanVaihe.getOid());
        }

        assertEquals("4", oids.get(0));
        assertEquals("6", oids.get(2));
        Collections.reverse(oids);
        List<ValinnanVaihe> jarjesta = vaiheResource.jarjesta(oids);
        assertEquals("6", jarjesta.get(0).getOid());
        assertEquals("4", jarjesta.get(2).getOid());
        jarjesta = hakuResource.valinnanVaihesForHakukohde("oid6");
        assertEquals("6", jarjesta.get(0).getOid());
        assertEquals("4", jarjesta.get(2).getOid());
    }

    @Test(expected = RuntimeException.class)
    public void testJarjestaEriHakuvaiheita() {
        List<String> oids = new ArrayList<String>();

        oids.add("4");
        oids.add("5");
        oids.add("6");

        oids.add("1");

        vaiheResource.jarjesta(oids);
    }

    private Valintatapajono newJono() {
        Valintatapajono jono;
        jono = new Valintatapajono();
        jono.setNimi("Uusi valintaryhmä");
        jono.setOid("oid123");
        jono.setAloituspaikat(1);
        jono.setSiirretaanSijoitteluun(false);
        jono.setTasapistesaanto(Tasapistesaanto.ARVONTA);
        jono.setAktiivinen(true);
        return jono;
    }


    @Test
    public void testKuuluuSijoitteluun() {
        boolean oid;
        oid = vaiheResource.kuuluuSijoitteluun("3401").get("sijoitteluun");
        assertEquals(false, oid);

        oid = vaiheResource.kuuluuSijoitteluun("3501").get("sijoitteluun");
        assertEquals(false, oid);

        oid = vaiheResource.kuuluuSijoitteluun("3601").get("sijoitteluun");
        assertEquals(true, oid);

        oid = vaiheResource.kuuluuSijoitteluun("3701").get("sijoitteluun");
        assertEquals(false, oid);
    }

}
