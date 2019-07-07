import com.wavesplatform.wavesj.Transaction;
import lib.Account;
import lib.Node;
import lib.Version;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static lib.Node.runDockerNode;
import static lib.actions.invoke.Arg.arg;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.MethodOrderer.Alphanumeric;

@TestMethodOrder(Alphanumeric.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestSuite {

    private Node node;
    private Account alice, bob, carol;

    @BeforeAll
    void before() throws IOException {
        node = runDockerNode(Version.MAINNET);

        alice = new Account(node, 1_00000000L);
        bob = new Account(node, 1_00000000L);
        carol = new Account(node, 1_00000000L);

        alice.setsScript(s -> s.script("wallet.ride"));
    }

    @AfterAll
    void after() {
        node.stopDockerNode();
    }

    @Nested
    @TestMethodOrder(Alphanumeric.class)
    class Positive {

        @Test
        void a_canDepositWaves() {
            long aliceInitBalance = alice.balance();
            long amount = 100;

            bob.invokes(i -> i.dApp(alice).function("deposit").withWavesPayment(amount));

            assertAll("data and balances",
                    () -> assertEquals(1, alice.data().size()),
                    () -> assertEquals(amount, alice.dataInt(bob.address())),

                    () -> assertEquals(aliceInitBalance + amount, alice.balance())
            );
        }

        @Test
        void b_canDepositWavesTwice() {
            long prevDeposit = alice.dataInt(bob.address());
            long amount = 50;

            bob.invokes(i -> i.dApp(alice).function("deposit").withWavesPayment(amount));

            assertAll("data",
                    () -> assertEquals(1, alice.data().size()),
                    () -> assertEquals(prevDeposit + amount, alice.dataInt(bob.address()))
            );
        }

        @Test
        void c_accountsStoredSeparately() {
            long bobDeposit = alice.dataInt(bob.address());
            long amount = 20;

            carol.invokes(i -> i.dApp(alice).function("deposit").withWavesPayment(amount));

            assertAll("data",
                    () -> assertEquals(2, alice.data().size()),
                    () -> assertEquals(bobDeposit, alice.dataInt(bob.address())),
                    () -> assertEquals(amount, alice.dataInt(carol.address()))
            );
        }

        @Test
        void d_canWithdrawPartially() {
            long aliceInitBalance = alice.balance();
            long bobInitBalance = bob.balance();
            long bobDeposit = alice.dataInt(bob.address());
            long carolDeposit = alice.dataInt(carol.address());
            long amount = 1;

            Transaction invoke = bob.invokes(i -> i.dApp(alice).function("withdraw", arg(amount)));

            assertAll("data and balances",
                    () -> assertEquals(2, alice.data().size()),
                    () -> assertEquals(bobDeposit - amount, alice.dataInt(bob.address())),
                    () -> assertEquals(carolDeposit, alice.dataInt(carol.address())),

                    () -> assertEquals(aliceInitBalance - amount, alice.balance()),
                    () -> assertEquals(bobInitBalance + amount - invoke.getFee(), bob.balance())
            );
        }

        @Test
        void e_canWithdrawAll() {
            long aliceInitBalance = alice.balance();
            long bobInitBalance = bob.balance();
            long amount = alice.dataInt(bob.address());

            Transaction invoke = bob.invokes(i -> i.dApp(alice).function("withdraw", arg(amount)));

            assertAll("data and balances",
                    () -> assertEquals(2, alice.data().size()),
                    () -> assertEquals(0, alice.dataInt(bob.address())),

                    () -> assertEquals(aliceInitBalance - amount, alice.balance()),
                    () -> assertEquals(bobInitBalance + amount - invoke.getFee(), bob.balance())
            );
        }

    }

}
