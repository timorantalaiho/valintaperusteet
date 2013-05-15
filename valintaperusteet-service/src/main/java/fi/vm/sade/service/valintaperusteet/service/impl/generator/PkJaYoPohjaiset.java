package fi.vm.sade.service.valintaperusteet.service.impl.generator;

import fi.vm.sade.service.valintaperusteet.model.*;
import scala.actors.threadpool.Arrays;

/**
 * User: kwuoti
 * Date: 5.3.2013
 * Time: 12.14
 */
public class PkJaYoPohjaiset {

    public static final String tyokokemuskuukaudet = "tyokokemuskuukaudet";
    public static final String sukupuoli = "sukupuoli";
    public static final String koulutuspaikkaAmmatillisenTutkintoon = "koulutuspaikkaAmmatillisenTutkintoon";

    public static Laskentakaava luoHakutoivejarjestyspisteteytysmalli() {
        Funktiokutsu pisteet = GenericHelper.luoLukuarvo(2.0);
        Funktiokutsu nollaarvo = GenericHelper.luoLukuarvo(0.0);
        Funktiokutsu ensimmainenHakutoive = GenericHelper.luoEnsimmainenHakutoive();

        Funktiokutsu jos = GenericHelper.luoJosFunktio(ensimmainenHakutoive, pisteet, nollaarvo);
        Laskentakaava laskentakaava = GenericHelper.luoLaskentakaavaJaNimettyFunktio(jos,
                "Hakutoivejärjestyspisteytys, 2 aste, pk ja yo");
        return laskentakaava;
    }


    public static Laskentakaava luoTyokokemuspisteytysmalli() {

        Arvovalikonvertteriparametri[] konvs = {
                GenericHelper.luoArvovalikonvertteriparametri(3.0, 6.0, 1.0),
                GenericHelper.luoArvovalikonvertteriparametri(6.0, 12.0, 2.0),
                GenericHelper.luoArvovalikonvertteriparametri(12.0, 100000.0, 3.0)
        };

        Funktiokutsu f = GenericHelper.luoHaeLukuarvo(GenericHelper.luoValintaperusteViite(
                tyokokemuskuukaudet, true, false, Valintaperustelahde.HAETTAVA_ARVO), Arrays.asList(konvs));
        return GenericHelper.luoLaskentakaavaJaNimettyFunktio(f, "Työkokemuspisteytys, 2 aste, pk ja yo");
    }

    public static Laskentakaava luoSukupuolipisteytysmalli() {
        Funktiokutsu thenHaara = GenericHelper.luoLukuarvo(2.0);
        Funktiokutsu elseHaara = GenericHelper.luoLukuarvo(0.0);
        Funktiokutsu ehto = GenericHelper.luoDemografia(sukupuoli, 30.0);

        Funktiokutsu jos = GenericHelper.luoJosFunktio(ehto, thenHaara, elseHaara);
        return GenericHelper.luoLaskentakaavaJaNimettyFunktio(jos, "Sukupuolipisteytys, 2 aste, pk ja yo");
    }

    public static Laskentakaava ilmanKoulutuspaikkaaPisteytysmalli() {
        Funktiokutsu thenHaara = GenericHelper.luoLukuarvo(8.0);
        Funktiokutsu elseHaara = GenericHelper.luoLukuarvo(0.0);
        Funktiokutsu ehto = GenericHelper.luoEi(GenericHelper.luoHaeTotuusarvo(
                GenericHelper.luoValintaperusteViite(koulutuspaikkaAmmatillisenTutkintoon, false, false,
                        Valintaperustelahde.HAETTAVA_ARVO)));

        Funktiokutsu jos = GenericHelper.luoJosFunktio(ehto, thenHaara, elseHaara);

        return GenericHelper.luoLaskentakaavaJaNimettyFunktio(
                jos, "Ilman koulutuspaikkaa -pisteytys, 2 aste, pk ja yo");
    }

    public static Laskentakaava luoYleinenKoulumenestysLaskentakaava(Laskentakaava laskentakaava, String nimi) {
        Funktiokutsu konvertteri = new Funktiokutsu();
        konvertteri.setFunktionimi(Funktionimi.KONVERTOILUKUARVO);
        Funktioargumentti funk = new Funktioargumentti();
        funk.setLaskentakaavaChild(laskentakaava);
        funk.setIndeksi(1);

        konvertteri.getFunktioargumentit().add(funk);
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(5.50, 5.75, 1));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(5.75, 6.00, 2));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(6.00, 6.25, 3));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(6.25, 6.50, 4));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(6.50, 6.75, 5));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(6.75, 7.00, 6));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(7.00, 7.25, 7));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(7.25, 7.50, 8));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(7.50, 7.75, 9));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(7.75, 8.00, 10));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(8.00, 8.25, 11));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(8.25, 8.50, 12));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(8.50, 8.75, 13));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(8.75, 9.00, 14));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(9.00, 9.25, 15));
        konvertteri.getArvovalikonvertteriparametrit().add(GenericHelper.luoArvovalikonvertteriparametri(9.25, 10.1, 16));

        Laskentakaava palautettavaLaskentakaava = GenericHelper.luoLaskentakaavaJaNimettyFunktio(konvertteri,
                nimi);
        return palautettavaLaskentakaava;
    }
}
