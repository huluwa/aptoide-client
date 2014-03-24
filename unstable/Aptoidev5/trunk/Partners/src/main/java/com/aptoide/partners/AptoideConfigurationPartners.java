package com.aptoide.partners;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import cm.aptoide.ptdev.SearchManager;
import cm.aptoide.ptdev.configuration.AccountGeneral;
import cm.aptoide.ptdev.configuration.AptoideConfiguration;
import cm.aptoide.ptdev.configuration.Defaults;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by tdeus on 12/23/13.
 */
public class AptoideConfigurationPartners extends AptoideConfiguration {

    public static String PARTNERTYPE;
    public static String PARTNERID;
    public static String DEFAULTSTORENAME;
    public static boolean MATURECONTENTSWITCH = true;
    public static String SPLASHSCREEN;
    public static String SPLASHSCREENLAND;
    public static String BRAND = "";
    public static boolean MATURECONTENTSWITCHVALUE = true;
    public static boolean MULTIPLESTORES = true;
    public static boolean CUSTOMEDITORSCHOICE = false;
    public static boolean SEARCHSTORES = true;
    public static String APTOIDETHEME = "";
    public static String MARKETNAME = "";
    public static String ADUNITID = "";
    public static boolean CREATESHORTCUT = true;

    public static String THEME = null;
    public static String AVATAR = null;
    public static String DESCRIPTION = null;
    public static String VIEW = null;
    public static String ITEMS = null;
    public static String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String OEM_AUTO_UPDATE_URL = "http://%s.aptoide.com/latest_version_%s.xml";
    static enum Elements { BOOTCONF, APTOIDECONF, PARTNERTYPE, PARTNERID, DEFAULTSTORENAME, BRAND, SPLASHSCREEN, MATURECONTENTSWITCH, MATURECONTENTSWITCHVALUE,SEARCHSTORES, MULTIPLESTORES, CUSTOMEDITORSCHOICE, APTOIDETHEME, SPLASHSCREENLAND, MARKETNAME, ADUNITID, CREATESHORTCUT,
        STORECONF, THEME, AVATAR, DESCRIPTION, VIEW, ITEMS }

    private static Context context = AptoidePartner.getContext();

    @Override
    public String getAutoUpdateUrl(){
        return String.format(OEM_AUTO_UPDATE_URL, DEFAULTSTORENAME, DEFAULTSTORENAME);
    }


    public String getDefaultTopAppsUrl(){
        return super.getTopAppsUrl();
    }


    public String getDefaultEditorsUrl(){

        return super.getEditorsUrl();
    }

    @Override
    public String getTopAppsUrl(){

        if(CUSTOMEDITORSCHOICE){
            return "http://" + DEFAULTSTORENAME + ".store.aptoide.com/top.xml";
        }

        return super.getTopAppsUrl();
    }


    @Override
    public String getEditorsUrl(){

        if(CUSTOMEDITORSCHOICE){
            return "http://" + DEFAULTSTORENAME + ".store.aptoide.com/editors_more.xml";
        }

        return super.getEditorsUrl();
    }

    public String getPartnerType() { return PARTNERTYPE; }
    public static void setPartnerType(String partnerType){ AptoideConfigurationPartners.PARTNERTYPE = partnerType; }

    public static String getPartnerId() { return PARTNERID; }
    public static void setPartnerId(String partnerId){ AptoideConfigurationPartners.PARTNERID = partnerId; }

    @Override
    public String getDefaultStore() { return DEFAULTSTORENAME; }
    public static void setDefaultStoreName(String defaultStoreName){ AptoideConfigurationPartners.DEFAULTSTORENAME = defaultStoreName; }

    public boolean getMatureContentSwitch() { return MATURECONTENTSWITCH; }
    public static void setMatureContentSwitch(boolean matureContentSwitch) { AptoideConfigurationPartners.MATURECONTENTSWITCH = matureContentSwitch; }

    public String getSplashscreen(){ return SPLASHSCREEN; }
    public static void setSplashscreen(String splashscreen){ AptoideConfigurationPartners.SPLASHSCREEN = splashscreen; }

