package fi.vm.sade.service.valintaperusteet.laskenta

import java.lang.{Boolean => JBoolean}
import java.math.{RoundingMode, BigDecimal => JBigDecimal}
import java.util.{Collection => JCollection}

import fi.vm.sade.service.valintaperusteet.dto.model.Osallistuminen
import fi.vm.sade.service.valintaperusteet.laskenta.JsonFormats._
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.{Demografia, Ei, HaeLukuarvo, HaeMerkkijonoJaKonvertoiLukuarvoksi, HaeMerkkijonoJaKonvertoiTotuusarvoksi, HaeMerkkijonoJaVertaaYhtasuuruus, HaeTotuusarvo, HakukohteenValintaperuste, Hakutoive, Jos, KonvertoiLukuarvo, Lukuarvo, Negaatio, NimettyLukuarvo, NimettyTotuusarvo, Osamaara, Pienempi, PienempiTaiYhtasuuri, Pyoristys, Suurempi, SuurempiTaiYhtasuuri, SyotettavaValintaperuste, Totuusarvo, Yhtasuuri, _}
import fi.vm.sade.service.valintaperusteet.laskenta.api.Hakemus.Kentat
import fi.vm.sade.service.valintaperusteet.laskenta.api.tila.Tila.Tilatyyppi
import fi.vm.sade.service.valintaperusteet.laskenta.api.tila._
import fi.vm.sade.service.valintaperusteet.laskenta.api.{Hakemus, Hakukohde, Laskentatulos, FunktioTulos => FTulos, SyotettyArvo => SArvo}
import org.slf4j.LoggerFactory
import play.api.libs.json._

import scala.collection.JavaConversions
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

private case class Tulos[T](tulos: Option[T], tila: Tila, historia: Historia)

private case class SyotettyArvo(tunniste: String,
                                arvo: Option[String],
                                laskennallinenArvo: Option[String],
                                osallistuminen: Osallistuminen,
                                syotettavanarvontyyppiKoodiUri: Option[String],
                                tilastoidaan: Boolean
                               )

private case class FunktioTulos(tunniste: String,
                                arvo: String,
                                nimiFi: String,
                                nimiSv: String,
                                nimiEn: String,
                                omaopintopolku: Boolean
                               )

private object Laskentamoodi extends Enumeration {
  type Laskentamoodi = Value

  val VALINTALASKENTA = Value("VALINTALASKENTA")
  val VALINTAKOELASKENTA = Value("VALINTAKOELASKENTA")

}

object Laskin {
  val LOG = LoggerFactory.getLogger(classOf[Laskin])

  private val ZERO: BigDecimal = BigDecimal("0.0")
  private val ONE: BigDecimal = BigDecimal("1.0")
  private val TWO: BigDecimal = BigDecimal("2.0")
  private val HUNDRED: BigDecimal = BigDecimal("100.0")

  private def wrapSyotetytArvot(sa: Map[String, SyotettyArvo]): Map[String, SArvo] = {
    sa.map(e => (e._1 -> new SArvo(e._1, if (e._2.arvo.isEmpty) null else e._2.arvo.get,
      if (e._2.laskennallinenArvo.isEmpty) null else e._2.laskennallinenArvo.get, e._2.osallistuminen,
      if (e._2.syotettavanarvontyyppiKoodiUri.isEmpty) null else e._2.syotettavanarvontyyppiKoodiUri.get, e._2.tilastoidaan)))
  }

  private def wrapFunktioTulokset(sa: Map[String, FunktioTulos]): Map[String, FTulos] = {
    sa.map(e => (e._1 -> new FTulos(e._1, e._2.arvo, e._2.nimiFi, e._2.nimiSv, e._2.nimiEn, e._2.omaopintopolku)))
  }

  protected def suoritaValintalaskentaLukuarvofunktiolla(hakukohde: Hakukohde,
                             hakemus: Hakemus,
                             kaikkiHakemukset: Option[JCollection[Hakemus]],
                             laskettava: Lukuarvofunktio): Laskentatulos[JBigDecimal] = {

    val laskin = if(kaikkiHakemukset.isDefined) new Laskin(hakukohde, hakemus, kaikkiHakemukset.get.toSet) else new Laskin(hakukohde, hakemus)

    laskin.laske(laskettava) match {
      case Tulos(tulos, tila, historia) => {
        new Laskentatulos[JBigDecimal](tila, if (tulos.isEmpty) null else tulos.get.underlying,
          String.valueOf(Json.toJson(wrapHistoria(hakemus, historia))),
          wrapSyotetytArvot(laskin.getSyotetytArvot), wrapFunktioTulokset(laskin.getFunktioTulokset))
      }
    }

  }

  protected def suoritaValintalaskentaTotuusarvofunktiolla(hakukohde: Hakukohde,
                             hakemus: Hakemus,
                             kaikkiHakemukset: Option[JCollection[Hakemus]],
                             laskettava: Totuusarvofunktio): Laskentatulos[JBoolean] = {

    val laskin = if(kaikkiHakemukset.isDefined) new Laskin(hakukohde, hakemus, kaikkiHakemukset.get.toSet) else new Laskin(hakukohde, hakemus)

    laskin.laske(laskettava) match {
      case Tulos(tulos, tila, historia) => {
        new Laskentatulos[JBoolean](tila, if (tulos.isEmpty) null else Boolean.box(tulos.get),
          String.valueOf(Json.toJson(wrapHistoria(hakemus, historia))),
          wrapSyotetytArvot(laskin.getSyotetytArvot), wrapFunktioTulokset(laskin.getFunktioTulokset))
      }
    }
  }

  def suoritaValintalaskenta(hakukohde: Hakukohde,
                             hakemus: Hakemus,
                             kaikkiHakemukset: JCollection[Hakemus],
                             laskettava: Lukuarvofunktio): Laskentatulos[JBigDecimal] = {

    suoritaValintalaskentaLukuarvofunktiolla(hakukohde, hakemus, Some(kaikkiHakemukset), laskettava)
  }

  def suoritaValintalaskenta(hakukohde: Hakukohde,
                             hakemus: Hakemus,
                             kaikkiHakemukset: java.util.Collection[Hakemus],
                             laskettava: Totuusarvofunktio): Laskentatulos[JBoolean] = {

    suoritaValintalaskentaTotuusarvofunktiolla(hakukohde, hakemus, Some(kaikkiHakemukset), laskettava)

  }

  def suoritaValintakoelaskenta(hakukohde: Hakukohde,
                                hakemus: Hakemus,
                                laskettava: Lukuarvofunktio): Laskentatulos[JBigDecimal] = {

    suoritaValintalaskentaLukuarvofunktiolla(hakukohde, hakemus, None, laskettava)
  }

