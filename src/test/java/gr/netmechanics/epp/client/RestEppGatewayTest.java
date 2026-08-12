package gr.netmechanics.epp.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gr.netmechanics.epp.client.error.EppGatewayException;
import gr.netmechanics.epp.client.impl.commands.Hello;
import gr.netmechanics.epp.client.impl.elements.Greeting;
import gr.netmechanics.epp.client.xml.EppXmlCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class RestEppGatewayTest {

    private static final String EPP_URL = "http://localhost/epp/proxy";

    private static final String GREETING_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
        + "<epp xmlns=\"urn:ietf:params:xml:ns:epp-1.0\">"
        + "<greeting>"
        + "<svID>.gr and .ελ ccTLD EPP Service</svID>"
        + "<svDate>2025-04-25T09:41:55.429Z</svDate>"
        + "<svcMenu>"
        + "<version>1.0</version>"
        + "<lang>en</lang>"
        + "<lang>el</lang>"
        + "<objURI>urn:ietf:params:xml:ns:host-1.0</objURI>"
        + "<objURI>urn:ietf:params:xml:ns:contact-1.0</objURI>"
        + "<objURI>urn:ietf:params:xml:ns:domain-1.0</objURI>"
        + "</svcMenu>"
        + "</greeting>"
        + "</epp>";

    private MockRestServiceServer mockServer;
    private RestEppGateway gateway;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();

        StaticApplicationContext context = new StaticApplicationContext();
        context.registerBean(EppPropertiesProvider.class, RestEppGatewayTest::fixedUrlPropertiesProvider);
        context.refresh();

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule());
        EppXmlCodec codec = new EppXmlCodec(xmlMapper);
        gateway = new RestEppGateway(restClientBuilder.build(), codec, context);
    }

    @Test
    void hello_sends_epp_content_type_and_parses_greeting_response() {
        mockServer.expect(requestTo(EPP_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Content-Type", "application/epp+xml;charset=UTF-8"))
            .andRespond(withSuccess(GREETING_XML, MediaType.valueOf("application/epp+xml;charset=UTF-8")));

        Greeting greeting = gateway.hello(new Hello());

        assertThat(greeting.getServerId()).isEqualTo(".gr and .ελ ccTLD EPP Service");
        assertThat(greeting.getLanguages()).containsExactly("en", "el");
        assertThat(greeting.getObjectUris()).contains(
            "urn:ietf:params:xml:ns:host-1.0",
            "urn:ietf:params:xml:ns:contact-1.0",
            "urn:ietf:params:xml:ns:domain-1.0");
        assertThat(greeting.getVersion()).isEqualTo("1.0");

        mockServer.verify();
    }

    @Test
    void hello_wraps_server_error_in_epp_gateway_exception() {
        mockServer.expect(requestTo(EPP_URL))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withServerError());

        assertThatThrownBy(() -> gateway.hello(new Hello()))
            .isInstanceOf(EppGatewayException.class);

        mockServer.verify();
    }

    private static EppPropertiesProvider fixedUrlPropertiesProvider() {
        return new EppPropertiesProvider() {
            @Override
            public boolean isUseSandbox() {
                return true;
            }

            @Override
            public String getClientId() {
                return "client";
            }

            @Override
            public String getPassword() {
                return "password";
            }

            @Override
            public String getLanguage() {
                return "en";
            }

            @Override
            public Long getClTrId() {
                return 1L;
            }

            @Override
            public String getUrl() {
                return EPP_URL;
            }
        };
    }
}
