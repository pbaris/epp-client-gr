package gr.netmechanics.epp.client;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.check.domain.DomainCheckRequest;
import gr.netmechanics.epp.client.impl.commands.create.domain.DomainCreateRequest;
import gr.netmechanics.epp.client.impl.commands.delete.domain.DomainDeleteRequest;
import gr.netmechanics.epp.client.impl.commands.info.domain.DomainInfoRequest;
import gr.netmechanics.epp.client.impl.commands.renew.domain.DomainRenewRequest;
import gr.netmechanics.epp.client.impl.commands.transfer.domain.DomainTransferRequest;
import gr.netmechanics.epp.client.impl.commands.update.domain.DomainUpdateRequest;
import org.springframework.lang.NonNull;

/**
 * Domain commands (RFC3731), reached via {@link EppClient#domains()}.
 * Every command returns {@code null} if no EPP session could be established with the server.
 */
public class EppDomainCommands {

    private final EppCommandSender sender;

    EppDomainCommands(final EppCommandSender sender) {
        this.sender = sender;
    }

    /**
     * Retrieves detailed information about a specific domain.
     *
     * @param infoRequest the request containing details of the domain for which information is being queried
     * @return the response from the EPP server containing the domain information
     */
    public EppCommandResponse info(@NonNull final DomainInfoRequest infoRequest) {
        return sender.send(infoRequest);
    }

    /**
     * Checks the availability of specified domain names against the EPP server.
     *
     * @param checkRequest the request containing the domain names to be checked for availability
     * @return the response from the EPP server indicating the availability of the specified domain names
     */
    public EppCommandResponse check(@NonNull final DomainCheckRequest checkRequest) {
        return sender.send(checkRequest);
    }

    /**
     * Creates a new domain.
     *
     * @param createRequest the request object containing the details of the domain to be created
     * @return the response from the EPP server indicating the result of the domain creation process
     */
    public EppCommandResponse create(@NonNull final DomainCreateRequest createRequest) {
        return sender.send(createRequest);
    }

    /**
     * Updates an existing domain.
     *
     * @param updateRequest the request containing the details of the changes to be made to the domain,
     *                      such as contact modifications, name server adjustments, or status updates
     * @return the response from the EPP server indicating the result of the domain update process
     */
    public EppCommandResponse update(@NonNull final DomainUpdateRequest updateRequest) {
        return sender.send(updateRequest);
    }

    /**
     * Renews an existing domain.
     *
     * @param renewRequest the request containing the details of the domain to be renewed,
     *                     including its name, current expiration date, and renewal period
     * @return the response from the EPP server indicating the result of the domain renewal process
     */
    public EppCommandResponse renew(@NonNull final DomainRenewRequest renewRequest) {
        return sender.send(renewRequest);
    }

    /**
     * Transfers a domain.
     *
     * @param transferRequest the request containing the details of the domain transfer,
     *                        including the domain name, transfer operation type, and authentication code
     * @return the response from the EPP server indicating the result of the domain transfer process
     */
    public EppCommandResponse transfer(@NonNull final DomainTransferRequest transferRequest) {
        return sender.send(transferRequest);
    }

    /**
     * Deletes a domain.
     *
     * @param deleteRequest the request containing the details of the domain to be deleted, including its name
     * @return the response from the EPP server indicating the result of the domain deletion process
     */
    public EppCommandResponse delete(@NonNull final DomainDeleteRequest deleteRequest) {
        return sender.send(deleteRequest);
    }
}
