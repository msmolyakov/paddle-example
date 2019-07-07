import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction;
import lib.Account;
import lib.Node;
import lib.Version;
import lib.api.StateChanges;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static lib.Node.runDockerNode;
import static lib.actions.invoke.Arg.arg;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JacksonTest {

    private Node node;
    private Account alice;

    @BeforeEach
    void before() {
        node = runDockerNode(Version.TESTNET);
        alice = new Account("alice", node, 1000_00000000L);

        alice.setsScript(s -> s.script("jackson.ride"));
    }

    @Test
    void test() {
        InvokeScriptTransaction tx = alice.invokes(i ->
                i.function("some", arg("Hello!".getBytes()), arg(true), arg(1000), arg("some"))
                .fee(900000));

        StateChanges changes = node.api.stateChanges(tx.getId().toString());
        assertEquals(4, changes.data.size());
    }

    @AfterEach
    void after() {
        node.stopDockerNode();
    }

}
