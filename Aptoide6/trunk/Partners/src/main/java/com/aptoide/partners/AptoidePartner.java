package com.aptoide.partners;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.AptoideThemePicker;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.model.Login;
import cm.aptoide.ptdev.preferences.ManagerPreferences;

/**
 * Created by tdeus on 12/23/13.
 */
public class AptoidePartner extends Aptoide {



    public Login getLogin() {

        if(!TextUtils.isEmpty(getContext().getString(R.string.privacy_username))){

            Login login = new Login();
            Database database = new Database(Aptoide.getDb());
            Cursor store = database.getStore(-200);
            String username = null;
            String password = null;
            for(store.moveToFirst(); !store.isAfterLast(); store.moveToNext()){
                username = store.getString(store.getColumnIndex(Schema.Repo.COLUMN_USERNAME));
                password = store.getString(store.getColumnIndex(Schema.Repo.COLUMN_PASSWORD));
            }

            if(username==null){
                username = getContext().getString(R.string.privacy_username);
                password = getContext().getString(R.string.privacy_password);
            }

            if(username==null){
                return null;
            }

            login.setUsername(username);
            login.setPassword(password);

            return login;
        }

        return null;

    }


    public void setLogin(Login login) {
        Database database = new Database(Aptoide.getDb());
        database.updateStoreLogin(Aptoide.getConfiguration().getDefaultStore(), login.getUsername(), login.getPassword());
    }

