package lib;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.wavesplatform.wavesj.Transaction;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Node {

    DockerClient docker;
    String containerId = "";
    public com.wavesplatform.wavesj.Node wavesNode;
    public Account rich;

    public static Node runDockerNode(Version version) throws URISyntaxException, DockerException, InterruptedException {
        Node node = new Node();
        String tag = version == Version.MAINNET ? "mainnet" : "testnet";

        node.docker = new DefaultDockerClient("unix:///var/run/docker.sock");
        node.docker.pull("msmolyakov/waves-private-node:" + tag);

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
                .image("msmolyakov/waves-private-node:" + tag).exposedPorts(ports)
                .build();

        ContainerCreation container = node.docker.createContainer(containerConfig);
        node.containerId = container.id();

        node.docker.startContainer(node.containerId);

        node.wavesNode = new com.wavesplatform.wavesj.Node("http://127.0.0.1:6869", 'R');
        node.rich = new Account("rich", node);

        //wait node readiness
        Thread.sleep(8000);
        for (int repeat = 0; repeat < 6; repeat++) {
            try {
                node.wavesNode.getVersion();
                break;
            } catch (IOException e) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignore) {}
            }
        }

        // Exec command inside running container with attached STDOUT and STDERR
        /*String[] command = {"sh", "-c", "ls"};
        ExecCreation execCreation = docker.execCreate(
                containerId, command, DockerClient.ExecCreateParam.attachStdout(),
                DockerClient.ExecCreateParam.attachStderr());
        LogStream output = docker.execStart(execCreation.id());
        String execOutput = output.readFully();*/

        return node;
    }

    public void stopDockerNode() throws DockerException, InterruptedException {
        docker.killContainer(containerId);
        docker.removeContainer(containerId);
        docker.close();
    }

    public Transaction waitForTransaction(String id) throws TimeoutException {
        for (int repeat = 0; repeat < 100; repeat++) {
            try {
                return wavesNode.getTransaction(id);
            } catch (IOException e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) {}
            }
        }
        throw new TimeoutException("Could not wait for transaction " + id + " in 10 seconds");
    }

}
