package gr.netmechanics.epp.client;

import org.springframework.context.ApplicationEvent;

/**
 * @author Panos Bariamis (pbaris)
 */
public class EppRefreshEvent extends ApplicationEvent {

    public EppRefreshEvent(final Object source) {
        super(source);
    }
}
