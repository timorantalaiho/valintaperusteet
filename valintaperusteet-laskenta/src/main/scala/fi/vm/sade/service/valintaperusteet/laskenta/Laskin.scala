package fi.vm.sade.service.valintaperusteet.laskenta

import api.Hakukohde
import api.Hakemus
import api.Laskentatulos
import api.Osallistuminen
import api.{SyotettyArvo => SArvo}
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta._
import java.util.{Map => JMap}
import fi.vm.sade.service.valintaperusteet.laskenta.api.tila._
import org.slf4j.LoggerFactory
import scala.collection.mutable.ListBuffer
import java.math.{BigDecimal => BigDec}
import java.math.RoundingMode
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.KonvertoiLukuarvo
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Negaatio
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Totuusarvo
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Osamaara
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Pyoristys
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Suurempi
import scala.Some
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Yhtasuuri
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Jos
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.PienempiTaiYhtasuuri
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.HaeMerkkijonoJaKonvertoiTotuusarvoksi
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Demografia
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.HaeMerkkijonoJaKonvertoiLukuarvoksi
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Lukuarvo
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Pienempi
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.NimettyTotuusarvo
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.HaeLukuarvo
import scala.Tuple3
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.NimettyLukuarvo
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.SyotettavaValintaperuste
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.SuurempiTaiYhtasuuri
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.HaeTotuusarvo
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.HaeMerkkijonoJaVertaaYhtasuuruus
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.HakukohteenValintaperuste
import scala.Tuple2
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Ei
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Hakutoive
import java.lang
import scala.collection.JavaConversions._
import com.codahale.jerkson.Json

object Laskin {
  val LOG = LoggerFactory.getLogger(classOf[Laskin])

  private def wrapSyotetyArvot(sa: Map[String, SyotettyArvo]): Map[String, SArvo] = {
    sa.map(e => (e._1 -> new SArvo(e._1, if (e._2.arvo.isEmpty) null else e._2.arvo.get,
      if (e._2.laskennallinenArvo.isEmpty) null else e._2.laskennallinenArvo.get, e._2.osallistuminen)))
  }

  def suoritaValintalaskenta(hakukohde: Hakukohde,
                             hakemus: Hakemus,
                             kaikkiHakemukset: java.util.Collection[Hakemus],
                             laskettava: Lukuarvofunktio): Laskentatulos[BigDec] = {
    val laskin = new Laskin(hakukohde, hakemus, kaikkiHakemukset.toSet)
    laskin.laske(laskettava) match {
      case Tulos(tulos, tila, historia) => {
        new Laskentatulos[BigDec](tila, if (tulos.isEmpty) null else tulos.get.underlying
          , new StringBuffer().append(Json.generate(wrapHistoria(hakemus, historia))), wrapSyotetyArvot(laskin.getSyotetytArvot))
      }
    }
  }

  def suoritaValintalaskenta(hakukohde: Hakukohde,
                             hakemus: Hakemus,
                             kaikkiHakemukset: java.util.Collection[Hakemus],
                             laskettava: Totuusarvofunktio): Laskentatulos[lang.Boolean] = {
    val laskin = new Laskin(hakukohde, hakemus, kaikkiHakemukset.toSet)
    laskin.laske(laskettava) match {
      case Tulos(tulos, tila, historia) => {
        new Laskentatulos[lang.Boolean](tila, if (tulos.isEmpty) null else Boolean.box(tulos.get)
          , new StringBuffer().append(Json.generate(wrapHistoria(hakemus, historia))), wrapSyotetyArvot(laskin.getSyotetytArvot))
      }
    }
  }

  def suoritaValintakoelaskenta(hakukohde: Hakukohde,
                                hakemus: Hakemus,
                                laskettava: Lukuarvofunktio): Laskentatulos[BigDec] = {
    val laskin = new Laskin(hakukohde, hakemus)
    laskin.laske(laskettava) match {
      case Tulos(tulos, tila, historia) => {
        new Laskentatulos[BigDec](tila, if (tulos.isEmpty) null else tulos.get.underlying
          , new StringBuffer().append(Json.generate(wrapHistoria(hakemus, historia))), wrapSyotetyArvot(laskin.getSyotetytArvot))
      }
    }
  }

