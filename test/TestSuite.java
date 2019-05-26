import util.Account;
import util.Version;
import util.Node;
import com.spotify.docker.client.exceptions.DockerException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.Node.runDockerNode;
import static util.actions.invoke.Arg.arg;

public class TestSuite {

    Node node;
    Account rich;
    Account alice;
    Account bob;

    @Before
    public void before() throws DockerException, InterruptedException, URISyntaxException {
        node = runDockerNode(Version.TESTNET);
        rich = new Account("rich", node);
        alice = new Account("alice", node);
        bob = new Account("bob", node);
    }

    @Test
    public void test0() {
        /*
        * Node node = new Node(host, chainId);
        * Account alice = new Account("seed text");
        * alice
        *   .uses(node);
        *   .hasWavesBalance(10_00000000)
        *   .setsScript("dapp.ride");
        *
        * alice.invokes().defaultFunction().butGetsError();
        * InvokeScriptTx some = alice.invokes().function("some").withArg(INT, 1).successfully();
        * some.result() shouldHave WriteSet([]);
        *
        * alice.issues("Scam Asset").withDecimals(0).successfully();
        * alice().reissues(); alice.burns(); alice.exchanges();
        * alice.leases(); alice.cancelsLease(); alice.createsAlias(); alice.massTransfers();
        * alice.writesData(); alice.sponsors(); alice.setsAssetScript();
        * alice.placesOrder(); alice.cancelsOrder();
        *
        * alice.data(); alice.data("").asInt();
        */
    }

    @Test
    public void test1() throws IOException, TimeoutException {
        rich.transfers(1000_00000000L).to(alice).successfully();

        String assetId =
                alice.issues("Rude Asset").withDecimals(0).reissuable().successfully()
                        .getId().toString();
        alice.setsScript("dapp.ride").successfully();

        //TODO ничо не сделать до генезиса

        alice.invokes().function("genesis", arg(assetId)).successfully();

        assertEquals(alice.data().size(), 5);
        assertTrue(alice.dataStr("last").startsWith(","));
        assertEquals(alice.dataInt("height"), 0);
        assertEquals(alice.dataInt("base"), 1);
        assertEquals(alice.dataStr("utx"), "");
        assertEquals(alice.dataInt("utx-size"), 0);
    }

    @After
    public void after() throws DockerException, InterruptedException {
        node.stopDockerNode();
    }

}
