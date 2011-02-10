package cm.aptoide.pt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleAdapter.ViewBinder;

public class BaseManagement extends Activity {

	static private PackageManager mPm;
	static private PackageInfo pkginfo;
	
	static private DbHandler db = null;
	static private Context mctx = null;
	
	static protected SharedPreferences sPref;
	static protected SharedPreferences.Editor prefEdit;

	static private ProgressDialog pd;
	
	static private Vector<ApkNode> apk_lst = null;
	static private String order_lst = "abc";
	
	private String my_order = "abc";
	
	static protected SimpleAdapter availAdpt = null;
	static protected SimpleAdapter instAdpt = null;
	static protected SimpleAdapter updateAdpt = null;
	
	protected static final int INSTALL = 127;
	protected static final int REMOVE = 128;
	protected static final int UPDATE = 129;
	private static final String APK_PATH = "/sdcard/.aptoide/";
	
	
	private static SimpleAdapter main_catg_adpt = null;
	private static SimpleAdapter app_catg_adpt = null;
	private static SimpleAdapter game_catg_adpt = null;
	
	private static final String[] main_ctg = {"Applications", "Games", "Others"};
	private static final String[] app_ctg = {"Comics", "Communication", "Entertainment", "Finance", "Health", "Lifestyle", "Multimedia", 
   		 "News & Weather", "Productivity", "Reference", "Shopping", "Social", "Sports", "Themes", "Tools", 
		 "Travel", "Demo", "Software Libraries", "Other"};
	private static final String[] game_ctg = {"Arcade & Action", "Brain & Puzzle", "Cards & Casinon", "Casual", "Other"};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mPm = getPackageManager();
		db = new DbHandler(this);
		mctx = this;
		sPref = getSharedPreferences("aptoide_prefs", MODE_PRIVATE);
		prefEdit = sPref.edit();
			
		int[] main_ctg_count = db.getCountMainCtg();
        Map<String, Object> count_lst_app = db.getCountSecCatg(1);
        Map<String, Object> count_lst_games = db.getCountSecCatg(0);
		
		List<Map<String, Object>> main_catg = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> app_catg = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> game_catg = new ArrayList<Map<String, Object>>();
        Map<String, Object> apk_line = null;
        
