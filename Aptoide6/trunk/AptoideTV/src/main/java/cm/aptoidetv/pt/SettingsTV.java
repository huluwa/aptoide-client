package cm.aptoidetv.pt;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import cm.aptoide.ptdev.utils.AptoideUtils;

/**
 * Created by tdeus on 3/19/14.
 */
public class SettingsTV extends cm.aptoide.ptdev.Settings {

    private Context mctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mctx = this;
        Preference hwSpecs = findPreference("hwspecs");
        hwSpecs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mctx);
                alertDialogBuilder.setTitle(getString(cm.aptoide.ptdev.R.string.setting_hwspecstitle));
                alertDialogBuilder
                        .setIcon(android.R.drawable.ic_menu_info_details)
                        .setMessage(getString(cm.aptoide.ptdev.R.string.setting_sdk_version)+ ": "+ AptoideUtils.HWSpecifications.getSdkVer()+"\n" +
                                getString(cm.aptoide.ptdev.R.string.setting_screen_size)+ ": "+AptoideUtils.HWSpecifications.getScreenSize(mctx)+"\n" +
                                getString(cm.aptoide.ptdev.R.string.setting_esgl_version)+ ": "+AptoideUtils.HWSpecifications.getGlEsVer(mctx) +"\n" +
                                getString(cm.aptoide.ptdev.R.string.screenCode)+ ": "+AptoideUtils.HWSpecifications.getNumericScreenSize(mctx) + "/" + AptoideUtils.HWSpecifications.getDensityDpi(mctx) +"\n" +
                                getString(cm.aptoide.ptdev.R.string.cpuAbi)+ ": "+AptoideUtils.HWSpecifications.getCpuAbi() + " " + AptoideUtils.HWSpecifications.getCpuAbi2() +"\n" +
                                (AptoideConfigurationTV.PARTNERID!=null ? "Partner ID: " + AptoideConfigurationTV.PARTNERID : "")
                        )
                        .setCancelable(false)
                        .setNeutralButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                return true;
            }
        });

        PreferenceScreen screen = (PreferenceScreen) findPreference("root");

//        Log.d("MatureContentSwitch","value: "+((AptoideConfigurationTV)AptoideTV.getConfiguration()).getMatureContentSwitch());
		if(!((AptoideConfigurationTV)AptoideTV.getConfiguration()).getMatureContentSwitch()){
            screen.removePreference(findPreference("adultContent"));
		}

        screen.removePreference(findPreference("changetheme"));
        screen.removePreference(findPreference("about"));
    }
}
