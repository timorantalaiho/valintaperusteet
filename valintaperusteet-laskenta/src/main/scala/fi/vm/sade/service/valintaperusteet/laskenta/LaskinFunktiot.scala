package fi.vm.sade.service.valintaperusteet.laskenta

import fi.vm.sade.service.valintaperusteet.laskenta.api.Hakemus.Kentat
import fi.vm.sade.service.valintaperusteet.laskenta.api.tila._
import fi.vm.sade.service.valintaperusteet.laskenta.api.{Hakukohde, Hakemus}
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta._
import scala.Some
import scala.Tuple2
import fi.vm.sade.service.valintaperusteet.laskenta.Laskenta.Lukuarvovalikonversio
import scala.util.Try
import scala.collection.JavaConversions
import fi.vm.sade.service.valintaperusteet.model.TekstiRyhma

trait LaskinFunktiot {

  val pattern = """\{\{([A-Za-z\d\-_]+)\.([A-Za-z\d\-_]+)\}\}""".r

  protected def ehdollinenTulos[A, B](tulos: (Option[A], Tila), f: (A, Tila) => Tuple2[Option[B], List[Tila]]): Tuple2[Option[B], List[Tila]] = {
    val (alkupTulos, alkupTila) = tulos
    alkupTulos match {
      case Some(t) => f(t, alkupTila)
      case None => (None, List(alkupTila))
    }
  }

  protected def suomenkielinenHylkaysperusteMap(teksti: String) = {
    JavaConversions.mapAsJavaMap(Map("FI" -> teksti))
  }

  protected def suoritaKonvertointi[S, T](tulos: Tuple2[Option[S], Tila],
                                        konvertteri: Konvertteri[S, T]) = {

    ehdollinenTulos[S, T](tulos, (t, tila) => {
      val (konvertoituTulos, konvertoituTila) = konvertteri.konvertoi(t)
      (konvertoituTulos, List(tila, konvertoituTila))
    })


  }

