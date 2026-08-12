package gr.netmechanics.epp.client;

public interface EppPropertiesProvider {
    boolean isUseSandbox();

    String getClientId();

    String getPassword();

    String getLanguage();

    Long getClTrId();

    String getUrl();

    default long getConnectTimeoutMillis() {
        return 10_000;
    }

    default long getReadTimeoutMillis() {
        return 30_000;
    }
}