  def suoritaValintakoelaskenta(hakukohde: Hakukohde,
                                hakemus: Hakemus,
                                laskettava: Totuusarvofunktio): Laskentatulos[JBoolean] = {
    suoritaValintalaskentaTotuusarvofunktiolla(hakukohde, hakemus, None, laskettava)
  }

  def laske(hakukohde: Hakukohde, hakemus: Hakemus, laskettava: Totuusarvofunktio): (Option[Boolean], Tila) = {
    val tulos = new Laskin(hakukohde, hakemus).laskeTotuusarvo(laskettava)
    (tulos.tulos, tulos.tila)
  }

  def laske(hakukohde: Hakukohde, hakemus: Hakemus, laskettava: Lukuarvofunktio): (Option[JBigDecimal], Tila) = {

    val tulos = new Laskin(hakukohde, hakemus).laskeLukuarvo(laskettava)
    (tulos.tulos.map(_.underlying()), tulos.tila)
  }

  private def wrapHistoria(hakemus: Hakemus, historia: Historia) = {
    val v: Map[String, Option[Any]] = hakemus.kentat.map(f => (f._1 -> Some(f._2))) ++ hakemus.metatiedot.map(f => (f._1 -> Some(f._2)))

    val name = s"Laskenta hakemukselle (${hakemus.oid})"
    Historia(name, historia.tulos, historia.tilat, Some(List(historia)), Some(v))
  }
}

