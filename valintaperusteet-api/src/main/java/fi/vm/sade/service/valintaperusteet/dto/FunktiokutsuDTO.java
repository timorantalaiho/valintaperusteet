package fi.vm.sade.service.valintaperusteet.dto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import fi.vm.sade.service.valintaperusteet.dto.model.ValidointivirheDTO;
import fi.vm.sade.service.valintaperusteet.dto.model.Funktionimi;

/**
 * User: wuoti Date: 2.12.2013 Time: 9.15
 */
@ApiModel(value = "FunktiokutsuDTO", description = "Funktiokutsu")
public class FunktiokutsuDTO {

    @ApiModelProperty(value = "Funktion nimi", required = true)
    private Funktionimi funktionimi;

    @ApiModelProperty(value = "Tallennetun tuloksen tunniste")
    private String tulosTunniste;

    @ApiModelProperty(value = "Tallennetun tuloksen suomenkielinen teksti")
    private String tulosTekstiFi;

    @ApiModelProperty(value = "Tallennetun tuloksen ruotsinkielinen teksti")
    private String tulosTekstiSv;

    @ApiModelProperty(value = "Tallennetun tuloksen englanninkielinen teksti")
    private String tulosTekstiEn;

    @ApiModelProperty(value = "Tallennetaanko tulos", required = true)
    private Boolean tallennaTulos;

    @ApiModelProperty(value = "Arvokonvertteriparametrit")
    private Set<ArvokonvertteriparametriDTO> arvokonvertteriparametrit = new HashSet<ArvokonvertteriparametriDTO>();

    @ApiModelProperty(value = "Arvovälikonvertteriparametrit")
    private Set<ArvovalikonvertteriparametriDTO> arvovalikonvertteriparametrit = new HashSet<ArvovalikonvertteriparametriDTO>();

    @ApiModelProperty(value = "Syöteparametrit")
    private Set<SyoteparametriDTO> syoteparametrit = new HashSet<SyoteparametriDTO>();

    @ApiModelProperty(value = "Funktioargumentit")
    private List<FunktioargumenttiDTO> funktioargumentit = new LinkedList<FunktioargumenttiDTO>();

    @ApiModelProperty(value = "Valintaperusteviitteet")
    private Set<ValintaperusteViiteDTO> valintaperusteviitteet = new TreeSet<ValintaperusteViiteDTO>();

    @ApiModelProperty(value = "Validointivirheet")
    private List<ValidointivirheDTO> validointivirheet = new ArrayList<ValidointivirheDTO>();

    public Funktionimi getFunktionimi() {
        return funktionimi;
    }

    public void setFunktionimi(Funktionimi funktionimi) {
        this.funktionimi = funktionimi;
    }

    public Set<ArvokonvertteriparametriDTO> getArvokonvertteriparametrit() {
        return arvokonvertteriparametrit;
    }

    public void setArvokonvertteriparametrit(Set<ArvokonvertteriparametriDTO> arvokonvertteriparametrit) {
        this.arvokonvertteriparametrit = arvokonvertteriparametrit;
    }

    public Set<ArvovalikonvertteriparametriDTO> getArvovalikonvertteriparametrit() {
        return arvovalikonvertteriparametrit;
    }

    public void setArvovalikonvertteriparametrit(Set<ArvovalikonvertteriparametriDTO> arvovalikonvertteriparametrit) {
        this.arvovalikonvertteriparametrit = arvovalikonvertteriparametrit;
    }

    public Set<SyoteparametriDTO> getSyoteparametrit() {
        return syoteparametrit;
    }

    public void setSyoteparametrit(Set<SyoteparametriDTO> syoteparametrit) {
        this.syoteparametrit = syoteparametrit;
    }

    public List<FunktioargumenttiDTO> getFunktioargumentit() {
        return funktioargumentit;
    }

    public void setFunktioargumentit(List<FunktioargumenttiDTO> funktioargumentit) {
        this.funktioargumentit = funktioargumentit;
    }

    public Set<ValintaperusteViiteDTO> getValintaperusteviitteet() {
        return valintaperusteviitteet;
    }

    public void setValintaperusteviitteet(Set<ValintaperusteViiteDTO> valintaperusteviitteet) {
        this.valintaperusteviitteet = valintaperusteviitteet;
    }

    public List<ValidointivirheDTO> getValidointivirheet() {
        return validointivirheet;
    }

    public void setValidointivirheet(List<ValidointivirheDTO> validointivirheet) {
        this.validointivirheet = validointivirheet;
    }

    public String getTulosTunniste() {
        return tulosTunniste;
    }

    public void setTulosTunniste(String tulosTunniste) {
        this.tulosTunniste = tulosTunniste;
    }

    public String getTulosTekstiFi() {
        return tulosTekstiFi;
    }

    public void setTulosTekstiFi(String tulosTekstiFi) {
        this.tulosTekstiFi = tulosTekstiFi;
    }

    public String getTulosTekstiSv() {
        return tulosTekstiSv;
    }

    public void setTulosTekstiSv(String tulosTekstiSv) {
        this.tulosTekstiSv = tulosTekstiSv;
    }

    public String getTulosTekstiEn() {
        return tulosTekstiEn;
    }

    public void setTulosTekstiEn(String tulosTekstiEn) {
        this.tulosTekstiEn = tulosTekstiEn;
    }

    public Boolean getTallennaTulos() {
        return tallennaTulos;
    }

    public void setTallennaTulos(Boolean tallennaTulos) {
        this.tallennaTulos = tallennaTulos;
    }
}