import com.wavesplatform.wavesj.Transaction;
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static util.Node.runDockerNode;
import static util.actions.invoke.Arg.arg;

public class TestSuite {

    Node node;
    Account alice;
    Account bob;
    String assetId;

    @Before
    public void before() throws DockerException, InterruptedException, URISyntaxException, IOException, TimeoutException {
        node = runDockerNode(Version.TESTNET);
        alice = new Account("alice", node);
        bob = new Account("bob", node);

        node.rich.transfers(1000_00000000L).to(alice).successfully();

        assetId = alice.issues("Rude Asset").withDecimals(0).reissuable().successfully().getId().toString();
        alice.setsScript("dapp.ride").successfully();
    }

    @Test
    public void test0() {
        /*
        * alice.invokes().defaultFunction().butGetsError();
        * invokeScriptTx.result() shouldHave WriteSet([]);
        *
        * alice().reissues(); alice.burns(); alice.exchanges();
        * alice.leases(); alice.cancelsLease(); alice.createsAlias(); alice.massTransfers();
        * alice.writesData(); alice.sponsors(); alice.setsAssetScript();
        * alice.placesOrder(); alice.cancelsOrder();
        */
    }

    @Test
    public void dAppCanCreateGenesis() throws IOException, TimeoutException {
        Transaction invoke = alice.invokes().function("genesis", arg(assetId)).withFee(900_000).successfully();

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
