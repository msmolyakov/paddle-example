package lib.api.route;

import lib.api.deser.ScriptInfo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface Addresses {

    @GET("addresses/scriptInfo/{address}")
    Call<ScriptInfo> scriptInfo(@Path("address") String address);

}
