package fi.vm.sade.service.valintaperusteet.dto;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import fi.vm.sade.service.valintaperusteet.dto.model.Kieli;

/**
 * User: wuoti Date: 2.12.2013 Time: 9.15
 */
@ApiModel(value = "KuvausDTO", description = "Kuvaus")
public class KuvausDTO {

    @ApiModelProperty(value = "Kuvauksen kieli", required = true)
    private Kieli kieli;

    @ApiModelProperty(value = "Teksti", required = true)
    private String teksti;


    public Kieli getKieli() {
        return kieli;
    }

    public void setKieli(Kieli kieli) {
        this.kieli = kieli;
    }

    public String getTeksti() {
        return teksti;
    }

    public void setTeksti(String teksti) {
        this.teksti = teksti;
    }
}