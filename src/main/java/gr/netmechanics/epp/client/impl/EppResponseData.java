package gr.netmechanics.epp.client.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.commands.check.CheckResponse;
import gr.netmechanics.epp.client.impl.commands.create.CreateResponse;
import gr.netmechanics.epp.client.impl.commands.info.InfoResponse;
import gr.netmechanics.epp.client.impl.commands.renew.RenewResponse;
import gr.netmechanics.epp.client.impl.commands.transfer.TransferResponse;
import gr.netmechanics.epp.client.xml.CheckResponseDeserializer;
import gr.netmechanics.epp.client.xml.CreateResponseDeserializer;
import gr.netmechanics.epp.client.xml.InfoResponseDeserializer;
import gr.netmechanics.epp.client.xml.RenewResponseDeserializer;
import gr.netmechanics.epp.client.xml.TransferResponseDeserializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EppResponseData {

    @JsonDeserialize(using = InfoResponseDeserializer.class)
    @JacksonXmlProperty(localName = "infData")
    private InfoResponse info;

    @JsonDeserialize(using = CheckResponseDeserializer.class)
    @JacksonXmlProperty(localName = "chkData")
    private CheckResponse check;

    @JsonDeserialize(using = CreateResponseDeserializer.class)
    @JacksonXmlProperty(localName = "creData")
    private CreateResponse create;

    @JsonDeserialize(using = RenewResponseDeserializer.class)
    @JacksonXmlProperty(localName = "renData")
    private RenewResponse renew;

    @JsonDeserialize(using = TransferResponseDeserializer.class)
    @JacksonXmlProperty(localName = "trnData")
    private TransferResponse transfer;

}
