package fi.vm.sade.service.valintaperusteet.dto;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import fi.vm.sade.service.valintaperusteet.model.JsonViews;
import org.codehaus.jackson.map.annotate.JsonView;

/**
 * User: wuoti
 * Date: 27.11.2013
 * Time: 16.56
 */
@ApiModel(value = "KoodiDTO", description = "Koodiston koodi")
public class KoodiDTO {

    @ApiModelProperty(value = "URI", required = true)
    @JsonView(JsonViews.Basic.class)
    private String uri;

    @ApiModelProperty(value = "Suomenkielinen nimi")
    @JsonView(JsonViews.Basic.class)
    private String nimiFi;

    @ApiModelProperty(value = "Ruotsinkielinen nimi")
    @JsonView(JsonViews.Basic.class)
    private String nimiSv;

    @ApiModelProperty(value = "Englanninkielinen nimi")
    @JsonView(JsonViews.Basic.class)
    private String nimiEn;

    @ApiModelProperty(value = "Koodin arvo")
    @JsonView(JsonViews.Basic.class)
    private String arvo;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getNimiFi() {
        return nimiFi;
    }

    public void setNimiFi(String nimiFi) {
        this.nimiFi = nimiFi;
    }

    public String getNimiSv() {
        return nimiSv;
    }

    public void setNimiSv(String nimiSv) {
        this.nimiSv = nimiSv;
    }

    public String getNimiEn() {
        return nimiEn;
    }

    public void setNimiEn(String nimiEn) {
        this.nimiEn = nimiEn;
    }

    public String getArvo() {
        return arvo;
    }

    public void setArvo(String arvo) {
        this.arvo = arvo;
    }
}