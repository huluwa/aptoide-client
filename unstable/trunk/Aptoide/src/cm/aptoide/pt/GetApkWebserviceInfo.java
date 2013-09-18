package cm.aptoide.pt;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import cm.aptoide.pt.services.ServiceManagerDownload;
import cm.aptoide.pt.views.ViewApk;
import cm.aptoide.pt.webservices.WebserviceGetApkInfo;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 18-09-2013
 * Time: 16:51
 * To change this template use File | Settings | File Templates.
 */
public class GetApkWebserviceInfo extends AsyncTask<Long, Void, WebserviceGetApkInfo>{

    private final Context context;
    private final ServiceManagerDownload serviceDownloadManager;
    private final boolean showProgressDialog;
    ProgressDialog pd;
    private ViewApk apk;

    public GetApkWebserviceInfo(Context context, ServiceManagerDownload serviceDownloadManager, boolean b){
        this.context = context;
        this.serviceDownloadManager= serviceDownloadManager;
        this.showProgressDialog = b;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (showProgressDialog) {
            pd = new ProgressDialog(context);
            pd.setMessage(context.getString(R.string.please_wait));
            pd.show();
        }
    }

    @Override
    protected WebserviceGetApkInfo doInBackground(Long... params) {
        this.apk = Database.getInstance().getApk(params[0], Category.INFOXML);
        try {
            return new WebserviceGetApkInfo(context, apk.getWebservicesPath(), apk, Category.INFOXML, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(WebserviceGetApkInfo webserviceGetApkInfo) {
        super.onPostExecute(webserviceGetApkInfo);
        if(pd != null && pd.isShowing()){
            pd.dismiss();
        }

        if(webserviceGetApkInfo!=null){
            try {
                apk.setMd5(webserviceGetApkInfo.getApkMd5());
                apk.setPath(webserviceGetApkInfo.getApkDownloadPath());

                if(webserviceGetApkInfo.hasOBB()){
                    apk.setMainObbUrl(webserviceGetApkInfo.getMainOBB().getString("path"));
                    apk.setMainObbFileName(webserviceGetApkInfo.getMainOBB().getString("filename"));
                    apk.setMainObbMd5(webserviceGetApkInfo.getMainOBB().getString("md5sum"));

                    if(webserviceGetApkInfo.hasPatchOBB()){
                        apk.setPatchObbUrl(webserviceGetApkInfo.getPatchOBB().getString("path"));
                        apk.setPatchObbFileName(webserviceGetApkInfo.getPatchOBB().getString("filename"));
                        apk.setPatchObbMd5(webserviceGetApkInfo.getPatchOBB().getString("md5sum"));
                    }

                }
                JSONArray array = webserviceGetApkInfo.getApkPermissions();
                ArrayList<String > permissionList = new ArrayList<String>();
                for(int i = 0; i!=array.length();i++){
                    permissionList.add(array.getString(i));
                }
                apk.setPermissionsList(permissionList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            serviceDownloadManager.startDownload(serviceDownloadManager.getDownload(apk), apk);
            Toast toast = Toast.makeText(context, context.getString(R.string.starting_download) + ": " + apk.getName(), Toast.LENGTH_SHORT);
            toast.show();

        }else{
            Toast toast = Toast.makeText(context, context.getString(R.string.an_error_check_net), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}