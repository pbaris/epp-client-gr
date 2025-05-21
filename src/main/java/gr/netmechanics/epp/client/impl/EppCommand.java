package gr.netmechanics.epp.client.impl;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.commands.LoginRequest;
import gr.netmechanics.epp.client.impl.commands.LogoutRequest;
import gr.netmechanics.epp.client.impl.commands.check.CheckRequest;
import gr.netmechanics.epp.client.impl.commands.create.CreateRequest;
import gr.netmechanics.epp.client.impl.commands.delete.DeleteRequest;
import gr.netmechanics.epp.client.impl.commands.info.InfoRequest;
import gr.netmechanics.epp.client.impl.commands.renew.RenewRequest;
import gr.netmechanics.epp.client.impl.commands.transfer.TransferRequest;
import gr.netmechanics.epp.client.impl.commands.transfer.TransferRequestWrapper;
import gr.netmechanics.epp.client.impl.commands.update.UpdateRequest;
import gr.netmechanics.epp.client.impl.elements.ext.HasExtension;
import gr.netmechanics.epp.client.xml.ExtensionSerializer;
import lombok.Getter;

/**
 * @author Panos Bariamis (pbaris)
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EppCommand {

    @JacksonXmlProperty(localName = "info")
    private InfoRequest infoRequest;

    @JacksonXmlProperty(localName = "check")
    private CheckRequest checkRequest;

    @JacksonXmlProperty(localName = "create")
    private CreateRequest createRequest;

    @JacksonXmlProperty(localName = "update")
    private UpdateRequest updateRequest;

    @JacksonXmlProperty(localName = "renew")
    private RenewRequest renewRequest;

    @JacksonXmlProperty(localName = "delete")
    private DeleteRequest deleteRequest;

    @JacksonXmlProperty(localName = "transfer")
    private TransferRequestWrapper transferRequest;

    @JacksonXmlProperty(localName = "login")
    private LoginRequest loginRequest;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "logout")
    private LogoutRequest logoutRequest;

    @JacksonXmlProperty(localName = "extension")
    private ExtensionNode extension;

    @JacksonXmlProperty(localName = "clTRID")
    private String clientTransactionId;

    <T extends EppRequest> EppCommand(final T request, final long cti) {
        if (request instanceof InfoRequest r) {
            this.infoRequest = r;

        } else if (request instanceof CheckRequest r) {
            this.checkRequest = r;

        } else if (request instanceof CreateRequest r) {
            this.createRequest = r;

        } else if (request instanceof UpdateRequest r) {
            this.updateRequest = r;

        } else if (request instanceof RenewRequest r) {
            this.renewRequest = r;

        } else if (request instanceof DeleteRequest r) {
            this.deleteRequest = r;

        } else if (request instanceof TransferRequest r) {
            this.transferRequest = new TransferRequestWrapper(r);

        } else if (request instanceof LoginRequest r) {
            this.loginRequest = r;

        } else if (request instanceof LogoutRequest r) {
            this.logoutRequest = r;
        }

        if (request instanceof HasExtension r && r.getExtension() != null) {
            extension = new ExtensionNode(r.getExtension());
        }

        Optional.ofNullable(EppRequestType.fromInstance(request))
            .ifPresent(type -> this.clientTransactionId = type.name() + '-' + cti);
    }

    private record ExtensionNode(
        @JsonSerialize(using = ExtensionSerializer.class)
        EppExtension cmdExtension) {
    }
}
