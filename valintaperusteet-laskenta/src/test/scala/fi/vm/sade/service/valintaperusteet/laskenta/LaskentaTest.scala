package fi.vm.sade.service.valintaperusteet.laskenta

import java.math.{BigDecimal => BigDec}
import scala.math.BigDecimal._

import fi.vm.sade.service.valintaperusteet.laskenta.api.{Hakukohde, Osallistuminen}
import org.scalatest._
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta._
import scala.collection.JavaConversions._
import fi.vm.sade.kaava.LaskentaTestUtil._
import fi.vm.sade.service.valintaperusteet.laskenta.api.tila.HylattyMetatieto
import scala._
import fi.vm.sade.service.valintaperusteet.laskenta.api.tila.VirheMetatieto.VirheMetatietotyyppi
import fi.vm.sade.service.valintaperusteet.laskenta.api.tila.HylattyMetatieto.Hylattymetatietotyyppi
import java.util
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.KonvertoiLukuarvo
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Totuusarvo
import fi.vm.sade.service.valintaperusteet.laskenta.api.Hakemus
import fi.vm.sade.kaava.LaskentaTestUtil.Hakemus
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Arvokonvertteri
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Suurempi
import scala.Some
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Arvokonversio
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Jos
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Demografia
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.HakukohteenValintaperuste
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.HaeMerkkijonoJaKonvertoiLukuarvoksi
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Lukuarvo
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Lukuarvovalikonversio
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.HakemuksenValintaperuste
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.HaeLukuarvo
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Skaalaus
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.SyotettavaValintaperuste
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Lukuarvovalikonvertteri
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.HaeTotuusarvo
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.HaeMerkkijonoJaVertaaYhtasuuruus

/**
 *
 * User: bleed
 * Date: 1/12/13
 * Time: 6:48 PM
 */
class LaskentaTest extends FunSuite {

  val hakukohde = new Hakukohde("123", new util.HashMap[String, String])
  val tyhjaHakemus = Hakemus("", Nil, Map[String, String]())


  test("Lukuarvo function returns its value") {
    val (tulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, Lukuarvo(BigDecimal("5.0")))
    assert(BigDecimal(tulos.get) == BigDecimal("5.0"))
  }

  test("Summa function sums two function values") {
    val (tulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, Summa(Lukuarvo(BigDecimal("5.0")), Lukuarvo(BigDecimal("6.0"))))
    assert(BigDecimal(tulos.get) == BigDecimal("11.0"))
  }

  test("KonvertoiLukuarvolukuarvoksi") {
    val arvokonvertteri = Arvokonvertteri[BigDecimal, BigDecimal](List(Arvokonversio(1, 10, false),
      Arvokonversio(BigDecimal("2.0"), BigDecimal("20.0"), false), Arvokonversio(3, 30, false)))
    val konvertoiLukuarvoLukuarvoksi = KonvertoiLukuarvo(arvokonvertteri, Lukuarvo(2))

    val (tulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, konvertoiLukuarvoLukuarvoksi)
    assert(BigDecimal(tulos.get) == BigDecimal("20.0"))
  }

  test("KonvertoiLukuarvovaliLukuarvoksi") {
    val konv = Lukuarvovalikonvertteri(List(Lukuarvovalikonversio(1, 10, 1, false, false),
      Lukuarvovalikonversio(10, 20, 2, false, false),
      Lukuarvovalikonversio(20, 30, 3, false, false)))
    val l = KonvertoiLukuarvo(konv, Lukuarvo(15))
    val (tulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, l)
    assert(BigDecimal(tulos.get) == BigDecimal(2.0))
  }

  test("SummaNParasta selects the n greatest values and calculates their sum") {
    val luvut = List(Lukuarvo(5.0), Lukuarvo(6.0), Lukuarvo(2.0), Lukuarvo(1.0))
    val (tulos, tila) = Laskin.laske(hakukohde, tyhjaHakemus, SummaNParasta(3, luvut: _*))
    assert(BigDecimal(tulos.get) == BigDecimal(13.0))
  }

  test("NMinimi returns the nth lowest value") {
    val luvut = List(Lukuarvo(5.0), Lukuarvo(6.0), Lukuarvo(2.0), Lukuarvo(1.0))
    val (tulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, NMinimi(2, luvut: _*))
    assert(BigDecimal(tulos.get) == BigDecimal(2.0))
  }

  test("NMaksimi returns the nth greatest value") {
    val luvut = List(Lukuarvo(5.0), Lukuarvo(6.0), Lukuarvo(2.0), Lukuarvo(1.0))
    val (tulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, NMaksimi(2, luvut: _*))
    assert(BigDecimal(tulos.get) == BigDecimal(5.0))
  }

  test("Mediaani returns the middle value of a sequence") {

    val luvut = List(Lukuarvo(5.0), Lukuarvo(6.0), Lukuarvo(2.0), Lukuarvo(1.0))
    val (tulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, Mediaani(luvut))
    // Parillisesta listasta lasketaan keskimmäisten lukujen keskiarvo
    assert(BigDecimal(tulos.get) == BigDecimal(3.5))

    val luvut2 = Lukuarvo(10.0) :: luvut
    // Parittomasta listasta palautetaan keskimmäinen luku

    val (tulos2, _) = Laskin.laske(hakukohde, tyhjaHakemus, Mediaani(luvut2))
    assert(BigDecimal(tulos2.get) == BigDecimal(5.0))
  }

  test("Jos") {
    val ehto = Suurempi(Lukuarvo(5.0), Lukuarvo(10.0))
    val ifHaara = Lukuarvo(100.0)
    val elseHaara = Lukuarvo(500.0)

    val ifLause = Jos(ehto, ifHaara, elseHaara)

    val (tulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, ifLause)
    assert(BigDecimal(tulos.get) == BigDecimal(500.0))

  }

