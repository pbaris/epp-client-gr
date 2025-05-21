package gr.netmechanics.epp.client;

import static gr.netmechanics.epp.client.EppConstants.BEAN_REQUEST_FLOW;
import static gr.netmechanics.epp.client.EppConstants.HTTP_REQUEST_CHANNEL;
import static gr.netmechanics.epp.client.EppConstants.HTTP_RESPONSE_CHANNEL;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import gr.netmechanics.epp.client.xml.ObjectToXMLTransformer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.integration.http.dsl.Http;
import org.springframework.stereotype.Service;

/**
 * @author Panos Bariamis (pbaris)
 */
@Service
@RequiredArgsConstructor
public class EppRequestFlowManager {

    private final XmlMapper xmlMapper;
    private final EppPropertiesProvider eppProps;
    private final IntegrationFlowContext flowContext;

    private IntegrationFlowRegistration flowRegistration;

    @PostConstruct
    private void init() {
        startFlow(eppProps);
    }

    private void startFlow(final EppPropertiesProvider eppProps) {
        IntegrationFlow flow = IntegrationFlow.from(HTTP_REQUEST_CHANNEL)
            .transform(new ObjectToXMLTransformer(xmlMapper))
            .handle(Http.outboundGateway(eppProps.getUrl())
                .httpMethod(HttpMethod.POST)
                .charset(StandardCharsets.UTF_8.name())
                .expectedResponseType(String.class))
            .channel(HTTP_RESPONSE_CHANNEL)
            .get();

        flowRegistration = flowContext.registration(flow)
            .id(BEAN_REQUEST_FLOW)
            .register();
        flowRegistration.start();
    }

    private void stopFlow() {
        if (flowRegistration != null) {
            flowRegistration.destroy();
            flowRegistration = null;
        }
    }

    public void restartFlow(final EppPropertiesProvider props) {
        stopFlow();
        startFlow(props);
    }
}
