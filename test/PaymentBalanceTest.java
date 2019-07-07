import lib.Account;
import lib.Node;
import lib.Version;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static lib.Node.runDockerNode;
import static lib.actions.invoke.Arg.arg;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentBalanceTest {

    private Node node;
    private Account alice, bob, carol;
    private String assetId;

    @BeforeEach
    void before() throws IOException {
        node = runDockerNode(Version.TESTNET);

        alice = new Account(node, 10_00000000L);
        bob = new Account(node, 1_00000000L);
        carol = new Account(node);

        alice.setsScript("payment-balance.ride").successfully();
    }

    @Test
    void paymentIsPartOfDAppBalance() throws IOException {
        assetId = alice.issues("Asset").withQuantity(1500).withDecimals(0).successfully().getId().toString();
        alice.transfers(500, assetId).to(bob).successfully();

        bob.invokes(i -> i.dApp(alice).function("some", arg(carol.address())).withPayment(500, assetId));

        assertAll("data and balances",
                () -> assertEquals(1000, alice.dataInt("own")),
                () -> assertEquals(500, alice.dataInt("caller")),
                () -> assertEquals(0, alice.dataInt("recipient")),

                () -> assertEquals(1500, carol.assetBalance(assetId))
        );
    }

    @AfterEach
    void after() {
        node.stopDockerNode();
    }

}