  test("Totuusarvo") {
    val arvo = Totuusarvo(true)
    val (tulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, arvo)
    assert(tulos.get)

    val arvo2 = Totuusarvo(false)
    val (tulos2, _) = Laskin.laske(hakukohde, tyhjaHakemus, arvo2)
    assert(!tulos2.get)
  }

  test("HaeLukuarvo") {
    val hakemus = Hakemus("", Nil, Map("paattotodistus_ka" -> "8.7"))

    val (tulos, _) = Laskin.laske(hakukohde, hakemus,
      HaeLukuarvo(None, None, HakemuksenValintaperuste("paattotodistus_ka", false)))
    assert(BigDecimal(tulos.get) == BigDecimal("8.7"))
  }

  test("HaeTotuusarvo") {
    val hakemus = Hakemus("", Nil, Map("onYlioppilas" -> "true"))

    val (tulos, _) = Laskin.laske(hakukohde, hakemus,
      HaeTotuusarvo(None, None, HakemuksenValintaperuste("onYlioppilas", false)))
    assert(tulos.get)
  }

  test("HaeMerkkijonoJaKonvertoiLukuarvoksi") {
    val hakemus = Hakemus("", Nil, Map("AI_yo" -> "L"))

    val konv = Arvokonvertteri[String, BigDecimal](List(Arvokonversio("puuppa", BigDecimal("20.0"), false),
      Arvokonversio("L", BigDecimal(10.0), false)))
    val f = HaeMerkkijonoJaKonvertoiLukuarvoksi(konv, None, HakemuksenValintaperuste("AI_yo", false))
    val (tulos, tila) = Laskin.laske(hakukohde, hakemus, f)
    assert(BigDecimal(tulos.get) == BigDecimal("10.0"))
  }

  test("Demografia") {
    val hakemukset = List(
      Hakemus("hakemusoid1",
        List("hakutoiveoid1", "hakutoiveoid2", "hakutoiveoid3"),
        Map("SUKUpuoli" -> "mies")),
      Hakemus("hakemusoid2",
        List("hakutoiveoid1", "hakutoiveoid2", "hakutoiveoid3"),
        Map("SUKUPUOLI" -> "mies")),
      Hakemus("hakemusoid3",
        List("hakutoiveoid1", "hakutoiveoid2", "hakutoiveoid3"),
        Map("Sukupuoli" -> "mies")),
      Hakemus("hakemusoid4",
        List("hakutoiveoid1", "hakutoiveoid2", "hakutoiveoid3"),
        Map("sukupuoli" -> "nainen")),
      Hakemus("hakemusoid5",
        List("hakutoiveoid1", "hakutoiveoid2", "hakutoiveoid3"),
        Map()))

    val hakukohde = new Hakukohde("hakutoiveoid1", new util.HashMap[String, String])

    val funktio = Demografia("funktioid1", "sukupuoli", BigDecimal("33.0"))

    val tulokset = hakemukset.map(h => Laskin.suoritaValintalaskenta(hakukohde, h, hakemukset, funktio))

    assert(!tulokset(0).getTulos)
    assert(!tulokset(1).getTulos)
    assert(!tulokset(2).getTulos)
    assert(tulokset(3).getTulos)
    assert(tulokset(4).getTulos)
  }

  test("Konvertoilukuarvovalilukuarvoksi aarirajat") {

    def luoFunktio(f: Lukuarvo): KonvertoiLukuarvo = {
      KonvertoiLukuarvo(
        f = f,
        konvertteri = Lukuarvovalikonvertteri(
          konversioMap = List(
            Lukuarvovalikonversio(
              min = BigDecimal("0.0"),
              max = BigDecimal("5.0"),
              palautaHaettuArvo = false,
              paluuarvo = BigDecimal("1.0"),
              hylkaysperuste = false),
            Lukuarvovalikonversio(
              min = BigDecimal("5.0"),
              max = BigDecimal("8.0"),
              palautaHaettuArvo = false,
              paluuarvo = BigDecimal("2.0"),
              hylkaysperuste = false),
            Lukuarvovalikonversio(
              min = BigDecimal("8.0"),
              max = BigDecimal("10.0"),
              palautaHaettuArvo = false,
              paluuarvo = BigDecimal("3.0"),
              hylkaysperuste = false))))
    }

    val konvertoiNolla = luoFunktio(Lukuarvo(BigDecimal("0.0")))
    val konvertoiViisi = luoFunktio(Lukuarvo(BigDecimal("5.0")))
    val konvertoiKahdeksan = luoFunktio(Lukuarvo(BigDecimal("8.0")))
    val konvertoiKymmenen = luoFunktio(Lukuarvo(BigDecimal("10.0")))

    val (nollaTulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, konvertoiNolla)
    val (viisiTulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, konvertoiViisi)
    val (kahdeksanTulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, konvertoiKahdeksan)
    val (kymmenenTulos, _) = Laskin.laske(hakukohde, tyhjaHakemus, konvertoiKymmenen)

    assert(BigDecimal(nollaTulos.get) == BigDecimal("1.0"))
    assert(BigDecimal(viisiTulos.get) == BigDecimal("2.0"))
    assert(BigDecimal(kahdeksanTulos.get) == BigDecimal("3.0"))
    assert(BigDecimal(kymmenenTulos.get) == BigDecimal("3.0"))
  }

