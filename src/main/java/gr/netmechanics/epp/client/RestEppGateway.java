package gr.netmechanics.epp.client;

import java.nio.charset.StandardCharsets;

import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.EppCommandRequest;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.commands.Hello;
import gr.netmechanics.epp.client.impl.elements.Greeting;
import gr.netmechanics.epp.client.xml.EppXmlCodec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class RestEppGateway implements EppGateway {

    private static final MediaType EPP_XML = new MediaType("application", "epp+xml", StandardCharsets.UTF_8);

    @Qualifier("eppRestClient")
    private final RestClient restClient;
    private final EppXmlCodec codec;
    private final ApplicationContext context;

    @Override
    public Greeting hello(final Hello hello) throws EppGatewayException {
        return exchange(hello, Greeting.class);
    }

    @Override
    public EppCommandResponse sendCommand(final EppCommandRequest command) throws EppGatewayException {
        return exchange(command, EppCommandResponse.class);
    }

    private <T> T exchange(final Object payload, final Class<T> responseType) {
        String requestXml = codec.marshal(payload);
        String url = context.getBean(EppPropertiesProvider.class).getUrl();

        try {
            String responseXml = restClient.post()
                .uri(url)
                .contentType(EPP_XML)
                .accept(EPP_XML)
                .body(requestXml)
                .retrieve()
                .body(String.class);

            return codec.unmarshal(responseXml, responseType);

        } catch (RestClientException e) {
            throw new EppGatewayException("Failed to communicate with the EPP server", e);
        }
    }
}
