package lib.api.route;

import lib.api.deser.AssetDetails;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Assets {

    @GET("assets/details/{assetId}")
    Call<AssetDetails> assetDetails(@Path("assetId") String assetId, @Query("full") boolean full);

}
