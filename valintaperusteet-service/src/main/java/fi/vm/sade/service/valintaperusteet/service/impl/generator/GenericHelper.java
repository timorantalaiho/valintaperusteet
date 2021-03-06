package fi.vm.sade.service.valintaperusteet.service.impl.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fi.vm.sade.service.valintaperusteet.dto.model.Funktionimi;
import fi.vm.sade.service.valintaperusteet.dto.model.Valintaperustelahde;
import fi.vm.sade.service.valintaperusteet.model.*;

public class GenericHelper {
    private GenericHelper() {
    }

    public static List<Funktioargumentti> luoFunktioargumentit(FunktionArgumentti... args) {
        List<Funktioargumentti> fargs = new ArrayList<Funktioargumentti>();
        for (int i = 0; i < args.length; ++i) {
            FunktionArgumentti k = args[i];
            Funktioargumentti arg = new Funktioargumentti();
            if (k instanceof Funktiokutsu) {
                arg.setFunktiokutsuChild((Funktiokutsu) k);
            } else if (k instanceof Laskentakaava) {
                arg.setLaskentakaavaChild((Laskentakaava) k);
            }
            arg.setIndeksi(i + 1);
            fargs.add(arg);
        }
        return fargs;
    }

    public static Funktiokutsu luoKeskiarvo(FunktionArgumentti... args) {
        Funktiokutsu funktiokutsu = new Funktiokutsu();
        funktiokutsu.setFunktionimi(Funktionimi.KESKIARVO);
        funktiokutsu.getFunktioargumentit().addAll(luoFunktioargumentit(args));
        return funktiokutsu;
    }

    public static Funktiokutsu luoMaksimi(FunktionArgumentti... args) {
        Funktiokutsu funktiokutsu = new Funktiokutsu();
        funktiokutsu.setFunktionimi(Funktionimi.MAKSIMI);
        funktiokutsu.getFunktioargumentit().addAll(luoFunktioargumentit(args));
        return funktiokutsu;
    }

    public static Funktiokutsu nParastaKeskiarvo(int n, FunktionArgumentti... args) {
        Funktiokutsu nparasta = new Funktiokutsu();
        nparasta.setFunktionimi(Funktionimi.KESKIARVONPARASTA);
        Syoteparametri s = new Syoteparametri();
        s.setAvain("n");
        s.setArvo(String.valueOf(n));
        nparasta.getSyoteparametrit().add(s);
        nparasta.getFunktioargumentit().addAll(luoFunktioargumentit(args));
        return nparasta;
    }

    public static Funktiokutsu luoLukuarvo(double arvo) {
        Funktiokutsu funktiokutsu = new Funktiokutsu();
        funktiokutsu.setFunktionimi(Funktionimi.LUKUARVO);
        Syoteparametri syoteparametri = new Syoteparametri();
        syoteparametri.setAvain("luku");
        syoteparametri.setArvo(String.valueOf(arvo));
        funktiokutsu.getSyoteparametrit().add(syoteparametri);
        return funktiokutsu;
    }

    public static ValintaperusteViite luoValintaperusteViite(String tunniste, boolean onPakollinen, Valintaperustelahde lahde) {
        return luoValintaperusteViite(tunniste, onPakollinen, lahde, "");
    }

    public static ValintaperusteViite luoValintaperusteViite(String tunniste, boolean onPakollinen, Valintaperustelahde lahde, String kuvaus) {
        return luoValintaperusteViite(tunniste, onPakollinen, lahde, kuvaus, false);
    }

    public static ValintaperusteViite luoValintaperusteViite(String tunniste, boolean onPakollinen, Valintaperustelahde lahde, String kuvaus, boolean epasuoraViittaus) {
        ValintaperusteViite vp = new ValintaperusteViite();
        vp.setTunniste(tunniste);
        vp.setOnPakollinen(onPakollinen);
        vp.setLahde(lahde);
        vp.setKuvaus(kuvaus);
        vp.setEpasuoraViittaus(epasuoraViittaus);
        vp.setIndeksi(1);
        return vp;
    }

