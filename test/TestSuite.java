import util.Version;
import util.Node;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction.FunctionCall;
import com.wavesplatform.wavesj.transactions.InvokeScriptTransaction.Payment;
import com.wavesplatform.wavesj.transactions.TransferTransaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.wavesplatform.wavesj.ByteString.EMPTY;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.Node.runDockerNode;

public class TestSuite {

    Node node;

    @Before
    public void before() throws DockerException, InterruptedException, URISyntaxException {
        node = runDockerNode(Version.TESTNET);
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
        * alice.transfers(); alice().reissues(); alice.burns(); alice.exchanges();
        * alice.leases(); alice.cancelsLease(); alice.createsAlias(); alice.massTransfers();
        * alice.writesData(); alice.sponsors(); alice.setsAssetScript();
        * alice.placesOrder(); alice.cancelsOrder();
        *
        * alice.data(); alice.data("").asInt();
        */
    }

    @Test
    public void test1() throws IOException, TimeoutException {
        PrivateKeyAccount rich = PrivateKeyAccount.fromSeed("rich", 0, (byte) 'R');
        PrivateKeyAccount alice = PrivateKeyAccount.fromSeed("alice", 0, (byte) 'R');

        Transaction transfer = waitForTransaction(node.transfer(
                rich, alice.getAddress(), 1000_00000000L, 100000, EMPTY));

        Transaction setScript = waitForTransaction(node.setScript(alice,
                new String(Files.readAllBytes(Paths.get("dapp.ride"))), (byte) 'R', 1000000));

        //TODO ничо не сделать до генезиса

        Transaction genesis = waitForTransaction(node.send(new InvokeScriptTransaction((byte) 'R',
                alice, alice.getAddress(), new FunctionCall("genesis"), new ArrayList<>(),
                500000, "WAVES", System.currentTimeMillis(), new ArrayList<>()).sign(alice)));

        assertEquals(node.getData(alice.getAddress()).size(), 5);
        assertTrue(((String) node.getDataByKey(alice.getAddress(), "last").getValue()).startsWith(","));
        assertEquals(((long) node.getDataByKey(alice.getAddress(), "height").getValue()), 0);
        assertEquals(((long) node.getDataByKey(alice.getAddress(), "base").getValue()), 1);
        assertEquals(node.getDataByKey(alice.getAddress(), "utx").getValue(), "");
        assertEquals(((long) node.getDataByKey(alice.getAddress(), "utx-size").getValue()), 0);
    }

    Transaction waitForTransaction(String id) throws TimeoutException {
        for (int repeat = 0; repeat < 20; repeat++) {
            try {
                return node.getTransaction(id);
            } catch (IOException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignore) {}
            }
        }
        throw new TimeoutException("Could not wait for transaction " + id + " in 10 seconds");
    }



    @After
    public void after() throws DockerException, InterruptedException {
        docker.killContainer(containerId);
        docker.removeContainer(containerId);
        docker.close();
    }

}