  test("Konvertoilukuarvovali, ei maaritetty") {
    val f = KonvertoiLukuarvo(
      f = Lukuarvo(BigDecimal("11.0")),
      konvertteri = Lukuarvovalikonvertteri(
        konversioMap = List(
          Lukuarvovalikonversio(
            min = BigDecimal("0.0"),
            max = BigDecimal("5.0"),
            palautaHaettuArvo = false,
            paluuarvo = BigDecimal("1.0"),
            hylkaysperuste = false),
          Lukuarvovalikonversio(
            min = BigDecimal("5.0"),
            max = BigDecimal("8.0"),
            palautaHaettuArvo = false,
            paluuarvo = BigDecimal("2.0"),
            hylkaysperuste = false),
          Lukuarvovalikonversio(
            min = BigDecimal("8.0"),
            max = BigDecimal("10.0"),
            palautaHaettuArvo = false,
            paluuarvo = BigDecimal("3.0"),
            hylkaysperuste = false))))

    val (tulos, tila) = Laskin.laske(hakukohde, tyhjaHakemus, f)
    assertTulosTyhja(tulos)
    assertTilaVirhe(tila, VirheMetatietotyyppi.ARVOVALIKONVERTOINTI_VIRHE)
  }

  test("Syotettava valintaperuste, osallistumistieto puuttuu") {
    val hakemus = Hakemus("", Nil, Map("valintakoe" -> "8.7"))

    val (tulos, tila) = Laskin.laske(hakukohde, hakemus,
      HaeLukuarvo(None, None, SyotettavaValintaperuste("valintakoe", true, "valintakoe-OSALLISTUMINEN")))

    assertTulosTyhja(tulos)
    assertTilaVirhe(tila, VirheMetatietotyyppi.SYOTETTAVA_ARVO_MERKITSEMATTA)
  }

  test("Syotettava valintaperuste, osallistumistieto EI_OSALLISTUNUT") {
    val hakemus = Hakemus("", Nil, Map("valintakoe" -> "8.7",
      "valintakoe-OSALLISTUMINEN" -> Osallistuminen.EI_OSALLISTUNUT.name))

    val (tulos, tila) = Laskin.laske(hakukohde, hakemus,
      HaeLukuarvo(None, None, SyotettavaValintaperuste("valintakoe", true, "valintakoe-OSALLISTUMINEN")))

    assertTulosTyhja(tulos)
    assertTilaHylatty(tila, HylattyMetatieto.Hylattymetatietotyyppi.EI_OSALLISTUNUT_HYLKAYS)
  }

  test("Syotettava valintaperuste, osallistumistieto OSALLISTUI") {
    val hakemus = Hakemus("", Nil, Map("valintakoe" -> "8.7",
      "valintakoe-OSALLISTUMINEN" -> Osallistuminen.OSALLISTUI.name))

    val (tulos, tila) = Laskin.laske(hakukohde, hakemus,
      HaeLukuarvo(None, None, SyotettavaValintaperuste("valintakoe", true, "valintakoe-OSALLISTUMINEN")))

    assert(BigDecimal(tulos.get) == BigDecimal("8.7"))
    assertTilaHyvaksyttavissa(tila)
  }

  test("Syotettava valintaperuste, osallistumistietoa ei voida tulkita") {
    val hakemus = Hakemus("", Nil, Map("valintakoe" -> "8.7",
      "valintakoe-OSALLISTUMINEN" -> "ehkaosallistui"))

    val (tulos, tila) = Laskin.laske(hakukohde, hakemus,
      HaeLukuarvo(None, None, SyotettavaValintaperuste("valintakoe", true, "valintakoe-OSALLISTUMINEN")))

    assertTulosTyhja(tulos)
    assertTilaVirhe(tila, VirheMetatietotyyppi.OSALLISTUSMISTIETOA_EI_VOIDA_TULKITA)
  }

  test("Virhetila on ensisijainen muihin nahden") {
    val hylkaavaHaeLukuarvo = new HaeLukuarvo(
      konvertteri = None,
      oletusarvo = Some(BigDecimal("10.0")),
      valintaperusteviite = HakemuksenValintaperuste(
        tunniste = "hylkaavaLukuarvo",
        pakollinen = true))

    val hyvaksyttavissaHaeLukuarvo = new HaeLukuarvo(
      konvertteri = None,
      oletusarvo = None,
      valintaperusteviite = HakemuksenValintaperuste(
        tunniste = "hyvaksyttavissaLukuarvo",
        pakollinen = true))

    val epavalidiLukuarvo = new HaeLukuarvo(
      konvertteri = None,
      oletusarvo = Some(BigDecimal("1000.0")),
      valintaperusteviite = HakemuksenValintaperuste(
        tunniste = "epavalidiLukuarvo",
        pakollinen = true))

    val hakemus = Hakemus("", Nil,
      Map(
        "hyvaksyttavissaLukuarvo" -> "100.0",
        "epavalidiLukuarvo" -> "satatuhatta"))

    val (tulos1, tila1) = Laskin.laske(hakukohde, hakemus, hylkaavaHaeLukuarvo)
    assert(BigDecimal(tulos1.get) == BigDecimal("10.0"))
    assertTilaHylatty(tila1, Hylattymetatietotyyppi.PAKOLLINEN_VALINTAPERUSTE_HYLKAYS)

    val (tulos2, tila2) = Laskin.laske(hakukohde, hakemus, hyvaksyttavissaHaeLukuarvo)
    assert(BigDecimal(tulos2.get) == BigDecimal("100.0"))
    assertTilaHyvaksyttavissa(tila2)

    val (tulos3, tila3) = Laskin.laske(hakukohde, hakemus, epavalidiLukuarvo)
    assertTulosTyhja(tulos3)
    assertTilaVirhe(tila3, VirheMetatietotyyppi.VALINTAPERUSTETTA_EI_VOIDA_TULKITA_LUKUARVOKSI)

    val summa = Summa(hylkaavaHaeLukuarvo, hyvaksyttavissaHaeLukuarvo, epavalidiLukuarvo)
    val (summatulos, summatila) = Laskin.laske(hakukohde, hakemus, summa)
    assert(BigDecimal(summatulos.get) == BigDecimal("110.0"))
    assertTilaVirhe(summatila, VirheMetatietotyyppi.VALINTAPERUSTETTA_EI_VOIDA_TULKITA_LUKUARVOKSI)
  }

