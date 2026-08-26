package gr.netmechanics.epp.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Setter
@ConfigurationProperties(prefix = "epp")
public class EppProperties {

    @Getter private String clientId;
    @Getter private String password;
    @Getter private String language;
    @Getter private boolean useSandbox;
    @Getter private long connectTimeoutMillis;
    @Getter private long readTimeoutMillis;

    // should be changed for test and debug
    private Long clTrId;

    public EppProperties(
        final String clientId,
        final String password,
        @DefaultValue("el") final String language,
        @DefaultValue("true") final boolean useSandbox,
        @DefaultValue("10000") final long connectTimeoutMillis,
        @DefaultValue("30000") final long readTimeoutMillis,
        final Long clTrId) {

        this.clientId = clientId;
        this.password = password;
        this.language = language;
        this.useSandbox = useSandbox;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
        this.clTrId = clTrId;
    }

    public Long getClTrId() {
        return clTrId != null ? clTrId : System.currentTimeMillis();
    }

    public String getUrl() {
        return useSandbox ? EppConstants.URL_SANDBOX : EppConstants.URL_PRODUCTION;
    }
}
