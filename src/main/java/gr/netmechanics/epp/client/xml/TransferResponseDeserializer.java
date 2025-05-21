package gr.netmechanics.epp.client.xml;

import gr.netmechanics.epp.client.impl.EppResponse;
import gr.netmechanics.epp.client.impl.commands.transfer.TransferResponse;
import gr.netmechanics.epp.client.impl.commands.transfer.domain.DomainTransferResponse;

/**
 * @author Panos Bariamis (pbaris)
 */
public class TransferResponseDeserializer extends AbstractResponseDeserializer<TransferResponse> {

    @Override
    protected Class<DomainTransferResponse> getDomainResponseClass() {
        return DomainTransferResponse.class;
    }

    @Override
    protected Class<EppResponse> getContactResponseClass() {
        throw new UnsupportedOperationException("Contact transfer is not supported");
    }

    @Override
    protected Class<EppResponse> getHostResponseClass() {
        throw new UnsupportedOperationException("Host transfer is not defined");
    }
}