  test("hae hakukohteen valintaperuste") {
    val tunniste = "hakukohteenvalintaperustetunniste"

    val funktio = HaeLukuarvo(
      konvertteri = None,
      oletusarvo = None,
      valintaperusteviite = HakukohteenValintaperuste(tunniste, false, false))

    val hakukohde = new Hakukohde("hakukohdeoid", Map(tunniste -> "100.0"))

    val (tulos, tila) = Laskin.laske(hakukohde, tyhjaHakemus, funktio)
    assert(BigDecimal(tulos.get) == BigDecimal("100.0"))
    assertTilaHyvaksyttavissa(tila)
  }

  test("hae hakukohteen valintaperuste, pakollinen, tunnistetta ei loydy") {
    val tunniste = "hakukohteenvalintaperustetunniste"

    val funktio = HaeLukuarvo(
      konvertteri = None,
      oletusarvo = None,
      valintaperusteviite = HakukohteenValintaperuste(tunniste, true, false))

    val hakukohde = new Hakukohde("hakukohdeoid", Map("toinen tunniste" -> "100.0"))

    val (tulos, tila) = Laskin.laske(hakukohde, tyhjaHakemus, funktio)
    assertTulosTyhja(tulos)
    assertTilaVirhe(tila, VirheMetatietotyyppi.HAKUKOHTEEN_VALINTAPERUSTE_MAARITTELEMATTA_VIRHE)
  }

  test("hae hakukohteen valintaperuste, ei pakollinen, tunnistetta ei loydy") {
    val tunniste = "hakukohteenvalintaperustetunniste"

    val funktio = HaeLukuarvo(
      konvertteri = None,
      oletusarvo = None,
      valintaperusteviite = HakukohteenValintaperuste(tunniste, false, false))

    val hakukohde = new Hakukohde("hakukohdeoid", Map("toinen tunniste" -> "100.0"))

    val (tulos, tila) = Laskin.laske(hakukohde, tyhjaHakemus, funktio)
    assertTulosTyhja(tulos)
    assertTilaHyvaksyttavissa(tila)
  }

  test("syotettavat arvot") {
    val funktio = Summa(
      HaeLukuarvo(
        konvertteri = None,
        oletusarvo = Some(BigDecimal("5.0")),
        valintaperusteviite = new SyotettavaValintaperuste(
          tunniste = "tunniste1",
          pakollinen = false,
          osallistuminenTunniste = "tunniste1-OSALLISTUMINEN"
        )
      ),
      Summa(
        HaeMerkkijonoJaKonvertoiLukuarvoksi(
          konvertteri = Arvokonvertteri[String, BigDecimal](
            konversioMap = List(Arvokonversio[String, BigDecimal](arvo = "konvertoitava1", paluuarvo = BigDecimal("20.0"), hylkaysperuste = false))
          ),
          oletusarvo = None,
          valintaperusteviite = SyotettavaValintaperuste(
            tunniste = "tunniste2",
            pakollinen = false,
            osallistuminenTunniste = "tunniste2-OSALLISTUMINEN"
          )),
        HaeLukuarvo(
          konvertteri = None,
          oletusarvo = None,
          valintaperusteviite = HakemuksenValintaperuste(
            tunniste = "tunniste3",
            pakollinen = false
          )
        ),
        Jos(
          ehto = HaeMerkkijonoJaVertaaYhtasuuruus(
            oletusarvo = None,
            valintaperusteviite = SyotettavaValintaperuste(
              tunniste = "tunniste4",
              pakollinen = true,
              osallistuminenTunniste = "tunniste4-OSALLISTUMINEN"
            ), vertailtava = "vertailtava4"
          ),
          ifHaara = Lukuarvo(BigDecimal("100.0")),
          elseHaara = Lukuarvo(BigDecimal("200.0"))
        )
      ),
      HaeLukuarvo(
        konvertteri = None,
        oletusarvo = Some(BigDecimal("1.0")),
        valintaperusteviite = new SyotettavaValintaperuste(
          tunniste = "tunniste5",
          pakollinen = false,
          osallistuminenTunniste = "tunniste5-OSALLISTUMINEN"
        )
      ),
      HaeLukuarvo(
        konvertteri = None,
        oletusarvo = Some(BigDecimal("3.0")),
        valintaperusteviite = new SyotettavaValintaperuste(
          tunniste = "tunniste6",
          pakollinen = true,
          osallistuminenTunniste = "tunniste6-OSALLISTUMINEN"
        )
      )
    )

    val hakemus = Hakemus("hakemusOid", List[String](), Map("tunniste1-OSALLISTUMINEN" -> Osallistuminen.OSALLISTUI.name(),
      "tunniste2" -> "konvertoitava1", "tunniste2-OSALLISTUMINEN" -> Osallistuminen.OSALLISTUI.name(),
      "tunniste3" -> "50.0", "tunniste4" -> "vertailtava-ei-sama", "tunniste4-OSALLISTUMINEN" -> Osallistuminen.OSALLISTUI.name(),
      "tunniste5-OSALLISTUMINEN" -> Osallistuminen.EI_OSALLISTUNUT.name()))

    val tulos = Laskin.suoritaValintalaskenta(new Hakukohde("hakukohdeOid", Map[String, String]()), hakemus, List[Hakemus](), funktio)
    assert(tulos.getTulos.equals(BigDecimal("276.0").underlying()))
    assertTilaVirhe(tulos.getTila, VirheMetatietotyyppi.SYOTETTAVA_ARVO_MERKITSEMATTA)
    assert(tulos.getSyotetytArvot.size == 5)

    assert(tulos.getSyotetytArvot.contains("tunniste1"))
    val arvo1 = tulos.getSyotetytArvot.get("tunniste1")
    assert(Option(arvo1.getArvo).isEmpty)
    assert(BigDecimal(arvo1.getLaskennallinenArvo).equals(BigDecimal("5.0")))
    assert(arvo1.getOsallistuminen.equals(Osallistuminen.OSALLISTUI))

    assert(tulos.getSyotetytArvot.contains("tunniste2"))
    val arvo2 = tulos.getSyotetytArvot.get("tunniste2")
    assert(arvo2.getArvo.equals("konvertoitava1"))
    assert(BigDecimal(arvo2.getLaskennallinenArvo).equals(BigDecimal("20.0")))
    assert(arvo2.getOsallistuminen.equals(Osallistuminen.OSALLISTUI))

    assert(tulos.getSyotetytArvot.contains("tunniste4"))
    val arvo4 = tulos.getSyotetytArvot.get("tunniste4")
    assert(arvo4.getArvo.equals("vertailtava-ei-sama"))
    assert(!arvo4.getLaskennallinenArvo.toBoolean)
    assert(arvo4.getOsallistuminen.equals(Osallistuminen.OSALLISTUI))

    assert(tulos.getSyotetytArvot.contains("tunniste5"))
    val arvo5 = tulos.getSyotetytArvot.get("tunniste5")
    assert(Option(arvo5.getArvo).isEmpty)
    assert(BigDecimal(arvo5.getLaskennallinenArvo).equals(BigDecimal("1.0")))
    assert(arvo5.getOsallistuminen.equals(Osallistuminen.EI_OSALLISTUNUT))

    assert(tulos.getSyotetytArvot.contains("tunniste6"))
    val arvo6 = tulos.getSyotetytArvot.get("tunniste6")
    assert(Option(arvo6.getArvo).isEmpty)
    assert(Option(arvo6.getLaskennallinenArvo).isEmpty)
    assert(arvo6.getOsallistuminen.equals(Osallistuminen.MERKITSEMATTA))
  }


