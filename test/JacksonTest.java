import com.spotify.docker.client.exceptions.DockerException;
import lib.Account;
import lib.Node;
import lib.Version;
import lib.api.StateChanges;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static lib.Node.runDockerNode;
import static lib.actions.invoke.Arg.arg;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JacksonTest {

    private Node node;
    private Account alice;

    @Before
    public void before() throws DockerException, InterruptedException, URISyntaxException, IOException, TimeoutException {
        node = runDockerNode(Version.TESTNET);
        alice = new Account("alice", node, 1000_00000000L);

        alice.setsScript("jackson.ride").successfully();
    }

    @Test
    public void test() throws IOException, TimeoutException {
        String invokeId = alice.invokes()
                .function("some", arg("Hello!".getBytes()), arg(true), arg(1000), arg("some"))
                .withFee(900000).successfully().getId().toString();

        StateChanges changes = node.stateChanges(invokeId);
        assertThat(changes.data.size(), is(4));
    }

    @After
    public void after() throws DockerException, InterruptedException {
        node.stopDockerNode();
    }

}
