/**
 * ManagerCache,		auxilliary class to Aptoide's ServiceData
 * Copyright (C) 2011  Duarte Silveira
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

package cm.aptoide.pt.data.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.os.StatFs;
import android.util.Log;
import cm.aptoide.pt.data.util.Constants;
import cm.aptoide.pt.data.util.Md5Handler;

/**
 * ManagerCache, models cache I/O
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class ManagerCache {

	/** Ongoing */
	private ArrayList<ViewCache> cacheViews;
	
	/** Object reuse pool */
	private ArrayList<ViewCache> cachePool;
	
	ExecutorService tasksPool = Executors.newCachedThreadPool();
	
		
	public ManagerCache() {
		this.cacheViews = new ArrayList<ViewCache>();
		this.cachePool = new ArrayList<ViewCache>();
		
		if(!isFreeSpaceInSdcard()){
			//TODO raise exception
		}
	}
	

	public synchronized ViewCache getNewViewCache(String localPath){
		if(cachePool.isEmpty()){
			return new ViewCache(localPath);
		}else{
			ViewCache viewCache = cachePool.remove(0);
			viewCache.reuse(localPath);
			return viewCache;
		}
	}	
	
	public synchronized ViewCache getNewViewCache(String localPath, String md5hash){
		if(cachePool.isEmpty()){
			return new ViewCache(localPath, md5hash);
		}else{
			ViewCache viewCache = cachePool.remove(0);
			viewCache.reuse(localPath, md5hash);
			return viewCache;
		}
	}
	
	public ViewCache getNewRepoDeltaViewCache(int repoHashid){
		return getNewViewCache(Constants.PATH_CACHE_REPOS+repoHashid+".delta"+".xml");
	}
	
	public ViewCache getNewRepoBareViewCache(int repoHashid){
		return getNewViewCache(Constants.PATH_CACHE_REPOS+repoHashid+".bare"+".xml");
	}
	
	public ViewCache getNewRepoIconViewCache(int repoHashid){
		return getNewViewCache(Constants.PATH_CACHE_REPOS+repoHashid+".icon"+".xml");
	}
	
	public ViewCache getNewRepoAppIconViewCache(int repoHashid, int appHashid){
		return getNewViewCache(Constants.PATH_CACHE_REPOS+repoHashid+".icon."+appHashid+".xml");
	}
	
	public ViewCache getNewRepoDownloadViewCache(int repoHashid){
		return getNewViewCache(Constants.PATH_CACHE_REPOS+repoHashid+".download"+".xml");
	}
	
	public ViewCache getNewRepoAppDownloadViewCache(int repoHashid, int appHashid){
		return getNewViewCache(Constants.PATH_CACHE_REPOS+repoHashid+".download."+appHashid+".xml");
	}
	
	public ViewCache getNewRepoExtrasViewCache(int repoHashid){
		return getNewViewCache(Constants.PATH_CACHE_REPOS+repoHashid+".extras"+".xml");
	}
	
	public ViewCache getNewRepoAppExtrasViewCache(int repoHashid, int appHashid){
		return getNewViewCache(Constants.PATH_CACHE_REPOS+repoHashid+".extras."+appHashid+".xml");
	}
	
	public ViewCache getNewRepoStatsViewCache(int repoHashid){
		return getNewViewCache(Constants.PATH_CACHE_REPOS+repoHashid+".stats"+".xml");
	}
	
	public ViewCache getNewRepoAppStatsViewCache(int repoHashid, int appHashid){
		return getNewViewCache(Constants.PATH_CACHE_REPOS+repoHashid+".stats."+appHashid+".xml");
	}
	
	public ViewCache getNewRepoAppCommentsViewCache(int repoHashid, int appHashid){
		return getNewViewCache(Constants.PATH_CACHE_REPOS+repoHashid+".comments."+appHashid+".xml");
	}
	
	public ViewCache getNewIconViewCache(int appHashid){
		return getNewViewCache(Constants.PATH_CACHE_ICONS+appHashid);
	}
	
	public ViewCache getNewScreenViewCache(int appHashid, int orderNumber){
		return getNewViewCache(Constants.PATH_CACHE_SCREENS+appHashid+"."+orderNumber);
	}
	
	public ViewCache getNewAppViewCache(int appHashid, String md5sum){
		return getNewViewCache(Constants.PATH_CACHE_APKS+appHashid, md5sum);
	}
	
	public ViewCache getNewMyappDownloadViewCache(String myappName){
		return getNewViewCache(Constants.PATH_CACHE_MYAPPS+myappName);
	}
	
	public ViewCache getNewLatestVersionDownloadViewCache(){
		return getNewViewCache(Constants.FILE_LATEST_VERSION_INFO);
	}
	
	
	public boolean isFreeSpaceInSdcard(){
		File sdcard_file = new File(Constants.PATH_SDCARD);
		if(!sdcard_file.exists() || !sdcard_file.canWrite()){
			
//			final AlertDialog upd_alrt = new AlertDialog.Builder(mctx).create();
//			upd_alrt.setIcon(android.R.drawable.ic_dialog_alert);
//			upd_alrt.setTitle(getText(R.string.remote_in_noSD_title));
//			upd_alrt.setMessage(getText(R.string.remote_in_noSD));
//			upd_alrt.setButton(getText(R.string.btn_ok), new OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					finish();
//				}
//			});
//			upd_alrt.show();
			
			Log.d("Aptoide-ManagerCache","No writable SDCARD...");
			
			return false;
			
		}else{
		
			StatFs stat = new StatFs(sdcard_file.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			long availableBlocks = stat.getAvailableBlocks();
	
			long total = (blockSize * totalBlocks)/1024/1024;
			long avail = (blockSize * availableBlocks)/1024/1024;
			Log.d("Aptoide","* * * * * * * * * *");
			Log.d("Aptoide", "Total: " + total + " Mb");
			Log.d("Aptoide", "Available: " + avail + " Mb");
	
			if(avail < 10 ){
				Log.d("Aptoide","No space left on SDCARD...");
				Log.d("Aptoide","* * * * * * * * * *");
	
//				final AlertDialog upd_alrt = new AlertDialog.Builder(mctx).create();
//				upd_alrt.setIcon(android.R.drawable.ic_dialog_alert);
//				upd_alrt.setTitle(getText(R.string.remote_in_noSD_title));
//				upd_alrt.setMessage(getText(R.string.remote_in_noSDspace));
//				upd_alrt.setButton(getText(R.string.btn_ok), new OnClickListener() {
//					public void onClick(DialogInterface dialog, int which) {
//						finish();
//					}
//				});
//				upd_alrt.show();
				
				return false;
				
			}else{
				Log.d("Aptoide","Ok!");
				Log.d("Aptoide","* * * * * * * * * *");
	
				File cache_path = new File(Constants.PATH_CACHE);
				if(!cache_path.exists()){
					cache_path.mkdir();
				}
	
				File icon_path = new File(Constants.PATH_CACHE_ICONS);
				if(!icon_path.exists()){
					icon_path.mkdir();
				}
				
				File screens_path = new File(Constants.PATH_CACHE_SCREENS);
				if(!screens_path.exists()){
					screens_path.mkdir();
				}
				
				File repos_path = new File(Constants.PATH_CACHE_REPOS);
				if(!repos_path.exists()){
					repos_path.mkdir();
				}
				
				File apks_path = new File(Constants.PATH_CACHE_APKS);
				if(!apks_path.exists()){
					apks_path.mkdir();
				}
				
				File myapps_path = new File(Constants.PATH_CACHE_MYAPPS);
				if(!myapps_path.exists()){
					myapps_path.mkdir();
				}
				
				return true;
			}
		}
	}
	
	/**
	 * clearCache, if it exists remove it
	 * 
	 * @param cache
	 */
	public void clearCache(final ViewCache cache){
		File file = new File(cache.getLocalPath());
		if(file.exists()){
			file.delete();
			Log.d("Aptoide-ManagerCache", "deleted: "+cache.getLocalPath());
		}
	}
	
	/**
	 * clearIconCache, removes all icons from cache
	 */
	public void clearIconCache(){
		tasksPool.execute(new Runnable() {
			@Override
			public void run() {
				File iconsPath = new File(Constants.PATH_CACHE_ICONS);
				File[] icons = iconsPath.listFiles();
				for (File icon : icons) {
					icon.delete();
				}
				Log.d("Aptoide-ManagerCache", "icon cache cleared");
			}
		});
	}
	
	/**
	 * clearApkCache, removes all apks from cache
	 */
	public void clearApkCache(){
		tasksPool.execute(new Runnable() {
			@Override
			public void run() {
				File apksPath = new File(Constants.PATH_CACHE_APKS);
				File[] apks = apksPath.listFiles();
				for (File apk : apks) {
					apk.delete();
				}
				Log.d("Aptoide-ManagerCache", "apk cache cleared");
			}
		});
	}
	
	public boolean isIconCached(int appHashid){
		String iconPath = Constants.PATH_CACHE_ICONS+appHashid;
		File icon = new File(iconPath);
		if(icon.exists()){
			Log.d("Aptoide-ManagerCache", "icon already exists: "+appHashid);
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isScreenCached(int appHashid, int orderNumber){
		String iconPath = Constants.PATH_CACHE_SCREENS+appHashid+"."+orderNumber;
		File icon = new File(iconPath);
		if(icon.exists()){
			Log.d("Aptoide-ManagerCache", "screen already exists: "+appHashid+" orderNumber: "+orderNumber);
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isMyAppCached(String myappName){
		String myappPath = Constants.PATH_CACHE_MYAPPS+myappName;
		File myapp = new File(myappPath);
		if(myapp.exists()){
			Log.d("Aptoide-ManagerCache", "myapp already exists: "+myappName);			
			return true;
		}else{
			return false;
		}
	}
	
	public ViewCache cacheMyapp(String originalPath, String myappName){
		try {
			FileInputStream original = new FileInputStream(originalPath);
			FileOutputStream cache = new FileOutputStream(Constants.PATH_CACHE_MYAPPS+myappName);
			byte data[] = new byte[8096];
			int bytesRead;
			while((bytesRead = original.read(data, 0, 8096)) > 0){
				cache.write(data, 0, bytesRead);
			}
			
			cache.flush();
			cache.close();
			original.close();
			
			return getNewViewCache(Constants.PATH_CACHE_MYAPPS+myappName);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	}
	
	public boolean isApkCached(int appHashid){
		String apkPath = Constants.PATH_CACHE_APKS+appHashid;
		File apk = new File(apkPath);
		if(apk.exists()){
			Log.d("Aptoide-ManagerCache", "apk already exists: "+appHashid);			
			return true;
		}else{
			return false;
		}
	}
	
	public void cacheIcon(final int appHashid, final Bitmap icon){
		tasksPool.execute(new Runnable() {
			@Override
			public void run() {
				if(!isIconCached(appHashid)){
					try {
						FileOutputStream out = new FileOutputStream(Constants.PATH_CACHE_ICONS+appHashid);
						icon.compress(Bitmap.CompressFormat.PNG, 90, out);
						Log.d("Aptoide-ManagerCache", "stored installed app icon in: "+Constants.PATH_CACHE_ICONS+appHashid);
					} catch (Exception e) {
						//TODO handle exception
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	public void calculateMd5Hash(ViewCache cache){
		File file = new File(cache.getLocalPath());
		Md5Handler hash = new Md5Handler();
		
		cache.setMd5Sum(hash.md5Calc(file));		
	}
	
	public boolean md5CheckOk(ViewCache cache){
		File file = new File(cache.getLocalPath());
		Md5Handler hash = new Md5Handler();
		if(cache.getMd5sum().equalsIgnoreCase(hash.md5Calc(file))){
			Log.d("Aptoide-ManagerCache", "md5Check OK!");
			return true;
		}else{
			Log.d("Aptoide-ManagerCache",cache.getMd5sum()+ " VS " + hash.md5Calc(file));	//TODO refactor log
			return false;
		}
	}
	
}
