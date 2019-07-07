import lib.Account;
import lib.Node;
import lib.Version;
import lib.api.exceptions.ApiError;
import lib.exceptions.NodeError;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static lib.Node.connectToNode;
import static lib.Node.runDockerNode;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
class ApiErrorTest {

    private Node node;
    private Account alice;
    private String assetId;

    @BeforeAll
    void before() throws IOException {
        node = runDockerNode(Version.MAINNET);

        alice = new Account(node, 10_00000000L);

        assetId = alice.issues("Asset").successfully().getId().toString();
    }

    @AfterAll
    void after() {
        node.stopDockerNode();
    }

    @Test
    void a() {
        assertEquals("Asset", node.api.assetDetails(assetId).name);
    }

    @Test
    void b() {
        ApiError e = assertThrows(ApiError.class, () ->
                System.out.println("result -> " + node.api.assetDetails("r3r3r3").name)
        );
        assertAll("error fields",
                () -> assertEquals(199, e.error),
                () -> assertEquals("Failed to find issue transaction by ID", e.message)
        );
    }

    @Test
    void c() {
        Node unexistedNode = connectToNode("http://localhost:9999/", 'U');

        NodeError e = assertThrows(NodeError.class, () ->
                unexistedNode.api.assetDetails(assetId)
        );
        assertTrue(e.getMessage().contains("Failed to connect to"));
    }

}
