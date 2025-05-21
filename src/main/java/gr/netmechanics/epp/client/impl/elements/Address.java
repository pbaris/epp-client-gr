package gr.netmechanics.epp.client.impl.elements;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Address {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "street")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> streets;

    @JacksonXmlProperty(localName = "city")
    private String city;

    @JacksonXmlProperty(localName = "sp")
    private String stateOrProvince;

    @JacksonXmlProperty(localName = "pc")
    private String postalCode;

    @JacksonXmlProperty(localName = "cc")
    private String countryCode;

    public static AddressBuilder builder() {
        return new AddressBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AddressBuilder implements EppBuilder {

        private String street1;
        private String street2;
        private String street3;
        private String city;
        private String stateOrProvince;
        private String postalCode;
        private String countryCode;

        public AddressBuilder street1(final String street1) {
            this.street1 = street1;
            return this;
        }

        public AddressBuilder street2(final String street2) {
            this.street2 = street2;
            return this;
        }

        public AddressBuilder street3(final String street3) {
            this.street3 = street3;
            return this;
        }

        public AddressBuilder city(final String city) {
            this.city = city;
            return this;
        }

        public AddressBuilder stateOrProvince(final String stateOrProvince) {
            this.stateOrProvince = stateOrProvince;
            return this;
        }

        public AddressBuilder postalCode(final String postalCode) {
            this.postalCode = postalCode;
            return this;
        }

        public AddressBuilder countryCode(final String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Address build() {
            var it = new Address();
            it.city = requireNonEmpty(city, "Address city must be specified");
            it.postalCode = requireNonEmpty(postalCode, "Address postal code must be specified");
            it.stateOrProvince = requireNonEmpty(stateOrProvince, "Address state or province must be specified");
            it.countryCode = requireNonEmpty(countryCode, "Address country code must be specified").toLowerCase();

            it.streets = Stream.of(street1, street2, street3).filter(Objects::nonNull).toList();
            if (it.streets.isEmpty()) {
                it.streets = null;
            }

            return it;
        }
    }
}
