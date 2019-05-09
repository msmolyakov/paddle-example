import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.*;
import com.wavesplatform.wavesj.Node;
import com.wavesplatform.wavesj.PrivateKeyAccount;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class TestSuite {

    DockerClient docker;

    @Before
    public void before() throws DockerException, InterruptedException, DockerCertificateException {
        docker = DefaultDockerClient.fromEnv().build();
        docker.pull("msmolyakov/waves-private-node:testnet");

        String[] ports = {"6890", "6891"};
        Map<String, List<PortBinding>> portBindings = new HashMap<>();
        for (String port : ports) { // TODO randomly allocated
            List<PortBinding> hostPorts = new ArrayList<>();
            hostPorts.add(PortBinding.of("0.0.0.0", port));
            portBindings.put(port, hostPorts);
        }

        HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image("busybox").exposedPorts(ports)
                .cmd("sh", "-c", "while :; do sleep 1; done")
                .build();

        ContainerCreation container = docker.createContainer(containerConfig);
        String id = container.id();

        docker.startContainer(id);

        // Exec command inside running container with attached STDOUT and STDERR
        String[] command = {"sh", "-c", "ls"};
        ExecCreation execCreation = docker.execCreate(
                id, command, DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());
        LogStream output = docker.execStart(execCreation.id());
        String execOutput = output.readFully();

        docker.killContainer(id);
        docker.removeContainer(id);
        docker.close();
    }

    @Test
    public void test1() throws URISyntaxException {
        Node node = new Node("http://127.0.0.1:6869", 'R');
        PrivateKeyAccount alice = PrivateKeyAccount.fromSeed("alice", 0, (byte) 'R');


    }

}
