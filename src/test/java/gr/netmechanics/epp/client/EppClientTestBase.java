package gr.netmechanics.epp.client;

import static org.assertj.core.api.Assertions.assertThat;

import gr.netmechanics.epp.client.config.EppClientAutoConfiguration;
import gr.netmechanics.epp.client.config.TestHelperConfiguration;
import gr.netmechanics.epp.client.impl.EppCommandResponse;
import gr.netmechanics.epp.client.impl.EppExtension;
import gr.netmechanics.epp.client.util.XmlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Panos Bariamis (pbaris)
 */
@SpringBootTest(classes = {TestHelperConfiguration.class, EppClientAutoConfiguration.class})
public abstract class EppClientTestBase {

    @Autowired
    protected XmlUtil xmlUtil;

    @Autowired
    protected EppPropertiesProvider eppProps;

    @Autowired
    protected EppClient eppClient;

    protected EppCommandResponse assertCommandSuccess(final EppCommandResponse cmd) {
        assertThat(cmd).isNotNull();
        assertThat(cmd.isSuccess()).isTrue();
        return cmd;
    }

    protected EppCommandResponse assertCommandFail(final EppCommandResponse cmd) {
        assertThat(cmd).isNotNull();
        assertThat(cmd.isSuccess()).isFalse();
        return cmd;
    }

    protected <T extends EppExtension> void assertCommentEqual(final EppCommandResponse cmd, final String comment) {
        assertThat(cmd.<T>getExtension()).satisfies(ext -> {
            assertThat(ext).isNotNull();
            assertThat(ext.getComments()).satisfies(comments -> {
                assertThat(comments).isNotNull().hasSize(1);
                assertThat(comments.getFirst().getText()).isEqualTo(comment);
            });
        });
    }
}