    public static ValintaperusteViite luoValintaperusteViite(String tunniste, boolean onPakollinen, Valintaperustelahde lahde, String kuvaus, boolean epasuoraViittaus, TekstiRyhma kuvaukset) {
        ValintaperusteViite vp = new ValintaperusteViite();
        vp.setTunniste(tunniste);
        vp.setOnPakollinen(onPakollinen);
        vp.setLahde(lahde);
        vp.setKuvaus(kuvaus);
        vp.setEpasuoraViittaus(epasuoraViittaus);
        vp.setIndeksi(1);
        vp.setKuvaukset(kuvaukset);
        return vp;
    }

    public static Funktiokutsu luoHaeLukuarvo(ValintaperusteViite vp) {
        Funktiokutsu funktiokutsu = new Funktiokutsu();
        funktiokutsu.setFunktionimi(Funktionimi.HAELUKUARVO);
        funktiokutsu.getValintaperusteviitteet().add(vp);
        return funktiokutsu;
    }

    public static Funktiokutsu luoHaeLukuarvoEhdolla(ValintaperusteViite vp, ValintaperusteViite ehto) {
        Funktiokutsu funktiokutsu = new Funktiokutsu();
        funktiokutsu.setFunktionimi(Funktionimi.HAELUKUARVOEHDOLLA);
        vp.setIndeksi(1);
        funktiokutsu.getValintaperusteviitteet().add(vp);
        ehto.setIndeksi(2);
        funktiokutsu.getValintaperusteviitteet().add(ehto);
        return funktiokutsu;
    }

    public static Syoteparametri luoSyoteparametri(String avain, String arvo) {
        Syoteparametri sp = new Syoteparametri();
        sp.setAvain(avain);
        sp.setArvo(arvo);
        return sp;
    }

    public static Funktiokutsu luoHaeLukuarvo(ValintaperusteViite vp, double oletusarvo) {
        Funktiokutsu funktiokutsu = luoHaeLukuarvo(vp);
        funktiokutsu.getSyoteparametrit().add(luoSyoteparametri("oletusarvo", String.valueOf(oletusarvo)));
        return funktiokutsu;
    }

    public static Funktiokutsu luoEnsimmainenHakutoive() {
        Funktiokutsu hakutoive = new Funktiokutsu();
        hakutoive.setFunktionimi(Funktionimi.HAKUTOIVE);
        Syoteparametri a = new Syoteparametri();
        a.setAvain("n");
        a.setArvo("1");
        hakutoive.getSyoteparametrit().add(a);
        return hakutoive;
    }

    public static Funktiokutsu luoNsHakutoive(int n) {
        Funktiokutsu hakutoive = new Funktiokutsu();
        hakutoive.setFunktionimi(Funktionimi.HAKUTOIVE);
        Syoteparametri a = new Syoteparametri();
        a.setAvain("n");
        a.setArvo(String.valueOf(n));
        hakutoive.getSyoteparametrit().add(a);
        return hakutoive;
    }

    public static Funktiokutsu luoJosFunktio(Funktiokutsu ehto, Funktiokutsu totta, Funktiokutsu vale) {
        Funktiokutsu f = new Funktiokutsu();
        f.setFunktionimi(Funktionimi.JOS);
        Funktioargumentti josArgumentti = new Funktioargumentti();
        josArgumentti.setFunktiokutsuChild(ehto);
        josArgumentti.setIndeksi(1);
        f.getFunktioargumentit().add(josArgumentti);
        Funktioargumentti tottaArgumentti = new Funktioargumentti();
        tottaArgumentti.setFunktiokutsuChild(totta);
        tottaArgumentti.setIndeksi(2);
        f.getFunktioargumentit().add(tottaArgumentti);
        Funktioargumentti valettaArgumentti = new Funktioargumentti();
        valettaArgumentti.setFunktiokutsuChild(vale);
        valettaArgumentti.setIndeksi(3);
        f.getFunktioargumentit().add(valettaArgumentti);
        return f;
    }

    public static Funktiokutsu luoNimettyFunktio(Funktiokutsu funktiokutsu, String nimi) {
        Funktiokutsu nimettyFunktio = new Funktiokutsu();
        switch (funktiokutsu.getFunktionimi().getTyyppi()) {
            case LUKUARVOFUNKTIO:
                nimettyFunktio.setFunktionimi(Funktionimi.NIMETTYLUKUARVO);
                break;
            case TOTUUSARVOFUNKTIO:
                nimettyFunktio.setFunktionimi(Funktionimi.NIMETTYTOTUUSARVO);
                break;
            case EI_VALIDI:
                throw new RuntimeException("Funktiokutsu ei ole validi");
        }
        Syoteparametri s = new Syoteparametri();
        s.setAvain("nimi");
        s.setArvo(nimi);
        nimettyFunktio.getSyoteparametrit().add(s);
        Funktioargumentti funktioargumentti = new Funktioargumentti();
        funktioargumentti.setFunktiokutsuChild(funktiokutsu);
        funktioargumentti.setIndeksi(1);
        nimettyFunktio.getFunktioargumentit().add(funktioargumentti);
        return nimettyFunktio;
    }

