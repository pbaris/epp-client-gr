package gr.netmechanics.epp.client;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.info.registrar.RegistrarInfoRequest;
import org.springframework.lang.NonNull;

/**
 * Registrar commands, reached via {@link EppClient#registrar()}.
 * Every command returns {@code null} if no EPP session could be established with the server.
 */
public class EppRegistrarCommands {

    private final EppCommandSender sender;

    EppRegistrarCommands(final EppCommandSender sender) {
        this.sender = sender;
    }

    /**
     * Retrieves detailed information about the registrar account.
     *
     * @param infoRequest the request for which registrar information is being queried
     * @return the response from the EPP server containing the registrar information
     */
    public EppCommandResponse info(@NonNull final RegistrarInfoRequest infoRequest) {
        return sender.send(infoRequest);
    }
}
