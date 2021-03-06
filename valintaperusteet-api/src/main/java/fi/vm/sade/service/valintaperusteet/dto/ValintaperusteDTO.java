package fi.vm.sade.service.valintaperusteet.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import fi.vm.sade.service.valintaperusteet.dto.model.Funktiotyyppi;
import fi.vm.sade.service.valintaperusteet.dto.model.Valintaperustelahde;

@ApiModel(value = "ValintaperusteDTO", description = "Valintaperuste")
public class ValintaperusteDTO {

    @ApiModelProperty(value = "Tunniste")
    private String tunniste;

    @ApiModelProperty(value = "Kuvaus")
    private String kuvaus;

    @ApiModelProperty(value = "Funktiotyyppi")
    private Funktiotyyppi funktiotyyppi;

    @ApiModelProperty(value = "Valintaperusteen lähde")
    private Valintaperustelahde lahde;

    @ApiModelProperty(value = "Onko valintaperuste pakollinen, eli laskenta merkitsee virheelliseksi hakemuksen millä ei ole arvoa tunnisteelle.")
    private boolean onPakollinen;

    @ApiModelProperty(value = "Arvovälin minimi")
    private String min;

    @ApiModelProperty(value = "Arvovälin maksimi")
    private String max;

    @ApiModelProperty(value = "Arvot")
    private List<String> arvot;

    @ApiModelProperty(value = "Osallistumistunniste")
    private String osallistuminenTunniste;

    @ApiModelProperty(value = "Vaatiiko syötettävä arvo osallistumisen")
    private Boolean vaatiiOsallistumisen = true;

    @ApiModelProperty(value = "Voidaanko arvo syöttää kaikille vai vaan kutsutuille")
    private Boolean syotettavissaKaikille = true;

    @ApiModelProperty(value = "Tilastoidaanko")
    private Boolean tilastoidaan = null;

    @ApiModelProperty(value = "Syötettävän arvon tyyppi")
    private KoodiDTO syötettavanArvonTyyppi = null;

    public Boolean getTilastoidaan() {
        return tilastoidaan;
    }

    public void setTilastoidaan(Boolean tilastoidaan) {
        this.tilastoidaan = tilastoidaan;
    }

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

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public List<String> getArvot() {
        return arvot;
    }

    public void setArvot(List<String> arvot) {
        this.arvot = arvot;
    }

    public String getOsallistuminenTunniste() {
        return osallistuminenTunniste;
    }

    public void setOsallistuminenTunniste(String osallistuminenTunniste) {
        this.osallistuminenTunniste = osallistuminenTunniste;
    }

    public Boolean getVaatiiOsallistumisen() {
        return vaatiiOsallistumisen;
    }

    public void setVaatiiOsallistumisen(Boolean vaatiiOsallistumisen) {
        this.vaatiiOsallistumisen = vaatiiOsallistumisen;
    }

    public Boolean getSyotettavissaKaikille() {
        return syotettavissaKaikille;
    }

    public void setSyotettavissaKaikille(Boolean syotettavissaKaikille) {
        this.syotettavissaKaikille = syotettavissaKaikille;
    }

    public KoodiDTO getSyötettavanArvonTyyppi() {
        return syötettavanArvonTyyppi;
    }

    public void setSyötettavanArvonTyyppi(KoodiDTO syötettavanArvonTyyppi) {
        this.syötettavanArvonTyyppi = syötettavanArvonTyyppi;
    }
}
