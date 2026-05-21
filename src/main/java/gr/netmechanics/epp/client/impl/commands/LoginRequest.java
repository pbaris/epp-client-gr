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
            req.password = validatePassword(requireNonEmpty(password, "Password must be specified"));
            if (newPassword != null) {
                req.newPassword = validatePassword(newPassword);
            }

            String ver = requireNonEmpty(version, "EPP version must be specified");
            String lang = requireNonEmpty(language, "EPP language must be specified");

            req.options = new LoginOptions(ver, lang);
            req.services = new Services(requireNonEmpty(objectUris, "Object URIs must be specified"));
            return req;
        }

        private static final String SPECIAL_CHARS = "~!@#$%^&*(){}:;-_+=\\/?[]";

        private String validatePassword(final String password) {
            if (password.length() < 8 || password.length() > 16) {
                throw new IllegalArgumentException("Password length must be between 8 and 16");
            }

            boolean hasLower = false;
            boolean hasUpper = false;
            boolean hasDigit = false;
            boolean hasSpecial = false;

            for (char c : password.toCharArray()) {
                if (Character.isLowerCase(c)) {
                    hasLower = true;
                } else if (Character.isUpperCase(c)) {
                    hasUpper = true;
                } else if (Character.isDigit(c)) {
                    hasDigit = true;
                } else if (SPECIAL_CHARS.indexOf(c) != -1) {
                    hasSpecial = true;
                }
            }

            if (!(hasLower && hasUpper && hasDigit && hasSpecial)) {
                throw new IllegalArgumentException("Password must contain at least one character from each group: a-z, A-Z, 0-9, and special characters");
            }

            return password;
        }
    }
}
