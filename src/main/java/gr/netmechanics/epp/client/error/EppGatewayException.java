package gr.netmechanics.epp.client.error;

/**
 * @author Panos Bariamis (pbaris)
 */
public class EppGatewayException extends RuntimeException {

    public EppGatewayException(final String message) {
        super(message);
    }

    public EppGatewayException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
