package fi.vm.sade.service.valintaperusteet.service.impl.actors;

import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import fi.vm.sade.service.valintaperusteet.dto.*;
import fi.vm.sade.service.valintaperusteet.dto.mapping.ValintaperusteetModelMapper;
import fi.vm.sade.service.valintaperusteet.dto.model.ValinnanVaiheTyyppi;
import fi.vm.sade.service.valintaperusteet.dto.model.Valintaperustelahde;
import fi.vm.sade.service.valintaperusteet.model.*;
import fi.vm.sade.service.valintaperusteet.service.*;
import fi.vm.sade.service.valintaperusteet.service.impl.LuoValintaperusteetServiceImpl;
import fi.vm.sade.service.valintaperusteet.service.impl.actors.messages.LukionValintaperuste;
import fi.vm.sade.service.valintaperusteet.service.impl.actors.messages.LuoValintaperuste;
import fi.vm.sade.service.valintaperusteet.service.impl.generator.PkJaYoPohjaiset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import scala.concurrent.duration.Duration;

import javax.inject.Named;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kjsaila
 * Date: 17/12/13
 * Time: 13:10
 * To change this template use File | Settings | File Templates.
 */

@Named("LukionValintaperusteetActorBean")
@Component
@org.springframework.context.annotation.Scope(value = "prototype")
public class LukionValintaperusteetActorBean extends UntypedActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Autowired
    private HakukohdekoodiService hakukohdekoodiService;

    @Autowired
    private JpaTransactionManager transactionManager;

    public LukionValintaperusteetActorBean() {

    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return new OneForOneStrategy(5, Duration.create("10 seconds"),
                new Function<Throwable, SupervisorStrategy.Directive>() {
                    public SupervisorStrategy.Directive apply(Throwable cause) {
                        log.error("Virhe valintaperusteiden luonnissa (LukionValintaperusteetActorBean). Syy: {}, viesti:{}", cause.getCause(), cause.getMessage());
                        return SupervisorStrategy.restart();
                    }
                });
    }

    public void onReceive(Object message) throws Exception {

        if (message instanceof LukionValintaperuste) {
            LukionValintaperuste peruste = (LukionValintaperuste)message;
            KoodiDTO hakukohdekoodi = peruste.getHakukohdekoodi();
            ValintaryhmaDTO pkvr = peruste.getPainotettuKeskiarvoVr();
            ValintaryhmaDTO pklsvr = peruste.getPainotettuKeskiarvoJaLisanayttoVr();
            ValintaryhmaDTO pkpsvr = peruste.getPainotettuKeskiarvoJaPaasykoeVr();
            ValintaryhmaDTO pkpslsvr = peruste.getPainotettuKeskiarvoJaPaasykoeJaLisanayttoVr();


            TransactionStatus tx = transactionManager.getTransaction(new DefaultTransactionDefinition());

            hakukohdekoodiService
                    .lisaaHakukohdekoodiValintaryhmalle(pkvr.getOid(), hakukohdekoodi);

            hakukohdekoodiService.lisaaHakukohdekoodiValintaryhmalle(pklsvr.getOid(),
                    hakukohdekoodi);

            hakukohdekoodiService.lisaaHakukohdekoodiValintaryhmalle(pkpsvr.getOid(),
                    hakukohdekoodi);

            hakukohdekoodiService.lisaaHakukohdekoodiValintaryhmalle(
                    pkpslsvr.getOid(), hakukohdekoodi);

            transactionManager.commit(tx);

        }else if(message instanceof Exception) {
            Exception exp = (Exception)message;
            exp.printStackTrace();
            getContext().stop(self());
        } else {
            unhandled(message);
            getContext().stop(getSelf());
        }

    }


}