/*
 * ApplicationAptoide, part of Aptoide
 * Copyright (C) 2012 Duarte Silveira
 * duarte.silveira@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package cm.aptoide.pt;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import cm.aptoide.com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import cm.aptoide.com.nostra13.universalimageloader.core.DisplayImageOptions;
import cm.aptoide.com.nostra13.universalimageloader.core.ImageLoader;
import cm.aptoide.com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import cm.aptoide.com.nostra13.universalimageloader.core.assist.FlushedInputStream;
import cm.aptoide.com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import cm.aptoide.com.nostra13.universalimageloader.core.download.ImageDownloader;
import cm.aptoide.pt.preferences.ManagerPreferences;
import cm.aptoide.pt.util.NetworkUtils;
import org.holoeverywhere.app.Activity;
import org.holoeverywhere.app.Application;
import org.holoeverywhere.preference.SharedPreferences;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;

/**
 * ApplicationAptoide, centralizes, statically, calls to instantiated objects
 *
 * @author dsilveira
 *
 */
//@ReportsCrashes(
//   		formKey = "",
//   		formUri = "http://www.backendofyourchoice.com/reportpath"
//)

public class ApplicationAptoide extends Application {

	private ManagerPreferences managerPreferences;
	private static Context context;
	public static boolean DEBUG_MODE = false;
	public static File DEBUG_FILE;
    public static String PARTNERID;
    public static String DEFAULTSTORE;
    public static boolean MATURECONTENTSWITCH = true;
    public static String SPLASHSCREEN;
    public static String SPLASHSCREENLAND;
    public static String BRAND;
    public static boolean MATURECONTENTSWITCHVALUE = true;
    public static boolean MULTIPLESTORES = true;
    public static boolean CUSTOMEDITORSCHOICE = false;
    public static boolean SEARCHSTORES = true;
    public static String APTOIDETHEME = "";
    public static String MARKETNAME = "";

    public static String STORETHEME = null;
    public static String STORENAME = null;
    public static String STOREAVATAR = null;
    public static String STOREDESCRIPTION = null;
    public static String STOREVIEW = null;
    public static String STOREITEMS = null;
    private static boolean restartLauncher;

    public static boolean isRestartLauncher() {
        return restartLauncher;
    }

    public static void setRestartLauncher(boolean restartLauncher) {
        ApplicationAptoide.restartLauncher = restartLauncher;
    }

    static enum Elements {APTOIDEBOOT, PARTNERID, DEFAULTSTORE, BRAND, SPLASHSCREEN, MATURECONTENTSWITCH, MATURECONTENTSWITCHVALUE,SEARCHSTORES, MULTIPLESTORES, CUSTOMEDITORSCHOICE, APTOIDETHEME, SPLASHSCREENLAND, MARKETNAME,
    		STORETHEME, STORENAME, STOREAVATAR, STOREDESCRIPTION, STOREVIEW, STOREITEMS }


