package cm.aptoide.ptdev.services;

import android.util.Log;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.model.ResponseCode;
import cm.aptoide.ptdev.utils.AptoideUtils;
import com.octo.android.robospice.request.SpiceRequest;


/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 19-11-2013
 * Time: 11:28
 * To change this template use File | Settings | File Templates.
 */
public class CheckServerRequest extends SpiceRequest<ResponseCode> {
    private final String url;
    private final Login login;


    public CheckServerRequest(String url, Login login) {
        super(ResponseCode.class);
        this.url = url;
        this.login = login;

    }

    @Override
    public Class<ResponseCode> getResultType() {
        return null;
    }

    @Override
    public ResponseCode loadDataFromNetwork() throws Exception {
        ResponseCode responseCode = new ResponseCode();
        if(login!=null){
            responseCode.responseCode = AptoideUtils.NetworkUtils.checkServerConnection(url, login.getUsername(), login.getPassword());
        }else{
            responseCode.responseCode = AptoideUtils.NetworkUtils.checkServerConnection(url, null, null);
        }

        Log.d("Aptoide-Log", responseCode.getClass().getCanonicalName());

        return responseCode;
    }
}
