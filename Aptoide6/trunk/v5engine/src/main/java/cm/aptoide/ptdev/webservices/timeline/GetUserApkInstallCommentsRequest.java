package cm.aptoide.ptdev.webservices.timeline;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.timeline.json.ApkInstallComments;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by asantos on 24-09-2014.
 */
public class GetUserApkInstallCommentsRequest extends RetrofitSpiceRequest<ApkInstallComments, GetUserApkInstallCommentsRequest.GetUserApkInstallComments> {

    private long postID;
    private int limit;
    private int offset;
    public void setPostID(long id) { this.postID = id; }
    public void setPostLimit(int limit) { this.limit = limit; }
    public void setPostOffSet(int offset) { this.offset = offset; }
    public GetUserApkInstallCommentsRequest() {    super(ApkInstallComments.class, GetUserApkInstallComments.class);    }

    public interface GetUserApkInstallComments{
        @POST(WebserviceOptions.WebServicesLink+"3/getUserApkInstallComments")
        @FormUrlEncoded
        public ApkInstallComments run(@FieldMap HashMap<String, String> args);
    }

    @Override
    public ApkInstallComments loadDataFromNetwork() throws Exception {
//        GenericUrl url= new GenericUrl(getUrl());

        HashMap<String, String > parameters = new HashMap<String, String>();
        parameters.put("mode" , "json");
        parameters.put("id", String.valueOf(postID));
        parameters.put("limit", String.valueOf(limit));
        parameters.put("offset", String.valueOf(offset));

        String token = SecurePreferences.getInstance().getString("access_token", "empty");
        parameters.put("access_token", token);

        return getService().run(parameters);
    }
}
