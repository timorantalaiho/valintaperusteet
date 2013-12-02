package fi.vm.sade.service.valintaperusteet.resource;

import com.wordnik.swagger.annotations.*;
import fi.vm.sade.service.valintaperusteet.dto.*;
import fi.vm.sade.service.valintaperusteet.dto.mapping.ValintaperusteetModelMapper;
import fi.vm.sade.service.valintaperusteet.model.JsonViews;
import fi.vm.sade.service.valintaperusteet.service.ValinnanVaiheService;
import fi.vm.sade.service.valintaperusteet.service.ValintakoeService;
import fi.vm.sade.service.valintaperusteet.service.ValintatapajonoService;
import fi.vm.sade.service.valintaperusteet.service.exception.ValinnanVaiheEiOleOlemassaException;
import fi.vm.sade.service.valintaperusteet.service.exception.ValinnanVaihettaEiVoiPoistaaException;
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
 * User: jukais
 * Date: 17.1.2013
 * Time: 14.42
 */
@Component
@Path("valinnanvaihe")
@PreAuthorize("isAuthenticated()")
@Api(value = "/valinnanvaihe", description = "Resurssi valinnan vaiheiden käsittelyyn")
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
    @ApiOperation(value = "Hakee valinnan vaiheen OID:n perusteella", response = ValinnanVaiheDTO.class)
    public ValinnanVaiheDTO read(@ApiParam(value = "OID", required = true) @PathParam("oid") String oid) {
        return modelMapper.map(valinnanVaiheService.readByOid(oid), ValinnanVaiheDTO.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView({JsonViews.Basic.class})
    @Path("{oid}/valintatapajono")
    @Secured({READ, UPDATE, CRUD})
    @ApiOperation(value = "Hakee valinnan vaiheen valintatapajonot OID:n perusteella", response = ValintatapajonoDTO.class)
    public List<ValintatapajonoDTO> listJonos(@ApiParam(value = "Valinnan vaiheen OID", required = true) @PathParam("oid") String oid) {
        return modelMapper.mapList(jonoService.findJonoByValinnanvaihe(oid), ValintatapajonoDTO.class);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView(JsonViews.Basic.class)
    @Path("{oid}/valintakoe")
    @Secured({READ, UPDATE, CRUD})
    @ApiOperation(value = "Hakee valintakokeet valinnan vaiheen OID:n perusteella", response = ValintakoeDTO.class)
    public List<ValintakoeDTO> listValintakokeet(@ApiParam(value = "Valinnan vaiheen OID", required = true) @PathParam("oid") String oid) {
        return modelMapper.mapList(valintakoeService.findValintakoeByValinnanVaihe(oid), ValintakoeDTO.class);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView({JsonViews.Basic.class})
    @Path("{parentOid}/valintatapajono")
    @Secured({UPDATE, CRUD})
    @ApiOperation(value = "Lisää valintatapajonon valinnan vaiheelle")
    public Response addJonoToValinnanVaihe(@ApiParam(value = "Valinnan vaiheen OID", required = true) @PathParam("parentOid") String parentOid,
                                           @ApiParam(value = "Lisättävä valintatapajono", required = true) ValintatapajonoCreateDTO jono) {
        try {
            ValintatapajonoDTO inserted = modelMapper.map(jonoService.lisaaValintatapajonoValinnanVaiheelle(parentOid, jono, null), ValintatapajonoDTO.class);
            return Response.status(Response.Status.CREATED).entity(inserted).build();
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
    @ApiOperation(value = "Lisää valintakokeen valinnan vaiheelle")
    public Response addValintakoeToValinnanVaihe(@ApiParam(value = "Valinnan vaiheen OID", required = true) @PathParam("parentOid") String parentOid,
                                                 @ApiParam(value = "Lisättävä valintakoe", required = true) ValintakoeCreateDTO koe) {
        try {
            ValintakoeDTO vk = modelMapper.map(valintakoeService.lisaaValintakoeValinnanVaiheelle(parentOid, koe), ValintakoeDTO.class);
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
    @ApiOperation(value = "Päivittää valinnan vaihetta", response = ValinnanVaiheDTO.class)
    public ValinnanVaiheDTO update(@ApiParam(value = "Päivitettävän valinnan vaiheen OID", required = true) @PathParam("oid") String oid,
                                   @ApiParam(value = "Päivitettävän valinnan vaiheen uudet tiedot", required = true) ValinnanVaiheCreateDTO valinnanVaihe) {
        return modelMapper.map(valinnanVaiheService.update(oid, valinnanVaihe), ValinnanVaiheDTO.class);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView(JsonViews.Basic.class)
    @Path("jarjesta")
    @Secured({UPDATE, CRUD})
    @ApiOperation(value = "Järjestää valinnan vaiheet parametrina annetun OID-listan mukaiseen järjestykseen", response = ValinnanVaiheDTO.class)
    public List<ValinnanVaiheDTO> jarjesta(@ApiParam(value = "Valinnan vaiheiden uusi järjestys", required = true) List<String> oids) {
        return modelMapper.mapList(valinnanVaiheService.jarjestaValinnanVaiheet(oids), ValinnanVaiheDTO.class);
    }

    @DELETE
    @Path("{oid}")
    @Secured({CRUD})
    @ApiOperation(value = "Poistaa valinnan vaiheen OID:n perusteetlla")
    @ApiResponses({
            @ApiResponse(code = 404, message = "Valinnan vaihetta ei ole olemassa"),
            @ApiResponse(code = 400, message = "Valinnan vaihetta ei voida poistaa, esim. se on peritty")
    })
    public Response delete(@ApiParam(value = "Valinnan vaiheen OID", required = true) @PathParam("oid") String oid) {
        try {
            valinnanVaiheService.deleteByOid(oid);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (ValinnanVaiheEiOleOlemassaException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        } catch (ValinnanVaihettaEiVoiPoistaaException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("{oid}/kuuluuSijoitteluun")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView({JsonViews.Basic.class})
    @Secured({READ, UPDATE, CRUD})
    @ApiOperation(value = "Palauttaa tiedon siitä, kuuluuko valinnan vaihe sijoitteluun", response = Boolean.class)
    public Map<String, Boolean> kuuluuSijoitteluun(@ApiParam(value = "Valinnan vaiheen OID", required = true) @PathParam("oid") String oid) {
        Map<String, Boolean> map = new HashMap<String, Boolean>();
        map.put("sijoitteluun", valinnanVaiheService.kuuluuSijoitteluun(oid));
        return map;
    }


}
