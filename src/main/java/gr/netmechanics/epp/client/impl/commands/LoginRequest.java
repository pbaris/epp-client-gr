package gr.netmechanics.epp.client.impl.commands;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlCData;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.EppRequest;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LoginRequest implements EppRequest {

    @JacksonXmlProperty(localName = "clID")
    private String clientId;

    @JacksonXmlCData
    @JacksonXmlProperty(localName = "pw")
    private String password;

    @JacksonXmlCData
    @JacksonXmlProperty(localName = "newPW")
    private String newPassword;

    @JacksonXmlProperty(localName = "options")
    private LoginOptions options;

    @JacksonXmlProperty(localName = "svcs")
    private Services services;

    public static LoginRequestBuilder builder() {
        return new LoginRequestBuilder();
    }

    private record LoginOptions(
        @JacksonXmlProperty(localName = "version")
        String version,

        @JacksonXmlProperty(localName = "lang")
        String language) {
    }

    private record Services(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "objURI")
        List<String> objectUris) {
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LoginRequestBuilder implements EppBuilder {
        private String clientId;
        private String password;
        private String newPassword;
        private String version;
        private String language;
        private List<String> objectUris;

        public LoginRequestBuilder clientId(final String clientId) {
            this.clientId = clientId;
            return this;
        }

        public LoginRequestBuilder password(final String password) {
            this.password = password;
            return this;
        }

        public LoginRequestBuilder newPassword(final String newPassword) {
            this.newPassword = newPassword;
            return this;
        }

        public LoginRequestBuilder version(final String version) {
            this.version = version;
            return this;
        }

        public LoginRequestBuilder language(final String language) {
            this.language = language;
            return this;
        }

        public LoginRequestBuilder objectUris(final List<String> objectUris) {
            this.objectUris = objectUris;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public LoginRequest build() {
            var req = new LoginRequest();
            req.clientId = requireNonEmpty(clientId, "Client ID must be specified");
            req.password = requireNonEmpty(password, "Password must be specified");
            req.newPassword = newPassword;

            String ver = requireNonEmpty(version, "EPP version must be specified");
            String lang = requireNonEmpty(language, "EPP language must be specified");

            req.options = new LoginOptions(ver, lang);
            req.services = new Services(requireNonEmpty(objectUris, "Object URIs must be specified"));
            return req;
        }
    }
}
