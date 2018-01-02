package fi.vm.sade.service.valintaperusteet.resource.impl;

import com.google.common.collect.ImmutableMap;
import fi.vm.sade.service.valintaperusteet.dto.JarjestyskriteeriDTO;
import fi.vm.sade.service.valintaperusteet.dto.JarjestyskriteeriInsertDTO;
import fi.vm.sade.service.valintaperusteet.dto.mapping.ValintaperusteetModelMapper;
import fi.vm.sade.service.valintaperusteet.model.Jarjestyskriteeri;
import fi.vm.sade.service.valintaperusteet.resource.JarjestyskriteeriResource;
import fi.vm.sade.service.valintaperusteet.service.JarjestyskriteeriService;
import fi.vm.sade.service.valintaperusteet.service.exception.JarjestyskriteeriEiOleOlemassaException;
import fi.vm.sade.service.valintaperusteet.service.exception.JarjestyskriteeriaEiVoiPoistaaException;
import fi.vm.sade.service.valintaperusteet.service.exception.LaskentakaavaOidTyhjaException;
import fi.vm.sade.sharedutils.AuditLog;
import fi.vm.sade.sharedutils.ValintaResource;
import fi.vm.sade.sharedutils.ValintaperusteetOperation;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static fi.vm.sade.service.valintaperusteet.roles.ValintaperusteetRole.*;
import static fi.vm.sade.service.valintaperusteet.util.ValintaperusteetAudit.toNullsafeString;

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
    @PreAuthorize(READ_UPDATE_CRUD)
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
    @PreAuthorize(UPDATE_CRUD)
    @ApiOperation(value = "Päivittää järjestyskriteeriä OID:n perusteella")
    @ApiResponses(@ApiResponse(code = 400, message = "Laskentakaavaa ei ole määritetty"))
    public Response update(
            @ApiParam(value = "OID", required = true) @PathParam("oid") String oid,
            @ApiParam(value = "Järjestyskriteerin uudet tiedot ja laskentakaava", required = true) JarjestyskriteeriInsertDTO jk, @Context HttpServletRequest request) {
        try {
            JarjestyskriteeriDTO old = modelMapper.map(jarjestyskriteeriService.readByOid(oid), JarjestyskriteeriDTO.class);
            JarjestyskriteeriDTO update = modelMapper.map(jarjestyskriteeriService.update(oid, jk.getJarjestyskriteeri(), jk.getLaskentakaavaId()), JarjestyskriteeriDTO.class);
            AuditLog.log(ValintaperusteetOperation.JARJESTYSKRITEERI_PAIVITYS, ValintaResource.JARJESTYSKRITEERIT, oid, update, old, request);
            return Response.status(Response.Status.ACCEPTED).entity(update).build();
        } catch (LaskentakaavaOidTyhjaException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
    }

    @DELETE
    @Path("/{oid}")
    @PreAuthorize(CRUD)
    @ApiOperation(value = "Poistaa järjestyskriteerin OID:n perusteella")
    @ApiResponses(@ApiResponse(code = 403, message = "Järjestyskriteeriä ei voida poistaa, esim. se on peritty"))
    public Response delete(@ApiParam(value = "OID", required = true) @PathParam("oid") String oid, @Context HttpServletRequest request) {
        try {
            JarjestyskriteeriDTO old = modelMapper.map(jarjestyskriteeriService.readByOid(oid), JarjestyskriteeriDTO.class);
            jarjestyskriteeriService.deleteByOid(oid);
            AuditLog.log(ValintaperusteetOperation.JARJESTYSKRITEERI_POISTO, ValintaResource.JARJESTYSKRITEERIT, oid, null, old, request);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (JarjestyskriteeriaEiVoiPoistaaException e) {
            throw new WebApplicationException(e, Response.Status.FORBIDDEN);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/jarjesta")
    @PreAuthorize(UPDATE_CRUD)
    @ApiOperation(value = "Järjestää järjestyskriteerit annetun listan mukaiseen järjestykseen")
    public List<JarjestyskriteeriDTO> jarjesta(@ApiParam(value = "Uusi järjestys", required = true) List<String> oids, @Context HttpServletRequest request) {
        List<Jarjestyskriteeri> jks = jarjestyskriteeriService.jarjestaKriteerit(oids);

        Map<String,String> additionalInfo = ImmutableMap.of("Uusi järjestys", toNullsafeString(oids));
        AuditLog.log(ValintaperusteetOperation.JARJESTYSKRITEERIT_JARJESTA, ValintaResource.JARJESTYSKRITEERIT, null, null, null, request, additionalInfo);

        return modelMapper.mapList(jks, JarjestyskriteeriDTO.class);
    }
}
