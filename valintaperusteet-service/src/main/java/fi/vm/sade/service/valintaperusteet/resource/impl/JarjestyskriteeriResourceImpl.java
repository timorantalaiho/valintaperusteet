package fi.vm.sade.service.valintaperusteet.resource.impl;

import static fi.vm.sade.service.valintaperusteet.roles.ValintaperusteetRole.CRUD;
import static fi.vm.sade.service.valintaperusteet.roles.ValintaperusteetRole.READ;
import static fi.vm.sade.service.valintaperusteet.roles.ValintaperusteetRole.UPDATE;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fi.vm.sade.service.valintaperusteet.dto.mapping.ValintaperusteetModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import fi.vm.sade.service.valintaperusteet.dto.JarjestyskriteeriDTO;
import fi.vm.sade.service.valintaperusteet.dto.JarjestyskriteeriInsertDTO;
import fi.vm.sade.service.valintaperusteet.resource.JarjestyskriteeriResource;
import fi.vm.sade.service.valintaperusteet.service.JarjestyskriteeriService;
import fi.vm.sade.service.valintaperusteet.service.exception.JarjestyskriteeriEiOleOlemassaException;
import fi.vm.sade.service.valintaperusteet.service.exception.JarjestyskriteeriaEiVoiPoistaaException;
import fi.vm.sade.service.valintaperusteet.service.exception.LaskentakaavaOidTyhjaException;

/**
 * Created with IntelliJ IDEA. User: jukais Date: 31.1.2013 Time: 10.51 To
 * change this template use File | Settings | File Templates.
 */
@Component
@Path("jarjestyskriteeri")
@PreAuthorize("isAuthenticated()")
@Api(value = "/jarjestyskriteeri", description = "Resurssi järjestyskriteerien käsittelyyn")
public class JarjestyskriteeriResourceImpl implements JarjestyskriteeriResource {
    @Autowired
    JarjestyskriteeriService jarjestyskriteeriService;

    @Autowired
    private ValintaperusteetModelMapper modelMapper;

    @GET
    @Path("/{oid}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured({ READ, UPDATE, CRUD })
    @ApiOperation(value = "Hakee järjestyskriteerin OID:n perusteella", response = JarjestyskriteeriDTO.class)
    @ApiResponses(@ApiResponse(code = 404, message = "Järjestyskriteeriä ei löydy"))
    public JarjestyskriteeriDTO readByOid(@ApiParam(value = "OID", required = true) @PathParam("oid") String oid) {
        try {
            return modelMapper.map(jarjestyskriteeriService.readByOid(oid), JarjestyskriteeriDTO.class);
        } catch (JarjestyskriteeriEiOleOlemassaException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }
    }

    @POST
    @Path("/{oid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured({ UPDATE, CRUD })
    @ApiOperation(value = "Päivittää järjestyskriteeriä OID:n perusteella")
    @ApiResponses(@ApiResponse(code = 400, message = "Laskentakaavaa ei ole määritetty"))
    public Response update(
            @ApiParam(value = "OID", required = true) @PathParam("oid") String oid,
            @ApiParam(value = "Järjestyskriteerin uudet tiedot ja laskentakaava", required = true) JarjestyskriteeriInsertDTO jk) {
        try {
            JarjestyskriteeriDTO update = modelMapper.map(
                    jarjestyskriteeriService.update(oid, jk.getJarjestyskriteeri(), jk.getLaskentakaavaId()),
                    JarjestyskriteeriDTO.class);
            return Response.status(Response.Status.ACCEPTED).entity(update).build();
        } catch (LaskentakaavaOidTyhjaException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
    }

    @DELETE
    @Path("/{oid}")
    @Secured({ CRUD })
    @ApiOperation(value = "Poistaa järjestyskriteerin OID:n perusteella")
    @ApiResponses(@ApiResponse(code = 403, message = "Järjestyskriteeriä ei voida poistaa, esim. se on peritty"))
    public Response delete(@ApiParam(value = "OID", required = true) @PathParam("oid") String oid) {
        try {
            jarjestyskriteeriService.deleteByOid(oid);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (JarjestyskriteeriaEiVoiPoistaaException e) {
            throw new WebApplicationException(e, Response.Status.FORBIDDEN);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/jarjesta")
    @Secured({ UPDATE, CRUD })
    @ApiOperation(value = "Järjestää järjestyskriteerit annetun listan mukaiseen järjestykseen")
    public List<JarjestyskriteeriDTO> jarjesta(@ApiParam(value = "Uusi järjestys", required = true) List<String> oids) {
        return modelMapper.mapList(jarjestyskriteeriService.jarjestaKriteerit(oids), JarjestyskriteeriDTO.class);
    }
}