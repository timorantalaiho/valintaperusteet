package fi.vm.sade.service.valintaperusteet.dto;

import fi.vm.sade.service.valintaperusteet.model.Funktiotyyppi;
import fi.vm.sade.service.valintaperusteet.model.JsonViews;
import fi.vm.sade.service.valintaperusteet.model.Valintaperustelahde;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.List;

/**
 * User: wuoti
 * Date: 28.5.2013
 * Time: 14.52
 */
public class ValintaperusteDTO {

    @JsonView({JsonViews.Basic.class})
    private String tunniste;

    @JsonView({JsonViews.Basic.class})
    private String kuvaus;

    @JsonView({JsonViews.Basic.class})
    private Funktiotyyppi funktiotyyppi;

    @JsonView({JsonViews.Basic.class})
    private Valintaperustelahde lahde;

    @JsonView({JsonViews.Basic.class})
    private boolean onPakollinen;

    @JsonView({JsonViews.Basic.class})
    private Double min;

    @JsonView({JsonViews.Basic.class})
    private Double max;

    @JsonView({JsonViews.Basic.class})
    private List<String> arvot;

    public String getTunniste() {
        return tunniste;
    }

    public void setTunniste(String tunniste) {
        this.tunniste = tunniste;
    }

    public String getKuvaus() {
        return kuvaus;
    }

    public void setKuvaus(String kuvaus) {
        this.kuvaus = kuvaus;
    }

    public Funktiotyyppi getFunktiotyyppi() {
        return funktiotyyppi;
    }

    public void setFunktiotyyppi(Funktiotyyppi funktiotyyppi) {
        this.funktiotyyppi = funktiotyyppi;
    }

    public Valintaperustelahde getLahde() {
        return lahde;
    }

    public void setLahde(Valintaperustelahde lahde) {
        this.lahde = lahde;
    }

    public boolean isOnPakollinen() {
        return onPakollinen;
    }

    public void setOnPakollinen(boolean onPakollinen) {
        this.onPakollinen = onPakollinen;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public List<String> getArvot() {
        return arvot;
    }

    public void setArvot(List<String> arvot) {
        this.arvot = arvot;
    }
}