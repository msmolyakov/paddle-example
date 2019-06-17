import com.spotify.docker.client.exceptions.DockerException;
import lib.Account;
import lib.Node;
import lib.Version;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static lib.Node.runDockerNode;
import static lib.actions.invoke.Arg.arg;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentBalanceTest {

    private Node node;
    private Account alice, bob, carol;
    private String assetId;

    @BeforeEach
    void before() throws DockerException, InterruptedException, URISyntaxException, IOException, TimeoutException {
        node = runDockerNode(Version.TESTNET);

        alice = new Account(node, 10_00000000L);
        bob = new Account(node, 1_00000000L);
        carol = new Account(node);

        alice.setsScript("payment-balance.ride").successfully();
    }

    @Test
    void paymentIsPartOfDAppBalance() throws IOException, TimeoutException {
        assetId = alice.issues("Asset").withQuantity(1500).withDecimals(0).successfully().getId().toString();
        alice.transfers(500, assetId).to(bob).successfully();

        bob.invokes(alice).function("some", arg(carol.address())).withPayment(500, assetId).successfully();

        assertAll("data and balances",
                () -> assertEquals(1000, alice.dataInt("own")),
                () -> assertEquals(500, alice.dataInt("caller")),
                () -> assertEquals(0, alice.dataInt("recipient")),

                () -> assertEquals(1500, carol.assetBalance(assetId))
        );
    }

    @AfterEach
    void after() throws DockerException, InterruptedException {
        node.stopDockerNode();
    }

}