  test("skaalaus, lahdeskaalaa ei annettu") {
    val funktiokutsu = Skaalaus(
      skaalattava = HaeLukuarvo(konvertteri = None,
        oletusarvo = None,
        valintaperusteviite = HakemuksenValintaperuste(tunniste = "tunniste1", pakollinen = true)),
      kohdeskaala = (BigDecimal("-1.0"), BigDecimal("2.0")),
      lahdeskaala = None)


    val hakemukset = List(
      Hakemus("hakemusOid1", List(), Map("tunniste1" -> "0.0")),
      Hakemus("hakemusOid2", List(), Map("tunniste1" -> "1000.0")),
      Hakemus("hakemusOid3", List(), Map("tunniste1" -> "2000.0")),
      Hakemus("hakemusOid4", List(), Map("tunniste1" -> "3000.0"))
    )

    val tulokset = hakemukset.map(h => {
      Laskin.suoritaValintalaskenta(hakukohde, h, hakemukset, funktiokutsu)
    })

    assert(tulokset.size == 4)
    assert(tulokset(0).getTulos.equals(BigDecimal("-1.0").underlying))
    assertTilaHyvaksyttavissa(tulokset(0).getTila)

    assert(tulokset(1).getTulos.equals(BigDecimal("0.0").underlying))
    assertTilaHyvaksyttavissa(tulokset(1).getTila)

    assert(tulokset(2).getTulos.equals(BigDecimal("1.0").underlying))
    assertTilaHyvaksyttavissa(tulokset(2).getTila)

    assert(tulokset(3).getTulos.equals(BigDecimal("2.0").underlying))
    assertTilaHyvaksyttavissa(tulokset(3).getTila)
  }

  test("skaalaus, lahdeskaalaa annettu") {
    val funktiokutsu = Skaalaus(
      skaalattava = HaeLukuarvo(konvertteri = None,
        oletusarvo = None,
        valintaperusteviite = HakemuksenValintaperuste(tunniste = "tunniste1", pakollinen = true)),
      kohdeskaala = (BigDecimal("-1.0"), BigDecimal("2.0")),
      lahdeskaala = Some((BigDecimal("-1000.0"), BigDecimal("9000.0"))))


    val hakemukset = List(
      Hakemus("hakemusOid1", List(), Map("tunniste1" -> "0.0")),
      Hakemus("hakemusOid2", List(), Map("tunniste1" -> "1000.0")),
      Hakemus("hakemusOid3", List(), Map("tunniste1" -> "2000.0")),
      Hakemus("hakemusOid4", List(), Map("tunniste1" -> "3000.0"))
    )

    val tulokset = hakemukset.map(h => {
      Laskin.suoritaValintalaskenta(hakukohde, h, hakemukset, funktiokutsu)
    })

    assert(tulokset.size == 4)
    assert(tulokset(0).getTulos.equals(BigDecimal("-0.7").underlying))
    assertTilaHyvaksyttavissa(tulokset(0).getTila)

    assert(tulokset(1).getTulos.equals(BigDecimal("-0.4").underlying))
    assertTilaHyvaksyttavissa(tulokset(1).getTila)

    assert(tulokset(2).getTulos.equals(BigDecimal("-0.1").underlying))
    assertTilaHyvaksyttavissa(tulokset(2).getTila)

    assert(tulokset(3).getTulos.equals(BigDecimal("0.2").underlying))
    assertTilaHyvaksyttavissa(tulokset(3).getTila)
  }

