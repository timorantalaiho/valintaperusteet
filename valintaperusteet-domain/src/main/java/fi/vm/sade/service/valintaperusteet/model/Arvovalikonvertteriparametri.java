package fi.vm.sade.service.valintaperusteet.model;

import org.codehaus.jackson.map.annotate.JsonView;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * User: kwuoti Date: 21.2.2013 Time: 9.20
 */
@Entity
@Table(name = "arvovalikonvertteriparametri")
@Cacheable(true)
public class Arvovalikonvertteriparametri extends Konvertteriparametri {


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("[min=");
        sb.append(minValue);
        sb.append(", max=");
        sb.append(maxValue);
        sb.append("]");

        return sb.toString();
    }

    @JsonView(JsonViews.Basic.class)
    //@Column(precision = 17, scale = 4, name = "minvalue", nullable = false)
    @Column(name = "minvalue", nullable = false)
    private String minValue;

    @JsonView(JsonViews.Basic.class)
    @Column(name = "maxvalue", nullable = false)
    private String maxValue;

    @JsonView(JsonViews.Basic.class)
    @Column(name = "palauta_haettu_arvo")
    private String palautaHaettuArvo;

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public String getPalautaHaettuArvo() {
        return palautaHaettuArvo;
    }

    public void setPalautaHaettuArvo(String palautaHaettuArvo) {
        this.palautaHaettuArvo = palautaHaettuArvo;
    }

    private static final long serialVersionUID = 7028232303346391201L;
}
