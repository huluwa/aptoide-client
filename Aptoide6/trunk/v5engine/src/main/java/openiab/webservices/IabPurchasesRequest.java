package openiab.webservices;









import java.io.EOFException;
import java.util.HashMap;

import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.webservices.AddApkCommentVoteRequest;

import cm.aptoide.ptdev.webservices.OauthErrorHandler;
import openiab.webservices.json.IabConsumeJson;
import openiab.webservices.json.IabPurchasesJson;
import retrofit.RetrofitError;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

public class IabPurchasesRequest extends BaseRequest<IabPurchasesJson, IabPurchasesRequest.Webservice> {

    private String type;

    public interface Webservice{
        @POST("/webservices.aptoide.com/webservices/3/processInAppBilling")
        @FormUrlEncoded
        IabPurchasesJson processInAppBilling(@FieldMap HashMap<String, String> args);
    }

    public IabPurchasesRequest() {
        super(IabPurchasesJson.class, Webservice.class);
    }

    @Override
    public IabPurchasesJson loadDataFromNetwork() throws Exception {
//        GenericUrl url = getURL();

        HashMap<String, String> parameters = new HashMap<String, String>();

        parameters.put("apiversion",apiVersion);
        parameters.put("reqtype","iabpurchases");

        parameters.put("package",packageName);
        parameters.put("purchasetype",type);

        token = SecurePreferences.getInstance().getString("access_token", null);

        parameters.put("access_token",token);
        parameters.put("mode","json");
//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//
//        request.setUnsuccessfulResponseHandler(new OAuthRefreshAccessTokenHandler(parameters, getHttpRequestFactory()));
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//
//        HttpResponse response;
//        try{
//            response = request.execute();
//        } catch (EOFException e){
//
//            HttpHeaders httpHeaders = new HttpHeaders();
//            httpHeaders.put("Connection", "close");
//            request.setHeaders(httpHeaders);
//            response = request.execute();
//        }
//
//        return response.parseAs(getResultType());

        IabPurchasesJson response = null;

        try{
            response = getService().processInAppBilling(parameters);
        }catch (RetrofitError error){
            OauthErrorHandler.handle(error);
        }

        return response;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
