package fi.vm.sade.service.valintaperusteet.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * User: kwuoti
 * Date: 21.2.2013
 * Time: 9.21
 */
@Entity
@Table(name = "arvokonvertteriparametri")
@Cacheable(true)
public class Arvokonvertteriparametri extends Konvertteriparametri {
    @Column(name = "arvo")
    private String arvo;

    @Column(name = "hylkaysperuste", nullable = false)
    private String hylkaysperuste;

    @JoinColumn(name = "tekstiryhma_id", nullable = true)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    private TekstiRyhma kuvaukset;

    public String getArvo() {
        return arvo;
    }

    public void setArvo(String arvo) {
        this.arvo = arvo;
    }

    public String getHylkaysperuste() {
        return hylkaysperuste;
    }

    public void setHylkaysperuste(String hylkaysperuste) {
        this.hylkaysperuste = hylkaysperuste;
    }

    public TekstiRyhma getKuvaukset() {
        return kuvaukset;
    }

    public void setKuvaukset(TekstiRyhma kuvaukset) {
        this.kuvaukset = kuvaukset;
    }
}