  protected def suoritaOptionalKonvertointi[T](tulos: Tuple2[Option[T], Tila],
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

  protected def haeValintaperuste(tunniste: String, pakollinen: Boolean, kentat: Kentat): (Option[String], Tila) = {
    kentat.get(tunniste) match {
      case Some(s) if (!s.trim.isEmpty) => (Some(s), new Hyvaksyttavissatila)
      case _ => {
        val tila = if (pakollinen) {
          new Hylattytila(suomenkielinenHylkaysperusteMap(s"Pakollista arvoa (tunniste $tunniste) ei ole olemassa"),
            new PakollinenValintaperusteHylkays(tunniste))
        } else new Hyvaksyttavissatila

        (None, tila)
      }
    }
  }

  protected def haeValintaperusteHakemukselta(tunniste: String, kentat: Kentat): Option[Any] = {
    kentat.get(tunniste) match {
      case Some(s) if (!s.trim.isEmpty) => {
        val pilkuton = s.replace(',', '.')
        val result = Try(s.toBoolean).getOrElse(
          Try(BigDecimal(pilkuton)).getOrElse(s)
        )
        Some(result)
      }
      case _ =>  None
    }
  }

  protected def haeValintaperusteHakukohteelta(tunniste: String, hakukohde: Hakukohde): Option[Any] = {
    hakukohde.valintaperusteet.get(tunniste) match {
      case Some(s) if (!s.trim.isEmpty) => {
        val pilkuton = s.replace(',', '.')
        val result = Try(s.toBoolean).getOrElse(
          Try(BigDecimal(pilkuton)).getOrElse(s)
        )
        Some(result)
      }
      case _ => None
    }
  }

  protected def haeValintaperuste(tunniste: String): Option[Any] = {
    val pilkuton = tunniste.replace(',', '.')
    if(tunniste.equalsIgnoreCase("true") || tunniste.equalsIgnoreCase("false")) {
      return Some(tunniste.toBoolean)
    }
    Some(Try(BigDecimal(pilkuton)).getOrElse(tunniste));
  }

  protected def ehtoTayttyy(ehto: String, kentat: Kentat): Boolean = {
    ehto match {
      case pattern(avain, oletus) => {
        haeValintaperusteHakemukselta(avain, kentat) match {
          case Some(arvo: String) => if(arvo.equals(oletus)) true else false
          case _ => false
        }
      }
      case _ => false
    }
  }

  protected def haeArvovali(tunnisteet: (String,String), hakukohde: Hakukohde, kentat: Kentat): Option[(BigDecimal, BigDecimal)] = {
    val min = tunnisteet._1 match {
      case pattern(source, identifier) => {
        source match {
          case "hakemus" => haeValintaperusteHakemukselta(identifier, kentat)
          case "hakukohde" => haeValintaperusteHakukohteelta(identifier, hakukohde)
          case _ => None
        }
      }
      case s: String => haeValintaperuste(s)
    }
    val max = tunnisteet._2 match {
      case pattern(source, identifier) => {
        source match {
          case "hakemus" => haeValintaperusteHakemukselta(identifier, kentat)
          case "hakukohde" => haeValintaperusteHakukohteelta(identifier, hakukohde)
          case _ => None
        }
      }
      case s: String => haeValintaperuste(s)
    }

    (min, max) match {
      case (Some(minimi: BigDecimal), Some(maksimi: BigDecimal)) => Some((minimi, maksimi))
      case _ => None
    }
  }

  protected def palautettavaTila(tilat: Seq[Tila]): Tila = {
    tilat.filter(_ match {
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
  }

  protected def moodiVirhe(virheViesti: String, funktioNimi: String, moodi: String) = {
    (None, List(new Virhetila(suomenkielinenHylkaysperusteMap(virheViesti), new VirheellinenLaskentamoodiVirhe(funktioNimi, moodi))),
      Historia(funktioNimi, None, List(), None, None))
  }

  protected def string2boolean(s: String, tunniste: String, oletustila: Tila = new Hyvaksyttavissatila): (Option[Boolean], Tila) = {
    try {
      (Some(s.toBoolean), oletustila)
    } catch {
      case e: Throwable => (None, new Virhetila(suomenkielinenHylkaysperusteMap(s"Arvoa $s ei voida muuttaa Boolean-tyyppiseksi (tunniste $tunniste)"),
        new ValintaperustettaEiVoidaTulkitaTotuusarvoksiVirhe(tunniste)))
    }
  }

  protected def string2bigDecimal(s: String, tunniste: String, oletustila: Tila = new Hyvaksyttavissatila): (Option[BigDecimal], Tila) = {
    try {
      (Some(BigDecimal(s.replace(',', '.'))), oletustila)
    } catch {
      case e: Throwable => (None, new Virhetila(suomenkielinenHylkaysperusteMap(s"Arvoa $s ei voida muuttaa BigDecimal-tyyppiseksi (tunniste $tunniste)"),
        new ValintaperustettaEiVoidaTulkitaLukuarvoksiVirhe(tunniste)))
    }
  }

  protected def string2integer(s: Option[String], default: Int):Int = {
    try {
      s.get.toInt
    } catch {
      case e:Exception => default
    }
  }

  def konversioToLukuarvovalikonversio[S, T](konversiot: Seq[Konversio], kentat: Kentat, hakukohde: Hakukohde): (Option[Lukuarvovalikonvertteri], List[Tila]) = {

    def getLukuarvovaliKonversio(k: Konversio) = {
      val tilat = k match {
        case l: LukuarvovalikonversioMerkkijonoilla => {
          val min = l.min match {
            case pattern(source, identifier) => {
              source match {
                case "hakemus" => haeValintaperusteHakemukselta(identifier, kentat)
                case "hakukohde" => haeValintaperusteHakukohteelta(identifier, hakukohde)
                case _ => None
              }
            }
            case s: String => haeValintaperuste(s)
          }
          val max = l.max match {
            case pattern(source, identifier) => {
              source match {
                case "hakemus" => haeValintaperusteHakemukselta(identifier, kentat)
                case "hakukohde" => haeValintaperusteHakukohteelta(identifier, hakukohde)
                case _ => None
              }
            }
            case s: String => haeValintaperuste(s)
          }
          val paluuarvo = l.paluuarvo match {
            case pattern(source, identifier) => {
              source match {
                case "hakemus" => haeValintaperusteHakemukselta(identifier, kentat)
                case "hakukohde" => haeValintaperusteHakukohteelta(identifier, hakukohde)
                case _ => None
              }
            }
            case s: String => haeValintaperuste(s)
          }
          val palautaHaettuArvo = l.palautaHaettuArvo match {
            case pattern(source, identifier) => {
              source match {
                case "hakemus" => haeValintaperusteHakemukselta(identifier, kentat)
                case "hakukohde" => haeValintaperusteHakukohteelta(identifier, hakukohde)
                case _ => None
              }
            }
            case s: String => haeValintaperuste(s)
          }
          val hylkaysperuste = l.hylkaysperuste match {
            case pattern(source, identifier) => {
              source match {
                case "hakemus" => haeValintaperusteHakemukselta(identifier, kentat)
                case "hakukohde" => haeValintaperusteHakukohteelta(identifier, hakukohde)
              }
            }
            case s: String => haeValintaperuste(s)
          }
          (min, max, paluuarvo, palautaHaettuArvo, hylkaysperuste, Some(l.kuvaukset))
        }
        case lk: Lukuarvovalikonversio => {

         (Some(lk.min),Some(lk.max),Some(lk.paluuarvo),Some(lk.palautaHaettuArvo),Some(lk.hylkaysperuste), Some(lk.kuvaukset))
        }
        case _ => {
          (None,None,None,None,false,None,None)
        }
      }

      tilat match {
        case (Some(min: BigDecimal), Some(max: BigDecimal),Some(p: BigDecimal), Some(ph: Boolean), Some(h: Boolean), Some(k: TekstiRyhma)) => {
          Some(Lukuarvovalikonversio(min, max, p, ph, h, k))
        }
        case (Some(min: BigDecimal), Some(max: BigDecimal),Some(p: BigDecimal), Some(ph: Boolean), Some(h: Boolean), Some(null)) => {
          Some(Lukuarvovalikonversio(min, max, p, ph, h, new TekstiRyhma()))
        }
        case _ => None
      }

    }

    val konvertoidut = konversiot.map(konv => getLukuarvovaliKonversio(konv))

    if(konvertoidut.contains(None)) {
      (None, List(new Virhetila(suomenkielinenHylkaysperusteMap(s"Konversioita ei voitu muuttaa Arvovälikonversioiksi"),new ArvokonvertointiVirhe())))
    } else {
      val konvertteri = Lukuarvovalikonvertteri(konvertoidut.map(k => k.get))
      (Some(konvertteri), List(new Hyvaksyttavissatila))
    }


  }

  def konversioToArvokonversio[S, T](konversiot: Seq[Konversio], kentat: Kentat, hakukohde: Hakukohde): (Option[Arvokonvertteri[S,T]], List[Tila]) = {

    def getArvoKonversio(k: Konversio) = {
      val tilat = k match {
        case a: ArvokonversioMerkkijonoilla[S,T] => {
          val arvo = a.arvo match {
            case pattern(source, identifier) => {
              source match {
                case "hakemus" => haeValintaperusteHakemukselta(identifier, kentat)
                case "hakukohde" => haeValintaperusteHakukohteelta(identifier, hakukohde)
                case _ => None
              }
            }
            case s: String => haeValintaperuste(s)
          }
          val hylkaysperuste = a.hylkaysperuste match {
            case pattern(source, identifier) => {
              source match {
                case "hakemus" => haeValintaperusteHakemukselta(identifier, kentat)
                case "hakukohde" => haeValintaperusteHakukohteelta(identifier, hakukohde)
              }
            }
            case s: String => haeValintaperuste(s)
          }

          (arvo, Some(a.paluuarvo), hylkaysperuste, Some(a.kuvaukset))
        }
        case ak: Arvokonversio[_,_] => {

          (Some(ak.arvo),Some(ak.paluuarvo),Some(ak.hylkaysperuste), Some(ak.kuvaukset))
        }
        case _ => {
          (None,None,None,None)
        }
      }

      tilat match {
        case (Some(arvo: Any), Some(paluuarvo: Any), Some(h: Boolean), Some(k: TekstiRyhma)) => {
          Some(Arvokonversio(arvo, paluuarvo, h, k))
        }
        case (Some(arvo: Any), Some(paluuarvo: Any), Some(h: Boolean), Some(null)) => {
          Some(Arvokonversio(arvo, paluuarvo, h, new TekstiRyhma()))
        }
        case _ => None
      }

    }

    val konvertoidut = konversiot.map(konv => getArvoKonversio(konv))

    if(konvertoidut.contains(None)) {
      (None, List(new Virhetila(suomenkielinenHylkaysperusteMap(s"Konversioita ei voitu muuttaa Arvokonversioiksi"),new ArvokonvertointiVirhe())))
    } else {
      val konvertteri = Arvokonvertteri[S,T](konvertoidut.map(k => k.get))
      (Some(konvertteri), List(new Hyvaksyttavissatila))
    }


  }

}
