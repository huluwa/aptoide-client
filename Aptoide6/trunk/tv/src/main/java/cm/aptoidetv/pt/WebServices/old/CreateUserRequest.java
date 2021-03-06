package cm.aptoidetv.pt.WebServices.old;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import java.util.HashMap;

import cm.aptoidetv.pt.AppTV;
import cm.aptoidetv.pt.WebServices.old.json.OAuth;
import retrofit.converter.Converter;
import retrofit.converter.JacksonConverter;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 04-11-2013
 * Time: 11:29
 * To change this template use File | Settings | File Templates.
 */

public class CreateUserRequest extends RetrofitSpiceRequest<OAuth, CreateUserRequest.Webservice> {


    public interface Webservice{
        @POST("/webservices.aptoide.com/webservices/3/createUser")
        @FormUrlEncoded
        OAuth createUser(@FieldMap HashMap<String, String> args);
    }

    protected Converter createConverter() {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new JacksonConverter(objectMapper);
    }

    private String email;
    private String pass;
    private String name = "";

    public CreateUserRequest() {
        super(OAuth.class, CreateUserRequest.Webservice.class);
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setPass(String pass){
        this.pass = pass;
    }

    @Override
    public OAuth loadDataFromNetwork() throws Exception {

        //GenericUrl url = new GenericUrl(baseUrl);


        HashMap<String, String > parameters = new HashMap<String, String>();
        String passhash = AptoideUtils.Algorithms.computeSHA1sum(pass);
        parameters.put("mode", "json");
        parameters.put("email", email);
        parameters.put("passhash", passhash);

        if(AppTV.getConfiguration().getExtraId().length()>0){
            parameters.put("oem_id", AppTV.getConfiguration().getExtraId());
        }

        parameters.put("hmac", AptoideUtils.Algorithms.computeHmacSha1(email+passhash+name, "bazaar_hmac"));


        //RestAdapter adapter = new RestAdapter.Builder().setEndpoint("http://").setConverter(createConverter()).build();
        //setService(adapter.create(getRetrofitedInterfaceClass()));



        return getService().createUser(parameters);
//        HttpContent content = new UrlEncodedContent(parameters);
//
//        HttpRequest request = getHttpRequestFactory().buildPostRequest(url, content);
//
//        request.setParser(new JacksonFactory().createJsonObjectParser());
//
//        return request.execute().parseAs( getResultType() );
    }

    public void setName(String name) {
        this.name = name;
    }
}
