package lib.api;

import lib.api.deser.AssetDetails;
import lib.api.deser.ScriptInfo;
import lib.api.deser.StateChangesInfo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NodeApi {

    @GET("addresses/scriptInfo/{address}")
    Call<ScriptInfo> scriptInfo(@Path("address") String address);

    @GET("assets/details/{assetId}")
    Call<AssetDetails> assetDetails(@Path("assetId") String assetId, @Query("full") boolean full);

    @GET("debug/stateChanges/info/{id}")
    Call<StateChangesInfo> stateChanges(@Path("id") String id);

}
