package gr.netmechanics.epp.client;

import static gr.netmechanics.epp.client.EppConstants.HTTP_REQUEST_CHANNEL;

import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.EppCommandRequest;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.Hello;
import gr.netmechanics.epp.client.impl.elements.Greeting;
import org.springframework.integration.annotation.MessagingGateway;

/**
 * @author Panos Bariamis (pbaris)
 */
@MessagingGateway(defaultRequestChannel = HTTP_REQUEST_CHANNEL)
public interface EppGateway {

    /**
     * Sends a Hello message to the EPP server and receives a server greeting.
     *
     * @param hello the Hello message to be sent to the server
     * @return the server's greeting response
     * @throws EppGatewayException if there is an issue during communication with the EPP server
     */
    Greeting hello(Hello hello) throws EppGatewayException;

    /**
     * Sends an EPP command to the server and retrieves the corresponding response.
     *
     * @param command the EPP command to be sent
     * @return the server's response to the provided command
     * @throws EppGatewayException if there is an issue during communication or processing of the command
     */
    EppCommandResponse sendCommand(EppCommandRequest command) throws EppGatewayException;
}
