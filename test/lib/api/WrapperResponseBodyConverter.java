package lib.api;

import okhttp3.ResponseBody;
import retrofit2.Converter;

import javax.annotation.Nullable;
import java.io.IOException;

public class WrapperResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private Converter<ResponseBody, ?> converter;

    public WrapperResponseBodyConverter(Converter<ResponseBody, ?> converter) {
        this.converter = converter;
    }

    @Nullable
    @Override
    public T convert(ResponseBody value) throws IOException {
        try {
            return (T) converter.convert(value);
        } catch (IOException e) {
            return (T) converter.convert(value);
        }
    }

}
