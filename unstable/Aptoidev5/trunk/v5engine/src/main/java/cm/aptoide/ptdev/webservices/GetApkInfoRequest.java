package cm.aptoide.ptdev.webservices;

import android.util.Log;
import cm.aptoide.ptdev.webservices.json.GetApkInfoJson;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 06-11-2013
 * Time: 10:48
 * To change this template use File | Settings | File Templates.
 */
public class GetApkInfoRequest extends GoogleHttpClientSpiceRequest<GetApkInfoJson> {


    private String repoName;
    private String packageName;
    private String versionName;



    public GetApkInfoRequest() {
        super(GetApkInfoJson.class);
    }

    @Override
    public GetApkInfoJson loadDataFromNetwork() throws Exception {

        String baseUrl = "http://webservices.aptoide.com/webservices/getApkInfo/"+repoName+"/"+packageName+"/"+versionName+"/json";
        GenericUrl url = new GenericUrl(baseUrl);

        Log.e("Aptoide-Request", baseUrl);
        HttpRequest request = getHttpRequestFactory().buildGetRequest(url);

        request.setParser(new JacksonFactory().createJsonObjectParser());

        return request.execute().parseAs(getResultType());
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionName() {
        return versionName;
    }
}