        int p = 0;
        for (String node : main_ctg) {
        	apk_line = new HashMap<String, Object>();
        	apk_line.put("name", node);
            apk_line.put("cat_count",main_ctg_count[p] + " available");
            main_catg.add(apk_line);
            p++;
		}
        main_catg_adpt = new SimpleAdapter(mctx, main_catg, R.layout.catglist, 
         		new String[] {"name", "name", "cat_count"}, new int[] {R.id.cntrl, R.id.name, R.id.cat_count});
        
        
        for (String node : app_ctg) {
        	Integer count = (Integer)count_lst_app.get(node);
        	if (count == null)
        		count = 0;
        	apk_line = new HashMap<String, Object>();
        	apk_line.put("cntrl", "apps");
        	apk_line.put("name", node);
            apk_line.put("cat_count",count + " available");
            app_catg.add(apk_line);
		}
        app_catg_adpt = new SimpleAdapter(mctx, app_catg, R.layout.catglist, 
         		new String[] {"cntrl", "name", "cat_count"}, new int[] {R.id.cntrl, R.id.name, R.id.cat_count});
        
        
        for (String node : game_ctg) {
        	Integer count = (Integer)count_lst_games.get(node);
        	if (count == null)
        		count = 0;
        	apk_line = new HashMap<String, Object>();
        	apk_line.put("cntrl", "games");
        	apk_line.put("name", node);
        	apk_line.put("cat_count",count + " files");
        	game_catg.add(apk_line);
        }
       game_catg_adpt = new SimpleAdapter(mctx, game_catg, R.layout.catglist, 
        		new String[] {"cntrl", "name", "cat_count"}, new int[] {R.id.cntrl, R.id.name, R.id.cat_count});
        
	}
	
	protected boolean resumeMe(){
		String istype;
		if(my_order.equalsIgnoreCase("abc")){
			my_order = "recent";
			istype = "Most recent first";
		}else{
			my_order = "abc";
			istype = "Alphabetically";
		}
		if(!order_lst.equalsIgnoreCase(my_order)){
			order_lst = my_order;
			redraw();
		}
		Toast.makeText(mctx, istype, Toast.LENGTH_SHORT).show();
		return true;
	}
		
	@Override
	protected void onResume() {
		super.onResume();

		if(sPref.contains("order_lst")){
			order_lst = sPref.getString("order_lst", "abc");
			prefEdit.remove("order_lst");
			prefEdit.commit();
			redraw();
		}
		
		if(sPref.getBoolean("update", false)){
			redraw();
			prefEdit.remove("update");
			prefEdit.commit();
		}
	}

	protected void removeApk(String apk_pkg){
		try {
			pkginfo = mPm.getPackageInfo(apk_pkg, 0);
		} catch (NameNotFoundException e) {	}
		Uri uri = Uri.fromParts("package", pkginfo.packageName, null);
		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		prefEdit.putString("pkg", pkginfo.packageName);
		prefEdit.commit();
		startActivityForResult(intent,REMOVE); 
	}
	
	protected void installApk(String apk_path){
		pkginfo = mPm.getPackageArchiveInfo(apk_path, 0);
		if(pkginfo == null){
			// Ficheiro está corrupto, temos de verificar!
		}else{
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + apk_path), "application/vnd.android.package-archive");
			prefEdit.putString("pkg", pkginfo.packageName);
			prefEdit.commit();
			startActivityForResult(intent,INSTALL);
		}
	}
	
	protected void updateApk(String apk_path, String apk_id){	
		pkginfo = mPm.getPackageArchiveInfo(apk_path, 0);
		Intent intent = new Intent();
    	intent.setAction(android.content.Intent.ACTION_VIEW);
    	intent.setDataAndType(Uri.parse("file://" + apk_path), "application/vnd.android.package-archive");
    	prefEdit.putString("pkg", pkginfo.packageName);
    	prefEdit.commit();
    	startActivityForResult(intent,UPDATE);
	}
	
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == INSTALL){
			String apkid = sPref.getString("pkg", null);
			try {
				pkginfo = mPm.getPackageInfo(apkid, 0);
				db.insertInstalled(apkid);
				prefEdit.remove("pkg");
				prefEdit.commit();
				redraw();
			} catch (NameNotFoundException e) {	}
		}else if(requestCode == REMOVE){
			String apkid = sPref.getString("pkg", null);
			try {
				pkginfo = mPm.getPackageInfo(apkid, 0);
			} catch (NameNotFoundException e) {	
				db.removeInstalled(apkid);
				prefEdit.remove("pkg");
				prefEdit.commit();
				redraw();
			}
		}else if(requestCode == UPDATE){
			String apkid = sPref.getString("pkg", null);
			try {
				pkginfo = mPm.getPackageInfo(apkid, 0);
			} catch (NameNotFoundException e) {	}
			int vercode = pkginfo.versionCode;
			if(db.wasUpdate(apkid, vercode)){
				db.removeInstalled(apkid);
				db.insertInstalled(apkid);
				prefEdit.remove("pkg");
				prefEdit.commit();
				redraw();
			}
		}
	}

	protected void redraw(){
		
		List<Map<String, Object>> availMap = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> instMap = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> updtMap = new ArrayList<Map<String, Object>>();
        Map<String, Object> apk_line;
        
        if(apk_lst != null)
        	apk_lst.clear();
        apk_lst = db.getAll(order_lst);
        
        
		for(ApkNode node: apk_lst){
         	apk_line = new HashMap<String, Object>();
         	apk_line.put("pkg", node.apkid);
         	String iconpath = new String(getString(R.string.icons_path)+node.apkid);
         	apk_line.put("icon", iconpath);
         	apk_line.put("rat", node.rat);
         	if(node.status == 1){
         		apk_line.put("status", getString(R.string.installed) + " " + node.ver);
         		apk_line.put("name", node.name);
         		instMap.add(apk_line);
         	}else if(node.status == 2){
         		apk_line.put("status2", getString(R.string.installed_update) + " " + node.ver);
         		apk_line.put("name2", node.name);
         		updtMap.add(apk_line);
         		instMap.add(apk_line);
         	}else{
         		apk_line.put("status", "Version: " + node.ver);
         		apk_line.put("name", node.name);
         		availMap.add(apk_line);
         	}
         }
		
         availAdpt = new SimpleAdapter(mctx, availMap, R.layout.listicons, 
         		new String[] {"pkg", "name", "name2", "status", "status2", "icon", "rat"}, new int[] {R.id.pkg, R.id.name, R.id.nameup, R.id.isinst, R.id.isupdt, R.id.appicon, R.id.rating});
         
         availAdpt.setViewBinder(new LstBinder());
                
         instAdpt = new SimpleAdapter(mctx, instMap, R.layout.listicons, 
          		new String[] {"pkg", "name", "name2", "status", "status2", "icon", "rat"}, new int[] {R.id.pkg, R.id.name, R.id.nameup, R.id.isinst, R.id.isupdt, R.id.appicon, R.id.rating});
          
         instAdpt.setViewBinder(new LstBinder());
         
         updateAdpt = new SimpleAdapter(mctx, updtMap, R.layout.listicons, 
          		new String[] {"pkg", "name", "name2", "status", "status2", "icon", "rat"}, new int[] {R.id.pkg, R.id.name, R.id.nameup, R.id.isinst, R.id.isupdt, R.id.appicon, R.id.rating});
         
         updateAdpt.setViewBinder(new LstBinder());
         
		 prefEdit.putBoolean("changeavail", true);
		 prefEdit.putBoolean("changeinst", true);
		 prefEdit.putBoolean("changeupdt", true);
		 prefEdit.commit();
		 
	}
	
	
	 class LstBinder implements ViewBinder
		{
			public boolean setViewValue(View view, Object data, String textRepresentation)
			{
				if(view.getClass().toString().equalsIgnoreCase("class android.widget.RatingBar")){
					RatingBar tmpr = (RatingBar)view;
					tmpr.setRating(new Float(textRepresentation));
				}else if(view.getClass().toString().equalsIgnoreCase("class android.widget.TextView")){
					TextView tmpr = (TextView)view;
					tmpr.setText(textRepresentation);
				}else if(view.getClass().toString().equalsIgnoreCase("class android.widget.ImageView")){
					ImageView tmpr = (ImageView)view;	
					File icn = new File(textRepresentation);
					if(icn.exists() && icn.length() > 0){
						new Uri.Builder().build();
	    				tmpr.setImageURI(Uri.parse(textRepresentation));
	             	}else{
	             		tmpr.setImageResource(android.R.drawable.sym_def_app_icon);
	             	}
				}else if(view.getClass().toString().equalsIgnoreCase("class android.widget.LinearLayout")){
					LinearLayout tmpr = (LinearLayout)view;
					tmpr.setTag(textRepresentation);
				}else{
					return false;
				}
				return true;
			}
		}

	 class SimpeLstBinder implements ViewBinder
		{
			public boolean setViewValue(View view, Object data, String textRepresentation)
			{
				if(view.getClass().toString().equalsIgnoreCase("class android.widget.TextView")){
					TextView tmpr = (TextView)view;
					tmpr.setText(textRepresentation);
				}else if(view.getClass().toString().equalsIgnoreCase("class android.widget.LinearLayout")){
					LinearLayout tmpr = (LinearLayout)view;
					tmpr.setTag(textRepresentation);
				}else{
					return false;
				}
				return true;
			}
		}
	 
	 protected String downloadFile(String apkid){
		 Vector<DownloadNode> tmp_serv = new Vector<DownloadNode>();
		 String getserv = new String();
		 String md5hash = null;
		 String repo = null;

		 try{

			 tmp_serv = db.getPathHash(apkid);

			 if(tmp_serv.size() > 0){
				 DownloadNode node = new DownloadNode();
				 node = tmp_serv.firstElement();
				 getserv = node.repo + "/" + node.path;
				 md5hash = node.md5h;
				 repo = node.repo;
			 }

			 
			 if(getserv.length() == 0)
				 throw new TimeoutException();

			 Message msg = new Message();
			 msg.arg1 = 0;
			 msg.obj = new String(getserv);
			 download_handler.sendMessage(msg);

			 String path = new String(APK_PATH+db.getName(apkid)+".apk");
			 FileOutputStream saveit = new FileOutputStream(path);
			 
			 HttpResponse mHttpResponse = NetworkApis.getHttpResponse(getserv, repo, mctx);
			 
			 
			 if(mHttpResponse.getStatusLine().getStatusCode() == 401){
				 return null;
			 }else{
				 InputStream getit = mHttpResponse.getEntity().getContent();
                 byte data[] = new byte[8096];
				 int readed;
				 while((readed = getit.read(data, 0, 8096)) != -1) {
					 saveit.write(data,0,readed);
				 }
			 }
			 File f = new File(path);
			 Md5Handler hash = new Md5Handler();
			 
			 if(md5hash == null || md5hash.equalsIgnoreCase(hash.md5Calc(f))){
				 return path;
			 }else{
				 return null;
			 }
		 } catch(Exception e){
			 return null;
		 }
	 }
	 
	 
	 protected SimpleAdapter getRootCtg(){
		 main_catg_adpt.setViewBinder(new SimpeLstBinder());
		 return main_catg_adpt;
	 }
	 
	 protected SimpleAdapter getAppCtg(){
		 app_catg_adpt.setViewBinder(new SimpeLstBinder());
		 return app_catg_adpt;
	 }
	 
	 protected SimpleAdapter getGamesCtg(){
		 game_catg_adpt.setViewBinder(new SimpeLstBinder());
		 return game_catg_adpt;
	 }
	 
	 protected SimpleAdapter getAvailable(){
		 if(sPref.getBoolean("mode", false)){
        	 main_catg_adpt.setViewBinder(new SimpeLstBinder());
        	 return main_catg_adpt;
         }
		 return availAdpt;
	 }
	 
	 protected SimpleAdapter getGivenCatg(String ctg, int ord){
		 Log.d("Aptoide","Fetch: " + ctg);
		 List<Map<String, Object>> availMap = new ArrayList<Map<String, Object>>();
		 Map<String, Object> apk_line;
		 Vector<ApkNode> tmp_lst = null;
		 SimpleAdapter rtnadp = null;

		 if(tmp_lst != null)
			 tmp_lst.clear();
		 tmp_lst = db.getAll(order_lst, ctg, ord);


		 for(ApkNode node: tmp_lst){
			 apk_line = new HashMap<String, Object>();
			 apk_line.put("pkg", node.apkid);
			 String iconpath = new String(getString(R.string.icons_path)+node.apkid);
			 apk_line.put("icon", iconpath);
			 apk_line.put("rat", node.rat);
			 apk_line.put("status", "Version: " + node.ver);
			 apk_line.put("name", node.name);
			 availMap.add(apk_line);
		 }


		 rtnadp = new SimpleAdapter(mctx, availMap, R.layout.listicons, 
				 new String[] {"pkg", "name", "name2", "status", "status2", "icon", "rat"}, new int[] {R.id.pkg, R.id.name, R.id.nameup, R.id.isinst, R.id.isupdt, R.id.appicon, R.id.rating});

		 rtnadp.setViewBinder(new LstBinder());

		 return rtnadp;
	 }

	 protected Handler download_handler = new Handler() {
		 @Override
		 public void handleMessage(Message msg) {
			 if(msg.arg1 == 0){
				 pd = ProgressDialog.show(mctx, "Download", getString(R.string.download_alrt) + msg.obj.toString(), true);
			 }else{
				 pd.dismiss();
			 }
		 }
	 };

	 protected Handler download_error_handler = new Handler() {
		 @Override
		 public void handleMessage(Message msg) {
			 Toast.makeText(mctx, getString(R.string.error_download_alrt), Toast.LENGTH_LONG).show();
		 }
	 };
	 
	 @Override
		public void onConfigurationChanged(Configuration newConfig) {
			super.onConfigurationChanged(newConfig);
		}	
}
