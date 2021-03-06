package fi.vm.sade.service.valintaperusteet.dto;

import java.util.List;

public class HakukohteenValintaperusteAvaimetDTO {
    private List<String> tunnisteet;

    private List<String> minimit;

    private List<String> maksimit;

    private List<String> palautaHaettutArvot;

    private List<String> arvot;

    private List<String> hylkaysperusteet;

    public List<String> getTunnisteet() {
        return tunnisteet;
    }

    public void setTunnisteet(List<String> tunnisteet) {
        this.tunnisteet = tunnisteet;
    }

    public List<String> getMinimit() {
        return minimit;
    }

    public void setMinimit(List<String> minimit) {
        this.minimit = minimit;
    }

    public List<String> getMaksimit() {
        return maksimit;
    }

    public void setMaksimit(List<String> maksimit) {
        this.maksimit = maksimit;
    }

    public List<String> getPalautaHaettutArvot() {
        return palautaHaettutArvot;
    }

    public void setPalautaHaettutArvot(List<String> palautaHaettutArvot) {
        this.palautaHaettutArvot = palautaHaettutArvot;
    }

    public List<String> getArvot() {
        return arvot;
    }

    public void setArvot(List<String> arvot) {
        this.arvot = arvot;
    }

    public List<String> getHylkaysperusteet() {
        return hylkaysperusteet;
    }

    public void setHylkaysperusteet(List<String> hylkaysperusteet) {
        this.hylkaysperusteet = hylkaysperusteet;
    }
}
