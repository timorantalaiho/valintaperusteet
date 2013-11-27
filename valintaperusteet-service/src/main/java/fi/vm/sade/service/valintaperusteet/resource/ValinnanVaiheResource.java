package fi.vm.sade.service.valintaperusteet.resource;

import fi.vm.sade.service.valintaperusteet.dto.ValinnanVaiheDTO;
import fi.vm.sade.service.valintaperusteet.dto.ValintakoeDTO;
import fi.vm.sade.service.valintaperusteet.dto.mapping.ValintaperusteetModelMapper;
import fi.vm.sade.service.valintaperusteet.model.JsonViews;
import fi.vm.sade.service.valintaperusteet.model.ValinnanVaihe;
import fi.vm.sade.service.valintaperusteet.model.Valintakoe;
import fi.vm.sade.service.valintaperusteet.model.Valintatapajono;
import fi.vm.sade.service.valintaperusteet.service.ValinnanVaiheService;
import fi.vm.sade.service.valintaperusteet.service.ValintakoeService;
import fi.vm.sade.service.valintaperusteet.service.ValintatapajonoService;
import org.codehaus.jackson.map.annotate.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.vm.sade.service.valintaperusteet.roles.ValintaperusteetRole.*;

/**
 * Created with IntelliJ IDEA.
 * User: jukais
 * Date: 17.1.2013
 * Time: 14.42
 * To change this template use File | Settings | File Templates.
 */
@Component
@Path("valinnanvaihe")
@PreAuthorize("isAuthenticated()")
public class ValinnanVaiheResource {

    @Autowired
    ValintatapajonoService jonoService;

    @Autowired
    ValintakoeService valintakoeService;

    @Autowired
    ValinnanVaiheService valinnanVaiheService;

    @Autowired
    private ValintaperusteetModelMapper modelMapper;

    protected final static Logger LOGGER = LoggerFactory.getLogger(ValinnanVaiheResource.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView(JsonViews.Basic.class)
    @Path("{oid}")
    @Secured({READ, UPDATE, CRUD})
    public ValinnanVaihe read(@PathParam("oid") String oid) {
        return valinnanVaiheService.readByOid(oid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView({JsonViews.Basic.class})
    @Path("{oid}/valintatapajono")
    @Secured({READ, UPDATE, CRUD})
    public List<Valintatapajono> listJonos(@PathParam("oid") String oid) {
        return jonoService.findJonoByValinnanvaihe(oid);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView(JsonViews.Basic.class)
    @Path("{oid}/valintakoe")
    @Secured({READ, UPDATE, CRUD})
    public List<Valintakoe> listValintakokeet(@PathParam("oid") String oid) {
        return valintakoeService.findValintakoeByValinnanVaihe(oid);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView({JsonViews.Basic.class})
    @Path("{parentOid}/valintatapajono")
    @Secured({UPDATE, CRUD})
    public Response addJonoToValinnanVaihe(@PathParam("parentOid") String parentOid, Valintatapajono jono) {
        try {
            jono = jonoService.lisaaValintatapajonoValinnanVaiheelle(parentOid, jono, null);
            return Response.status(Response.Status.CREATED).entity(jono).build();
        } catch (Exception e) {
            LOGGER.error("error in addJonoToValinnanVaihe", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView({JsonViews.Basic.class})
    @Path("{parentOid}/valintakoe")
    @Secured({UPDATE, CRUD})
    public Response addValintakoeToValinnanVaihe(@PathParam("parentOid") String parentOid, ValintakoeDTO koe) {
        try {
            Valintakoe vk = valintakoeService.lisaaValintakoeValinnanVaiheelle(parentOid, koe);
            return Response.status(Response.Status.CREATED).entity(vk).build();
        } catch (Exception e) {
            LOGGER.error("error in addValintakoeToValinnanVaihe", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView(JsonViews.Basic.class)
    @Path("{oid}")
    @Secured({UPDATE, CRUD})
    public ValinnanVaihe update(@PathParam("oid") String oid, ValinnanVaihe valinnanVaihe) {
        return valinnanVaiheService.update(oid, valinnanVaihe);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView(JsonViews.Basic.class)
    @Path("jarjesta")
    @Secured({UPDATE, CRUD})
    public List<ValinnanVaiheDTO> jarjesta(List<String> oids) {
        return modelMapper.mapList(valinnanVaiheService.jarjestaValinnanVaiheet(oids), ValinnanVaiheDTO.class);
    }

    @DELETE
    @Path("{oid}")
    @Secured({CRUD})
    public Response delete(@PathParam("oid") String oid) {
        valinnanVaiheService.deleteByOid(oid);
        return Response.status(Response.Status.ACCEPTED).build();
    }

    @GET
    @Path("{oid}/kuuluuSijoitteluun")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView({JsonViews.Basic.class})
    @Secured({READ, UPDATE, CRUD})
    public Map<String, Boolean> kuuluuSijoitteluun(@PathParam("oid") String oid) {
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        map.put("sijoitteluun", valinnanVaiheService.kuuluuSijoitteluun(oid));
        return map;
    }


}
