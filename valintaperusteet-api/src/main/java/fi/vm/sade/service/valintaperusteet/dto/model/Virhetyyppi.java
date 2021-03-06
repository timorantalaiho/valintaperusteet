package fi.vm.sade.service.valintaperusteet.dto.model;

public enum Virhetyyppi {
    FUNKTIONIMI_VIRHEELLINEN, VAARA_MAARA_SYOTEPARAMETREJA, SYOTEPARAMETRI_PUUTTUU, VIRHEELLINEN_SYOTEPARAMETRIN_TYYPPI,
    FUNKTIOKUTSU_EI_OTA_KONVERTTERIPARAMETREJA, EI_KONVERTTERIPARAMETREJA_MAARITELTY,
    VIRHEELLINEN_KONVERTTERIPARAMETRIN_PALUUARVOTYYPPI, VIRHEELLINEN_ARVOKONVERTTERIN_ARVOTYYPPI,
    ARVOVALIKONVERTTERIN_MIN_MAX_PUUTTEELLINEN, ARVOVALIKONVERTTERIN_MINIMI_SUUREMPI_KUIN_MAKSIMI,
    FUNKTIOKUTSU_EI_OTA_VALINTAPERUSTEPARAMETRIA, VALINTAPERUSTEPARAMETRI_PUUTTUUU,
    FUNKTIOKUTSU_EI_OTA_FUNKTIOARGUMENTTEJA, VAARA_MAARA_FUNKTIOARGUMENTTEJA, VIRHEELLINEN_FUNKTIOARGUMENTIN_TYYPPI,
    KONVERTTERIPARAMETRIN_PALUUARVO_PUUTTUU, ARVOKONVERTTERIN_ARVO_PUUTTUU, TYHJA_SYOTEPARAMETRIN_ARVO,
    VALINTAPERUSTEPARAMETRIN_TUNNISTE_PUUTTUU, FUNKTIOARGUMENTTIA_EI_MAARITELTY,
    FUNKTIOKUTSUA_EI_OLE_MAARITELTY_FUNKTIOARGUMENTILLE, FUNKTIOARGUMENTIN_LASKENTAKAAVA_VAARAN_TYYPPINEN_VIRHE,
    FUNKTIOARGUMENTIN_LASKENTAKAAVA_ON_LUONNOS, N_PIENEMPI_KUIN_YKSI, N_SUUREMPI_KUIN_FUNKTIOARGUMENTTIEN_LKM,
    PROSENTTIOSUUS_EPAVALIDI, ARVOVALIKONVERTTERIN_ARVOVALI_PUUTTEELLINEN, LAHDESKAALAA_EI_OLE_MAARITELTY, KOHDESKAALA_VIRHEELLINEN, LAHDESKAALA_VIRHEELLINEN, TARKKUUS_PIENEMPI_KUIN_NOLLA
}