    public String getSplashscreenLand(){ return SPLASHSCREENLAND; }
    public static void setSplashscreenLand(String splashscreenLand){ AptoideConfigurationPartners.SPLASHSCREENLAND = splashscreenLand; }

    public String getBrand(){ return BRAND; }
    public static void setBrand(String stringBrand){ AptoideConfigurationPartners.BRAND = stringBrand; }

    public boolean getMatureContentSwitchValue() { return MATURECONTENTSWITCHVALUE; }
    public void setMatureContentSwitchValue(boolean matureContentSwitchValue) { AptoideConfigurationPartners.MATURECONTENTSWITCHVALUE = matureContentSwitchValue; }

    public boolean getMultistores() { return MULTIPLESTORES; }
    public static void setMultistores(boolean multistores) { AptoideConfigurationPartners.MULTIPLESTORES = multistores; }

    public boolean getCustomEditorsChoice() { return CUSTOMEDITORSCHOICE; }
    public static void setCustomEditorsChoice(boolean customEditorsChoice) { AptoideConfigurationPartners.CUSTOMEDITORSCHOICE = customEditorsChoice; }

    public boolean getSearchStores() { return SEARCHSTORES; }
    public static void setSearchStores(boolean searchStores) { AptoideConfigurationPartners.SEARCHSTORES = searchStores; }

    public String getTheme() { return APTOIDETHEME.toUpperCase(Locale.ENGLISH); }
    public static void setTheme(String appTheme) { AptoideConfigurationPartners.APTOIDETHEME = appTheme; }

    public String getMarketName(){ return MARKETNAME; }
    public static void setMarketName(String marketName){ AptoideConfigurationPartners.MARKETNAME = marketName; }

    public String getAdUnitId(){ return ADUNITID; }
    public static void setAdUnitId(String adUnitId){ AptoideConfigurationPartners.ADUNITID = adUnitId; }

    @Override
    public String getExtraId(){
        return PARTNERID;
    }

    public boolean getCreateShortcut() { return CREATESHORTCUT; }
    public static void setCreateShortcut(boolean createShortcut) { AptoideConfigurationPartners.CREATESHORTCUT = createShortcut; }

    public String getStoreAvatar(){ return AVATAR; }
    public static void setStoreAvatar(String avatar){ AptoideConfigurationPartners.AVATAR = avatar; }

    public String getStoreTheme(){ return THEME; }
    public static void setStoreTheme(String theme){ AptoideConfigurationPartners.THEME = theme; }

    public String getStoreDescription(){ return DESCRIPTION; }
    public static void setDescription(String description){ AptoideConfigurationPartners.DESCRIPTION = description; }

    public String getStoreView(){ return VIEW; }
    public static void setView(String view){ AptoideConfigurationPartners.VIEW = view; }

    @Override
    public boolean isSaveOldRepos(){
        return false;
    }

    public String getStoreItems(){ return ITEMS; }
    public static void setItems(String items){ AptoideConfigurationPartners.ITEMS = items; }

