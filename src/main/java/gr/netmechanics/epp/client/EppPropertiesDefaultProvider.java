package gr.netmechanics.epp.client;

import lombok.RequiredArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@RequiredArgsConstructor
public class EppPropertiesDefaultProvider implements EppPropertiesProvider {

    private final EppProperties properties;

    @Override
    public boolean isUseSandbox() {
        return properties.isUseSandbox();
    }

    @Override
    public String getClientId() {
        return properties.getClientId();
    }

    @Override
    public String getPassword() {
        return properties.getPassword();
    }

    @Override
    public String getLanguage() {
        return properties.getLanguage();
    }

    @Override
    public Long getClTrId() {
        return properties.getClTrId();
    }

    @Override
    public String getUrl() {
        return properties.getUrl();
    }
}
