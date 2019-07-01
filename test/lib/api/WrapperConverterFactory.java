package lib.api;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class WrapperConverterFactory extends Converter.Factory {

    private JacksonConverterFactory factory;

    public WrapperConverterFactory(JacksonConverterFactory factory) {
        this.factory = factory;
    }

    @Nullable
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        Converter<ResponseBody, ?> jacksonConverter = factory.responseBodyConverter(type, annotations, retrofit);
        return new WrapperResponseBodyConverter(jacksonConverter);
    }

}