    public static void parseBootConfigStream(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();


        parser.parse(inputStream,new DefaultHandler(){
            StringBuilder sb = new StringBuilder();

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                super.startElement(uri, localName, qName, attributes);
                sb.setLength(0);
            }


            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
                super.characters(ch, start, length);    //To change body of overridden methods use File | Settings | File Templates.
                sb.append(ch,start,length);
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                super.endElement(uri, localName, qName);
                try{
                    Elements element = Elements.valueOf(localName.toUpperCase(Locale.ENGLISH));
                    switch (element) {
                        case PARTNERTYPE:
                            PARTNERTYPE = sb.toString();
                            AptoideConfigurationPartners.setPartnerType(PARTNERTYPE);
                            Log.d("Partner type", PARTNERTYPE + "");
                            break;
                        case PARTNERID:
                            PARTNERID = sb.toString();
                            AptoideConfigurationPartners.setPartnerId(PARTNERID);
                            Log.d("Partner ID", PARTNERID + "");
                            break;
                        case DEFAULTSTORENAME:
                            DEFAULTSTORENAME = sb.toString();
                            AptoideConfigurationPartners.setDefaultStoreName(DEFAULTSTORENAME);
                            Log.d("Default store", DEFAULTSTORENAME + "");
                            break;
                        case BRAND:
                            BRAND = sb.toString();
                            AptoideConfigurationPartners.setBrand(BRAND);
                            Log.d("Brand", BRAND+ "");
                            break;
                        case SPLASHSCREEN:
                            SPLASHSCREEN = sb.toString();
                            AptoideConfigurationPartners.setSplashscreen(SPLASHSCREEN);
                            Log.d("Splashscreen", SPLASHSCREEN+ "");
                            break;
                        case SPLASHSCREENLAND:
                            SPLASHSCREENLAND = sb.toString();
                            AptoideConfigurationPartners.setSplashscreenLand(SPLASHSCREENLAND);
                            Log.d("Splashscreen landscape", SPLASHSCREENLAND+ "");
                            break;
                        case MATURECONTENTSWITCH:
                            MATURECONTENTSWITCH = Boolean.parseBoolean(sb.toString());
                            AptoideConfigurationPartners.setMatureContentSwitch(MATURECONTENTSWITCH);
                            Log.d("Mature content Switch", MATURECONTENTSWITCH + "");
                            break;
                        case MATURECONTENTSWITCHVALUE:
                            MATURECONTENTSWITCHVALUE = Boolean.parseBoolean(sb.toString());
                            AptoideConfigurationPartners.setMatureContentSwitch(MATURECONTENTSWITCHVALUE);
                            Log.d("Mature content value", MATURECONTENTSWITCHVALUE+ "");
                            break;
                        case MULTIPLESTORES:
                            MULTIPLESTORES = Boolean.parseBoolean(sb.toString());
                            AptoideConfigurationPartners.setMultistores(MULTIPLESTORES);
                            Log.d("Multiple stores", MULTIPLESTORES+ "");
                            break;
                        case CUSTOMEDITORSCHOICE:
                            CUSTOMEDITORSCHOICE = Boolean.parseBoolean(sb.toString());
                            AptoideConfigurationPartners.setCustomEditorsChoice(CUSTOMEDITORSCHOICE);
                            Log.d("Custom editors choice", CUSTOMEDITORSCHOICE+ "");
                            break;
                        case APTOIDETHEME:
                            APTOIDETHEME = sb.toString();
                            AptoideConfigurationPartners.setTheme(APTOIDETHEME);
                            Log.d("APTOIDETHEME", APTOIDETHEME+ "");
                            break;
                        case MARKETNAME:
                            MARKETNAME = sb.toString();
                            AptoideConfigurationPartners.setMarketName(MARKETNAME);
                            Log.d("Market name", MARKETNAME+ "");
                            break;
                        case SEARCHSTORES:
                            SEARCHSTORES = Boolean.parseBoolean(sb.toString());
                            AptoideConfigurationPartners.setSearchStores(SEARCHSTORES);
                            Log.d("Search stores", SEARCHSTORES+ "");
                            break;
                        case ADUNITID:
                            ADUNITID = sb.toString();
                            AptoideConfigurationPartners.setAdUnitId(ADUNITID);
                            Log.d("AdUnitId", ADUNITID+ "");
                            break;
                        case CREATESHORTCUT:
                            CREATESHORTCUT = Boolean.parseBoolean(sb.toString());
                            AptoideConfigurationPartners.setCreateShortcut(CREATESHORTCUT);
                            Log.d("Create Shortcut", CREATESHORTCUT+ "");
                            break;

                        case THEME:
                            THEME = sb.toString();
                            AptoideConfigurationPartners.setStoreTheme(THEME);
                            Log.d("Store Theme", THEME+ "");
                            break;
                        case AVATAR:
                            AVATAR = sb.toString();
                            AptoideConfigurationPartners.setStoreAvatar(AVATAR);
                            Log.d("Store avatar", AVATAR+ "");
                            break;
                        case DESCRIPTION:
                            DESCRIPTION = sb.toString();
                            AptoideConfigurationPartners.setDescription(DESCRIPTION);
                            Log.d("Store description", DESCRIPTION+ "");
                            break;
                        case ITEMS:
                            ITEMS = sb.toString();
                            AptoideConfigurationPartners.setItems(ITEMS);
                            Log.d("Store items", ITEMS+ "");
                            break;
                        case VIEW:
                            VIEW = sb.toString();
                            AptoideConfigurationPartners.setView(VIEW);
                            Log.d("Store view", VIEW+ "");
                            break;

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        savePreferences();

        createSdCardBinary();
    }

    public static void createSdCardBinary() {
        if (PARTNERID != null) {

            HashMap<String, String> map = new HashMap<String, String>();

            map.put("PARTNERID", PARTNERID);
            map.put("PARTNERTYPE", PARTNERTYPE);
            map.put("DEFAULTSTORE", DEFAULTSTORENAME);
            map.put("MATURECONTENTSWITCH", MATURECONTENTSWITCH + "");
            map.put("BRAND", BRAND);
            map.put("SPLASHSCREENLAND", SPLASHSCREENLAND);
            map.put("SPLASHSCREEN", SPLASHSCREEN);
            map.put("MATURECONTENTSWITCHVALUE", MATURECONTENTSWITCHVALUE + "");
            map.put("MULTIPLESTORES", MULTIPLESTORES + "");
            map.put("CUSTOMEDITORSCHOICE", CUSTOMEDITORSCHOICE + "");
            map.put("SEARCHSTORES", SEARCHSTORES + "");
            map.put("APTOIDETHEME", APTOIDETHEME);
            map.put("MARKETNAME", MARKETNAME);
            map.put("ADUNITID", ADUNITID);
            map.put("CREATESHORTCUT", CREATESHORTCUT + "");
            map.put("STOREDESCRIPTION", DESCRIPTION);
            map.put("STORETHEME", THEME);
            map.put("STOREAVATAR", AVATAR);
            map.put("STOREITEMS", ITEMS);
            map.put("STOREVIEW", VIEW);

            try {
                File fileDir = new File(SDCARD + "/.aptoide_settings");
                if (fileDir.mkdir()) {
                    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(SDCARD + "/.aptoide_settings/oem")));
                    oos.writeObject(map);
                    oos.close();

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void savePreferences() {

        SharedPreferences sPref = context.getSharedPreferences("aptoide_settings", 0);

        sPref.edit().putString("PARTNERID", PARTNERID)
                .putString("DEFAULTSTORE", DEFAULTSTORENAME)
                .putString("PARTNERTYPE", PARTNERTYPE)
                .putBoolean("MATURECONTENTSWITCH", MATURECONTENTSWITCH)
                .putString("BRAND", BRAND)
                .putString("SPLASHSCREENLAND", SPLASHSCREENLAND)
                .putString("SPLASHSCREEN", SPLASHSCREEN)
                .putBoolean("MATURECONTENTSWITCHVALUE", MATURECONTENTSWITCHVALUE)
                .putBoolean("MULTIPLESTORES", MULTIPLESTORES)
                .putBoolean("CUSTOMEDITORSCHOICE", CUSTOMEDITORSCHOICE)
                .putBoolean("SEARCHSTORES", SEARCHSTORES)
                .putString("APTOIDETHEME", APTOIDETHEME)
                .putString("MARKETNAME", MARKETNAME)
                .putString("ADUNITID", ADUNITID)
                .putBoolean("CREATESHORTCUT", CREATESHORTCUT)
                .putString("STOREDESCRIPTION", DESCRIPTION)
                .putString("STOREAVATAR", AVATAR)
                .putString("STORETHEME", THEME)
                .putString("STOREITEMS", ITEMS)
                .putString("STOREVIEW", VIEW)
                .commit();
    }

    @Override
    public Class getStartActivityClass(){
        return StartPartner.class;
    }

    public Class getAppViewActivityClass() {
        return com.aptoide.partners.AppViewActivityPartner.class;
    }

    public Class getSettingsActivityClass() {
        return com.aptoide.partners.SettingsPartner.class;
    }

    public String getUpdatesSyncAdapterAuthority(){
        return "com.aptoide.partners.UpdatesProvider";
    }

    public String getAutoUpdatesSyncAdapterAuthority(){
        return "com.aptoide.partners.AutoUpdateProvider";
    }

    public String getAccountType() { return AccountGeneralPartners.ACCOUNT_TYPE;
    };

    @Override
    public Class<?> getSearchActivityClass() {
        return com.aptoide.partners.SearchManagerPartners.class;
    }
}
