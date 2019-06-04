package lib.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NodeApi {

    @GET("debug/stateChanges/info/{id}")
    Call<Transaction> stateChanges(@Path("id") String id);

}
