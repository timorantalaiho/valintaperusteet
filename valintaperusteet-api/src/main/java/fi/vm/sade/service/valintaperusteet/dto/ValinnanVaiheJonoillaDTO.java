package fi.vm.sade.service.valintaperusteet.dto;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * User: wuoti Date: 27.11.2013 Time: 18.00
 */
@ApiModel(value = "ValinnanVaiheDTO", description = "Valinnan vaihe")
public class ValinnanVaiheJonoillaDTO extends ValinnanVaiheCreateDTO {

    @ApiModelProperty(value = "OID", required = true)
    private String oid;

    @ApiModelProperty(value = "Onko valinnan vaihe peritty")
    private Boolean inheritance;

    @ApiModelProperty(value = "Valinnan vaiheen valintatapajonot")
    private Set<ValintatapajonoDTO> jonot = new HashSet<ValintatapajonoDTO>();

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Boolean getInheritance() {
        return inheritance;
    }

    public void setInheritance(Boolean inheritance) {
        this.inheritance = inheritance;
    }

    public Set<ValintatapajonoDTO> getJonot() {
        return jonot;
    }

    public void setJonot(Set<ValintatapajonoDTO> jonot) {
        this.jonot = jonot;
    }
}