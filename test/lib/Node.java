package lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.wavesplatform.wavesj.Transaction;
import lib.api.Api;
import lib.api.NodeApi;
import lib.api.StateChanges;
import lib.api.deser.AssetDetails;
import lib.api.exceptions.NodeError;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.USE_LONG_FOR_INTS;

public class Node {

    DockerClient docker;
    String containerId = "";
    @Deprecated
    public com.wavesplatform.wavesj.Node wavesNode;
    public Account rich;

    //TODO move to node.api. Provide some methods through Account
    public Api api;

    public static Node runDockerNode(Version version) throws URISyntaxException, DockerException, InterruptedException, IOException, TimeoutException {
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
        node.api = new Api(node.wavesNode.getUri());

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


    public AssetDetails assetDetails(String assetId) {
        try {
            Response<AssetDetails> r = nodeApi.assetDetails(assetId, false).execute();
            if (r.isSuccessful())
                return r.body();
            else {
                throw parseError(r);
            }
        } catch (IOException e) {
            throw new NodeError(-1, "unknown error");
        }
    }

    public boolean isSmart(String assetIdOrAddress) throws IOException {
        if (assetIdOrAddress == null || assetIdOrAddress.isEmpty() || "WAVES".equals(assetIdOrAddress))
            return false;
        else if (assetIdOrAddress.length() > 40) {
            return assetDetails(assetIdOrAddress).scripted;
        } else
            return nodeApi.scriptInfo(assetIdOrAddress).execute().body().extraFee > 0;
    }

    public boolean isSmart(Account account) throws IOException {
        return isSmart(account.address());
    }

    //TODO move to node.api.debug.stateChanges()
    public StateChanges stateChanges(String txId) throws IOException {
        return nodeApi.stateChanges(txId).execute().body().stateChanges;
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
