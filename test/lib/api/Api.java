package lib.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lib.api.exceptions.NodeError;
import lib.api.route.Activation;
import lib.api.route.Addresses;
import lib.api.route.Assets;
import lib.api.route.Debug;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.USE_LONG_FOR_INTS;

public class Api {

    public Retrofit client;

    public Addresses addresses;
    public Assets assets;
    public Debug debug;

    public Api(URI nodeUri) {
        client = new Retrofit.Builder()
                .baseUrl(HttpUrl.get(nodeUri))
                .addConverterFactory(JacksonConverterFactory.create(new ObjectMapper()
                        .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .configure(USE_LONG_FOR_INTS, true)
                )).build();

        addresses = client.create(Addresses.class);
        assets = client.create(Assets.class);
        debug = client.create(Debug.class);
    }

    private NodeError parseError(Response<?> response) {
        Converter<ResponseBody, NodeError> converter =
                client.responseBodyConverter(NodeError.class, new Annotation[0]);

        try {
            return converter.convert(response.errorBody());
        } catch (IOException e) {
            return new NodeError();
        }
    }

}
