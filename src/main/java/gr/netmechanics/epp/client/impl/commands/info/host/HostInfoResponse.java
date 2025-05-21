package gr.netmechanics.epp.client.impl.commands.info.host;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.commands.info.InfoResponse;
import gr.netmechanics.epp.client.impl.elements.Audit;
import gr.netmechanics.epp.client.impl.elements.Domain;
import gr.netmechanics.epp.client.impl.elements.HostStatus;
import gr.netmechanics.epp.client.impl.elements.IPAddress;
import gr.netmechanics.epp.client.impl.schema.HostSchema;
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
public class HostInfoResponse implements HostSchema, InfoResponse {

    @JacksonXmlProperty(localName = "name")
    private Domain domain;

    @JacksonXmlProperty(localName = "roid")
    private String repositoryObjectId;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "status")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<HostStatus> statuses;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "addr")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<IPAddress> ipAddresses;

    @JsonUnwrapped
    private Audit audit;
}
