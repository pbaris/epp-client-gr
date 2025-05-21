package gr.netmechanics.epp.client.impl.commands.transfer.domain;

import java.time.LocalDate;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.commands.transfer.TransferResponse;
import gr.netmechanics.epp.client.impl.elements.Domain;
import gr.netmechanics.epp.client.impl.schema.DomainSchema;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DomainTransferResponse implements DomainSchema, TransferResponse {
    @JacksonXmlProperty(localName = "name")
    private Domain domain;

    @JacksonXmlProperty(localName = "trStatus")
    private String status;

    @JacksonXmlProperty(localName = "reID")
    private String clientRequestId;

    @JacksonXmlProperty(localName = "reDate")
    private LocalDate requestDate;

    @JacksonXmlProperty(localName = "acID")
    private String clientActId;

    @JacksonXmlProperty(localName = "acDate")
    private LocalDate actDate;

    @JacksonXmlProperty(localName = "exDate")
    private LocalDate expirationDate;
}
