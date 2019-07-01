package lib.api.route;

import lib.api.deser.StateChangesInfo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Debug {

    @GET("debug/stateChanges/info/{id}")
    Call<StateChangesInfo> stateChanges(@Path("id") String id);

}
