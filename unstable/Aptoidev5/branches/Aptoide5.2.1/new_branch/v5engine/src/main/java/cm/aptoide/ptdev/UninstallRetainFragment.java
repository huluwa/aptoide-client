package cm.aptoide.ptdev;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import cm.aptoide.ptdev.adapters.UninstallHelper;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.model.RollBackItem;
import cm.aptoide.ptdev.utils.AptoideUtils;

import java.io.File;

/**
 * Created by brutus on 27-12-2013.
 */
public class UninstallRetainFragment extends Fragment {


    private ActionBarActivity activity;

    private String appName;
    private String packageName;
    private String versionName;
    private String iconPath;

    private String versionToDowngrade;
    private RollBackItem.Action rollBackAction;

    private long id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        Log.d("Aptoide-Uninstaller", "Uninstalling");

        Bundle args = getArguments();

        if(args.containsKey( "id" )) {
            id = args.getLong( "id" );
            rollBackAction = RollBackItem.Action.UNINSTALLING;
        } else if(args.containsKey( "downgradeVersion" ) ){
            appName = args.getString( "name" );
            packageName = args.getString( "package" );
            versionName = args.getString( "version" );
            iconPath = args.getString( "icon" );
            versionToDowngrade = args.getString( "downgradeVersion" );
            rollBackAction = RollBackItem.Action.DOWNGRADING;
        } else {
            appName = args.getString( "name" );
            packageName = args.getString( "package" );
            versionName = args.getString( "version" );
            iconPath = args.getString( "icon" );
            rollBackAction = RollBackItem.Action.UNINSTALLING;
        }


        new UninstallTask().execute();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (ActionBarActivity) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    private class UninstallTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            AptoideDialog.pleaseWaitDialog().show(getFragmentManager(), "pleaseWaitDialog");
        }

        @Override
        protected Void doInBackground(Void... params) {

            Database db = new Database(Aptoide.getDb());
            RollBackItem rollBackItem;
            String apkMd5;

            try {

                if(id>0){
                    Cursor c = db.getApkInfo(id);
                    packageName = c.getString(c.getColumnIndex("package_name"));
                    appName = c.getString(c.getColumnIndex("name"));
                    versionName = c.getString(c.getColumnIndex("version_name"));
                    String icon = c.getString(c.getColumnIndex("icon"));
                    String repoIconPath = c.getString(c.getColumnIndex("iconpath"));
                    iconPath = repoIconPath + icon;
                }


                switch (rollBackAction) {
                    case DOWNGRADING:

                        apkMd5 = calcApkMd5(packageName);

                        rollBackItem = new RollBackItem(appName, packageName, versionToDowngrade, versionName, iconPath, null, apkMd5, null, "");

                        break;

                    default:
                        apkMd5 = db.getUnistallingActionMd5(packageName);

                        if (db.getUnistallingActionMd5(packageName) == null) {
                            apkMd5 = calcApkMd5(packageName);
                        }
                        rollBackItem = new RollBackItem(appName, packageName, versionName, null, iconPath, null, apkMd5, RollBackItem.Action.UNINSTALLING, "");
                        break;
                }

                db.insertRollbackAction(rollBackItem);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (activity != null) {
                if(rollBackAction == RollBackItem.Action.DOWNGRADING) {
                    UninstallHelper.uninstall(activity, packageName, true);
                } else {
                    UninstallHelper.uninstall(activity, packageName, false);
                }
            }

            if(getFragmentManager()!=null){
                DialogFragment pd = (DialogFragment) getFragmentManager().findFragmentByTag("pleaseWaitDialog");
                if(pd!=null){
                    pd.dismissAllowingStateLoss();
                }

                getFragmentManager().beginTransaction().remove(UninstallRetainFragment.this).commitAllowingStateLoss();

            }


        }

        private String calcApkMd5(String packageName) throws PackageManager.NameNotFoundException {
            String sourceDir = activity.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.sourceDir;
            File apkFile = new File(sourceDir);
            return AptoideUtils.Algorithms.md5Calc(apkFile);
        }

    }
}