package fi.vm.sade.service.valintaperusteet.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jukais on 13.3.2014.
 */
public class ValintatapajonoJarjestyskriteereillaDTO {
    private Integer aloituspaikat;
    private String kuvaus;
    private String nimi;
    private String oid;
    private int prioriteetti;
    private Boolean siirretaanSijoitteluun;
    private String tasasijasaanto;
    private List<ValintaperusteetJarjestyskriteeriDTO> jarjestyskriteerit = new ArrayList<ValintaperusteetJarjestyskriteeriDTO>();
    private boolean eiVarasijatayttoa;

    public void setAloituspaikat(Integer aloituspaikat) {
        this.aloituspaikat = aloituspaikat;
    }

    public Integer getAloituspaikat() {
        return aloituspaikat;
    }

    public void setKuvaus(String kuvaus) {
        this.kuvaus = kuvaus;
    }

    public String getKuvaus() {
        return kuvaus;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public String getNimi() {
        return nimi;
    }


    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOid() {
        return oid;
    }

    public void setPrioriteetti(int prioriteetti) {
        this.prioriteetti = prioriteetti;
    }

    public int getPrioriteetti() {
        return prioriteetti;
    }

    public void setSiirretaanSijoitteluun(Boolean siirretaanSijoitteluun) {
        this.siirretaanSijoitteluun = siirretaanSijoitteluun;
    }

    public Boolean getSiirretaanSijoitteluun() {
        return siirretaanSijoitteluun;
    }

    public void setTasasijasaanto(String tasasijasaanto) {
        this.tasasijasaanto = tasasijasaanto;
    }

    public String getTasasijasaanto() {
        return tasasijasaanto;
    }

    public List<ValintaperusteetJarjestyskriteeriDTO> getJarjestyskriteerit() {
        return jarjestyskriteerit;
    }

    public void setJarjestyskriteerit(List<ValintaperusteetJarjestyskriteeriDTO> jarjestyskriteerit) {
        this.jarjestyskriteerit = jarjestyskriteerit;
    }

    public boolean getEiVarasijatayttoa() {
        return eiVarasijatayttoa;
    }

    public void setEiVarasijatayttoa(boolean eiVarasijatayttoa) {
        this.eiVarasijatayttoa = eiVarasijatayttoa;
    }
}