    public static Laskentakaava luoLaskentakaava(Funktiokutsu funktiokutsu, String nimi) {
        Laskentakaava laskentakaava = new Laskentakaava();
        laskentakaava.setNimi(nimi);
        laskentakaava.setKuvaus(nimi);
        laskentakaava.setFunktiokutsu(funktiokutsu);
        laskentakaava.setOnLuonnos(false);
        return laskentakaava;
    }

    public static Laskentakaava luoLaskentakaavaJaNimettyFunktio(Funktiokutsu funktiokutsu, String nimi) {
        Funktiokutsu nimetty = luoNimettyFunktio(funktiokutsu, nimi);
        return luoLaskentakaava(nimetty, nimi);
    }

    public static Arvovalikonvertteriparametri luoArvovalikonvertteriparametri(double min, double max, double paluuarvo) {
        Arvovalikonvertteriparametri a = new Arvovalikonvertteriparametri();
        a.setMaxValue(String.valueOf(max));
        a.setMinValue(String.valueOf(min));
        a.setPalautaHaettuArvo("false");
        a.setPaluuarvo(String.valueOf(paluuarvo));
        a.setHylkaysperuste("false");
        return a;
    }

    public static Arvovalikonvertteriparametri luoArvovalikonvertteriparametri(double min, double max) {
        Arvovalikonvertteriparametri a = new Arvovalikonvertteriparametri();
        a.setMaxValue(String.valueOf(max));
        a.setMinValue(String.valueOf(min));
        a.setPalautaHaettuArvo("true");
        a.setHylkaysperuste("false");
        return a;
    }

    public static Arvovalikonvertteriparametri luoArvovalikonvertteriparametri(String min, String max) {
        Arvovalikonvertteriparametri a = new Arvovalikonvertteriparametri();
        a.setMaxValue(max);
        a.setMinValue(min);
        a.setPalautaHaettuArvo("true");
        a.setHylkaysperuste("false");
        return a;
    }

    public static Arvokonvertteriparametri luoArvokonvertteriparametri(String arvo, String paluuarvo, boolean hylkaysperuste) {
        Arvokonvertteriparametri a = new Arvokonvertteriparametri();
        a.setArvo(arvo);
        a.setPaluuarvo(paluuarvo);
        a.setHylkaysperuste(String.valueOf(hylkaysperuste));

        return a;
    }

    public static Funktiokutsu luoHaeMerkkijonoJaKonvertoiLukuarvoksi(ValintaperusteViite vp, Collection<Arvokonvertteriparametri> arvokonvertterit) {
        Funktiokutsu funktiokutsu = new Funktiokutsu();
        funktiokutsu.setFunktionimi(Funktionimi.HAEMERKKIJONOJAKONVERTOILUKUARVOKSI);
        funktiokutsu.getArvokonvertteriparametrit().addAll(arvokonvertterit);
        funktiokutsu.getValintaperusteviitteet().add(vp);
        return funktiokutsu;
    }

    public static Funktiokutsu luoDemografia(String tunniste, double prosenttiosuus) {
        Funktiokutsu funktiokutsu = new Funktiokutsu();
        funktiokutsu.setFunktionimi(Funktionimi.DEMOGRAFIA);
        funktiokutsu.getSyoteparametrit().add(luoSyoteparametri("tunniste", tunniste));
        funktiokutsu.getSyoteparametrit().add(luoSyoteparametri("prosenttiosuus", String.valueOf(prosenttiosuus)));
        return funktiokutsu;
    }

    public static Funktiokutsu luoHaeTotuusarvo(ValintaperusteViite vp) {
        Funktiokutsu funktiokutsu = new Funktiokutsu();
        funktiokutsu.setFunktionimi(Funktionimi.HAETOTUUSARVO);
        funktiokutsu.getValintaperusteviitteet().add(vp);
        return funktiokutsu;
    }

