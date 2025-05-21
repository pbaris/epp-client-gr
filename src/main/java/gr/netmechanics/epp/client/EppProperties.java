package gr.netmechanics.epp.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * @author Panos Bariamis (pbaris)
 */
@Setter
@ConfigurationProperties(prefix = "epp")
public class EppProperties {

    @Getter private String clientId;
    @Getter private String password;
    @Getter private String language;
    @Getter private boolean useSandbox;

    // should be changed for test and debug
    private Long clTrId;

    public EppProperties(
        final String clientId,
        final String password,
        @DefaultValue("el") final String language,
        @DefaultValue("true") final boolean useSandbox,
        final Long clTrId) {

        this.clientId = clientId;
        this.password = password;
        this.language = language;
        this.useSandbox = useSandbox;
        this.clTrId = clTrId;
    }

    public Long getClTrId() {
        return clTrId != null ? clTrId : System.currentTimeMillis();
    }

    public String getUrl() {
        return useSandbox ? EppConstants.URL_SANDBOX : EppConstants.URL_PRODUCTION;
    }
}
