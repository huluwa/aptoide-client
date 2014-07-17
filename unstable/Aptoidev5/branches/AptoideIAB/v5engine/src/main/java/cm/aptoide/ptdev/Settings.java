/*******************************************************************************
 * Copyright (c) 2012 rmateus.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
package cm.aptoide.ptdev;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import cm.aptoide.ptdev.dialogs.AdultDialog;
import cm.aptoide.ptdev.preferences.ManagerPreferences;
import cm.aptoide.ptdev.preferences.SecurePreferences;
import cm.aptoide.ptdev.utils.AptoideUtils;

import java.io.File;
import java.text.DecimalFormat;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	String aptoide_path = Aptoide.getConfiguration().getPathCache();
	String icon_path = aptoide_path + "icons/";
	ManagerPreferences preferences;
	Context mctx;
	private boolean unlocked = false;
    private static boolean isSetingPIN = false;

    private Dialog DialogSetAdultpin(final Preference mp){
        isSetingPIN=true;
        final View v = LayoutInflater.from(this).inflate(R.layout.dialog_requestpin, null);
        AlertDialog.Builder builder= new AlertDialog.Builder(this)
                .setMessage(R.string.asksetadultpinmessage)
                .setView(v)
                .setPositiveButton(R.string.setpin, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String input = ((EditText) v.findViewById(R.id.pininput)).getText().toString();
                        if (!TextUtils.isEmpty(input)) {
                            new SecurePreferences(Settings.this)
                                    .edit()
                                    .putInt(AdultDialog.MATUREPIN, new Integer(input))
                                    .commit();
                            mp.setTitle(R.string.RemoveMaturePinTitle);
                            mp.setSummary(R.string.RemoveMaturePinSummary);
                            //mp.setOnPreferenceClickListener(removeclick);
                        }
                        isSetingPIN=false;
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isSetingPIN=false;
                    }
                });
        return builder.create();
    }

    private void maturePinSetRemoveClick(){
        int pin = new SecurePreferences(this).getInt(AdultDialog.MATUREPIN,-1);
        final Preference mp= findPreference("Maturepin");
        if(pin!=-1) {
            // With Pin
            AdultDialog.DialogRequestMaturepin(Settings.this,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new SecurePreferences(Aptoide.getContext()).edit().putInt(AdultDialog.MATUREPIN,-1).commit();
                    final Preference mp= findPreference("Maturepin");
                    mp.setTitle(R.string.SetMaturePinTitle);
                    mp.setSummary(R.string.SetMaturePinSummary);
                }
            }).show();
        }
        else{
            DialogSetAdultpin(mp).show();// Without Pin

        }
    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        mctx = this;
        new GetDirSize().execute(new File(aptoide_path),new File(icon_path));
        preferences = new ManagerPreferences(mctx);
//        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
//
//			@Override
//			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
//					String key) {
//				preferences.setIconDownloadPermissions(new ViewIconDownloadPermissions(((CheckBoxPreference)findPreference("wifi")).isChecked(),
//						((CheckBoxPreference)findPreference("ethernet")).isChecked(),
//						((CheckBoxPreference)findPreference("4g")).isChecked(),
//						((CheckBoxPreference)findPreference("3g")).isChecked()));
//			}
//		});

        int pin = new SecurePreferences(this).getInt(AdultDialog.MATUREPIN,-1);
        final Preference mp= findPreference("Maturepin");
        if(pin!=-1) {
            Log.d("PINTEST","PinBuild");
            mp.setTitle(R.string.RemoveMaturePinTitle);
            mp.setSummary(R.string.RemoveMaturePinSummary);
        }
        mp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                maturePinSetRemoveClick();
                return true;
            }
        });
        findPreference("matureChkBox").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final CheckBoxPreference cb = (CheckBoxPreference) preference;
                if (!cb.isChecked()) {
                    cb.setChecked(true);
                    AdultDialog.BuildAreYouAdultDialog(Settings.this, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cb.setChecked(false);
                        }
                    }).show();
                }
                return true;
            }
        });

        findPreference("showAllUpdates").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SettingsResult();
                return true;
            }
        });

        findPreference("clearcache").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {


			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(unlocked){
					new DeleteDir().execute(new File(icon_path));
				}

				return false;
			}
		});
		findPreference("clearapk").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(unlocked){
					new DeleteDir().execute(new File(aptoide_path));
				}

				return false;
			}
		});



        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);


//		Preference hwspecs = (Preference) findPreference("hwspecs");
//		hwspecs.setIntent(new Intent(getBaseContext(), HWSpecActivity.class));
		Preference hwSpecs = findPreference("hwspecs");

        findPreference("theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                Toast.makeText(Settings.this, getString(R.string.restart_aptoide), Toast.LENGTH_LONG).show();
                return true;
            }
        });






		hwSpecs.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mctx);
				alertDialogBuilder.setTitle(getString(R.string.setting_hwspecstitle));
				alertDialogBuilder
					.setIcon(android.R.drawable.ic_menu_info_details)
					.setMessage(getString(R.string.setting_sdk_version)+ ": "+ AptoideUtils.HWSpecifications.getSdkVer()+"\n" +
							    getString(R.string.setting_screen_size)+ ": "+AptoideUtils.HWSpecifications.getScreenSize(mctx)+"\n" +
							    getString(R.string.setting_esgl_version)+ ": "+AptoideUtils.HWSpecifications.getGlEsVer(mctx) +"\n" +
                                getString(R.string.screenCode)+ ": "+AptoideUtils.HWSpecifications.getNumericScreenSize(mctx) + "/" + AptoideUtils.HWSpecifications.getDensityDpi(mctx) +"\n" +
                                getString(R.string.cpuAbi)+ ": "+AptoideUtils.HWSpecifications.getCpuAbi() + " " + AptoideUtils.HWSpecifications.getCpuAbi2()
//                            + (ApplicationAptoide.PARTNERID!=null ? "\nPartner ID:" + ApplicationAptoide.PARTNERID : "")
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

//		if(!ApplicationAptoide.MATURECONTENTSWITCH){
//			CheckBoxPreference mCheckBoxPref = (CheckBoxPreference) findPreference("matureChkBox");
//			PreferenceCategory mCategory = (PreferenceCategory) findPreference("filters");
//			mCategory.removePreference(mCheckBoxPref);
//		}

//		Preference showExcluded = findPreference("showexcludedupdates");
//		showExcluded.setIntent(new Intent(mctx, ExcludedUpdatesActivity.class));

		EditTextPreference maxFileCache = (EditTextPreference) findPreference("maxFileCache");

		maxFileCache.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		maxFileCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {
				((EditTextPreference) preference).getEditText().setText(PreferenceManager.getDefaultSharedPreferences(mctx).getString("maxFileCache","200"));
				return false;
			}
		});


		Preference about = findPreference("aboutDialog");
		about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
                View view = LayoutInflater.from(mctx).inflate(R.layout.dialog_about, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mctx).setView(view);
                final AlertDialog aboutDialog = alertDialogBuilder.create();
                aboutDialog.setTitle(getString(R.string.about_us));
                aboutDialog.setIcon(android.R.drawable.ic_menu_info_details);
                aboutDialog.setCancelable(false);
                aboutDialog.setButton(Dialog.BUTTON_NEUTRAL, getString(android.R.string.ok), new Dialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                aboutDialog.show();

				return true;
			}
		});

//		if(ApplicationAptoide.PARTNERID!=null){
//			PreferenceScreen preferenceScreen = getPreferenceScreen();
//			Preference etp = preferenceScreen.findPreference("aboutDialog");
//
//			PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference("about");
//			preferenceGroup.removePreference(etp);
//			preferenceScreen.removePreference(preferenceGroup);
//
//		}
//
//
//        if(!ApplicationAptoide.DEBUG_MODE){
//            PreferenceScreen preferenceScreen = getPreferenceScreen();
//            Preference etp = preferenceScreen.findPreference("devmode");
//            PreferenceGroup preferenceGroup = (PreferenceGroup) findPreference("devmode");
//            preferenceGroup.removePreference(etp);
//            preferenceScreen.removePreference(preferenceGroup);
//        }



//        getActionBar().setTitle("");
//        getActionBar().setHomeButtonEnabled(true);
//        getActionBar().setDisplayHomeAsUpEnabled(true);

        if(isSetingPIN) {
            Log.d("PINTEST","is Setting adult pin");
            DialogSetAdultpin(mp).show();
        }

    }

    private final void SettingsResult(){
        setResult(RESULT_OK);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {


    }


    public class DeleteDir extends AsyncTask<File, Void, Void> {
		ProgressDialog pd;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pd = new ProgressDialog(mctx);
			pd.setMessage(getString(R.string.please_wait));
			pd.show();
		}
		@Override
		protected Void doInBackground(File... params) {
			deleteDirectory(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			pd.dismiss();
			Toast toast= Toast.makeText(mctx, mctx.getString(R.string.clear_cache_sucess), Toast.LENGTH_SHORT);
			toast.show();
			new GetDirSize().execute(new File(aptoide_path),new File(icon_path));
		}

	}





	public class GetDirSize extends AsyncTask<File, Void, Double[]> {
		double getDirSize(File dir) {
			double size = 0;
			try{
				if (dir.isFile()) {
					size = dir.length();
				} else {
					File[] subFiles = dir.listFiles();
					for (File file : subFiles) {
						if (file.isFile()) {
							size += file.length();
						} else {
							size += this.getDirSize(file);
						}

					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			return size;
		}
		@Override
		protected Double[] doInBackground(File... dir) {
			Double[] sizes = new Double[2];

			for (int i = 0; i!=sizes.length;i++){
				sizes[i]=this.getDirSize(dir[i]) / 1024 / 1024;
			}
			return sizes;
		}

		@Override
		protected void onPostExecute(Double[] result) {
			super.onPostExecute(result);
			redrawSizes(result);
			unlocked=true;
		}

	}

	private void redrawSizes(Double[] size) {
		if(!Build.DEVICE.equals("alien_jolla_bionic")){
            findPreference("clearapk").setSummary(getString(R.string.clearcontent_sum)+" ("+getString(R.string.cache_using_X_mb, new DecimalFormat("#.##").format(size[0]))+")");
            findPreference("clearcache").setSummary(getString(R.string.clearcache_sum)+" ("+getString(R.string.cache_using_X_mb, new DecimalFormat("#.##").format(size[1]))+")");
        }else{
            findPreference("clearapk").setSummary(getString(R.string.clearcontent_sum_jolla)+" ("+getString(R.string.cache_using_X_mb, new DecimalFormat("#.##").format(size[0]))+")");
            findPreference("clearcache").setSummary(getString(R.string.clearcache_sum_jolla)+" ("+getString(R.string.cache_using_X_mb, new DecimalFormat("#.##").format(size[1]))+")");
        }

	}


	static public boolean deleteDirectory(File path) {
	    if( path.exists() ) {
	      File[] files = path.listFiles();
	      if (files == null) {
	          return true;
	      }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
	    }
	    return true ;
	  }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
