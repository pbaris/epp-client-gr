package gr.netmechanics.epp.client.impl;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import gr.netmechanics.epp.client.impl.commands.check.CheckResponse;
import gr.netmechanics.epp.client.impl.commands.create.CreateResponse;
import gr.netmechanics.epp.client.impl.commands.info.InfoResponse;
import gr.netmechanics.epp.client.impl.commands.renew.RenewResponse;
import gr.netmechanics.epp.client.impl.commands.transfer.TransferResponse;
import gr.netmechanics.epp.client.impl.schema.EppSchema;
import gr.netmechanics.epp.client.xml.ExtensionDeserializer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Panos Bariamis (pbaris)
 */
@Setter(AccessLevel.PRIVATE)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EppCommandResponse implements EppSchema, EppResponse {

    @JacksonXmlProperty(localName = "response")
    private ResponseNode response;

    @SuppressWarnings("unchecked")
    public <T extends InfoResponse> T getInfoResponse() {
        return response.data() == null ? null : (T) response.data().getInfo();
    }

    @SuppressWarnings("unchecked")
    public <T extends CheckResponse> T getCheckResponse() {
        return response.data() == null ? null : (T) response.data().getCheck();
    }

    @SuppressWarnings("unchecked")
    public <T extends CreateResponse> T getCreateResponse() {
        return response.data() == null ? null : (T) response.data().getCreate();
    }

    @SuppressWarnings("unchecked")
    public <T extends RenewResponse> T getRenewResponse() {
        return response.data() == null ? null : (T) response.data().getRenew();
    }

    @SuppressWarnings("unchecked")
    public <T extends TransferResponse> T getTransferResponse() {
        return response.data() == null ? null : (T) response.data().getTransfer();
    }

    @ToString.Include(name = "successfulResult")
    public EppResponseResult getSuccessfulResult() {
        return isSuccess() ? response.results.getFirst() : null;
    }

    @ToString.Include(name = "extension")
    @SuppressWarnings("unchecked")
    public <T extends EppExtension> T getExtension() {
        return response.extension() == null ? null : (T) response.extension().data();
    }

    public List<EppResponseResult> getResults() {
        return response.results();
    }

    @ToString.Include(name = "clientTransactionId")
    public String getClientTransactionId() {
        return response.transactionIdentifiers().clientTransactionId();
    }

    @ToString.Include(name = "serverTransactionId")
    public String getServerTransactionId() {
        return response.transactionIdentifiers().serverTransactionId();
    }

    @ToString.Include(name = "success")
    public boolean isSuccess() {
        return response.results != null
               && response.results.size() == 1
               && response.results.getFirst().isSuccess();
    }

    private record ResponseNode(
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "result")
        List<EppResponseResult> results,

        @JacksonXmlProperty(localName = "resData")
        EppResponseData data,

        @JacksonXmlProperty(localName = "extension")
        ExtensionNode extension,

        @JacksonXmlProperty(localName = "trID")
        TransactionIdentifiersNode transactionIdentifiers) {
    }

    private record ExtensionNode(
        @JsonDeserialize(using = ExtensionDeserializer.class)
        @JacksonXmlProperty(localName = "resData")
        EppExtension data) {
    }

    private record TransactionIdentifiersNode(
        @JacksonXmlProperty(localName = "clTRID")
        String clientTransactionId,

        @JacksonXmlProperty(localName = "svTRID")
        String serverTransactionId) {
    }
}
