package gr.netmechanics.epp.client;

import org.springframework.context.ApplicationEvent;

public class EppRefreshEvent extends ApplicationEvent {

    public EppRefreshEvent(final Object source) {
        super(source);
    }
}