    public static Funktiokutsu luoHaeTotuusarvo(ValintaperusteViite vp, boolean oletusarvo) {
        Funktiokutsu funktiokutsu = luoHaeTotuusarvo(vp);
        funktiokutsu.getSyoteparametrit().add(luoSyoteparametri("oletusarvo", String.valueOf(oletusarvo)));
        return funktiokutsu;
    }

    public static Funktiokutsu luoEi(FunktionArgumentti arg) {
        Funktiokutsu ei = new Funktiokutsu();
        ei.setFunktionimi(Funktionimi.EI);
        ei.getFunktioargumentit().addAll(luoFunktioargumentit(arg));
        return ei;
    }

    public static Funktiokutsu luoHaeLukuarvo(ValintaperusteViite vp, Collection<Arvovalikonvertteriparametri> konvs) {
        Funktiokutsu funktiokutsu = luoHaeLukuarvo(vp);
        funktiokutsu.getArvovalikonvertteriparametrit().addAll(konvs);
        return funktiokutsu;
    }

    public static Funktiokutsu luoHaeLukuarvo(ValintaperusteViite vp, double oletusarvo,
                                              Collection<Arvovalikonvertteriparametri> konvs) {
        Funktiokutsu funktiokutsu = luoHaeLukuarvo(vp, oletusarvo);
        funktiokutsu.getArvovalikonvertteriparametrit().addAll(konvs);
        return funktiokutsu;
    }

    public static Funktiokutsu luoTai(FunktionArgumentti... args) {
        Funktiokutsu funktiokutsu = new Funktiokutsu();
        funktiokutsu.setFunktionimi(Funktionimi.TAI);
        funktiokutsu.getFunktioargumentit().addAll(luoFunktioargumentit(args));
        return funktiokutsu;
    }

    public static Funktiokutsu luoHaeMerkkijonoJaKonvertoiTotuusarvoksi(ValintaperusteViite vp, boolean oletusarvo, Collection<Arvokonvertteriparametri> konvs) {
        Funktiokutsu funktiokutsu = luoHaeMerkkijonoJaKonvertoiTotuusarvoksi(vp, konvs);
        funktiokutsu.getSyoteparametrit().add(luoSyoteparametri("oletusarvo", String.valueOf(oletusarvo)));
        return funktiokutsu;
    }

    public static Funktiokutsu luoHaeMerkkijonoJaKonvertoiTotuusarvoksi(ValintaperusteViite vp, Collection<Arvokonvertteriparametri> konvs) {
        Funktiokutsu funktiokutsu = new Funktiokutsu();
        funktiokutsu.setFunktionimi(Funktionimi.HAEMERKKIJONOJAKONVERTOITOTUUSARVOKSI);
        funktiokutsu.getValintaperusteviitteet().add(vp);
        funktiokutsu.getArvokonvertteriparametrit().addAll(konvs);
        return funktiokutsu;
    }

    public static Funktiokutsu luoYhtasuuri(Funktiokutsu f1, Funktiokutsu f2) {
        Funktiokutsu f = new Funktiokutsu();
        f.setFunktionimi(Funktionimi.YHTASUURI);
        f.getFunktioargumentit().addAll(luoFunktioargumentit(f1, f2));
        return f;
    }

    public static Funktiokutsu luoJa(FunktionArgumentti... args) {
        Funktiokutsu f = new Funktiokutsu();
        f.setFunktionimi(Funktionimi.JA);
        f.getFunktioargumentit().addAll(luoFunktioargumentit(args));
        return f;
    }

    public static Funktiokutsu luoSumma(FunktionArgumentti... args) {
        Funktiokutsu f = new Funktiokutsu();
        f.setFunktionimi(Funktionimi.SUMMA);
        f.getFunktioargumentit().addAll(luoFunktioargumentit(args));

        return f;
    }

    public static Funktiokutsu luoPyoristys(Funktiokutsu f1, int tarkkuus) {
        Funktiokutsu f = new Funktiokutsu();
        f.setFunktionimi(Funktionimi.PYORISTYS);
        f.getSyoteparametrit().add(luoSyoteparametri("tarkkuus", String.valueOf(tarkkuus)));
        f.getFunktioargumentit().addAll(luoFunktioargumentit(f1));
        return f;
    }

