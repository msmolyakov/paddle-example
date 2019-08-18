import com.wavesplatform.wavesj.Transaction;
import im.mak.paddle.Account;
import im.mak.paddle.DockerNode;
import im.mak.paddle.exceptions.NodeError;
import org.junit.jupiter.api.*;

import static im.mak.paddle.Async.async;
import static im.mak.paddle.actions.invoke.Arg.arg;
import static im.mak.paddle.util.Script.fromFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.MethodOrderer.Alphanumeric;

@TestMethodOrder(Alphanumeric.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WalletTest {

    private DockerNode node;
    private Account alice, bob, carol;
    private String assetId;

    @BeforeAll
    void before() {
        node = new DockerNode();

        async(
                () -> {
                    alice = new Account(node, 1_00000000L);
                    alice.setsScript(s -> s.script(fromFile("wallet.ride")));
                },
                () -> {
                    bob = new Account(node, 2_00000000L);
                    assetId = bob.issues(a -> a.quantity(1000).decimals(0)).getId().toString();
                },
                () -> carol = new Account(node, 1_00000000L)
        );
    }

    @AfterAll
    void after() {
        node.shutdown();
    }

    @Test
    void a_canDepositWaves() {
        long aliceInitBalance = alice.balance();
        long amount = 100;

        bob.invokes(i -> i.dApp(alice).function("deposit").wavesPayment(amount));

        assertAll("data and balances",
                () -> assertThat(alice.data().size()).isEqualTo(1),
                () -> assertThat(alice.dataInt(bob.address())).isEqualTo(amount),

                () -> assertThat(alice.balance()).isEqualTo(aliceInitBalance + amount)
        );
    }

    @Test
    void b_cantDepositAsset() {
        NodeError error = assertThrows(NodeError.class, () -> {
            bob.invokes(i -> i.dApp(alice).function("deposit").payment(500, assetId));
        });
        assertThat(error).hasMessageContaining("can hodl waves only at the moment");
    }

    @Test
    void c_canDepositWavesTwice() {
        long prevDeposit = alice.dataInt(bob.address());
        long amount = 50;

        bob.invokes(i -> i.dApp(alice).function("deposit").wavesPayment(amount));

        assertAll("data",
                () -> assertThat(alice.data().size()).isEqualTo(1),
                () -> assertThat(alice.dataInt(bob.address())).isEqualTo(prevDeposit + amount)
        );
    }

    @Test
    void d_accountsStoredSeparately() {
        long bobDeposit = alice.dataInt(bob.address());
        long amount = 20;

        carol.invokes(i -> i.dApp(alice).function("deposit").wavesPayment(amount));

        assertAll("data",
                () -> assertThat(alice.data().size()).isEqualTo(2),
                () -> assertThat(alice.dataInt(bob.address())).isEqualTo(bobDeposit),
                () -> assertThat(alice.dataInt(carol.address())).isEqualTo(amount)
        );
    }

    @Test
    void e_cantWithdrawNegativeAmount() {
        NodeError error = assertThrows(NodeError.class, () -> {
            bob.invokes(i -> i.dApp(alice).function("withdraw", arg(-1)));
        });
        assertThat(error).hasMessageContaining("Can't withdraw negative amount");
    }

    @Test
    void f_cantWithdrawMoreThanHodled() {
        NodeError error = assertThrows(NodeError.class, () -> {
            bob.invokes(i -> i.dApp(alice).function("withdraw", arg(151)));
        });
        assertThat(error).hasMessageContaining("Not enough balance");
    }

    @Test
    void g_canWithdrawPartially() {
        long aliceInitBalance = alice.balance();
        long bobInitBalance = bob.balance();
        long bobDeposit = alice.dataInt(bob.address());
        long carolDeposit = alice.dataInt(carol.address());
        long amount = 1;

        Transaction invoke = bob.invokes(i -> i.dApp(alice).function("withdraw", arg(amount)));

        assertAll("data and balances",
                () -> assertThat(alice.data().size()).isEqualTo(2),
                () -> assertThat(alice.dataInt(bob.address())).isEqualTo(bobDeposit - amount),
                () -> assertThat(alice.dataInt(carol.address())).isEqualTo(carolDeposit),

                () -> assertThat(alice.balance()).isEqualTo(aliceInitBalance - amount),
                () -> assertThat(bob.balance()).isEqualTo(bobInitBalance + amount - invoke.getFee())
        );
    }

    @Test
    void h_canWithdrawAllOwnHodl() {
        long aliceInitBalance = alice.balance();
        long bobInitBalance = bob.balance();
        long amount = alice.dataInt(bob.address());

        Transaction invoke = bob.invokes(i -> i.dApp(alice).function("withdraw", arg(amount)));

        assertAll("data and balances",
                () -> assertThat(alice.data().size()).isEqualTo(2),
                () -> assertThat(alice.dataInt(bob.address())).isEqualTo(0),

                () -> assertThat(alice.balance()).isEqualTo(aliceInitBalance - amount),
                () -> assertThat(bob.balance()).isEqualTo(bobInitBalance + amount - invoke.getFee())
        );
    }

}
