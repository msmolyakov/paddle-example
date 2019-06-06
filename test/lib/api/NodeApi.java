package lib.api;

import lib.api.deser.StateChangesInfo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NodeApi {

    @GET("debug/stateChanges/info/{id}")
    Call<StateChangesInfo> stateChanges(@Path("id") String id);

}
