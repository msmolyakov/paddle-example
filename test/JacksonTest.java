import com.spotify.docker.client.exceptions.DockerException;
import lib.Account;
import lib.Node;
import lib.Version;
import lib.api.NodeApi;
import lib.api.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static lib.Node.runDockerNode;
import static lib.actions.invoke.Arg.arg;

public class JacksonTest {

    Node node;
    Account alice;

    @Before
    public void before() throws DockerException, InterruptedException, URISyntaxException, IOException, TimeoutException {
        node = runDockerNode(Version.TESTNET);
        alice = new Account("alice", node, 1000_00000000L);

        alice.setsScript("jackson.ride").successfully();
    }

    @Test
    public void test() throws IOException, TimeoutException {
        String invokeId = alice.invokes()
                .function("some", arg("SGVsbG8h"), arg(true), arg(1000), arg("some"))
                .withFee(900000).successfully().getId().toString();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:6869/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        NodeApi nodeApi = retrofit.create(NodeApi.class);

        Transaction tx = nodeApi.stateChanges(invokeId).execute().body();
        System.out.println(tx);
    }

    @After
    public void after() throws DockerException, InterruptedException {
        node.stopDockerNode();
    }

}