  def suoritaValintakoelaskenta(hakukohde: Hakukohde,
                                hakemus: Hakemus,
                                laskettava: Totuusarvofunktio): Laskentatulos[java.lang.Boolean] = {
    val laskin = new Laskin(hakukohde, hakemus)
    laskin.laske(laskettava) match {
      case Tulos(tulos, tila, historia) => {
        new Laskentatulos[lang.Boolean](tila, if (tulos.isEmpty) null else Boolean.box(tulos.get)
          , new StringBuffer().append(Json.generate(wrapHistoria(hakemus, historia))), wrapSyotetyArvot(laskin.getSyotetytArvot))
      }
    }
  }

  def laske(hakukohde: Hakukohde, hakemus: Hakemus, laskettava: Totuusarvofunktio): (Option[Boolean], Tila) = {
    val tulos = new Laskin(hakukohde, hakemus).laskeTotuusarvo(laskettava)
    (tulos.tulos, tulos.tila)
  }

  def laske(hakukohde: Hakukohde, hakemus: Hakemus, laskettava: Lukuarvofunktio): (Option[BigDec], Tila) = {
    val tulos = new Laskin(hakukohde, hakemus).laskeLukuarvo(laskettava)
    (tulos.tulos.map(_.underlying()), tulos.tila)
  }

  private def wrapHistoria(hakemus: Hakemus, historia: Historia) = {
    val v: Map[String, Option[Any]] = hakemus.kentat.map(f => (f._1 -> Some(f._2)))

    val name = new StringBuffer().append("Laskenta hakemukselle (").append(hakemus.oid).append(")").toString
    Historia(name, historia.tulos, historia.tilat, Some(List(historia)), Some(v))
  }
}

private case class Tulos[T](tulos: Option[T], tila: Tila, historia: Historia)

private case class SyotettyArvo(val tunniste: String, val arvo: Option[String],
                                val laskennallinenArvo: Option[String], val osallistuminen: Osallistuminen)

private object Laskentamoodi extends Enumeration {
  type Laskentamoodi = Value

  val VALINTALASKENTA = Value("VALINTALASKENTA")
  val VALINTAKOELASKENTA = Value("VALINTAKOELASKENTA")

}

