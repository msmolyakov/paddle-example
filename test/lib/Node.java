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
import lib.api.NodeApi;
import lib.api.StateChanges;
import lib.api.deser.AssetDetails;
import lib.api.deser.ScriptInfo;
import lib.api.deser.StateChangesInfo;
import lib.api.exceptions.ApiError;
import lib.exceptions.NodeError;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.*;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.USE_LONG_FOR_INTS;

public class Node {

    DockerClient docker;
    String containerId = "";
    public com.wavesplatform.wavesj.Node wavesNode;
    public Account rich;

    //TODO move to node.api. Provide some methods through Account
    private Retrofit retrofit;
    NodeApi nodeApi;

    public static Node connectToNode(String uri, char chainId) {
        try {
            Node node = new Node();
            node.wavesNode = new com.wavesplatform.wavesj.Node(uri, chainId);

            node.retrofit = new Retrofit.Builder()
                    .baseUrl(HttpUrl.get(node.wavesNode.getUri()))
                    .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper()
                            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .configure(USE_LONG_FOR_INTS, true)
                    )).build();
            node.nodeApi = node.retrofit.create(NodeApi.class);

            node.rich = new Account("create genesis wallet devnet-0-d", node);
            return node;
        } catch (URISyntaxException | IOException e) {
            throw new NodeError(e);
        }
    }

    public static Node runDockerNode(Version version) {
        try {
            Node node = new Node();
            String tag = version == Version.MAINNET ? "latest" : "testnet";

            node.docker = new DefaultDockerClient("unix:///var/run/docker.sock");
            node.docker.pull("wavesplatform/waves-private-node:" + tag);

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
                    .image("wavesplatform/waves-private-node:" + tag).exposedPorts(ports)
                    .build();

            ContainerCreation container = node.docker.createContainer(containerConfig);
            node.containerId = container.id();

            node.docker.startContainer(node.containerId);

            node.wavesNode = new com.wavesplatform.wavesj.Node("http://127.0.0.1:6869", 'R');
            node.retrofit = new Retrofit.Builder()
                    .baseUrl(HttpUrl.get(node.wavesNode.getUri()))
                    .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper()
                            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                            .configure(USE_LONG_FOR_INTS, true)
                    )).build();
            node.nodeApi = node.retrofit.create(NodeApi.class);

            node.rich = new Account("waves private node seed with waves tokens", node);

            //wait node readiness
            Thread.sleep(8000);
            for (int repeat = 0; repeat < 6; repeat++) {
                try {
                    node.wavesNode.getVersion();
                    break;
                } catch (IOException e) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignore) {
                    }
                }
            }

            return node;
        } catch (URISyntaxException | IOException | DockerException | InterruptedException e) {
            throw new NodeError(e);
        }
    }

    public void stopDockerNode() {
        try {
            docker.killContainer(containerId);
            docker.removeContainer(containerId);
            docker.close();
        } catch (DockerException | InterruptedException e) {
            throw new NodeError(e);
        }
    }

    public AssetDetails assetDetails(String assetId) {
        try {
            Response<AssetDetails> r = nodeApi.assetDetails(assetId, false).execute();
            if (!r.isSuccessful()) throw parseError(r);

            return r.body();
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public boolean isSmart(String assetIdOrAddress) {
        try {
            if (assetIdOrAddress == null || assetIdOrAddress.isEmpty() || "WAVES".equals(assetIdOrAddress))
                return false;
            else if (assetIdOrAddress.length() > 40) {
                return assetDetails(assetIdOrAddress).scripted;
            } else {
                Response<ScriptInfo> r = nodeApi.scriptInfo(assetIdOrAddress).execute();
                if (!r.isSuccessful()) throw parseError(r);

                return Objects.requireNonNull(r.body()).extraFee > 0;
            }
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public boolean isSmart(Account account) {
        return isSmart(account.address());
    }

    //TODO move to node.api.debug.stateChanges()
    public StateChanges stateChanges(String txId) {
        try {
            Response<StateChangesInfo> r = nodeApi.stateChanges(txId).execute();
            if (!r.isSuccessful()) throw parseError(r);
            return Objects.requireNonNull(r.body()).stateChanges;
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    public Transaction waitForTransaction(String id) {
        for (int repeat = 0; repeat < 100; repeat++) {
            try {
                return wavesNode.getTransaction(id);
            } catch (IOException e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {}
            }
        }
        throw new NodeError("Could not wait for transaction " + id + " in 10 seconds");
    }

    private ApiError parseError(Response<?> response) {
        Converter<ResponseBody, ApiError> converter =
                retrofit.responseBodyConverter(ApiError.class, new Annotation[0]);

        ApiError error;

        try {
            error = converter.convert(Objects.requireNonNull(response.errorBody()));
        } catch (IOException e) {
            return new ApiError();
        }

        return error;
    }

}
