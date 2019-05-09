import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import com.wavesplatform.wavesj.Node;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import com.wavesplatform.wavesj.Transaction;
import com.wavesplatform.wavesj.transactions.TransferTransaction;
import com.wavesplatform.wavesj.transactions.TransferTransactionV2;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.wavesplatform.wavesj.ByteString.EMPTY;
import static org.junit.Assert.assertEquals;

public class TestSuite {

    DockerClient docker;
    String containerId = "";

    Node node = new Node("http://127.0.0.1:6869", 'R');

    public TestSuite() throws URISyntaxException {
    }

    @Before
    public void before() throws DockerException, InterruptedException, DockerCertificateException, IOException {
        docker = DefaultDockerClient.fromEnv().build();
//        docker.pull("msmolyakov/waves-private-node:testnet"); //TODO from Docker Hub
        docker.build(Paths.get("docker"));

        String[] ports = {"6860", "6869"};
        Map<String, List<PortBinding>> portBindings = new HashMap<>();
        for (String port : ports) { // TODO randomly allocated?
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.of("0.0.0.0", port));
            portBindings.put(port, hostPorts);
        }

        HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image("pvt").exposedPorts(ports)
//                .cmd("sh", "-c", "while :; do sleep 1; done") //TODO del
                .build();

        ContainerCreation container = docker.createContainer(containerConfig);
        containerId = container.id();

        System.out.println(containerId); //TODO del
        docker.startContainer(containerId);

        // Exec command inside running container with attached STDOUT and STDERR
        /*String[] command = {"sh", "-c", "ls"};
        ExecCreation execCreation = docker.execCreate(
                containerId, command, DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());
        LogStream output = docker.execStart(execCreation.id());
        String execOutput = output.readFully();*/
    }

    @Test
    public void test1() throws IOException, TimeoutException {
        PrivateKeyAccount rich = PrivateKeyAccount.fromSeed("rich", 0, (byte) 'R');
        PrivateKeyAccount alice = PrivateKeyAccount.fromSeed("alice", 0, (byte) 'R');

        String transferTxId = node.transfer(rich, alice.getAddress(), 1000_00000000L, 100000, EMPTY);
        Transaction transfer = waitForTransaction(transferTxId);

        assertEquals(node.getBalance(alice.getAddress()), ((TransferTransaction) transfer).getAmount());
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
