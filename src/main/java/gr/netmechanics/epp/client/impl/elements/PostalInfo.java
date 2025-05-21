package gr.netmechanics.epp.client.impl.elements;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.elements.Address.AddressBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * @author Panos Bariamis (pbaris)
 */
@Slf4j
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PostalInfo {

    @JacksonXmlProperty(localName = "name")
    private String name;

    @JacksonXmlProperty(isAttribute = true, localName = "type")
    private String type;

    @JacksonXmlProperty(localName = "org")
    private String organization;

    @JacksonXmlProperty(localName = "addr")
    private Address address;

    public static PostalInfoBuilder builder() {
        return new PostalInfoBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class PostalInfoBuilder implements EppBuilder {

        private boolean update;

        private String name;
        private String type;
        private String organization;
        private AddressBuilder address;

        public PostalInfoBuilder update(final boolean update) {
            this.update = update;
            return this;
        }

        public PostalInfoBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public PostalInfoBuilder organization(final String organization) {
            this.organization = organization;
            return this;
        }

        public PostalInfoBuilder type(final String type) {
            this.type = type;
            return this;
        }

        public PostalInfoBuilder address(final AddressBuilder addressBuilder) {
            this.address = addressBuilder;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public PostalInfo build() {
            var it = new PostalInfo();
            if (!update) {
                it.name = requireNonEmpty(name, "PostalInfo name must be specified");
                it.organization = organization;

            } else if (StringUtils.hasText(name) || StringUtils.hasText(organization)) {
                log.warn("PostalInfo name and update organization is not supported when updating a contact");
            }

            it.type = List.of("loc", "int").contains(type) ? type : "loc";

            if (address == null) {
                throw new IllegalStateException("PostalInfo address must be specified");
            }
            it.address = address.build();

            return it;
        }
    }
}
