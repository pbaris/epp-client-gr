package gr.netmechanics.epp.client.impl.commands.renew.domain;

import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonEmpty;
import static gr.netmechanics.epp.client.impl.EppBuilder.requireNonNull;
import static gr.netmechanics.epp.client.impl.EppBuilder.requireYears;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.EppBuilder;
import gr.netmechanics.epp.client.impl.commands.renew.RenewRequest;
import gr.netmechanics.epp.client.impl.elements.Period;
import gr.netmechanics.epp.client.impl.schema.DomainSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainRenewRequest implements DomainSchema, RenewRequest {

    @JacksonXmlProperty(localName = "domain:name")
    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JacksonXmlProperty(localName = "domain:curExpDate")
    private LocalDate currentExpirationDate;

    @JacksonXmlProperty(localName = "domain:period")
    private Period period;

    public static DomainRenewRequestBuilder builder() {
        return new DomainRenewRequestBuilder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DomainRenewRequestBuilder implements EppBuilder {

        private String name;
        private LocalDate currentExpirationDate;
        private int years;

        public DomainRenewRequestBuilder domainName(final String name) {
            this.name = name;
            return this;
        }

        public DomainRenewRequestBuilder currentExpirationDate(final LocalDate date) {
            this.currentExpirationDate = date;
            return this;
        }

        public DomainRenewRequestBuilder years(final int years) {
            this.years = years;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public DomainRenewRequest build() {
            var req = new DomainRenewRequest();
            req.name = requireNonEmpty(name, "Domain name must be specified");
            req.currentExpirationDate = requireNonNull(currentExpirationDate, "Domain current expiration date must be specified");
            req.period = new Period(requireYears(years));
            return req;
        }
    }
}
