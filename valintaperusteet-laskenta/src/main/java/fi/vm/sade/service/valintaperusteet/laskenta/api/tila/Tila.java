package fi.vm.sade.service.valintaperusteet.laskenta.api.tila;

/**
 * User: kwuoti
 * Date: 24.2.2013
 * Time: 19.22
 */
public abstract class Tila {
    public static enum Tilatyyppi {
        HYLATTY(Hylattytila.class),
        HYVAKSYTTAVISSA(Hyvaksyttavissatila.class);

        private  Class<? extends Tila> tyyppi;

        Tilatyyppi(Class<? extends Tila> tyyppi) {
            this.tyyppi = tyyppi;
        }
    }

    public Tila(Tilatyyppi tilatyyppi) {
        this.tilatyyppi = tilatyyppi;
    }

    private Tilatyyppi tilatyyppi;

    public Tilatyyppi getTilatyyppi() {
        return tilatyyppi;
    }
}
