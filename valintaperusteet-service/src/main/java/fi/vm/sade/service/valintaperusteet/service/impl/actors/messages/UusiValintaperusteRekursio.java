package fi.vm.sade.service.valintaperusteet.service.impl.actors.messages;

import fi.vm.sade.service.valintaperusteet.dto.ValintaperusteDTO;
import fi.vm.sade.service.valintaperusteet.model.Funktiokutsu;

import java.util.Map;

public class UusiValintaperusteRekursio {
    private Long id;
    private Map<String, ValintaperusteDTO> valintaperusteet;
    private Map<String, String> hakukohteenValintaperusteet;

    public UusiValintaperusteRekursio(Long id, Map<String, ValintaperusteDTO> valintaperusteet, Map<String, String> hakukohteenValintaperusteet) {
        this.id = id;
        this.valintaperusteet = valintaperusteet;
        this.hakukohteenValintaperusteet = hakukohteenValintaperusteet;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Map<String, ValintaperusteDTO> getValintaperusteet() {
        return valintaperusteet;
    }

    public void setValintaperusteet(Map<String, ValintaperusteDTO> valintaperusteet) {
        this.valintaperusteet = valintaperusteet;
    }

    public Map<String, String> getHakukohteenValintaperusteet() {
        return hakukohteenValintaperusteet;
    }

    public void setHakukohteenValintaperusteet(Map<String, String> hakukohteenValintaperusteet) {
        this.hakukohteenValintaperusteet = hakukohteenValintaperusteet;
    }
}