    @Override
    public void bootImpl(ManagerPreferences managerPreferences) {
//        super.bootImpl(managerPreferences);
        Crashlytics.start(this);

        SharedPreferences sPref = getContext().getSharedPreferences("aptoide_settings", 0);
        /*if (sPref.contains("PARTNERID") && sPref.getString("PARTNERID", null) != null) {

            AptoideConfigurationPartners.PARTNERID = sPref.getString("PARTNERID", null);
            AptoideConfigurationPartners.PARTNERTYPE = sPref.getString("PARTNERTYPE", null);
            AptoideConfigurationPartners.DEFAULTSTORENAME = sPref.getString("DEFAULTSTORE", null);
            AptoideConfigurationPartners.MATURECONTENTSWITCH = sPref.getBoolean("MATURECONTENTSWITCH", true);
            AptoideConfigurationPartners.BRAND = sPref.getString("BRAND", "");
            AptoideConfigurationPartners.SPLASHSCREEN = sPref.getString("SPLASHSCREEN", null);
            AptoideConfigurationPartners.SPLASHSCREENLAND = sPref.getString("SPLASHSCREENLAND", null);
            AptoideConfigurationPartners.MATURECONTENTSWITCHVALUE = sPref.getBoolean("MATURECONTENTSWITCHVALUE", true);
            AptoideConfigurationPartners.MULTIPLESTORES = sPref.getBoolean("MULTIPLESTORES", true);
            AptoideConfigurationPartners.CUSTOMEDITORSCHOICE = sPref.getBoolean("CUSTOMEDITORSCHOICE", false);
            AptoideConfigurationPartners.SEARCHSTORES = sPref.getBoolean("SEARCHSTORES", true);
            AptoideConfigurationPartners.APTOIDETHEME = sPref.getString("APTOIDETHEME", "");
            AptoideConfigurationPartners.MARKETNAME = sPref.getString("MARKETNAME", "Aptoide");
            AptoideConfigurationPartners.ADUNITID = sPref.getString("ADUNITID", "18947d9a99e511e295fa123138070049");
            AptoideConfigurationPartners.CREATESHORTCUT = sPref.getBoolean("CREATESHORTCUT", true);
            AptoideConfigurationPartners.SPLASHCOLOR = sPref.getString("SPLASHCOLOR", "");
            AptoideConfigurationPartners.SHOWSPLASH = sPref.getBoolean("SHOWSPLASH", true);
            AptoideConfigurationPartners.SHOWADS = sPref.getBoolean("SHOWADS", true);
            AptoideConfigurationPartners.TIMELINE = sPref.getBoolean("TIMELINE", false);
            AptoideConfigurationPartners.ITEMS = sPref.getString("STOREITEMS", "applications,games,top_apps,latest_apps,latest_comments,latest_likes,favorites,recommended");
            AptoideConfigurationPartners.DESCRIPTION = sPref.getString("STOREDESCRIPTION", "");
            AptoideConfigurationPartners.THEME = sPref.getString("STORETHEME", "default");
            AptoideConfigurationPartners.AVATAR = sPref.getString("STOREAVATAR", "https://www.aptoide.com/includes/themes/default/images/repo_default_icon.png");
            AptoideConfigurationPartners.VIEW = sPref.getString("STOREVIEW", "list");

            AptoideConfigurationPartners.RESTRICTIONLIST = SecurePreferences.getInstance().getString("RESTRICTIONLIST", null);
            Log.d("Restriction List", "Retrived from secure preferences: " + AptoideConfigurationPartners.RESTRICTIONLIST);
            if (AptoideConfigurationPartners.PARTNERID != null && !new File(AptoideConfigurationPartners.SDCARD + "/.aptoide_settings/oem").exists()) {
                AptoideConfigurationPartners.createSdCardBinary();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isUpdate()) {
                            BufferedInputStream is = new BufferedInputStream(new URL("http://" + AptoideConfigurationPartners.DEFAULTSTORENAME + ".store.aptoide.com/boot_config.xml").openStream());
                            AptoideConfigurationPartners.parseBootConfigStream(is);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } else if (new File(AptoideConfigurationPartners.SDCARD + "/.aptoide_settings/oem").exists() && !getPackageName().equals("cm.aptoide.pt")) {

            try {
                Log.d("AptoidePartner", "Regenerating settings from SDCard.");
                HashMap<String, String> map = (HashMap<String, String>) new ObjectInputStream(new FileInputStream(new File(AptoideConfigurationPartners.SDCARD + "/.aptoide_settings/oem"))).readObject();

                AptoideConfigurationPartners.PARTNERID = map.get("PARTNERID");
                AptoideConfigurationPartners.PARTNERTYPE = map.get("PARTNERTYPE");
                AptoideConfigurationPartners.DEFAULTSTORENAME = map.get("DEFAULTSTORE");
                AptoideConfigurationPartners.MATURECONTENTSWITCH = Boolean.parseBoolean(map.get("MATURECONTENTSWITCH"));
                AptoideConfigurationPartners.BRAND = map.get("BRAND");
                AptoideConfigurationPartners.SPLASHSCREEN = map.get("SPLASHSCREEN");
                AptoideConfigurationPartners.SPLASHSCREENLAND = map.get("SPLASHSCREENLAND");
                AptoideConfigurationPartners.MATURECONTENTSWITCHVALUE = Boolean.parseBoolean(map.get("MATURECONTENTSWITCHVALUE"));
                AptoideConfigurationPartners.MULTIPLESTORES = Boolean.parseBoolean(map.get("MULTIPLESTORES"));
                AptoideConfigurationPartners.CUSTOMEDITORSCHOICE = Boolean.parseBoolean(map.get("CUSTOMEDITORSCHOICE"));
                AptoideConfigurationPartners.SEARCHSTORES = Boolean.parseBoolean(map.get("SEARCHSTORES"));
                AptoideConfigurationPartners.APTOIDETHEME = map.get("APTOIDETHEME");
                AptoideConfigurationPartners.MARKETNAME = map.get("MARKETNAME");
                AptoideConfigurationPartners.ADUNITID = map.get("ADUNITID");
                AptoideConfigurationPartners.CREATESHORTCUT = Boolean.parseBoolean(map.get("CREATESHORTCUT"));
                AptoideConfigurationPartners.SPLASHCOLOR = map.get("SPLASHCOLOR");
                AptoideConfigurationPartners.SHOWSPLASH = Boolean.parseBoolean(map.get("SHOWSPLASH"));
                AptoideConfigurationPartners.SHOWADS = Boolean.parseBoolean(map.get("SHOWADS"));
                AptoideConfigurationPartners.TIMELINE = Boolean.parseBoolean(map.get("TIMELINE"));
                AptoideConfigurationPartners.ITEMS = map.get("STOREITEMS");
                AptoideConfigurationPartners.DESCRIPTION = map.get("STOREDESCRIPTION");
                AptoideConfigurationPartners.THEME = map.get("STORETHEME");
                AptoideConfigurationPartners.AVATAR = map.get("STOREAVATAR");
                AptoideConfigurationPartners.VIEW = map.get("STOREVIEW");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (isUpdate()) {
                                BufferedInputStream is = new BufferedInputStream(new URL("http://" + AptoideConfigurationPartners.DEFAULTSTORENAME + ".store.aptoide.com/boot_config.xml").openStream());
                                AptoideConfigurationPartners.parseBootConfigStream(is);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


                AptoideConfigurationPartners.savePreferences();

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                InputStream inputStream = getContext().getAssets().open("boot_config.xml");
                AptoideConfigurationPartners.parseBootConfigStream(inputStream);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }

        }*/
        PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).edit().putBoolean("matureChkBox", ((AptoideConfigurationPartners)AptoidePartner.getConfiguration()).getMatureContentSwitchValue()).commit();
    }




    @Override
    public AptoideConfigurationPartners getAptoideConfiguration() {
        return new AptoideConfigurationPartners();
    }

    @Override
    public AptoideThemePicker getNewThemePicker() {
        return new com.aptoide.partners.AptoideThemePicker();
    }
}
