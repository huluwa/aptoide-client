package cm.aptoide.ptdev.webservices.timeline;

import java.util.HashMap;

import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.webservices.WebserviceOptions;
import cm.aptoide.ptdev.webservices.json.GenericResponseV2;

/**
 * Created by asantos on 24-09-2014.
 */
public class ChangeUserApkInstallStatusRequest extends TimelineRequest<GenericResponse> {

    public static final String STATUSACTIVE = "active";
    public static final String STATUSHIDDEN = "hidden";

    private long postID;
    public void setPostId(long id){this.postID = id;}
    private String status;

    public interface ChangeUserApkInstallStatus extends TimelineRequest.Webservice<GenericResponse>{

    }

    public void setPostStatus(String status){this.status = status;}

    public ChangeUserApkInstallStatusRequest() {  super(GenericResponse.class, ChangeUserApkInstallStatus.class);  }

    @Override
    protected String getUrl() {
        return WebserviceOptions.WebServicesLink+"3/changeUserApkInstallStatus";
    }
    @Override
    protected HashMap<String, String> fillWithExtraOptions(HashMap<String, String> parameters) {
        parameters.put("id", String.valueOf(postID));
        parameters.put("status", status);
        return parameters;
    }
}
