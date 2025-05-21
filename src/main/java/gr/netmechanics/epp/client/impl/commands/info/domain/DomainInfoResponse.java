package gr.netmechanics.epp.client.impl.commands.info.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.commands.info.InfoResponse;
import gr.netmechanics.epp.client.impl.elements.Audit;
import gr.netmechanics.epp.client.impl.elements.AuthorizationInfo;
import gr.netmechanics.epp.client.impl.elements.Contact;
import gr.netmechanics.epp.client.impl.elements.Domain;
import gr.netmechanics.epp.client.impl.elements.DomainStatus;
import gr.netmechanics.epp.client.impl.schema.DomainSchema;
import gr.netmechanics.epp.client.xml.NameServerDeserializer;
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
public class DomainInfoResponse implements DomainSchema, InfoResponse {

    @JacksonXmlProperty(localName = "name")
    private Domain domain;

    @JacksonXmlProperty(localName = "roid")
    private String repositoryObjectId;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "status")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<DomainStatus> statuses;

    @JacksonXmlProperty(localName = "registrant")
    private String registrant;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "contact")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Contact> contacts;

    @JacksonXmlProperty(localName = "ns")
    @JsonDeserialize(using = NameServerDeserializer.class)
    private List<String> nameServers;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "host")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> hosts;

    @JsonUnwrapped
    private Audit audit;

    @JacksonXmlProperty(localName = "authInfo")
    private AuthorizationInfo authorizationInfo;
}
