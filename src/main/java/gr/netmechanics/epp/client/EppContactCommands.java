package gr.netmechanics.epp.client;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.check.contact.ContactCheckRequest;
import gr.netmechanics.epp.client.impl.commands.create.contact.ContactCreateRequest;
import gr.netmechanics.epp.client.impl.commands.info.contact.ContactInfoRequest;
import gr.netmechanics.epp.client.impl.commands.update.contact.ContactUpdateRequest;
import org.springframework.lang.NonNull;

/**
 * Contact commands (RFC3733), reached via {@link EppClient#contacts()}.
 */
public class EppContactCommands {

    private final EppCommandSender sender;

    EppContactCommands(final EppCommandSender sender) {
        this.sender = sender;
    }

    /**
     * Retrieves detailed information about a specific contact.
     *
     * @param infoRequest the request containing details of the contact for which information is being queried
     * @return the response from the EPP server containing the contact information
     */
    public EppCommandResponse info(@NonNull final ContactInfoRequest infoRequest) {
        return sender.send(infoRequest);
    }

    /**
     * Checks the availability of specified contact IDs against the EPP server.
     *
     * @param checkRequest the request containing the contact IDs to be checked for availability
     * @return the response from the EPP server indicating the availability of the specified contact IDs
     */
    public EppCommandResponse check(@NonNull final ContactCheckRequest checkRequest) {
        return sender.send(checkRequest);
    }

    /**
     * Creates a new contact.
     *
     * @param createRequest the request object containing the details of the contact to be created
     * @return the response from the EPP server indicating the result of the contact creation process
     */
    public EppCommandResponse create(@NonNull final ContactCreateRequest createRequest) {
        return sender.send(createRequest);
    }

    /**
     * Updates an existing contact.
     *
     * @param updateRequest the request containing the details of the changes to be made to the contact,
     *                      such as contact information modifications or status updates
     * @return the response from the EPP server indicating the result of the contact update process
     */
    public EppCommandResponse update(@NonNull final ContactUpdateRequest updateRequest) {
        return sender.send(updateRequest);
    }
}
