import com.spotify.docker.client.exceptions.DockerException;
import lib.Account;
import lib.Node;
import lib.Version;
import lib.api.exceptions.NodeError;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static lib.Node.runDockerNode;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BurnTest {

    private Node node;
    private Account alice;

    @BeforeEach
    void before() throws DockerException, InterruptedException, URISyntaxException, IOException, TimeoutException {
        node = runDockerNode(Version.MAINNET);

        alice = new Account(node, 10_00000000L);
    }

    @AfterEach
    void after() throws DockerException, InterruptedException {
        node.stopDockerNode();
    }

    @Test
    void a() throws IOException, TimeoutException {
        String asset =
                alice.issues("Asset").successfully()
                        .getId().toString();

        System.out.println("result -> " + node.assetDetails(asset).name);

        assertThrows(NodeError.class, () ->
                System.out.println("result -> " + node.assetDetails("r3r3r3").name)
        );
    }

}
