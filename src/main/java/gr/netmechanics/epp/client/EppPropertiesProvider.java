package gr.netmechanics.epp.client;

/**
 * @author Panos Bariamis (pbaris)
 */
public interface EppPropertiesProvider {
    boolean isUseSandbox();

    String getClientId();

    String getPassword();

    String getLanguage();

    Long getClTrId();

    String getUrl();
}
