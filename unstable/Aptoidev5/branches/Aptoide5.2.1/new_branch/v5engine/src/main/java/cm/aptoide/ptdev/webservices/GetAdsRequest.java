package cm.aptoide.ptdev.webservices;

import android.content.Context;
import android.text.TextUtils;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.ads.AptoideAdNetworks;
import cm.aptoide.ptdev.preferences.EnumPreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.json.ApkSuggestionJson;

/**
 * Created by rmateus on 29-07-2014.
 */
public class GetAdsRequest extends GoogleHttpClientSpiceRequest<ApkSuggestionJson> {

    private int CONNECTION_TIMEOUT = 10000;
    private final Context context;
    private String location;
    private String keyword;
    private int limit;
    private String package_name;
    private String repo;
    private boolean filter_pkg;

    public void setFilter_pkg(boolean filter_pkg) {
        this.filter_pkg = filter_pkg;
    }
    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public GetAdsRequest(Context context) {
        super(ApkSuggestionJson.class);
        this.context = context;
    }

    String url = "http://webservices.aptwords.net/api/2/getAds";
    ExecutorService executor = Executors.newSingleThreadExecutor();
    @Override
    public ApkSuggestionJson loadDataFromNetwork() throws Exception {

        HashMap<String, String> parameters = new HashMap<String, String>();

        parameters.put("q", AptoideUtils.filters(context));
        parameters.put("lang", AptoideUtils.getMyCountryCode(context));

        String myid = AptoideUtils.getSharedPreferences().getString(EnumPreferences.APTOIDE_CLIENT_UUID.name(), "NoInfo");
        parameters.put("cpuid", myid);

        String mature = "1";

        if(AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true)){
            mature = "0";
        }

        parameters.put("location","native-aptoide:" + location);
        parameters.put("type", "1-3");
        parameters.put("keywords", keyword);

        String oemid = Aptoide.getConfiguration().getExtraId();

        if( !TextUtils.isEmpty(oemid) ){
            parameters.put("oemid", oemid);
        }


        parameters.put("limit", String.valueOf(limit));

        parameters.put("get_mature", mature);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           

        parameters.put("partners", "1-3,5-6");


        parameters.put("app_pkg", package_name);
        parameters.put("app_store", repo);

        parameters.put("conn_type", AptoideUtils.NetworkUtils.getConnectionType().toString());

        if(Aptoide.DEBUG_MODE){
            parameters.put("country", AptoideUtils.getSharedPreferences().getString("forcecountry", null));
        }

        if(GooglePlayServicesUtil.isGooglePlayServicesAvailable(Aptoide.getContext())==0) {
            parameters.put("flag", "gms");
        }
        parameters.put("filter_pkg", String.valueOf(filter_pkg));
        GenericUrl url = new GenericUrl(this.url);

        HttpContent content = new UrlEncodedContent(parameters);

        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
        request.setSuppressUserAgentSuffix(true);
        request.setParser(new JacksonFactory().createJsonObjectParser());

        request.setConnectTimeout(CONNECTION_TIMEOUT);
        request.setReadTimeout(CONNECTION_TIMEOUT);

        ApkSuggestionJson result = request.execute().parseAs( getResultType() );

        Map<String, String> adsParams = new HashMap<String, String>();
        adsParams.put("placement", location);


        for(ApkSuggestionJson.Ads suggestionJson : result.getAds()) {
            String ad_type = suggestionJson.getInfo().getAd_type();
            adsParams.put("type", ad_type);

            FlurryAgent.logEvent("Get_Sponsored_Ad", adsParams);

            if(suggestionJson.getPartner() != null){

                try{

                    String impressionUrlString = suggestionJson.getPartner().getPartnerData().getImpression_url();

                    impressionUrlString = AptoideAdNetworks.parseString(suggestionJson.getPartner().getPartnerInfo().getName(), Aptoide.getContext(), impressionUrlString);

                    GenericUrl impressionUrl = new GenericUrl(impressionUrlString);
                    getHttpRequestFactory().buildGetRequest(impressionUrl).setSuppressUserAgentSuffix(true).executeAsync(executor);

                } catch (Exception ignored) {}

            }

//            Log.d("AdsFlurry", "Map is " + adsParams);
        }


        return result;
    }

    public void setTimeout(int timeout){
        CONNECTION_TIMEOUT = timeout;
    }


    public void setLocation(String location) {
        this.location = location;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