	@Override
	public void onCreate() {
//		ACRA.init(this);
		AptoideThemePicker.setAptoideTheme(this);
		setContext(getApplicationContext());
		 // Create global configuration and initialize ImageLoader with this configuration

		SharedPreferences sPref = getSharedPreferences("settings", MODE_PRIVATE);
        if(sPref.contains("PARTNERID")){

            PARTNERID = sPref.getString("PARTNERID",null);
            DEFAULTSTORE = sPref.getString("DEFAULTSTORE",null);
            MATURECONTENTSWITCH = sPref.getBoolean("MATURECONTENTSWITCH", true);
            BRAND = sPref.getString("BRAND", null);
            SPLASHSCREEN = sPref.getString("SPLASHSCREEN", null);
            SPLASHSCREENLAND = sPref.getString("SPLASHSCREEN_LAND", null);
            MATURECONTENTSWITCHVALUE = sPref.getBoolean("MATURECONTENTSWITCHVALUE", true);
            MULTIPLESTORES = sPref.getBoolean("MULTIPLESTORES",true);
            CUSTOMEDITORSCHOICE = sPref.getBoolean("CUSTOMEDITORSCHOICE",false);
            SEARCHSTORES = sPref.getBoolean("SEARCHSTORES",true);
            APTOIDETHEME = sPref.getString("APTOIDETHEME","DEFAULT");
            MARKETNAME = sPref.getString("MARKETNAME", "Aptoide");


        }else{
            try {
                InputStream file = getAssets().open("boot_config.xml");
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();

                parser.parse(file,new DefaultHandler(){
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
                                case PARTNERID:
                                    PARTNERID = sb.toString();
                                    Log.d("Partner ID", PARTNERID + "");
                                    break;
                                case DEFAULTSTORE:
                                    DEFAULTSTORE = sb.toString();
                                    Log.d("Default store", DEFAULTSTORE + "");
                                    break;
                                case BRAND:
                                    BRAND = sb.toString();
                                    Log.d("Brand", BRAND+ "");
                                    break;
                                case SPLASHSCREEN:
                                    SPLASHSCREEN = sb.toString();
                                    Log.d("Splashscreen", SPLASHSCREEN+ "");
                                    break;
                                case SPLASHSCREENLAND:
                                    SPLASHSCREENLAND = sb.toString();
                                    Log.d("Splashscreen landscape", SPLASHSCREENLAND+ "");
                                    break;
                                case MATURECONTENTSWITCH:
                                    MATURECONTENTSWITCH = Boolean.parseBoolean(sb.toString());
                                    Log.d("Mature content Switch", MATURECONTENTSWITCH+ "");
                                    break;
                                case MATURECONTENTSWITCHVALUE:
                                    MATURECONTENTSWITCHVALUE = Boolean.parseBoolean(sb.toString());
                                    Log.d("Mature content value", MATURECONTENTSWITCHVALUE+ "");
                                    break;
                                case MULTIPLESTORES:
                                    MULTIPLESTORES = Boolean.parseBoolean(sb.toString());
                                    Log.d("Multiple stores", MULTIPLESTORES+ "");
                                    break;
                                case CUSTOMEDITORSCHOICE:
                                    CUSTOMEDITORSCHOICE = Boolean.parseBoolean(sb.toString());
                                    Log.d("Custom editors choice", CUSTOMEDITORSCHOICE+ "");
                                    break;
                                case APTOIDETHEME:
                                	APTOIDETHEME = sb.toString();
                                	Log.d("Aptoide theme", APTOIDETHEME+ "");
                                    break;
                                case MARKETNAME:
                                	MARKETNAME = sb.toString();
                                	Log.d("Market name", MARKETNAME+ "");
                                	break;
                                case SEARCHSTORES:
                                    SEARCHSTORES = Boolean.parseBoolean(sb.toString());
                                    Log.d("Search stores", SEARCHSTORES+ "");
                                    break;

                                case STORETHEME:
                                	STORETHEME = sb.toString();
                                    Log.d("Store Theme", STORETHEME+ "");
                                    break;
                                case STOREAVATAR:
                                	STOREAVATAR = sb.toString();
                                    Log.d("Store avatar", STOREAVATAR+ "");
                                    break;
                                case STOREDESCRIPTION:
                                	STOREDESCRIPTION = sb.toString();
                                	Log.d("Store description", STOREDESCRIPTION+ "");
                                	break;
                                case STOREITEMS:
                                	STOREITEMS = sb.toString();
                                	Log.d("Store items", STOREITEMS+ "");
                                	break;
                                case STORENAME:
                                	STORENAME = sb.toString();
                                	Log.d("Store name", STORENAME+ "");
                                	break;
                                case STOREVIEW:
                                	STOREVIEW = sb.toString();
                                	Log.d("Store view", STOREVIEW+ "");
                                	break;

                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });



            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ParserConfigurationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (SAXException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            sPref.edit().putString("PARTNERID", PARTNERID)
            .putString("DEFAULTSTORE", DEFAULTSTORE)
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
            .commit();


            setRestartLauncher(true);
        }

        DEBUG_FILE = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/.aptoide/debug.log");

		if(DEBUG_FILE.exists()){
			DEBUG_MODE = true;
		}

		DisplayImageOptions options = new DisplayImageOptions.Builder()
															 .displayer(new FadeInBitmapDisplayer(1000))
															 .showStubImage(android.R.drawable.sym_def_app_icon)
															 .resetViewBeforeLoading()
															 .cacheInMemory()
															 .cacheOnDisc()
															 .build();

		ImageLoaderConfiguration config;

		if(DEBUG_MODE){
			config = new ImageLoaderConfiguration.Builder(getApplicationContext())
																				  .defaultDisplayImageOptions(options)
																				  .discCache(new UnlimitedDiscCache(new File(Environment.getExternalStorageDirectory().getPath()+"/.aptoide/icons/")))
																				  .enableLogging()
																				  .imageDownloader(new ImageDownloaderWithPermissions())
																				  .build();
		}else{
			config = new ImageLoaderConfiguration.Builder(getApplicationContext())
																				  .defaultDisplayImageOptions(options)
																				  .discCache(new UnlimitedDiscCache(new File(Environment.getExternalStorageDirectory().getPath()+"/.aptoide/icons/")))
																				  .imageDownloader(new ImageDownloaderWithPermissions())
																				  .build();
		}

        ImageLoader.getInstance().init(config);


		super.onCreate();

        managerPreferences = new ManagerPreferences(getApplicationContext());

    }

	public ManagerPreferences getManagerPreferences(){
		return managerPreferences;
	}

    public static void replaceOemIcon(){

        if(context.getSharedPreferences("settings", MODE_PRIVATE).contains("PARTNERID")){
            PackageManager pm = context.getPackageManager();

            for (EnumOem enumOem : EnumOem.values()) {

                if (enumOem.equals(EnumOem.valueOf(BRAND.toUpperCase(Locale.ENGLISH)))) {

                    pm.setComponentEnabledSetting(new ComponentName(context, "cm.aptoide.pt.Start-" + enumOem.name().toLowerCase(Locale.ENGLISH)),
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);

                    Log.d("ApplicationAptoide", "Setting " + enumOem.name().toLowerCase(Locale.ENGLISH) + " enabled");

                } else {

                    pm.setComponentEnabledSetting(new ComponentName(context, "cm.aptoide.pt.Start-" + enumOem.name().toLowerCase(Locale.ENGLISH)),
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);

                    Log.d("ApplicationAptoide", "Setting " + enumOem.name().toLowerCase(Locale.ENGLISH) + " disabled");


                }
                
                pm.setComponentEnabledSetting(new ComponentName(context, "cm.aptoide.pt.Start"),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);

            }
        }
    }

    @SuppressLint("NewApi")
	public static void restartLauncher(Context context) {
        try{

            replaceOemIcon();
            PackageManager pm = context.getPackageManager();
            ActivityManager am = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);

            try{
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD){
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.addCategory(Intent.CATEGORY_HOME);
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    List<ResolveInfo> resolves = pm.queryIntentActivities(i, 0);
                    for (ResolveInfo res : resolves) {
                        if (res.activityInfo != null) {
                            am.killBackgroundProcesses(res.activityInfo.packageName);
                            Log.d("ApplicationAptoide", "Killing: " + res.activityInfo.packageName);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d("ApplicationAptoide", "End restartLauncher");


    }

	/**
	 * @return the context
	 */
	public static Context getContext() {
		return context;
	}

	public class ImageDownloaderWithPermissions extends ImageDownloader{

		/** {@value} */
		public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
		/** {@value} */
		public static final int DEFAULT_HTTP_READ_TIMEOUT = 20 * 1000; // milliseconds

		private int connectTimeout;
		private int readTimeout;

		public ImageDownloaderWithPermissions() {
			this(DEFAULT_HTTP_CONNECT_TIMEOUT, DEFAULT_HTTP_READ_TIMEOUT);
		}

		public ImageDownloaderWithPermissions(int connectTimeout, int readTimeout) {
			this.connectTimeout = connectTimeout;
			this.readTimeout = readTimeout;
		}

		@Override
		public InputStream getStreamFromNetwork(URI imageUri) throws IOException {

	        boolean download = NetworkUtils.isPermittedConnectionAvailable(context, managerPreferences.getIconDownloadPermissions());

	        Log.d("AplicationAptoide", ""+download);
	        Log.d("AplicationAptoide", ""+managerPreferences.getIconDownloadPermissions());

	        if(download){
	        	URLConnection conn = imageUri.toURL().openConnection();
				conn.setConnectTimeout(connectTimeout);
				conn.setReadTimeout(readTimeout);
				return new FlushedInputStream(new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE));
	        }else{
	        	return null;
	        }
		}

	}

	/**
	 * @param context the context to set
	 */
	public static void setContext(Context context) {
		ApplicationAptoide.context = context;
	}
}
