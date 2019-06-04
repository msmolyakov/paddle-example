import com.wavesplatform.wavesj.Transaction;
import lib.Account;
import lib.Version;
import lib.Node;
import com.spotify.docker.client.exceptions.DockerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static lib.Node.runDockerNode;
import static lib.actions.invoke.Arg.arg;

public class TestSuite {

    Node node;
    Account alice;
    Account bob;
    String assetId;

    @Before
    public void before() throws DockerException, InterruptedException, URISyntaxException, IOException, TimeoutException {
        node = runDockerNode(Version.TESTNET);
        alice = new Account("alice", node, 1000_00000000L);
        bob = new Account("bob", node, 100_00000000L);

        assetId = alice.issues("Rude Asset").withDecimals(0).reissuable().successfully().getId().toString();
        alice.setsScript("dapp.ride").successfully();
    }

    @Test
    public void test0() {
        /*
        * invokeScriptTx.result() shouldHave WriteSet([]);
        * alice.exchanges();
        * alice.placesOrder(); alice.cancelsOrder();
        */
    }

    @Test
    public void dAppCanCreateGenesis() throws IOException, TimeoutException {
        Transaction invoke = alice.invokes().function("genesis", arg(assetId)).withFee(900000).successfully();

        assertThat(alice.data(), hasSize(7));
        assertThat(alice.dataStr("assetId"), is(assetId));
        assertThat(alice.dataStr("last"), not(emptyString()));
        assertThat(alice.dataInt("height"), is(0L));
        assertThat(alice.dataStr("utx"), emptyString());
        assertThat(alice.dataInt("utx-size"), is(0L));
        assertThat(alice.dataStr("$" + alice.wavesAccount.getAddress()), is("dapp"));
//TODO getHeight bug        assertThat(alice.dataStr("@dapp"), is(alice.wavesAccount.getAddress() + "," + invoke.getHeight() + ",0,0"));
    }

    @After
    public void after() throws DockerException, InterruptedException {
        node.stopDockerNode();
    }

}
