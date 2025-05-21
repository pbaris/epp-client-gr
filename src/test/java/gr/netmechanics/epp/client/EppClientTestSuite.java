package gr.netmechanics.epp.client;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * @author Panos Bariamis (pbaris)
 */
@Suite
@SuiteDisplayName("EPP Client Tests")
@SelectClasses({
    SessionTests.class,
    ContactTests.class,
    HostTests.class,
    DomainTests.class,
    RefreshEppTest.class
})
//@IncludeTags("run-this")
public class EppClientTestSuite {
}