  test("skaalaus, hakemuksia liian vahan") {
    val funktiokutsu = Skaalaus(
      skaalattava = HaeLukuarvo(konvertteri = None,
        oletusarvo = None,
        valintaperusteviite = HakemuksenValintaperuste(tunniste = "tunniste1", pakollinen = true)),
      kohdeskaala = (BigDecimal("-1.0"), BigDecimal("2.0")),
      lahdeskaala = None)


    val hakemukset = List(
      Hakemus("hakemusOid1", List(), Map("tunniste1" -> "0.0"))
    )

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, hakemukset(0), hakemukset, funktiokutsu)
    assert(Option(tulos.getTulos).isEmpty)
    assertTilaVirhe(tulos.getTila, VirheMetatietotyyppi.TULOKSIA_LIIAN_VAHAN_LAHDESKAALAN_MAARITTAMISEEN)
  }

  test("skaalaus, lahdeskaalaa ei voida maarittaa") {
    val funktiokutsu = Skaalaus(
      skaalattava = HaeLukuarvo(konvertteri = None,
        oletusarvo = None,
        valintaperusteviite = HakemuksenValintaperuste(tunniste = "tunniste1", pakollinen = true)),
      kohdeskaala = (BigDecimal("-1.0"), BigDecimal("2.0")),
      lahdeskaala = None)


    val hakemukset = List(
      Hakemus("hakemusOid1", List(), Map("tunniste1" -> "10.0")),
      Hakemus("hakemusOid2", List(), Map("tunniste1" -> "10.0"))
    )

    val tulokset = hakemukset.map(h => Laskin.suoritaValintalaskenta(hakukohde, h, hakemukset, funktiokutsu))
    assert(tulokset.size == 2)
    tulokset.foreach {
      t =>
        assert(Option(t.getTulos).isEmpty)
        assertTilaVirhe(t.getTila, VirheMetatietotyyppi.TULOKSIA_LIIAN_VAHAN_LAHDESKAALAN_MAARITTAMISEEN)
    }
  }

  test("skaalaus, skaalattava arvo ei ole lahdeskaalassa") {
    val funktiokutsu = Skaalaus(
      skaalattava = HaeLukuarvo(konvertteri = None,
        oletusarvo = None,
        valintaperusteviite = HakemuksenValintaperuste(tunniste = "tunniste1", pakollinen = true)),
      kohdeskaala = (BigDecimal("-1.0"), BigDecimal("2.0")),
      lahdeskaala = Some((BigDecimal("-125.0"), BigDecimal("375.0"))))


    val hakemukset = List(
      Hakemus("hakemusOid1", List(), Map("tunniste1" -> "375.1")),
      Hakemus("hakemusOid2", List(), Map("tunniste1" -> "-125.0001"))
    )

    val tulokset = hakemukset.map(h => Laskin.suoritaValintalaskenta(hakukohde, h, hakemukset, funktiokutsu))
    assert(tulokset.size == 2)
    tulokset.foreach {
      t =>
        assert(Option(t.getTulos).isEmpty)
        assertTilaVirhe(t.getTila, VirheMetatietotyyppi.SKAALATTAVA_ARVO_EI_OLE_LAHDESKAALASSA)
    }
  }

  test("skaalaus useampaan kertaan") {
    val funktiokutsu = Skaalaus(
      skaalattava = Summa(
        fs = List(
          Lukuarvo(BigDecimal("100.0")),
          Skaalaus(
            skaalattava = HaeLukuarvo(
              konvertteri = None,
              oletusarvo = None,
              valintaperusteviite = HakemuksenValintaperuste(
                tunniste = "tunniste1",
                pakollinen = true
              )
            ),
            kohdeskaala = (BigDecimal("-50"), BigDecimal("50.0")),
            lahdeskaala = None
          )
        )
      ),
      kohdeskaala = (BigDecimal("4.0"), BigDecimal("10.0")),
      lahdeskaala = None
    )

    val hakemukset = List(
      Hakemus("hakemusOid1", List(), Map("tunniste1" -> "100.0")),
      Hakemus("hakemusOid2", List(), Map("tunniste1" -> "300.0")),
      Hakemus("hakemusOid3", List(), Map("tunniste1" -> "500.0"))
    )

    val tulokset = hakemukset.map(h => Laskin.suoritaValintalaskenta(hakukohde, h, hakemukset, funktiokutsu))
    assert(tulokset.size == 3)

    assert(tulokset(0).getTulos.equals(BigDecimal("4.0").underlying))
    assertTilaHyvaksyttavissa(tulokset(0).getTila)

    assert(tulokset(1).getTulos.equals(BigDecimal("7.0").underlying))
    assertTilaHyvaksyttavissa(tulokset(1).getTila)

    assert(tulokset(2).getTulos.equals(BigDecimal("10.0").underlying))
    assertTilaHyvaksyttavissa(tulokset(2).getTila)
  }

  test("demografia, ei ensisijaisia hakijoita") {
    val funktiokutsu = Demografia(
      tunniste = "tunniste1",
      prosenttiosuus = BigDecimal("33.0")
    )

    val hakemukset = List(
      Hakemus("hakemusoid1",
        List("hakutoiveoid1", "hakutoiveoid2", "hakutoiveoid3"),
        Map("tunniste1" -> "arvo")),
      Hakemus("hakemusoid2",
        List("hakutoiveoid1", "hakutoiveoid2", "hakutoiveoid3"),
        Map("tunniste1" -> "arvo")),
      Hakemus("hakemusoid3",
        List("hakutoiveoid1", "hakutoiveoid2", "hakutoiveoid3"),
        Map("tunniste1" -> "arvo")),
      Hakemus("hakemusoid4",
        List("hakutoiveoid1", "hakutoiveoid2", "hakutoiveoid3"),
        Map("tunniste1" -> "arvo")),
      Hakemus("hakemusoid5",
        List("hakutoiveoid1", "hakutoiveoid2", "hakutoiveoid3"),
        Map()))

    val hakukohde = new Hakukohde("ei-kenellakaan", new util.HashMap[String, String])
    val tulokset = hakemukset.map(h => Laskin.suoritaValintalaskenta(hakukohde, h, hakemukset, funktiokutsu))

    assert(!tulokset(0).getTulos)
    assert(!tulokset(1).getTulos)
    assert(!tulokset(2).getTulos)
    assert(!tulokset(3).getTulos)
    assert(!tulokset(4).getTulos)
  }

  test("painotettu keskiarvo") {
    val funktiokutsu = PainotettuKeskiarvo(
      fs = List(
        Pair(
          Lukuarvo(BigDecimal("1.50")),
          Lukuarvo(BigDecimal("30.0"))
        ),
        Pair(
          Lukuarvo(BigDecimal("0.50")),
          Lukuarvo(BigDecimal("100.0"))
        )
      )
    )

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, tyhjaHakemus, List(), funktiokutsu)
    assert(tulos.getTulos.compareTo(BigDecimal("47.5").underlying) == 0)
    assertTilaHyvaksyttavissa(tulos.getTila)
  }

  test("painotettu keskiarvo, tyhja painotettava") {
    val funktiokutsu = PainotettuKeskiarvo(
      fs = List(
        Pair(
          Lukuarvo(BigDecimal("1.50")),
          Lukuarvo(BigDecimal("30.0"))
        ),
        Pair(
          Lukuarvo(BigDecimal("0.50")),
          HaeLukuarvo(
            konvertteri = None,
            oletusarvo = None,
            valintaperusteviite =
              HakemuksenValintaperuste(
                tunniste = "tunniste1",
                pakollinen = false)
          )
        )
      )
    )

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, tyhjaHakemus, List(), funktiokutsu)
    assert(tulos.getTulos.compareTo(BigDecimal("45.0").underlying) == 0)
    assertTilaHyvaksyttavissa(tulos.getTila)
  }

  test("painotettu keskiarvo, tyhja painotuskerroin") {
    val funktiokutsu = PainotettuKeskiarvo(
      fs = List(
        Pair(
          Lukuarvo(BigDecimal("1.50")),
          Lukuarvo(BigDecimal("30.0"))
        ),
        Pair(
          HaeLukuarvo(
            konvertteri = None,
            oletusarvo = None,
            valintaperusteviite =
              HakemuksenValintaperuste(
                tunniste = "tunniste1",
                pakollinen = false)
          ),
          Lukuarvo(BigDecimal("0.50"))
        )
      )
    )

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, tyhjaHakemus, List(), funktiokutsu)
    assert(tulos.getTulos.compareTo(BigDecimal("45.0").underlying) == 0)
    assertTilaHyvaksyttavissa(tulos.getTila)
  }

  test("painotettu keskiarvo, tyhja painotettava ja tyhja painotuskerroin") {
    val funktiokutsu = PainotettuKeskiarvo(
      fs = List(
        Pair(
          Lukuarvo(BigDecimal("1.50")),
          Lukuarvo(BigDecimal("30.0"))
        ),
        Pair(
          HaeLukuarvo(
            konvertteri = None,
            oletusarvo = None,
            valintaperusteviite =
              HakemuksenValintaperuste(
                tunniste = "tunniste1",
                pakollinen = false)
          ),
          HaeLukuarvo(
            konvertteri = None,
            oletusarvo = None,
            valintaperusteviite =
              HakemuksenValintaperuste(
                tunniste = "tunniste2",
                pakollinen = false)
          )
        )
      )
    )

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, tyhjaHakemus, List(), funktiokutsu)
    assert(tulos.getTulos.compareTo(BigDecimal("45.0").underlying) == 0)
    assertTilaHyvaksyttavissa(tulos.getTila)
  }

  test("painotettu keskiarvo, tyhja paluuarvo") {
    val funktiokutsu = PainotettuKeskiarvo(
      fs = List(
        Pair(
          HaeLukuarvo(
            konvertteri = None,
            oletusarvo = None,
            valintaperusteviite =
              HakemuksenValintaperuste(
                tunniste = "tunniste1",
                pakollinen = false)
          ),
          HaeLukuarvo(
            konvertteri = None,
            oletusarvo = None,
            valintaperusteviite =
              HakemuksenValintaperuste(
                tunniste = "tunniste2",
                pakollinen = false)
          )
        )
      )
    )

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, tyhjaHakemus, List(), funktiokutsu)
    assert(Option(tulos.getTulos).isEmpty)
    assertTilaHyvaksyttavissa(tulos.getTila)
  }

  test("epasuora viittaus hakemuksen arvoon") {
    val funktiokutsu = HaeLukuarvo(
      konvertteri = None,
      oletusarvo = None,
      valintaperusteviite = HakukohteenValintaperuste(
        tunniste = "hakukohteentunniste",
        pakollinen = true,
        epasuoraViittaus = true
      )
    )

    val hakukohde = new Hakukohde("oid1", Map("hakukohteentunniste" -> "hakemuksentunniste"))
    val hakemus = new Hakemus("oid1", Map[java.lang.Integer, String](), Map("hakemuksentunniste" -> "100.0"))

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, hakemus, List(hakemus), funktiokutsu)
    assert(tulos.getTulos.compareTo(BigDecimal("100.0").underlying) == 0)
    assertTilaHyvaksyttavissa(tulos.getTila)
  }

  test("epasuora viittaus hakemuksen arvoon, oletusarvo") {
    val funktiokutsu = HaeLukuarvo(
      konvertteri = None,
      oletusarvo = Some(BigDecimal("100.0")),
      valintaperusteviite = HakukohteenValintaperuste(
        tunniste = "hakukohteentunniste",
        pakollinen = true,
        epasuoraViittaus = true
      )
    )

    val hakukohde = new Hakukohde("oid1", Map("hakukohteentunniste" -> "hakemuksentunniste"))
    val hakemus = new Hakemus("oid1", Map[java.lang.Integer, String](), Map[String, String]())

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, hakemus, List(hakemus), funktiokutsu)
    assert(tulos.getTulos.compareTo(BigDecimal("100.0").underlying) == 0)
    assertTilaHylatty(tulos.getTila, Hylattymetatietotyyppi.PAKOLLINEN_VALINTAPERUSTE_HYLKAYS)
  }

  test("epasuora viittaus hakemuksen arvoon, oletusarvo, hakukohteen arvo puuttuu") {
    val funktiokutsu = HaeLukuarvo(
      konvertteri = None,
      oletusarvo = Some(BigDecimal("100.0")),
      valintaperusteviite = HakukohteenValintaperuste(
        tunniste = "hakukohteentunniste",
        pakollinen = true,
        epasuoraViittaus = true
      )
    )

    val hakukohde = new Hakukohde("oid1", Map[String, String]())
    val hakemus = new Hakemus("oid1", Map[java.lang.Integer, String](), Map("hakemuksentunniste" -> "500.0"))

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, hakemus, List(hakemus), funktiokutsu)
    assert(tulos.getTulos.compareTo(BigDecimal("100.0").underlying) == 0)
    assertTilaVirhe(tulos.getTila, VirheMetatietotyyppi.HAKUKOHTEEN_VALINTAPERUSTE_MAARITTELEMATTA_VIRHE)
  }

  test("epasuora viittaus hakemuksen arvoon, ei oletusarvoa, ei pakollinen") {
    val funktiokutsu = HaeLukuarvo(
      konvertteri = None,
      oletusarvo = None,
      valintaperusteviite = HakukohteenValintaperuste(
        tunniste = "hakukohteentunniste",
        pakollinen = false,
        epasuoraViittaus = true
      )
    )

    val hakukohde = new Hakukohde("oid1", Map("hakukohteentunniste" -> "hakemuksentunniste"))
    val hakemus = new Hakemus("oid1", Map[java.lang.Integer, String](), Map[String, String]())

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, hakemus, List(hakemus), funktiokutsu)
    assert(Option(tulos.getTulos).isEmpty)
    assertTilaHyvaksyttavissa(tulos.getTila)
  }

  test("valintaperusteyhtasuuruus palauttaa true jos arvot ovat yhtasuuret") {
    val funktiokutsu = Valintaperusteyhtasuuruus(
      oid = "",
      valintaperusteet = Pair(
        HakukohteenValintaperuste(
          tunniste = "hakukohteentunniste",
          pakollinen = true,
          epasuoraViittaus = false
        ),
        HakemuksenValintaperuste(
          tunniste = "hakemuksentunniste",
          pakollinen = true))
    )

    val hakukohde = new Hakukohde("oid1", Map("hakukohteentunniste" -> "arvo"))
    val hakemus = new Hakemus("oid1", Map[java.lang.Integer, String](), Map("hakemuksentunniste" -> "arvo"))

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, hakemus, List(hakemus), funktiokutsu)
    assert(tulos.getTulos)
    assertTilaHyvaksyttavissa(tulos.getTila)
  }

  test("valintaperusteyhtasuuruus palauttaa false jos arvot ovat erisuuret") {
    val funktiokutsu = Valintaperusteyhtasuuruus(
      oid = "",
      valintaperusteet = Pair(
        HakukohteenValintaperuste(
          tunniste = "hakukohteentunniste",
          pakollinen = true,
          epasuoraViittaus = false
        ),
        HakemuksenValintaperuste(
          tunniste = "hakemuksentunniste",
          pakollinen = true))
    )

    val hakukohde = new Hakukohde("oid1", Map("hakukohteentunniste" -> "hakukohteenarvo"))
    val hakemus = new Hakemus("oid1", Map[java.lang.Integer, String](), Map("hakemuksentunniste" -> "hakemuksenarvo"))

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, hakemus, List(hakemus), funktiokutsu)
    assert(!tulos.getTulos)
    assertTilaHyvaksyttavissa(tulos.getTila)
  }

  test("valintaperusteyhtasuuruus, molemmat arvot tyhjia") {
    val funktiokutsu = Valintaperusteyhtasuuruus(
      oid = "",
      valintaperusteet = Pair(
        HakukohteenValintaperuste(
          tunniste = "hakukohteentunniste",
          pakollinen = false,
          epasuoraViittaus = false
        ),
        HakemuksenValintaperuste(
          tunniste = "hakemuksentunniste",
          pakollinen = false))
    )

    val hakukohde = new Hakukohde("oid1", Map[String, String]())
    val hakemus = new Hakemus("oid1", Map[java.lang.Integer, String](), Map[String, String]())

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, hakemus, List(hakemus), funktiokutsu)
    assert(tulos.getTulos)
    assertTilaHyvaksyttavissa(tulos.getTila)
  }

  test("valintaperusteyhtasuuruus, molemmat arvot tyhjia, toinen pakollinen") {
    val funktiokutsu = Valintaperusteyhtasuuruus(
      oid = "",
      valintaperusteet = Pair(
        HakukohteenValintaperuste(
          tunniste = "hakukohteentunniste",
          pakollinen = false,
          epasuoraViittaus = false
        ),
        HakemuksenValintaperuste(
          tunniste = "hakemuksentunniste",
          pakollinen = true))
    )

    val hakukohde = new Hakukohde("oid1", Map[String, String]())
    val hakemus = new Hakemus("oid1", Map[java.lang.Integer, String](), Map[String, String]())

    val tulos = Laskin.suoritaValintalaskenta(hakukohde, hakemus, List(hakemus), funktiokutsu)
    assert(tulos.getTulos)
    assertTilaHylatty(tulos.getTila, Hylattymetatietotyyppi.PAKOLLINEN_VALINTAPERUSTE_HYLKAYS)
  }
}
