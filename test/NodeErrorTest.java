import com.spotify.docker.client.exceptions.DockerException;
import lib.Account;
import lib.Node;
import lib.Version;
import lib.api.exceptions.NodeError;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static lib.Node.connectToNode;
import static lib.Node.runDockerNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
class NodeErrorTest {

    private Node node;
    private Account alice;
    private String assetId;

    @BeforeAll
    void before() throws DockerException, InterruptedException, URISyntaxException, IOException, TimeoutException {
        node = runDockerNode(Version.MAINNET);

        alice = new Account(node, 10_00000000L);

        assetId = alice.issues("Asset").successfully().getId().toString();
    }

    @AfterAll
    void after() throws DockerException, InterruptedException {
        node.stopDockerNode();
    }

    @Test
    void a() throws IOException {
        assertEquals("Asset", node.assetDetails(assetId).name);
    }

    @Test
    void b() {
        assertThrows(NodeError.class, () ->
                System.out.println("result -> " + node.assetDetails("r3r3r3").name)
        );
    }

    @Test
    void c() throws TimeoutException, IOException, URISyntaxException {
        Node unexistedNode = connectToNode("http://localhost:9999/", 'U');

        assertThrows(NodeError.class, () ->
            unexistedNode.assetDetails(assetId)
        );
    }

}
