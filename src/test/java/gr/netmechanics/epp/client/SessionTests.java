package gr.netmechanics.epp.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.EppResultCodes;
import gr.netmechanics.epp.client.impl.commands.Hello;
import gr.netmechanics.epp.client.impl.commands.LoginRequest;
import gr.netmechanics.epp.client.impl.commands.LoginRequest.LoginRequestBuilder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(OrderAnnotation.class)
class SessionTests extends EppClientTestBase {

    @Test
    @Order(1)
    void test_hello_marshalling() throws Exception {
        assertThat(xmlUtil.toXml(new Hello()))
            .isEqualTo(xmlUtil.xmlFromFile("hello.xml"));
    }

    @Test
    @Order(2)
    void test_hello() {
        assertThat(eppClient.hello()).satisfies(greeting -> {
            assertThat(greeting).isNotNull();
            assertThat(greeting.getServerId()).isEqualTo(".gr and .ελ ccTLD EPP Service");
        });
    }

    @Test
    @Order(3)
    void test_login() {
        LoginRequest loginRequest = loginRequestBuilder().build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.login(loginRequest));
        assertThat(cmd.getSuccessfulResult().getCode()).isEqualTo(EppResultCodes.COMMAND_COMPLETED_SUCCESSFULLY);
        assertCommentEqual(cmd, "Καλώς ήλθατε στο Μητρώο.");
    }

    @Test
    @Order(4)
    void test_login_change_password() {
        String newPassword = "testPass123!@";

        LoginRequest loginRequest = loginRequestBuilder()
            .newPassword(newPassword)
            .build();

        EppCommandResponse cmd = assertCommandSuccess(eppClient.login(loginRequest));
        assertThat(cmd.getSuccessfulResult().getCode()).isEqualTo(EppResultCodes.COMMAND_COMPLETED_SUCCESSFULLY);
        assertCommentEqual(cmd, "Καλώς ήλθατε στο Μητρώο. Ο κωδικός πρόσβασης έχει αλλάξει.");

        // reset the original password after the test
        loginRequest = loginRequestBuilder()
            .password(newPassword)
            .newPassword(eppProps.getPassword())
            .build();

        cmd = assertCommandSuccess(eppClient.login(loginRequest));
        assertThat(cmd.getSuccessfulResult().getCode()).isEqualTo(EppResultCodes.COMMAND_COMPLETED_SUCCESSFULLY);
        assertCommentEqual(cmd, "Καλώς ήλθατε στο Μητρώο. Ο κωδικός πρόσβασης έχει αλλάξει.");
    }

    @Test
    @Order(5)
    void test_logout() {
        EppCommandResponse cmd = assertCommandSuccess(eppClient.logout());
        assertThat(cmd.getSuccessfulResult().getCode()).isEqualTo(EppResultCodes.COMMAND_COMPLETED_SUCCESSFULLY_ENDING_SESSION);
    }

    @Test
    @Order(6)
    void test_password_validation() {
        LoginRequestBuilder builder = loginRequestBuilder()
            .clientId("clientId")
            .version("1.0")
            .language("el")
            .objectUris(List.of("uri"));

        // Valid password
        builder.password("Valid123!@").build();

        // Too short
        assertThatThrownBy(() -> builder.password("Val1!").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("length");

        // Too long
        assertThatThrownBy(() -> builder.password("Valid1234567890!@#").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("length");

        // Missing lower
        assertThatThrownBy(() -> builder.password("VALID123!@").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("character from each group");

        // Missing upper
        assertThatThrownBy(() -> builder.password("valid123!@").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("character from each group");

        // Missing digit
        assertThatThrownBy(() -> builder.password("Valid!!!@").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("character from each group");

        // Missing special
        assertThatThrownBy(() -> builder.password("Valid1234").build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("character from each group");
    }

    private LoginRequestBuilder loginRequestBuilder() {
        return LoginRequest.builder()
            .clientId(eppProps.getClientId())
            .password(eppProps.getPassword())
            .language("el")
            .version("1.0")
            .objectUris(List.of(
                "urn:ietf:params:xml:ns:contact-1.0",
                "urn:ietf:params:xml:ns:domain-1.0",
                "urn:ietf:params:xml:ns:host-1.0"));
    }
}