private class Laskin private(private val hakukohde: Hakukohde,
                             private val hakemus: Hakemus,
                             private val kaikkiHakemukset: Set[Hakemus],
                             private val laskentamoodi: Laskentamoodi.Laskentamoodi) extends LaskinFunktiot {

  def this(hakukohde: Hakukohde, hakemus: Hakemus) {
    this(hakukohde, hakemus, Set(), Laskentamoodi.VALINTAKOELASKENTA)
  }

  def this(hakukohde: Hakukohde, hakemus: Hakemus, kaikkiHakemukset: Set[Hakemus]) {
    this(hakukohde, hakemus, kaikkiHakemukset, Laskentamoodi.VALINTALASKENTA)
  }

  val syotetytArvot: scala.collection.mutable.Map[String, SyotettyArvo] = scala.collection.mutable.Map[String, SyotettyArvo]()
  val funktioTulokset: scala.collection.mutable.Map[String, FunktioTulos] = scala.collection.mutable.Map[String, FunktioTulos]()

  def getSyotetytArvot: Map[String, SyotettyArvo] = Map[String, SyotettyArvo](syotetytArvot.toList: _*)
  def getFunktioTulokset: Map[String, FunktioTulos] = Map[String, FunktioTulos](funktioTulokset.toList: _*)

  def laske(laskettava: Lukuarvofunktio): Tulos[BigDecimal] = {
    laskeLukuarvo(laskettava)
  }


  def laske(laskettava: Totuusarvofunktio): Tulos[Boolean] = {
    laskeTotuusarvo(laskettava)
  }

  private def haeValintaperuste[T](valintaperusteviite: Valintaperuste, kentat: Kentat,
                                   konv: (String => Tuple2[Option[T], List[Tila]]),
                                   oletusarvo: Option[T]): Tuple2[Option[T], List[Tila]] = {
    def haeValintaperusteenArvoHakemukselta(tunniste: String, pakollinen: Boolean) = {
      val (valintaperuste, tila) = haeValintaperuste(tunniste, pakollinen, kentat)

      valintaperuste match {
        case Some(s) => {
          val (konvertoituArvo, tilat) = konv(s)
          (valintaperuste, konvertoituArvo, tilat)
        }
        case None => {
          (valintaperuste, oletusarvo, List(tila))
        }
      }
    }

    // Jos kyseessä on syötettävä valintaperuste, pitää ensin tsekata osallistumistieto
    valintaperusteviite match {
      case SyotettavaValintaperuste(tunniste, pakollinen, osallistuminenTunniste, kuvaus, kuvaukset, vaatiiOsallistumisen, syotettavavissaKaikille, tyypinKoodiUri, tilastoidaan, ammatillisenKielikoeSpecialHandling) => {
        val (osallistuminen, osallistumistila) = hakemus.kentat.get(osallistuminenTunniste) match {
          case Some(osallistuiArvo) => {
            try {
              (Osallistuminen.valueOf(osallistuiArvo), new Hyvaksyttavissatila)
            } catch {
              case e: IllegalArgumentException => (Osallistuminen.MERKITSEMATTA,
                new Virhetila(suomenkielinenHylkaysperusteMap(s"Osallistumistietoa $osallistuiArvo ei pystytty tulkitsemaan (tunniste $osallistuminenTunniste)"),
                  new OsallistumistietoaEiVoidaTulkitaVirhe(osallistuminenTunniste)))
            }
          }
          case None => if(vaatiiOsallistumisen) (Osallistuminen.MERKITSEMATTA, new Hyvaksyttavissatila) else (Osallistuminen.EI_VAADITA, new Hyvaksyttavissatila)
        }

        val checkingAmmatillisenKielikoeOsallistuminenFromHakemus = !hakukohde.korkeakouluhaku && ammatillisenKielikoeSpecialHandling &&
          (osallistuminenTunniste == "kielikoe_fi-OSALLISTUMINEN" || osallistuminenTunniste == "kielikoe_sv-OSALLISTUMINEN")
        val overrideAmmatillisenKielikoeOsallistuminenToShowCorrectOsallistuminenForExistingResultInSure = checkingAmmatillisenKielikoeOsallistuminenFromHakemus && osallistuminen == Osallistuminen.OSALLISTUI

        val (arvo: Option[String], konvertoitu: Option[T], tilat) = if (pakollinen && vaatiiOsallistumisen && Osallistuminen.EI_OSALLISTUNUT == osallistuminen)
          (None, None, List(osallistumistila,
            new Hylattytila(tekstiryhmaToMap(kuvaukset),
              new EiOsallistunutHylkays(tunniste))))
        else if (pakollinen && Osallistuminen.MERKITSEMATTA == osallistuminen)
          (None, None, List(osallistumistila, new Virhetila(suomenkielinenHylkaysperusteMap(s"Pakollisen syötettävän kentän arvo on merkitsemättä (tunniste $tunniste)"),
            new SyotettavaArvoMerkitsemattaVirhe(tunniste))))
        else {
          val (arvo, konvertoitu, tilat) = haeValintaperusteenArvoHakemukselta(tunniste, pakollinen)
          val (osallistumislaskennassaKaytettavaArvo, osallistumislaskennassaKaytettavaLaskennallinenArvo) = if (overrideAmmatillisenKielikoeOsallistuminenToShowCorrectOsallistuminenForExistingResultInSure) {
            (Some("false"), Some(false).asInstanceOf[Some[T]])
          } else {
            (arvo, konvertoitu)
          }
          (osallistumislaskennassaKaytettavaArvo, osallistumislaskennassaKaytettavaLaskennallinenArvo, osallistumistila :: tilat)
        }

        val osallistuminenValueToUse = if (overrideAmmatillisenKielikoeOsallistuminenToShowCorrectOsallistuminenForExistingResultInSure) {
          Osallistuminen.MERKITSEMATTA
        } else {
          osallistuminen
        }

        syotetytArvot(tunniste) = SyotettyArvo(tunniste, arvo, konvertoitu.map(_.toString), osallistuminenValueToUse, tyypinKoodiUri, tilastoidaan)
        (konvertoitu, tilat)
      }
      case HakemuksenValintaperuste(tunniste, pakollinen) => {
        val (_, konvertoitu, tilat) = haeValintaperusteenArvoHakemukselta(tunniste, pakollinen)
        (konvertoitu, tilat)
      }
      case HakukohteenValintaperuste(tunniste, pakollinen, epasuoraViittaus) => {
        hakukohde.valintaperusteet.get(tunniste).filter(!_.trim.isEmpty) match {
          case Some(arvo) => {
            if (epasuoraViittaus) {
              val (_, konvertoitu, tilat) = haeValintaperusteenArvoHakemukselta(arvo, pakollinen)
              (konvertoitu, tilat)
            } else konv(arvo)
          }
          case None => {
            val tila = if (epasuoraViittaus || pakollinen) new Virhetila(suomenkielinenHylkaysperusteMap(s"Hakukohteen valintaperustetta $tunniste ei ole määritelty"),
              new HakukohteenValintaperusteMaarittelemattaVirhe(tunniste))
            else new Hyvaksyttavissatila

            val arvo = if (!oletusarvo.isEmpty) oletusarvo
            else None

            (arvo, List(tila))
          }
        }
      }
      case HakukohteenSyotettavaValintaperuste(tunniste, pakollinen, epasuoraViittaus, osallistumisenTunnistePostfix, kuvaus, kuvaukset, vaatiiOsallistumisen, syotettavissaKaikille, syotettavanarvontyyppiKoodiUri, tilastoidaan) => {
        hakukohde.valintaperusteet.get(tunniste).filter(!_.trim.isEmpty) match {
          case Some(arvo) => {
            if (epasuoraViittaus) {
              val ammatillisenKielikoeOsallistuminenSpecialHandling = !hakukohde.korkeakouluhaku && laskentamoodi == Laskentamoodi.VALINTAKOELASKENTA && tunniste == "kielikoe_tunniste"
              haeValintaperuste(SyotettavaValintaperuste(arvo, pakollinen, s"$arvo$osallistumisenTunnistePostfix", kuvaus, kuvaukset, vaatiiOsallistumisen, syotettavissaKaikille, syotettavanarvontyyppiKoodiUri, tilastoidaan, ammatillisenKielikoeOsallistuminenSpecialHandling),
                hakemus.kentat, konv, oletusarvo)
            } else konv(arvo)
          }
          case None => {
            val tila = if (epasuoraViittaus || pakollinen) new Virhetila(suomenkielinenHylkaysperusteMap(s"Hakukohteen valintaperustetta $tunniste ei ole määritelty"),
              new HakukohteenValintaperusteMaarittelemattaVirhe(tunniste))
            else new Hyvaksyttavissatila

            val arvo = if (!oletusarvo.isEmpty) oletusarvo
            else None

            (arvo, List(tila))
          }
        }
      }
    }
  }

  private def laskeTotuusarvo(laskettava: Totuusarvofunktio): Tulos[Boolean] = {

    def muodostaKoostettuTulos(fs: Seq[Totuusarvofunktio], trans: Seq[Boolean] => Boolean) = {
      val (tulokset, tilat, historiat) = fs.reverse.foldLeft((Nil, Nil, ListBuffer()): Tuple3[List[Option[Boolean]], List[Tila], ListBuffer[Historia]])((lst, f) => {
        laskeTotuusarvo(f) match {
          case Tulos(tulos, tila, historia) => {
            lst._3 += historia
            (tulos :: lst._1, tila :: lst._2, lst._3)
          }
        }
      })

      val (tyhjat, eiTyhjat) = tulokset.partition(_.isEmpty)

      // jos yksikin laskennasta saaduista arvoista on tyhjä, koko funktion laskenta palauttaa tyhjän
      val totuusarvo = if (!tyhjat.isEmpty) None else Some(trans(eiTyhjat.map(_.get)))
      (totuusarvo, tilat, Historia("Koostettu tulos", totuusarvo, tilat, Some(historiat.toList), None))
    }

    def muodostaYksittainenTulos(f: Totuusarvofunktio, trans: Boolean => Boolean) = {
      laskeTotuusarvo(f) match {
        case Tulos(tulos, tila, historia) => (tulos.map(trans(_)), List(tila), historia)
      }
    }

    def muodostaVertailunTulos(f1: Lukuarvofunktio, f2: Lukuarvofunktio,
                               trans: (BigDecimal, BigDecimal) => Boolean) = {
      val tulos1 = laskeLukuarvo(f1)
      val tulos2 = laskeLukuarvo(f2)
      val tulos = for {
        t1 <- tulos1.tulos
        t2 <- tulos2.tulos
      } yield trans(t1, t2)
      val tilat = List(tulos1.tila, tulos2.tila)
      (tulos, tilat, Historia("Vertailuntulos", tulos, tilat, Some(List(tulos1.historia, tulos2.historia)), None))
    }

    val (laskettuTulos, tilat, hist): Tuple3[Option[Boolean], List[Tila], Historia] = laskettava match {
      case Ja(fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, lst => lst.forall(b => b))
        (tulos, tilat, Historia("Ja", tulos, tilat, h.historiat, None))
      }
      case Tai(fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, lst => lst.exists(b => b))
        (tulos, tilat, Historia("Tai", tulos, tilat, h.historiat, None))
      }
      case Ei(fk, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaYksittainenTulos(fk, b => !b)
        (tulos, tilat, Historia("Ei", tulos, tilat, Some(List(h)), None))
      }
      case Totuusarvo(b, oid, tulosTunniste,_,_,_,_) => {
        val tilat = List(new Hyvaksyttavissatila)
        (Some(b), tilat, Historia("Totuusarvo", Some(b), tilat, None, None))
      }
      case Suurempi(f1, f2, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaVertailunTulos(f1, f2, (d1, d2) => d1 > d2)
        (tulos, tilat, Historia("Suurempi", tulos, tilat, Some(List(h)), None))
      }
      case SuurempiTaiYhtasuuri(f1, f2, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaVertailunTulos(f1, f2, (d1, d2) => d1 >= d2)
        (tulos, tilat, Historia("Suurempi tai yhtäsuuri", tulos, tilat, Some(List(h)), None))
      }
      case Pienempi(f1, f2, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaVertailunTulos(f1, f2, (d1, d2) => d1 < d2)
        (tulos, tilat, Historia("Pienempi", tulos, tilat, Some(List(h)), None))
      }
      case PienempiTaiYhtasuuri(f1, f2, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaVertailunTulos(f1, f2, (d1, d2) => d1 <= d2)
        (tulos, tilat, Historia("Pienempi tai yhtäsuuri", tulos, tilat, Some(List(h)), None))
      }
      case Yhtasuuri(f1, f2, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaVertailunTulos(f1, f2, (d1, d2) => d1 == d2)
        (tulos, tilat, Historia("Yhtäsuuri", tulos, tilat, Some(List(h)), None))
      }
      case HaeTotuusarvo(konvertteri, oletusarvo, valintaperusteviite, oid, tulosTunniste,_,_,_,_) => {
        val (konv, virheet) = konvertteri match {
          case Some(a: Arvokonvertteri[_,_]) => konversioToArvokonversio[Boolean, Boolean](a.konversioMap,hakemus.kentat, hakukohde)
          case _ => (konvertteri, List())
        }
        val (tulos, tila) = haeValintaperuste[Boolean](valintaperusteviite, hakemus.kentat,
          (s => suoritaOptionalKonvertointi[Boolean](string2boolean(s, valintaperusteviite.tunniste),
            konv)), oletusarvo)
        (tulos, tila, Historia("Hae totuusarvo", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
      }
      case NimettyTotuusarvo(nimi, f, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaYksittainenTulos(f, b => b)
        (tulos, tilat, Historia("Nimetty totuusarvo", tulos, tilat, Some(List(h)), Some(Map("nimi" -> Some(nimi)))))
      }

      case Hakutoive(n, _, _, _, _, _,_) => {
        val onko = Some(hakemus.onkoHakutoivePrioriteetilla(hakukohde.hakukohdeOid, n))
        val tilat = List(new Hyvaksyttavissatila)
        (onko, tilat, Historia("Hakutoive", onko, tilat, None, Some(Map("prioriteetti" -> Some(n)))))
      }

      case HakutoiveRyhmassa(n, ryhmaOid, _, _, _, _, _,_) => {
        val onko = Some(hakemus.onkoHakutoivePrioriteetilla(hakukohde.hakukohdeOid, n, Some(ryhmaOid)))
        val tilat = List(new Hyvaksyttavissatila)
        (onko, tilat, Historia("HakutoiveRyhmassa", onko, tilat, None, Some(Map("prioriteetti" -> Some(n)))))
      }

      case Hakukelpoisuus(oid, tulosTunniste,_,_,_,_) => {
        val onko = Some(hakemus.onkoHakukelpoinen(hakukohde.hakukohdeOid))
        val tilat = List(new Hyvaksyttavissatila)
        (onko, tilat, Historia("Hakukelpoisuus", onko, tilat, None, Some(Map("hakukohde" -> Some(hakukohde.hakukohdeOid)))))
      }

      case Demografia(oid, tulosTunniste,_,_,_,_, tunniste, prosenttiosuus) => {
        if (laskentamoodi != Laskentamoodi.VALINTALASKENTA) {
          val moodi = laskentamoodi.toString
          moodiVirhe(s"Demografia-funktiota ei voida suorittaa laskentamoodissa $moodi", "Demografia", moodi)
        } else {
          val ensisijaisetHakijat = kaikkiHakemukset.count(_.onkoHakutoivePrioriteetilla(hakukohde.hakukohdeOid, 1))

          val omaArvo = hakemus.kentat.map(e => (e._1.toLowerCase -> e._2)).get(tunniste.toLowerCase)
          val tulos = Some(if (ensisijaisetHakijat == 0) false
          else {
            val samojenArvojenLkm = kaikkiHakemukset.count(h => h.onkoHakutoivePrioriteetilla(hakukohde.hakukohdeOid, 1) &&
              h.kentat.map(e => (e._1.toLowerCase -> e._2)).get(tunniste.toLowerCase) == omaArvo)


            val vertailuarvo = BigDecimal(samojenArvojenLkm).underlying.divide(BigDecimal(ensisijaisetHakijat).underlying, 4, RoundingMode.HALF_UP)
            vertailuarvo.compareTo(prosenttiosuus.underlying.divide(Laskin.HUNDRED.underlying, 4, RoundingMode.HALF_UP)) != 1
          })

          val tila = new Hyvaksyttavissatila
          (tulos, List(tila), Historia("Demografia", tulos, List(tila), None, Some(Map("avain" -> Some(tunniste), "valintaperuste" -> omaArvo))))
        }
      }

      case HaeMerkkijonoJaKonvertoiTotuusarvoksi(konvertteri, oletusarvo, valintaperusteviite, oid, tulosTunniste,_,_,_,_) => {
        konvertteri match {
          case a: Arvokonvertteri[_,_] => {
            val (konv, virheet) = konversioToArvokonversio(a.konversioMap,hakemus.kentat, hakukohde)
            if(konv.isEmpty) {
              (None, virheet, Historia("Hae merkkijono ja konvertoi totuusarvoksi", None, virheet, None, None))
            } else {
              val (tulos, tila) = haeValintaperuste[Boolean](valintaperusteviite, hakemus.kentat,
                s => suoritaKonvertointi[String, Boolean]((Some(s), new Hyvaksyttavissatila), konv.get.asInstanceOf[Arvokonvertteri[String,Boolean]]), oletusarvo)
              (tulos, tila, Historia("Hae merkkijono ja konvertoi totuusarvoksi", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
            }

          }
          case _ => {
            val (tulos, tila) = haeValintaperuste[Boolean](valintaperusteviite, hakemus.kentat,
              s => suoritaKonvertointi[String, Boolean]((Some(s), new Hyvaksyttavissatila), konvertteri), oletusarvo)
            (tulos, tila, Historia("Hae merkkijono ja konvertoi totuusarvoksi", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
          }
        }
      }

      case HaeMerkkijonoJaVertaaYhtasuuruus(oletusarvo, valintaperusteviite, vertailtava, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tila) = haeValintaperuste[Boolean](valintaperusteviite, hakemus.kentat,
          (s => (Some(vertailtava.trim.equalsIgnoreCase(s.trim)), List(new Hyvaksyttavissatila))), oletusarvo)
        (tulos, tila, Historia("Hae merkkijono ja vertaa yhtasuuruus", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
      }

      case Valintaperusteyhtasuuruus(oid, tulosTunniste,_,_,_,_, (valintaperusteviite1, valintaperusteviite2)) => {
        val (arvo1, tilat1) = haeValintaperuste[String](valintaperusteviite1, hakemus.kentat, (s => (Some(s.trim.toLowerCase), List(new Hyvaksyttavissatila))), None)
        val (arvo2, tilat2) = haeValintaperuste[String](valintaperusteviite2, hakemus.kentat, (s => (Some(s.trim.toLowerCase), List(new Hyvaksyttavissatila))), None)

        val tulos = Some(arvo1 == arvo2)
        val tilat = tilat1 ::: tilat2
        (tulos, tilat, Historia("Valintaperusteyhtasuuruus", tulos, tilat, None, Some(Map("tunniste1" -> arvo1, "tunniste2" -> arvo2))))
      }
    }

    if (!laskettava.tulosTunniste.isEmpty) {
      val v = FunktioTulos(
        laskettava.tulosTunniste,
        laskettuTulos.getOrElse("").toString,
        laskettava.tulosTekstiFi,
        laskettava.tulosTekstiSv,
        laskettava.tulosTekstiEn,
        laskettava.omaopintopolku
      )
      funktioTulokset.update(laskettava.tulosTunniste, v)
    }
    Tulos(laskettuTulos, palautettavaTila(tilat), hist)
  }

  private def laskeLukuarvo(laskettava: Lukuarvofunktio): Tulos[BigDecimal] = {

    def summa(vals: Seq[BigDecimal]): BigDecimal = vals.reduceLeft(_ + _)
    def tulo(vals: Seq[BigDecimal]): BigDecimal = vals.reduceLeft(_ * _)

    def muodostaYksittainenTulos(f: Lukuarvofunktio, trans: BigDecimal => BigDecimal): (Option[BigDecimal], List[Tila], Historia) = {
      laskeLukuarvo(f) match {
        case Tulos(tulos, tila, historia) => (tulos.map(trans(_)), List(tila), historia)
      }
    }

    def onArvovalilla(arvo: BigDecimal, arvovali : (BigDecimal, BigDecimal), alarajaMukaan: Boolean, ylarajaMukaan: Boolean) = (alarajaMukaan, ylarajaMukaan) match {
      case (true, true) => if(arvo >= arvovali._1 && arvo <= arvovali._2) true else false
      case (true, false) => if(arvo >= arvovali._1 && arvo < arvovali._2) true else false
      case (false, true) => if(arvo > arvovali._1 && arvo <= arvovali._2) true else false
      case (false, false) => if(arvo > arvovali._1 && arvo < arvovali._2) true else false
    }

    def muodostaKoostettuTulos(fs: Seq[Lukuarvofunktio], trans: Seq[BigDecimal] => BigDecimal, ns: Option[Int] = None): (Option[BigDecimal], List[Tila], Historia) = {
      val tulokset = fs.reverse.foldLeft((Nil, Nil, ListBuffer()): Tuple3[List[BigDecimal], List[Tila], ListBuffer[Historia]])((lst, f) => {
        laskeLukuarvo(f) match {
          case Tulos(tulos, tila, historia) => {
            lst._3 += historia
            (if (!tulos.isEmpty) tulos.get :: lst._1 else lst._1, tila :: lst._2, lst._3)
          }
        }
      })

      val lukuarvo = if (tulokset._1.isEmpty || (ns.isDefined && tulokset._1.size < ns.get)) None else Some(trans(tulokset._1))
      (lukuarvo, tulokset._2, Historia("Koostettu tulos", lukuarvo, tulokset._2, Some(tulokset._3.toList), None))
    }

    def haeLukuarvo(konvertteri: Option[Konvertteri[BigDecimal, BigDecimal]], oletusarvo: Option[BigDecimal], valintaperusteviite: Valintaperuste, kentat: Kentat): (Option[BigDecimal], Seq[Tila], Historia)  = {
      val (konv, virheet) = konvertteri match {
        case Some(l: Lukuarvovalikonvertteri) => konversioToLukuarvovalikonversio(l.konversioMap,kentat, hakukohde)
        case Some(a: Arvokonvertteri[_,_]) => konversioToArvokonversio[BigDecimal, BigDecimal](a.konversioMap,kentat, hakukohde)
        case _ => (konvertteri, List())
      }
      val (tulos, tila) = haeValintaperuste[BigDecimal](valintaperusteviite, kentat,
        (s => suoritaOptionalKonvertointi[BigDecimal](string2bigDecimal(s, valintaperusteviite.tunniste), konv)), oletusarvo)
      (tulos, tila, Historia("Hae Lukuarvo", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))

    }

    def haeMerkkijonoJaKonvertoiLukuarvoksi(konvertteri: Konvertteri[String, BigDecimal], oletusarvo: Option[BigDecimal], valintaperusteviite: Valintaperuste, kentat: Kentat): (Option[BigDecimal], Seq[Tila], Historia)  = {

      konvertteri match {
        case a: Arvokonvertteri[_, _] => {
          val (konv, virheet) = konversioToArvokonversio(a.konversioMap, kentat, hakukohde)
          if (konv.isEmpty) {
            (None, virheet, Historia("Hae merkkijono ja konvertoi lukuarvoksi", None, virheet, None, None))
          } else {
            val (tulos, tila) = haeValintaperuste[BigDecimal](valintaperusteviite, kentat,
              s => suoritaKonvertointi[String, BigDecimal]((Some(s), new Hyvaksyttavissatila), konv.get.asInstanceOf[Arvokonvertteri[String, BigDecimal]]), oletusarvo)

            (tulos, tila, Historia("Hae merkkijono ja konvertoi lukuarvoksi", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
          }
        }
        case _ => {
          val (tulos, tila) = haeValintaperuste[BigDecimal](valintaperusteviite, kentat,
            s => suoritaKonvertointi[String, BigDecimal]((Some(s), new Hyvaksyttavissatila), konvertteri), oletusarvo)
          (tulos, tila, Historia("Hae merkkijono ja konvertoi lukuarvoksi", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
        }
      }
    }

    def filteredSuoritusTiedot(valintaperusteviite: Valintaperuste, ehdot: YoEhdot): List[Kentat] = {
      hakemus.metatiedot.getOrElse(valintaperusteviite.tunniste, List()).filter(kentat =>
          (ehdot.alkuvuosi.isEmpty ||  string2integer(kentat.get("SUORITUSVUOSI"), 9999) >= ehdot.alkuvuosi.get) &&
          (ehdot.loppuvuosi.isEmpty ||  string2integer(kentat.get("SUORITUSVUOSI"), 0) <= ehdot.loppuvuosi.get) &&
          (ehdot.alkulukukausi.isEmpty || (ehdot.alkuvuosi.isDefined &&
             (string2integer(kentat.get("SUORITUSVUOSI"), 0) > ehdot.alkuvuosi.get ||
              string2integer(kentat.get("SUORITUSLUKUKAUSI"), 0) >= ehdot.alkulukukausi.get))) &&
          (ehdot.loppulukukausi.isEmpty || (ehdot.loppuvuosi.isDefined &&
             (string2integer(kentat.get("SUORITUSVUOSI"), 9999) < ehdot.loppuvuosi.get ||
              string2integer(kentat.get("SUORITUSLUKUKAUSI"), 3) <= ehdot.loppulukukausi.get))) &&
          (ehdot.rooli.isEmpty || ehdot.rooli.contains(kentat.getOrElse("ROOLI", "")))
      )
    }

    val (laskettuTulos: Option[BigDecimal], tilat: Seq[Tila], historia: Historia) = laskettava match {
      case Lukuarvo(d, oid, tulosTunniste,_,_,_,_) => {
        val tila = List(new Hyvaksyttavissatila)
        (Some(d), tila, Historia("Lukuarvo", Some(d), tila, None, None))
      }
      case Negaatio(n, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaYksittainenTulos(n, d => -d)
        (tulos, tilat, Historia("Negaaatio", tulos, tilat, Some(List(h)), None))
      }
      case Summa(fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, summa)
        (tulos, tilat, Historia("Summa", tulos, tilat, h.historiat, None))
      }

      case SummaNParasta(n, fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => summa(ds.sortWith(_ > _).take(n)))
        (tulos, tilat, Historia("Summa N-parasta", tulos, tilat, h.historiat, Some(Map("n" -> Some(n)))))
      }

      case TuloNParasta(n, fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => tulo(ds.sortWith(_ > _).take(n)))
        (tulos, tilat, Historia("Tulo N-parasta", tulos, tilat, h.historiat, Some(Map("n" -> Some(n)))))
      }

      case Osamaara(osoittaja, nimittaja, oid, tulosTunniste,_,_,_,_) => {
        val nimittajaTulos = laskeLukuarvo(nimittaja)
        val osoittajaTulos = laskeLukuarvo(osoittaja)

        val (arvo, laskentatilat) = (for {
          n <- nimittajaTulos.tulos
          o <- osoittajaTulos.tulos
        } yield {
          if (n.intValue() == 0) (None, new Virhetila(suomenkielinenHylkaysperusteMap("Jako nollalla"), new JakoNollallaVirhe))
          else {
            (Some(BigDecimal(o.underlying.divide(n.underlying, 4, RoundingMode.HALF_UP))), new Hyvaksyttavissatila)
          }
        }) match {
          case Some((arvo, tila)) => (arvo, List(tila))
          case None => (None, List())
        }

        val tilat = osoittajaTulos.tila :: nimittajaTulos.tila :: laskentatilat
        (arvo, tilat, Historia("Osamäärä", arvo, tilat, Some(List(osoittajaTulos.historia, nimittajaTulos.historia)), None))
      }

      case Tulo(fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => ds.reduceLeft(_ * _))
        (tulos, tilat, Historia("Tulo", tulos, tilat, h.historiat, None))
      }

      case Keskiarvo(fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => BigDecimal(summa(ds).underlying.divide(BigDecimal(ds.size).underlying, 4, RoundingMode.HALF_UP)))
        (tulos, tilat, Historia("Keskiarvo", tulos, tilat, h.historiat, None))
      }

      case KeskiarvoNParasta(n, fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => {
          val kaytettavaN = scala.math.min(n, ds.size)
          BigDecimal(summa(ds.sortWith(_ > _).take(kaytettavaN)).underlying.divide(BigDecimal(kaytettavaN).underlying, 4, RoundingMode.HALF_UP))
        })
        (tulos, tilat, Historia("Keskiarvo N-parasta", tulos, tilat, h.historiat, Some(Map("n" -> Some(n)))))
      }
      case Minimi(fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => ds.min)
        (tulos, tilat, Historia("Minimi", tulos, tilat, h.historiat, None))
      }

      case Maksimi(fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => ds.max)
        (tulos, tilat, Historia("Maksimi", tulos, tilat, h.historiat, None))
      }

      case NMinimi(ns, fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => ds.sortWith(_ < _)(scala.math.min(ns, ds.size) - 1), Some(ns))
        (tulos, tilat, Historia("N-minimi", tulos, tilat, h.historiat, Some(Map("ns" -> Some(ns)))))
      }

      case NMaksimi(ns, fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => ds.sortWith(_ > _)(scala.math.min(ns, ds.size) - 1), Some(ns))
        (tulos, tilat, Historia("N-maksimi", tulos, tilat, h.historiat, Some(Map("ns" -> Some(ns)))))
      }

      case Mediaani(fs, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => {
          val sorted = ds.sortWith(_ < _)
          if (sorted.size % 2 == 1) sorted(sorted.size / 2)
          else BigDecimal((sorted(sorted.size / 2) + sorted(sorted.size / 2 - 1)).underlying.divide(Laskin.TWO.underlying, 4, RoundingMode.HALF_UP))
        })
        (tulos, tilat, Historia("Mediaani", tulos, tilat, h.historiat, None))
      }

      case Jos(ehto, thenHaara, elseHaara, oid, tulosTunniste,_,_,_,_) => {
        val ehtoTulos = laskeTotuusarvo(ehto)
        val thenTulos = laskeLukuarvo(thenHaara)
        val elseTulos = laskeLukuarvo(elseHaara)
        //historiat :+ historia1 :+ historia2
        val (tulos, tilat) = ehdollinenTulos[Boolean, BigDecimal]((ehtoTulos.tulos, ehtoTulos.tila), (cond, tila) => {
          if (cond) (thenTulos.tulos, List(tila, thenTulos.tila)) else (elseTulos.tulos, List(tila, elseTulos.tila))
        })
        (tulos, tilat, Historia("Jos", tulos, tilat, Some(List(ehtoTulos.historia, thenTulos.historia, elseTulos.historia)), None))
      }

      case KonvertoiLukuarvo(konvertteri, f, oid, tulosTunniste,_,_,_,_) => {
        laskeLukuarvo(f) match {
          case Tulos(tulos, tila, historia) => {
            val (konv, virheet) = konvertteri match {
              case l: Lukuarvovalikonvertteri => konversioToLukuarvovalikonversio(l.konversioMap,hakemus.kentat, hakukohde)
              case a: Arvokonvertteri[_,_] => konversioToArvokonversio[BigDecimal, BigDecimal](a.konversioMap,hakemus.kentat, hakukohde)
              case _ => (Some(konvertteri), List())
            }
            if(konv.isEmpty) {
              (None, virheet, Historia("Konvertoitulukuarvo", None, virheet, Some(List(historia)), None))
            } else {
              val (tulos2, tilat2) = suoritaKonvertointi[BigDecimal, BigDecimal]((tulos, tila), konv.get)
              (tulos2, tilat2, Historia("Konvertoitulukuarvo", tulos2, tilat2, Some(List(historia)), None))
            }

          }
        }

      }

      case HaeLukuarvo(konvertteri, oletusarvo, valintaperusteviite, oid, tulosTunniste,_,_,_,_) => {
        haeLukuarvo(konvertteri, oletusarvo, valintaperusteviite, hakemus.kentat)
      }

      case HaeYoPisteet(konvertteri, ehdot, oletusarvo, valintaperusteviite, oid, tulosTunniste,_,_,_,_) => {
        val suoritukset = filteredSuoritusTiedot(valintaperusteviite, ehdot)
        val sortedValues = suoritukset.sortWith((kentat1, kentat2) =>
          string2integer(kentat1.get("PISTEET"), 0) > string2integer(kentat2.get("PISTEET"), 0)
        )
        haeLukuarvo(konvertteri, oletusarvo, HakemuksenValintaperuste("PISTEET", valintaperusteviite.pakollinen), sortedValues.headOption.getOrElse(Map()))
      }

      case HaeLukuarvoEhdolla(konvertteri, oletusarvo, valintaperusteviite, ehto, oid, tulosTunniste,_,_,_,_) => {
        val tayttyy = ehtoTayttyy(ehto.tunniste, hakemus.kentat)

        val (konv, virheet) = konvertteri match {
          case Some(l: Lukuarvovalikonvertteri) => konversioToLukuarvovalikonversio(l.konversioMap,hakemus.kentat, hakukohde)
          case Some(a: Arvokonvertteri[_,_]) => konversioToArvokonversio[BigDecimal, BigDecimal](a.konversioMap,hakemus.kentat, hakukohde)
          case _ => (konvertteri, List())
        }

        val vp = if(tayttyy) valintaperusteviite else HakemuksenValintaperuste("", false)

        val (tulos, tila) = haeValintaperuste[BigDecimal](vp, hakemus.kentat,
          (s => suoritaOptionalKonvertointi[BigDecimal](string2bigDecimal(s, vp.tunniste),
            konv)), oletusarvo)
        (tulos, tila, Historia("Hae Lukuarvo Ehdolla", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))

      }

      case HaeMerkkijonoJaKonvertoiLukuarvoksi(konvertteri, oletusarvo, valintaperusteviite, oid, tulosTunniste,_,_,_,_) => {
        haeMerkkijonoJaKonvertoiLukuarvoksi(konvertteri, oletusarvo, valintaperusteviite, hakemus.kentat)
      }

      case HaeYoArvosana(konvertteri, ehdot, oletusarvo, valintaperusteviite, oid, tulosTunniste,_,_,_,_) => {
        val suoritukset = filteredSuoritusTiedot(valintaperusteviite, ehdot)
        val sortedValues = suoritukset.sortWith((kentat1, kentat2) =>
          ehdot.YO_ORDER.indexOf(kentat1.getOrElse("ARVO", "I")) < ehdot.YO_ORDER.indexOf(kentat2.getOrElse("ARVO", "I"))
        )
        haeMerkkijonoJaKonvertoiLukuarvoksi(konvertteri, oletusarvo, HakemuksenValintaperuste("ARVO", valintaperusteviite.pakollinen), sortedValues.headOption.getOrElse(Map()))
      }

      case HaeTotuusarvoJaKonvertoiLukuarvoksi(konvertteri, oletusarvo, valintaperusteviite, oid, tulosTunniste,_,_,_,_) => {
        konvertteri match {
          case a: Arvokonvertteri[_,_] => {
            val (konv, virheet) = konversioToArvokonversio(a.konversioMap,hakemus.kentat, hakukohde)
            if(konv.isEmpty) {
              (None, virheet, Historia("Hae totuusarvo ja konvertoi lukuarvoksi", None, virheet, None, None))
            } else {
              val (tulos, tila) = haeValintaperuste[BigDecimal](valintaperusteviite, hakemus.kentat,
                s => suoritaKonvertointi[Boolean, BigDecimal]((string2boolean(s, valintaperusteviite.tunniste, new Hyvaksyttavissatila)), konv.get.asInstanceOf[Arvokonvertteri[Boolean,BigDecimal]]), oletusarvo)

              (tulos, tila, Historia("Hae totuusarvo ja konvertoi lukuarvoksi", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
            }

          }
          case _ => {
            val (tulos, tila) = haeValintaperuste[BigDecimal](valintaperusteviite, hakemus.kentat,
              s => suoritaKonvertointi[Boolean, BigDecimal]((string2boolean(s, valintaperusteviite.tunniste, new Hyvaksyttavissatila)), konvertteri), oletusarvo)
            (tulos, tila, Historia("Hae totuusarvo ja konvertoi lukuarvoksi", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
          }
        }
      }


      case NimettyLukuarvo(nimi, f, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaYksittainenTulos(f, d => d)
        (tulos, tilat, Historia("Nimetty lukuarvo", tulos, tilat, Some(List(h)), Some(Map("nimi" -> Some(nimi)))))
      }

      case Pyoristys(tarkkuus, f, oid, tulosTunniste,_,_,_,_) => {
        val (tulos, tilat, h) = muodostaYksittainenTulos(f, d => d.setScale(tarkkuus, BigDecimal.RoundingMode.HALF_UP))
        (tulos, tilat, Historia("Pyöristys", tulos, tilat, Some(List(h)), Some(Map("tarkkuus" -> Some(tarkkuus)))))
      }
      case Hylkaa(f, hylkaysperustekuvaus, oid, tulosTunniste,_,_,_,_) => {
        laskeTotuusarvo(f) match {
          case Tulos(tulos, tila, historia) => {
            val tila2 = tulos.map(b => if (b) new Hylattytila(JavaConversions.mapAsJavaMap(hylkaysperustekuvaus.getOrElse(Map.empty[String,String])),
              new HylkaaFunktionSuorittamaHylkays)
            else new Hyvaksyttavissatila)
              .getOrElse(new Virhetila(suomenkielinenHylkaysperusteMap("Hylkäämisfunktion syöte on tyhjä. Hylkäystä ei voida tulkita."), new HylkaamistaEiVoidaTulkita))
            val tilat = List(tila, tila2)            
            (None, tilat, Historia("Hylkää", None, tilat, Some(List(historia)), None))
          }
        }
      }
      case HylkaaArvovalilla(f, hylkaysperustekuvaus, oid, tulosTunniste,_,_,_,_, (min,max)) => {
        laske(f) match {
          case Tulos(tulos, tila, historia) => {
            val arvovali = haeArvovali((min, max), hakukohde, hakemus.kentat)
            if(tulos.isEmpty && tila.getTilatyyppi.equals(Tilatyyppi.HYVAKSYTTAVISSA)) {
              val virheTila = new Virhetila(suomenkielinenHylkaysperusteMap("Hylkäämisfunktion syöte on tyhjä. Hylkäystä ei voida tulkita."), new HylkaamistaEiVoidaTulkita)
              (None, List(virheTila), Historia("Hylkää Arvovälillä", None, List(virheTila), Some(List(historia)), None))
            } else if(arvovali isEmpty) {
              val virheTila = new Virhetila(suomenkielinenHylkaysperusteMap("Arvovalin arvoja ei voida muuntaa lukuarvoiksi"), new HylkaamistaEiVoidaTulkita)
              (None, List(virheTila), Historia("Hylkää Arvovälillä", None, List(virheTila), Some(List(historia)), None))
            } else {
              val arvovaliTila = tulos.map(arvo => if(onArvovalilla(arvo, (arvovali.get._1,arvovali.get._2), true, false)) new Hylattytila(JavaConversions.mapAsJavaMap(hylkaysperustekuvaus.getOrElse(Map.empty[String,String])),
                new HylkaaFunktionSuorittamaHylkays) else new Hyvaksyttavissatila).getOrElse(new Hyvaksyttavissatila)

              val tilat = List(tila, arvovaliTila)
              (tulos, tilat, Historia("Hylkää Arvovälillä", tulos, tilat, Some(List(historia)), None))
            }
          }
        }

      }
      case Skaalaus(oid, tulosTunniste,_,_,_,_, skaalattava, (kohdeMin, kohdeMax), lahdeskaala) => {
        if (laskentamoodi != Laskentamoodi.VALINTALASKENTA) {
          val moodi = laskentamoodi.toString
          moodiVirhe(s"Skaalaus-funktiota ei voida suorittaa laskentamoodissa $moodi", "Skaalaus", moodi)
        } else {
          val tulos = laske(skaalattava)

          def skaalaa(skaalattavaArvo: BigDecimal,
                      kohdeskaala: (BigDecimal, BigDecimal),
                      lahdeskaala: (BigDecimal, BigDecimal)) = {
            val lahdeRange = lahdeskaala._2 - lahdeskaala._1
            val kohdeRange = kohdeskaala._2 - kohdeskaala._1
            (((skaalattavaArvo - lahdeskaala._1) * kohdeRange) / lahdeRange) + kohdeskaala._1
          }

          val (skaalauksenTulos, tila) = tulos.tulos match {
            case Some(skaalattavaArvo) => {
              lahdeskaala match {
                case Some((lahdeMin, lahdeMax)) => {
                  if (!onArvovalilla(skaalattavaArvo, (lahdeMin, lahdeMax), true, true)) {
                    (None, new Virhetila(suomenkielinenHylkaysperusteMap(s"Arvo ${skaalattavaArvo.toString} ei ole arvovälillä ${lahdeMin.toString} - ${lahdeMax.toString}"),
                      new SkaalattavaArvoEiOleLahdeskaalassaVirhe(skaalattavaArvo.underlying, lahdeMin.underlying, lahdeMax.underlying)))
                  } else {
                    val skaalattuArvo = skaalaa(skaalattavaArvo, (kohdeMin, kohdeMax), (lahdeMin, lahdeMax))
                    (Some(skaalattuArvo), new Hyvaksyttavissatila)
                  }
                }
                case None => {
                  val tulokset = kaikkiHakemukset.map(h => {
                    Option( Laskin.suoritaValintalaskenta(hakukohde, h, kaikkiHakemukset, skaalattava).getTulos)
                  }).filter(!_.isEmpty).map(_.get)

                  tulokset match {
                    case _ if tulokset.size < 2 => (None, new Virhetila(suomenkielinenHylkaysperusteMap("Skaalauksen lähdeskaalaa ei voida määrittää laskennallisesti. " +
                      "Tuloksia on vähemmän kuin 2 kpl tai kaikki tulokset ovat samoja."), new TuloksiaLiianVahanLahdeskaalanMaarittamiseenVirhe))
                    case _ => {
                      val lahdeSkaalaMin: BigDecimal = tulokset.min
                      val lahdeSkaalaMax: BigDecimal = tulokset.max

                      // Koska tulokset ovat setissä, tiedetään, että min ja max eivät koskaan voi olla yhtäsuuret
                      (Some(skaalaa(skaalattavaArvo, (kohdeMin, kohdeMax), (lahdeSkaalaMin, lahdeSkaalaMax))), new Hyvaksyttavissatila)
                    }
                  }
                }
              }
            }
            case None => (None, new Hyvaksyttavissatila)
          }

          val tilat = List(tila, tulos.tila)
          (skaalauksenTulos, tilat, Historia("Skaalaus", skaalauksenTulos, tilat, Some(List(tulos.historia)), None))
        }
      }

      case PainotettuKeskiarvo(oid, tulosTunniste,_,_,_,_, fs) => {
        val tulokset = fs.map(p => Pair(laskeLukuarvo(p._1), laskeLukuarvo(p._2)))
        val (tilat, historiat) = tulokset.reverse.foldLeft(Pair(List[Tila](), List[Historia]()))((lst, t) => Pair(t._1.tila :: t._2.tila :: lst._1, t._1.historia :: t._2.historia :: lst._2))

        val painokertointenSumma = tulokset.foldLeft(Laskin.ZERO) {
          (s, a) =>
            val painokerroin = a._1.tulos
            val painotettava = a._2.tulos

            s + (painotettava match {
              case Some(p) if(painokerroin.isEmpty) => Laskin.ONE
              case Some(p) => painokerroin.get
              case None => Laskin.ZERO
            })
        }

        val painotettuSumma = tulokset.foldLeft(Laskin.ZERO)((s, a) => s + a._1.tulos.getOrElse(Laskin.ONE) * a._2.tulos.getOrElse(Laskin.ZERO))

        val painotettuKeskiarvo = if(painokertointenSumma.intValue() == 0) None else Some(BigDecimal(painotettuSumma.underlying.divide(painokertointenSumma.underlying, 4, RoundingMode.HALF_UP)))

        (painotettuKeskiarvo, tilat, Historia("Painotettu keskiarvo", painotettuKeskiarvo, tilat, Some(historiat), None))
      }
    }
    if (!laskettava.tulosTunniste.isEmpty) {
      val v = FunktioTulos(
        laskettava.tulosTunniste,
        laskettuTulos.getOrElse("").toString,
        laskettava.tulosTekstiFi,
        laskettava.tulosTekstiSv,
        laskettava.tulosTekstiEn,
        laskettava.omaopintopolku
      )
      funktioTulokset.update(laskettava.tulosTunniste, v)
    }
    Tulos(laskettuTulos, palautettavaTila(tilat), historia)
  }
}