private class Laskin private(private val hakukohde: Hakukohde,
                             private val hakemus: Hakemus,
                             private val kaikkiHakemukset: Set[Hakemus],
                             private val laskentamoodi: Laskentamoodi.Laskentamoodi) {

  def this(hakukohde: Hakukohde, hakemus: Hakemus) {
    this(hakukohde, hakemus, Set(), Laskentamoodi.VALINTAKOELASKENTA)
  }

  def this(hakukohde: Hakukohde, hakemus: Hakemus, kaikkiHakemukset: Set[Hakemus]) {
    this(hakukohde, hakemus, kaikkiHakemukset, Laskentamoodi.VALINTALASKENTA)
  }

  private val syotetytArvot: scala.collection.mutable.Map[String, SyotettyArvo] = scala.collection.mutable.Map[String, SyotettyArvo]()

  def getSyotetytArvot = scala.collection.immutable.Map[String, SyotettyArvo](syotetytArvot.toList: _*)

  def laske(laskettava: Lukuarvofunktio) = {
    syotetytArvot.clear
    laskeLukuarvo(laskettava)
  }


  def laske(laskettava: Totuusarvofunktio) = {
    syotetytArvot.clear
    laskeTotuusarvo(laskettava)
  }

  private def ehdollinenTulos[A, B](tulos: (Option[A], Tila), f: (A, Tila) => Tuple2[Option[B], List[Tila]]): Tuple2[Option[B], List[Tila]] = {
    val (alkupTulos, alkupTila) = tulos
    alkupTulos match {
      case Some(t) => f(t, alkupTila)
      case None => (None, List(alkupTila))
    }
  }

  private def suoritaKonvertointi[S, T](tulos: Tuple2[Option[S], Tila],
                                        konvertteri: Konvertteri[S, T]) = {
    ehdollinenTulos[S, T](tulos, (t, tila) => {
      val (konvertoituTulos, konvertoituTila) = konvertteri.konvertoi(t)
      (konvertoituTulos, List(tila, konvertoituTila))
    })
  }

  private def suoritaOptionalKonvertointi[T](tulos: Tuple2[Option[T], Tila],
                                             konvertteri: Option[Konvertteri[T, T]]) = {
    ehdollinenTulos[T, T](tulos, (t, tila) => {
      konvertteri match {
        case Some(konv) => {
          val (konvertoituTulos, konvertoituTila) = konv.konvertoi(t)
          (konvertoituTulos, List(tila, konvertoituTila))
        }
        case None => (Some(t), List(tila))
      }
    })
  }

  private def haeValintaperuste(tunniste: String, pakollinen: Boolean, hakemus: Hakemus): (Option[String], Tila) = {
    hakemus.kentat.get(tunniste) match {
      case Some(s) if (!s.trim.isEmpty) => (Some(s), new Hyvaksyttavissatila)
      case _ => {
        val tila = if (pakollinen) {
          new Hylattytila("Pakollista arvoa (tunniste " + tunniste + ") ei " +
            "ole olemassa", new PakollinenValintaperusteHylkays(tunniste))
        } else new Hyvaksyttavissatila

        (None, tila)
      }
    }
  }

  private def haeValintaperuste[T](valintaperusteviite: Valintaperuste, hakemus: Hakemus,
                                   konv: (String => Tuple2[Option[T], List[Tila]]),
                                   oletusarvo: Option[T]): Tuple2[Option[T], List[Tila]] = {
    def haeValintaperusteenArvoHakemukselta(tunniste: String, pakollinen: Boolean) = {
      val (valintaperuste, tila) = haeValintaperuste(tunniste, pakollinen, hakemus)

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
      case SyotettavaValintaperuste(tunniste, pakollinen, osallistuminenTunniste) => {
        val (osallistuminen, osallistumistila) = hakemus.kentat.get(osallistuminenTunniste) match {
          case Some(osallistuiArvo) => {
            try {
              (Osallistuminen.valueOf(osallistuiArvo), new Hyvaksyttavissatila)
            } catch {
              case e: IllegalArgumentException => (Osallistuminen.MERKITSEMATTA, new Virhetila("Osallistumistietoa "
                + osallistuiArvo + " ei pystytty tulkitsemaan (tunniste " + osallistuminenTunniste + ")",
                new OsallistumistietoaEiVoidaTulkitaVirhe(osallistuminenTunniste)))
            }
          }
          case None => (Osallistuminen.MERKITSEMATTA, new Hyvaksyttavissatila)
        }

        // Jos valintaperusteelle on merkitty arvo "ei osallistunut" tai sitä ei ole merkitty, palautetaan hylätty-tila,
        // jos kyseessä on pakollinen tieto

        val (arvo, konvertoitu, tilat) = if (pakollinen && Osallistuminen.EI_OSALLISTUNUT == osallistuminen)
          (None, None, List(osallistumistila, new Hylattytila("Pakollisen syötettävän kentän arvo on '" + osallistuminen.name() + "' (tunniste "
            + tunniste + ")", new EiOsallistunutHylkays(tunniste))))
        else if (pakollinen && Osallistuminen.MERKITSEMATTA == osallistuminen)
          (None, None, List(osallistumistila, new Virhetila("Pakollisen syötettävän kentän arvo on merkitsemättä (tunniste "
            + tunniste + ")", new SyotettavaArvoMerkitsemattaVirhe(tunniste))))
        else {
          val (arvo, konvertoitu, tilat) = haeValintaperusteenArvoHakemukselta(tunniste, pakollinen)
          (arvo, konvertoitu, osallistumistila :: tilat)
        }

        syotetytArvot(tunniste) = SyotettyArvo(tunniste, arvo, konvertoitu.map(_.toString), osallistuminen)
        (konvertoitu, tilat)
      }
      case HakemuksenValintaperuste(tunniste, pakollinen) => {
        val (_, konvertoitu, tilat) = haeValintaperusteenArvoHakemukselta(tunniste, pakollinen)
        (konvertoitu, tilat)
      }
      case HakukohteenValintaperuste(tunniste) => {
        hakukohde.valintaperusteet.get(tunniste).filter(!_.trim.isEmpty).map(konv(_)).getOrElse(
          if (!oletusarvo.isEmpty) (oletusarvo, List(new Hyvaksyttavissatila))
          else {
            (None, List(new Virhetila("Hakukohteen valintaperustetta " + tunniste + " ei ole määritelty",
              new HakukohteenValintaperusteMaarittelemattaVirhe(tunniste))))
          })
      }
    }
  }


  private def string2boolean(s: String, tunniste: String, oletustila: Tila = new Hyvaksyttavissatila): (Option[Boolean], Tila) = {
    try {
      (Some(s.toBoolean), oletustila)
    } catch {
      case e: NumberFormatException => (None, new Virhetila("Arvoa " + s + " ei voida muuttaa "
        + "Boolean-tyyppiseksi (tunniste " + tunniste + ")",
        new ValintaperustettaEiVoidaTulkitaTotuusarvoksiVirhe(tunniste)))
    }
  }

  private def string2bigDecimal(s: String, tunniste: String, oletustila: Tila = new Hyvaksyttavissatila): (Option[BigDecimal], Tila) = {
    try {
      (Some(BigDecimal(s)), oletustila)
    } catch {
      case e: NumberFormatException => (None, new Virhetila("Arvoa " + s + " ei voida muuttaa "
        + "BigDecimal-tyyppiseksi (tunniste " + tunniste + ")",
        new ValintaperustettaEiVoidaTulkitaLukuarvoksiVirhe(tunniste)))
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
      case Ja(fs, oid) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, lst => lst.forall(b => b));
        (tulos, tilat, Historia("Ja", tulos, tilat, h.historiat, None))
      }
      case Tai(fs, oid) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, lst => lst.exists(b => b));
        (tulos, tilat, Historia("Tai", tulos, tilat, h.historiat, None))
      }
      case Ei(fk, oid) => {
        val (tulos, tilat, h) = muodostaYksittainenTulos(fk, b => !b)
        (tulos, tilat, Historia("Ei", tulos, tilat, Some(List(h)), None))
      }
      case Totuusarvo(b, oid) => {
        val tilat = List(new Hyvaksyttavissatila)
        (Some(b), tilat, Historia("Totuusarvo", Some(b), tilat, None, None))
      }
      case Suurempi(f1, f2, oid) => {
        val (tulos, tilat, h) = muodostaVertailunTulos(f1, f2, (d1, d2) => d1 > d2)
        (tulos, tilat, Historia("Suurempi", tulos, tilat, Some(List(h)), None))
      }
      case SuurempiTaiYhtasuuri(f1, f2, oid) => {
        val (tulos, tilat, h) = muodostaVertailunTulos(f1, f2, (d1, d2) => d1 >= d2);
        (tulos, tilat, Historia("Suurempi tai yhtäsuuri", tulos, tilat, Some(List(h)), None))
      }
      case Pienempi(f1, f2, oid) => {
        val (tulos, tilat, h) = muodostaVertailunTulos(f1, f2, (d1, d2) => d1 < d2)
        (tulos, tilat, Historia("Pienempi", tulos, tilat, Some(List(h)), None))
      }
      case PienempiTaiYhtasuuri(f1, f2, oid) => {
        val (tulos, tilat, h) = muodostaVertailunTulos(f1, f2, (d1, d2) => d1 <= d2)
        (tulos, tilat, Historia("Pienempi tai yhtäsuuri", tulos, tilat, Some(List(h)), None))
      }
      case Yhtasuuri(f1, f2, oid) => {
        val (tulos, tilat, h) = muodostaVertailunTulos(f1, f2, (d1, d2) => d1 == d2)
        (tulos, tilat, Historia("Yhtäsuuri", tulos, tilat, Some(List(h)), None))
      }
      case HaeTotuusarvo(konvertteri, oletusarvo, valintaperusteviite, oid) => {
        val (tulos, tila) = haeValintaperuste[Boolean](valintaperusteviite, hakemus,
          (s => suoritaOptionalKonvertointi[Boolean](string2boolean(s, valintaperusteviite.tunniste),
            konvertteri)), oletusarvo)
        (tulos, tila, Historia("Hae totuusarvo", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
      }
      case NimettyTotuusarvo(nimi, f, oid) => {
        val (tulos, tilat, h) = muodostaYksittainenTulos(f, b => b)
        (tulos, tilat, Historia("Nimetty totuusarvo", tulos, tilat, Some(List(h)), Some(Map("nimi" -> Some(nimi)))))
      }

      case Hakutoive(n, oid) => {
        val onko = Some(hakemus.onkoHakutoivePrioriteetilla(hakukohde.hakukohdeOid, n));
        val tilat = List(new Hyvaksyttavissatila)
        (onko, tilat, Historia("Hakutoive", onko, tilat, None, Some(Map("prioriteetti" -> Some(n)))))
      }

      case Demografia(oid, tunniste, prosenttiosuus) => {
        if (laskentamoodi != Laskentamoodi.VALINTALASKENTA) {
          (None, List(new Virhetila("Demografia-funktiota ei voida suorittaa laskentamoodissa "
            + laskentamoodi.toString, new VirheellinenLaskentamoodiVirhe("Demografia", laskentamoodi.toString))),
            Historia("Demografia", None, List(), None, None))
        } else {
          val ensisijaisetHakijat = kaikkiHakemukset.count(_.onkoHakutoivePrioriteetilla(hakukohde.hakukohdeOid, 1))

          val omaArvo = hakemus.kentat.map(e => (e._1.toLowerCase -> e._2)).get(tunniste.toLowerCase)
          val tulos = Some(if (ensisijaisetHakijat == 0) false
          else {
            val samojenArvojenLkm = kaikkiHakemukset.count(h => h.onkoHakutoivePrioriteetilla(hakukohde.hakukohdeOid, 1) &&
              h.kentat.map(e => (e._1.toLowerCase -> e._2)).get(tunniste.toLowerCase) == omaArvo)


            val vertailuarvo = BigDecimal(samojenArvojenLkm).underlying.divide(BigDecimal(ensisijaisetHakijat).underlying, 4, RoundingMode.HALF_UP)
            vertailuarvo.compareTo(prosenttiosuus.underlying.divide(BigDecimal("100.0").underlying, 4, RoundingMode.HALF_UP)) != 1
          })

          val tila = new Hyvaksyttavissatila
          (tulos, List(tila), Historia("Demografia", tulos, List(tila), None, Some(Map("avain" -> Some(tunniste), "valintaperuste" -> omaArvo))))
        }
      }

      case HaeMerkkijonoJaKonvertoiTotuusarvoksi(konvertteri, oletusarvo, valintaperusteviite, oid) => {
        val (tulos, tila) = haeValintaperuste[Boolean](valintaperusteviite, hakemus,
          (s => suoritaKonvertointi[String, Boolean]((Some(s), new Hyvaksyttavissatila), konvertteri)), oletusarvo)
        (tulos, tila, Historia("Hae merkkijono ja konvertoi totuusarvoksi", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
      }

      case HaeMerkkijonoJaVertaaYhtasuuruus(oletusarvo, valintaperusteviite, vertailtava, oid) => {
        val (tulos, tila) = haeValintaperuste[Boolean](valintaperusteviite, hakemus,
          (s => (Some(s.trim.equalsIgnoreCase(vertailtava.trim)), List(new Hyvaksyttavissatila))), oletusarvo)
        (tulos, tila, Historia("Hae merkkijono ja vertaa yhtasuuruus", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
      }
    }

    val palautettavaTila = tilat.filter(_ match {
      case _: Virhetila => true
      case _ => false
    }) match {
      case head :: tail => head
      case Nil => tilat.filter(_ match {
        case _: Hylattytila => true
        case _ => false
      }) match {
        case head :: tail => head
        case Nil => new Hyvaksyttavissatila
      }
    }

    Tulos(laskettuTulos, palautettavaTila, hist)
  }

  private def laskeLukuarvo(laskettava: Lukuarvofunktio): Tulos[BigDecimal] = {

    def summa(vals: Seq[BigDecimal]): BigDecimal = vals.reduceLeft(_ + _)

    def muodostaYksittainenTulos(f: Lukuarvofunktio, trans: BigDecimal => BigDecimal): (Option[BigDecimal], List[Tila], Historia) = {
      laskeLukuarvo(f) match {
        case Tulos(tulos, tila, historia) => (tulos.map(trans(_)), List(tila), historia)
      }
    }

    def muodostaKoostettuTulos(fs: Seq[Lukuarvofunktio], trans: Seq[BigDecimal] => BigDecimal): (Option[BigDecimal], List[Tila], Historia) = {
      val tulokset = fs.reverse.foldLeft((Nil, Nil, ListBuffer()): Tuple3[List[BigDecimal], List[Tila], ListBuffer[Historia]])((lst, f) => {
        laskeLukuarvo(f) match {
          case Tulos(tulos, tila, historia) => {
            lst._3 += historia
            (if (!tulos.isEmpty) tulos.get :: lst._1 else lst._1, tila :: lst._2, lst._3)
          }
        }
      })

      val lukuarvo = if (tulokset._1.isEmpty) None else Some(trans(tulokset._1))
      (lukuarvo, tulokset._2, Historia("Koostettu tulos", lukuarvo, tulokset._2, Some(tulokset._3.toList), None))
    }

    val (laskettuTulos: Option[BigDecimal], tilat: Seq[Tila], historia: Historia) = laskettava match {
      case Lukuarvo(d, oid) => {
        val tila = List(new Hyvaksyttavissatila)
        (Some(d), tila, Historia("Lukuarvo", Some(d), tila, None, None))
      }
      case Negaatio(n, oid) => {
        val (tulos, tilat, h) = muodostaYksittainenTulos(n, d => -d)
        (tulos, tilat, Historia("Negaaatio", tulos, tilat, Some(List(h)), None))
      }
      case Summa(fs, oid) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, summa)
        (tulos, tilat, Historia("Summa", tulos, tilat, h.historiat, None))
      }

      case SummaNParasta(n, fs, oid) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => summa(ds.sortWith(_ > _).take(n)))
        (tulos, tilat, Historia("Summa N-parasta", tulos, tilat, h.historiat, Some(Map("n" -> Some(n)))))
      }

      case Osamaara(osoittaja, nimittaja, oid) => {
        val nimittajaTulos = laskeLukuarvo(nimittaja)
        val osoittajaTulos = laskeLukuarvo(osoittaja)

        val (arvo, laskentatilat) = (for {
          n <- nimittajaTulos.tulos
          o <- osoittajaTulos.tulos
        } yield {
          if (n == 0.0) (None, new Virhetila("Jako nollalla", new JakoNollallaVirhe))
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

      case Tulo(fs, oid) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => ds.reduceLeft(_ * _))
        (tulos, tilat, Historia("Tulo", tulos, tilat, h.historiat, None))
      }

      case Keskiarvo(fs, oid) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => BigDecimal(summa(ds).underlying.divide(BigDecimal(ds.size).underlying, 4, RoundingMode.HALF_UP)))
        (tulos, tilat, Historia("Keskiarvo", tulos, tilat, h.historiat, None))
      }

      case KeskiarvoNParasta(n, fs, oid) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => {
          val kaytettavaN = scala.math.min(n, ds.size)
          BigDecimal(summa(ds.sortWith(_ > _).take(kaytettavaN)).underlying.divide(BigDecimal(kaytettavaN).underlying, 4, RoundingMode.HALF_UP))
        })
        (tulos, tilat, Historia("Keskiarvo N-parasta", tulos, tilat, h.historiat, Some(Map("n" -> Some(n)))))
      }
      case Minimi(fs, oid) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => ds.min)
        (tulos, tilat, Historia("Minimi", tulos, tilat, h.historiat, None))
      }

      case Maksimi(fs, oid) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => ds.max)
        (tulos, tilat, Historia("Maksimi", tulos, tilat, h.historiat, None))
      }

      case NMinimi(ns, fs, oid) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => ds.sortWith(_ < _)(scala.math.min(ns, ds.size) - 1))
        (tulos, tilat, Historia("N-minimi", tulos, tilat, h.historiat, Some(Map("ns" -> Some(ns)))))
      }

      case NMaksimi(ns, fs, oid) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => ds.sortWith(_ > _)(scala.math.min(ns, ds.size) - 1))
        (tulos, tilat, Historia("N-maksimi", tulos, tilat, h.historiat, Some(Map("ns" -> Some(ns)))))
      }

      case Mediaani(fs, oid) => {
        val (tulos, tilat, h) = muodostaKoostettuTulos(fs, ds => {
          val sorted = ds.sortWith(_ < _)
          if (sorted.size % 2 == 1) sorted(sorted.size / 2)
          else BigDecimal((sorted(sorted.size / 2) + sorted(sorted.size / 2 - 1)).underlying.divide(BigDecimal("2.0").underlying, 4, RoundingMode.HALF_UP))
        })
        (tulos, tilat, Historia("Mediaani", tulos, tilat, h.historiat, None))
      }

      case Jos(ehto, thenHaara, elseHaara, oid) => {
        val ehtoTulos = laskeTotuusarvo(ehto)
        val thenTulos = laskeLukuarvo(thenHaara)
        val elseTulos = laskeLukuarvo(elseHaara)
        //historiat :+ historia1 :+ historia2
        val (tulos, tilat) = ehdollinenTulos[Boolean, BigDecimal]((ehtoTulos.tulos, ehtoTulos.tila), (cond, tila) => {
          if (cond) (thenTulos.tulos, List(tila, thenTulos.tila)) else (elseTulos.tulos, List(tila, elseTulos.tila))
        })
        (tulos, tilat, Historia("Jos", tulos, tilat, Some(List(ehtoTulos.historia, thenTulos.historia, elseTulos.historia)), None))
      }

      case KonvertoiLukuarvo(konvertteri, f, oid) => {
        laskeLukuarvo(f) match {
          case Tulos(tulos, tila, historia) => {
            val (tulos2, tilat2) = suoritaKonvertointi[BigDecimal, BigDecimal]((tulos, tila), konvertteri)
            (tulos2, tilat2, Historia("Konvertoitulukuarvo", tulos2, tilat2, Some(List(historia)), None))
          }
        }

      }

      case HaeLukuarvo(konvertteri, oletusarvo, valintaperusteviite, oid) => {
        val (tulos, tila) = haeValintaperuste[BigDecimal](valintaperusteviite, hakemus,
          (s => suoritaOptionalKonvertointi[BigDecimal](string2bigDecimal(s, valintaperusteviite.tunniste),
            konvertteri)), oletusarvo)
        (tulos, tila, Historia("Hae Lukuarvo", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
      }
      case HaeMerkkijonoJaKonvertoiLukuarvoksi(konvertteri, oletusarvo, valintaperusteviite, oid) => {
        val (tulos, tila) = haeValintaperuste[BigDecimal](valintaperusteviite, hakemus,
          (s => suoritaKonvertointi[String, BigDecimal]((Some(s), new Hyvaksyttavissatila), konvertteri)), oletusarvo)
        (tulos, tila, Historia("Hae merkkijono ja konvertoi lukuarvoksi", tulos, tila, None, Some(Map("oletusarvo" -> oletusarvo))))
      }
      case NimettyLukuarvo(nimi, f, oid) => {
        val (tulos, tilat, h) = muodostaYksittainenTulos(f, d => d)
        (tulos, tilat, Historia("Nimetty lukuarvo", tulos, tilat, Some(List(h)), Some(Map("nimi" -> Some(nimi)))))
      }

      case Pyoristys(tarkkuus, f, oid) => {
        val (tulos, tilat, h) = muodostaYksittainenTulos(f, d => d.setScale(tarkkuus, BigDecimal.RoundingMode.HALF_UP))
        (tulos, tilat, Historia("Pyöristys", tulos, tilat, Some(List(h)), Some(Map("tarkkuus" -> Some(tarkkuus)))))
      }
      case Hylkaa(f, hylkaysperustekuvaus, oid) => {
        laskeTotuusarvo(f) match {
          case Tulos(tulos, tila, historia) => {
            val tila2 = tulos.map(b => if (b) new Hylattytila(hylkaysperustekuvaus.getOrElse("Hylätty hylkäämisfunktiolla"),
              new HylkaaFunktionSuorittamaHylkays)
            else new Hyvaksyttavissatila)
              .getOrElse(new Virhetila("Hylkäämisfunktion syöte on tyhjä. Hylkäystä ei voida tulkita.", new HylkaamistaEiVoidaTulkita))
            val tilat = List(tila, tila2)
            (None, tilat, Historia("Hylkää", None, tilat, Some(List(historia)), None))
          }
        }
      }
      case Skaalaus(oid, skaalattava, (kohdeMin, kohdeMax), lahdeskaala) => {
        if (laskentamoodi != Laskentamoodi.VALINTALASKENTA) {
          (None, List(new Virhetila("Skaalaus-funktiota ei voida suorittaa laskentamoodissa "
            + laskentamoodi.toString, new VirheellinenLaskentamoodiVirhe("Demografia", laskentamoodi.toString))),
            Historia("Skaalaus", None, List(), None, None))
        } else {
          val tulos = laske(skaalattava)

          def skaalaa(skaalattavaArvo: BigDecimal,
                      kohdeskaalaMin: BigDecimal,
                      kohdeskaalaMax: BigDecimal,
                      lahdeskaalaMin: BigDecimal,
                      lahdeskaalaMax: BigDecimal) = {
            val lahdeRange = lahdeskaalaMax - lahdeskaalaMin
            val kohdeRange = kohdeskaalaMax - kohdeskaalaMin
            (((skaalattavaArvo - lahdeskaalaMin) * kohdeRange) / lahdeRange) + kohdeskaalaMin
          }

          val (skaalauksenTulos, tila) = tulos.tulos match {
            case Some(skaalattavaArvo) => {
              lahdeskaala match {
                case Some((lahdeMin, lahdeMax)) => {
                  if (lahdeMin > skaalattavaArvo || lahdeMax < skaalattavaArvo) {
                    (None, new Virhetila("Arvo " + skaalattavaArvo.toString + " ei ole arvovälillä " + lahdeMin.toString + " - " + lahdeMax.toString,
                      new SkaalattavaArvoEiOleLahdeskaalassaVirhe(skaalattavaArvo.underlying, lahdeMin.underlying, lahdeMax.underlying)))
                  } else {

                    val skaalattuArvo = skaalaa(skaalattavaArvo, kohdeMin, kohdeMax, lahdeMin, lahdeMax)
                    (Some(skaalattuArvo), new Hyvaksyttavissatila)
                  }
                }
                case None => {
                  val tulokset = kaikkiHakemukset.map(h => {
                    Option(Laskin.suoritaValintalaskenta(hakukohde, h, kaikkiHakemukset, skaalattava).getTulos)
                  }).filter(!_.isEmpty).map(_.get)

                  tulokset match {
                    case _ if tulokset.size < 2 => (None, new Virhetila("Skaalauksen lähdeskaalaa ei voida määrittää laskennallisesti. " +
                      "Tuloksia on vähemmän kuin 2 kpl tai kaikki tulokset ovat samoja.", new TuloksiaLiianVahanLahdeskaalanMaarittamiseenVirhe))
                    case _ => {
                      val lahdeSkaalaMin: BigDecimal = tulokset.min
                      val lahdeSkaalaMax: BigDecimal = tulokset.max

                      // Koska tulokset ovat setissä, tiedetään, että min ja max eivät koskaan voi olla yhtäsuuret
                      (Some(skaalaa(skaalattavaArvo, kohdeMin, kohdeMax, lahdeSkaalaMin, lahdeSkaalaMax)), new Hyvaksyttavissatila)
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

      case PainotettuKeskiarvo(oid, fs) => {
        val tulokset = fs.map(p => Pair(laskeLukuarvo(p._1), laskeLukuarvo(p._2)))
        val (tilat, historiat) = tulokset.reverse.foldLeft(Pair(List[Tila](), List[Historia]()))((lst, t) => Pair(t._1.tila :: t._2.tila :: lst._1, t._1.historia :: t._2.historia :: lst._2))

        val painotetutArvot = for {t <- tulokset
                                   painotus <- t._1.tulos
                                   arvo <- t._2.tulos
        } yield painotus * arvo

        val painotettuKeskiarvo = if (!painotetutArvot.isEmpty) {
          Some(BigDecimal(summa(painotetutArvot).underlying.divide(BigDecimal(painotetutArvot.size).underlying, 4, RoundingMode.HALF_UP)))
        } else None

        (painotettuKeskiarvo, tilat, Historia("Painotettu keskiarvo", painotettuKeskiarvo, tilat, Some(historiat), None))
      }
    }


    val palautettavaTila = tilat.filter(_ match {
      case _: Virhetila => true
      case _ => false
    }) match {
      case head :: tail => head
      case Nil => tilat.filter(_ match {
        case _: Hylattytila => true
        case _ => false
      }) match {
        case head :: tail => head
        case Nil => new Hyvaksyttavissatila
      }
    }

    Tulos(laskettuTulos, palautettavaTila, historia)
  }
}

