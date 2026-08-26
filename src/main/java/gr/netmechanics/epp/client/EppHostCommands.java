package gr.netmechanics.epp.client;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.check.host.HostCheckRequest;
import gr.netmechanics.epp.client.impl.commands.create.host.HostCreateRequest;
import gr.netmechanics.epp.client.impl.commands.delete.host.HostDeleteRequest;
import gr.netmechanics.epp.client.impl.commands.info.host.HostInfoRequest;
import gr.netmechanics.epp.client.impl.commands.update.host.HostUpdateRequest;
import org.springframework.lang.NonNull;

/**
 * Host commands (RFC3732), reached via {@link EppClient#hosts()}.
 */
public class EppHostCommands {

    private final EppCommandSender sender;

    EppHostCommands(final EppCommandSender sender) {
        this.sender = sender;
    }

    /**
     * Retrieves detailed information about a specific host.
     *
     * @param infoRequest the request containing details of the host for which information is being queried
     * @return the response from the EPP server containing the host information
     */
    public EppCommandResponse info(@NonNull final HostInfoRequest infoRequest) {
        return sender.send(infoRequest);
    }

    /**
     * Checks the availability of specified hostnames against the EPP server.
     *
     * @param checkRequest the request containing the hostnames to be checked for availability
     * @return the response from the EPP server indicating the availability of the specified hostnames
     */
    public EppCommandResponse check(@NonNull final HostCheckRequest checkRequest) {
        return sender.send(checkRequest);
    }

    /**
     * Creates a new host.
     *
     * @param createRequest the request object containing the details of the host to be created,
     *                      including its name and optional IP addresses
     * @return the response from the EPP server indicating the result of the host creation process
     */
    public EppCommandResponse create(@NonNull final HostCreateRequest createRequest) {
        return sender.send(createRequest);
    }

    /**
     * Updates an existing host.
     *
     * @param updateRequest the request containing the details of the changes to be made to the host,
     *                      such as name modifications and IP address adjustment, or status updates
     * @return the response from the EPP server indicating the result of the host update process
     */
    public EppCommandResponse update(@NonNull final HostUpdateRequest updateRequest) {
        return sender.send(updateRequest);
    }

    /**
     * Deletes a host.
     *
     * @param deleteRequest the request containing the details of the host to be deleted, including its name
     * @return the response from the EPP server indicating the result of the host deletion process
     */
    public EppCommandResponse delete(@NonNull final HostDeleteRequest deleteRequest) {
        return sender.send(deleteRequest);
    }
}
