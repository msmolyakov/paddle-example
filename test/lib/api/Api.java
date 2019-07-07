package lib.api;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.net.URI;
import java.util.Objects;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.USE_LONG_FOR_INTS;

public class Api {

    public Retrofit client;

    private NodeApi nodeApi;

    public Api(URI nodeUri) {
        client = new Retrofit.Builder()
                .baseUrl(HttpUrl.get(nodeUri))
                .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper()
                        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .configure(USE_LONG_FOR_INTS, true)
                )).build();

        nodeApi = client.create(NodeApi.class);
    }

    public ScriptInfo scriptInfo(String address) {
        try {
            Response<ScriptInfo> r = nodeApi.scriptInfo(address).execute();
            if (!r.isSuccessful()) throw parseError(r);
            return r.body();
        } catch (IOException e) {
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

    public StateChanges stateChanges(String txId) {
        try {
            Response<StateChangesInfo> r = nodeApi.stateChanges(txId).execute();
            if (!r.isSuccessful()) throw parseError(r);
            return Objects.requireNonNull(r.body()).stateChanges;
        } catch (IOException e) {
            throw new NodeError(e);
        }
    }

    private ApiError parseError(Response<?> response) {
        Converter<ResponseBody, ApiError> converter =
                client.responseBodyConverter(ApiError.class, new Annotation[0]);
        try {
            return converter.convert(Objects.requireNonNull(response.errorBody()));
        } catch (IOException e) {
            throw new NodeError("can't parse response body");
        }
    }

}
