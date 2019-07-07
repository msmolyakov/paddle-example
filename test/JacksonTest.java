import lib.Account;
import lib.Node;
import lib.Version;
import lib.api.StateChanges;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static lib.Node.runDockerNode;
import static lib.actions.invoke.Arg.arg;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonTest {

    private Node node;
    private Account alice;

    @BeforeEach
    void before() throws IOException {
        node = runDockerNode(Version.TESTNET);
        alice = new Account("alice", node, 1000_00000000L);

        alice.setsScript("jackson.ride").successfully();
    }

    @Test
    void test() throws IOException {
        String invokeId = alice.invokes()
                .function("some", arg("Hello!".getBytes()), arg(true), arg(1000), arg("some"))
                .withFee(900000).successfully().getId().toString();

        StateChanges changes = node.api.stateChanges(invokeId);
        assertEquals(4, changes.data.size());
    }

    @AfterEach
    void after() {
        node.stopDockerNode();
    }

}
