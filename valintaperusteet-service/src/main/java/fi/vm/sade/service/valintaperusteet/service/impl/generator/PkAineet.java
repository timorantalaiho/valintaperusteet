package fi.vm.sade.service.valintaperusteet.service.impl.generator;

import java.util.Map;

import fi.vm.sade.service.valintaperusteet.dto.model.Valintaperustelahde;
import fi.vm.sade.service.valintaperusteet.model.Funktiokutsu;
import fi.vm.sade.service.valintaperusteet.model.Laskentakaava;

/**
 * User: wuoti Date: 31.5.2013 Time: 12.04
 */
public class PkAineet extends Aineet {
    public static final String PK_Valinnainen1 = "_VAL1";
    public static final String PK_Valinnainen2 = "_VAL2";
    public static final String PK_etuliite = "PK_";
    public static final String PK_kuvausjalkiliite = ", PK päättötodistus, mukaanlukien valinnaiset";
    public static final String PK_kuvausjalkiliite_ilman_valinnaisia = ", PK päättötodistus";

    public static final String kotitalous = "KO";
    public static final String kasityo = "KS";

    public static final String PK_OPPIAINE_TEMPLATE = PK_etuliite + "%s_OPPIAINE";

    private enum PkAine {
        KO(kotitalous, "Kotitalous"), KS(kasityo, "Käsityö");

        PkAine(String tunniste, String kuvaus) {
            this.tunniste = tunniste;
            this.kuvaus = kuvaus;
        }

        String tunniste;
        String kuvaus;
    }

    public PkAineet(boolean lasketaankoValinnaiset) {
        for (PkAine aine : PkAine.values()) {
            getAineet().put(aine.tunniste, aine.kuvaus);
        }

        for (Map.Entry<String, String> aine : getAineet().entrySet()) {
            String ainetunniste = aine.getKey();
            String ainekuvaus = aine.getValue();
            if(lasketaankoValinnaiset) {
                getKaavat().put(ainetunniste, luoPKAine(ainetunniste, ainekuvaus));
            } else {
                getKaavat().put(ainetunniste, luoPKAineIlmanValinnaisia(ainetunniste,ainekuvaus));
            }
        }
    }

    public static String pakollinen(String ainetunniste) {
        return PK_etuliite + ainetunniste;
    }

    public static String valinnainen1(String ainetunniste) {
        return PK_etuliite + ainetunniste + PK_Valinnainen1;
    }

    public static String valinnainen2(String ainetunniste) {
        return PK_etuliite + ainetunniste + PK_Valinnainen2;
    }

    private Laskentakaava luoPKAine(String ainetunniste, String kuvaus) {
        Funktiokutsu aine = GenericHelper.luoHaeLukuarvo(GenericHelper.luoValintaperusteViite(pakollinen(ainetunniste),
                false, Valintaperustelahde.HAETTAVA_ARVO));
        Funktiokutsu aineValinnainen1 = GenericHelper.luoHaeLukuarvo(GenericHelper.luoValintaperusteViite(
                valinnainen1(ainetunniste), false, Valintaperustelahde.HAETTAVA_ARVO));
        Funktiokutsu aineValinnainen2 = GenericHelper.luoHaeLukuarvo(GenericHelper.luoValintaperusteViite(
                valinnainen2(ainetunniste), false, Valintaperustelahde.HAETTAVA_ARVO));

        Funktiokutsu valinnainenKeskiarvo = GenericHelper.luoKeskiarvo(aineValinnainen1, aineValinnainen2);
        Funktiokutsu keskiarvo = GenericHelper.luoKeskiarvo(aine, valinnainenKeskiarvo);

        Laskentakaava laskentakaava = GenericHelper.luoLaskentakaavaJaNimettyFunktio(keskiarvo, kuvaus
                + PK_kuvausjalkiliite);
        return laskentakaava;
    }

    private Laskentakaava luoPKAineIlmanValinnaisia(String ainetunniste, String kuvaus) {
        Funktiokutsu aine = GenericHelper.luoHaeLukuarvo(GenericHelper.luoValintaperusteViite(pakollinen(ainetunniste),
                false, Valintaperustelahde.HAETTAVA_ARVO));

        Laskentakaava laskentakaava = GenericHelper.luoLaskentakaavaJaNimettyFunktio(aine, kuvaus
                + PK_kuvausjalkiliite_ilman_valinnaisia);
        return laskentakaava;
    }

    public static String oppiaine(String ainetunniste) {
        return String.format(PK_OPPIAINE_TEMPLATE, ainetunniste);
    }
}
