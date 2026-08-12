package gr.netmechanics.epp.client;

import org.springframework.stereotype.Component;

@Component
public class EppSessionCookieStore {

    private volatile String cookie;

    public String get() {
        return cookie;
    }

    public void set(final String cookie) {
        this.cookie = cookie;
    }

    public void clear() {
        this.cookie = null;
    }
}
