package gr.netmechanics.epp.client.impl.commands.info.registrar;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import gr.netmechanics.epp.client.impl.commands.info.InfoResponse;
import gr.netmechanics.epp.client.impl.schema.RegistrarSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RegistrarInfoResponse implements RegistrarSchema, InfoResponse {

    @JacksonXmlProperty(localName = "roid")
    private String repositoryObjectId;

    @JacksonXmlProperty(localName = "ddPaymentCode")
    private String directDebitPaymentCode;

    @JacksonXmlProperty(localName = "caAllowed")
    private boolean chargeableActionsAllowed;

    @JacksonXmlProperty(localName = "balance")
    private Balance balance;

    @JacksonXmlProperty(localName = "tbSuspendedOn")
    private LocalDate toBeSuspendedOn;

    @JacksonXmlProperty(localName = "suspendedOn")
    private LocalDate suspendedOn;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    @ToString
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Balance {

        @JacksonXmlProperty(isAttribute = true, localName = "currency")
        private String currency;

        @JacksonXmlText
        private BigDecimal amount;
    }
}
