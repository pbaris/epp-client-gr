package gr.netmechanics.epp.client;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Panos Bariamis (pbaris)
 */
@Component
@RequiredArgsConstructor
public class EppRefreshListener {

    @Lazy
    private final EppRequestFlowManager requestFlowManager;

    @Lazy
    private final EppClient eppClient;

    @Lazy
    private final ConfigurableApplicationContext context;

    @EventListener
    public void onRefresh(final EppRefreshEvent event) {
        EppPropertiesProvider eppProps = context.getBean(EppPropertiesProvider.class);
        eppClient.setEppProps(eppProps);
        requestFlowManager.restartFlow(eppProps);
    }
}