    public static Funktiokutsu luoHaeMerkkijonoJaVertaaYhtasuuruus(ValintaperusteViite vp, String vertailtava, boolean oletusarvo) {
        Funktiokutsu f = luoHaeMerkkijonoJaVertaaYhtasuuruus(vp, vertailtava);
        f.getSyoteparametrit().add(luoSyoteparametri("oletusarvo", String.valueOf(oletusarvo)));
        return f;
    }

    public static Funktiokutsu luoHaeMerkkijonoJaVertaaYhtasuuruus(ValintaperusteViite vp, String vertailtava) {
        Funktiokutsu f = new Funktiokutsu();
        f.setFunktionimi(Funktionimi.HAEMERKKIJONOJAVERTAAYHTASUURUUS);
        f.getValintaperusteviitteet().add(vp);
        f.getSyoteparametrit().add(luoSyoteparametri("vertailtava", vertailtava));
        return f;
    }

    public static Funktiokutsu luoSuurempiTaiYhtasuuriKuin(FunktionArgumentti f1, FunktionArgumentti f2) {
        Funktiokutsu f = new Funktiokutsu();
        f.setFunktionimi(Funktionimi.SUUREMPITAIYHTASUURI);
        f.getFunktioargumentit().addAll(luoFunktioargumentit(f1, f2));
        return f;
    }

    public static Funktiokutsu luoHylkaa(FunktionArgumentti arg, String hylkaysperustekuvaus, String hylkaysperustekuvaus_sv) {
        Funktiokutsu f = new Funktiokutsu();
        f.setFunktionimi(Funktionimi.HYLKAA);
        f.getFunktioargumentit().addAll(luoFunktioargumentit(arg));
        f.getSyoteparametrit().add(luoSyoteparametri("hylkaysperustekuvaus_FI", hylkaysperustekuvaus));
        f.getSyoteparametrit().add(luoSyoteparametri("hylkaysperustekuvaus_SV", hylkaysperustekuvaus_sv));
        return f;
    }

    public static Funktiokutsu luoHylkaaArvovalilla(FunktionArgumentti arg, String hylkaysperustekuvaus, String hylkaysperustekuvaus_sv, String min, String max) {
        Funktiokutsu f = new Funktiokutsu();
        f.setFunktionimi(Funktionimi.HYLKAAARVOVALILLA);
        f.getFunktioargumentit().addAll(luoFunktioargumentit(arg));
        f.getSyoteparametrit().add(luoSyoteparametri("hylkaysperustekuvaus_FI", hylkaysperustekuvaus));
        f.getSyoteparametrit().add(luoSyoteparametri("hylkaysperustekuvaus_SV", hylkaysperustekuvaus_sv));
        f.getSyoteparametrit().add(luoSyoteparametri("arvovaliMin", min));
        f.getSyoteparametrit().add(luoSyoteparametri("arvovaliMax", max));
        return f;
    }

    public static class Painotus {
        private final FunktionArgumentti painokerroin;
        private final FunktionArgumentti painotettava;

        public Painotus(FunktionArgumentti painokerroin, FunktionArgumentti painotettava) {
            this.painokerroin = painokerroin;
            this.painotettava = painotettava;
        }

        public FunktionArgumentti getPainokerroin() {
            return painokerroin;
        }

        public FunktionArgumentti getPainotettava() {
            return painotettava;
        }
    }

    public static Funktiokutsu luoPainotettuKeskiarvo(Painotus... painotukset) {
        Funktiokutsu fk = new Funktiokutsu();
        fk.setFunktionimi(Funktionimi.PAINOTETTUKESKIARVO);

        FunktionArgumentti[] args = new FunktionArgumentti[painotukset.length * 2];
        int i = 0;
        for (Painotus p : painotukset) {
            args[i] = p.getPainokerroin();
            args[i + 1] = p.getPainotettava();
            i += 2;
        }

        fk.getFunktioargumentit().addAll(luoFunktioargumentit(args));
        return fk;
    }

    public static Funktiokutsu luoValintaperusteyhtasuuruus(ValintaperusteViite vp1, ValintaperusteViite vp2) {
        Funktiokutsu fk = new Funktiokutsu();
        fk.setFunktionimi(Funktionimi.VALINTAPERUSTEYHTASUURUUS);

        vp1.setIndeksi(1);
        vp2.setIndeksi(2);
        fk.getValintaperusteviitteet().add(vp1);
        fk.getValintaperusteviitteet().add(vp2);
        return fk;
    }
}
