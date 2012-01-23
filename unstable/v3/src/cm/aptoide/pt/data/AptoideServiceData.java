/**
 * ServiceData, part of Aptoide
 * Copyright (C) 2011 Duarte Silveira
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
package cm.aptoide.pt.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import cm.aptoide.pt.AIDLAppInfo;
import cm.aptoide.pt.AIDLAptoideInterface;
import cm.aptoide.pt.AIDLReposInfo;
import cm.aptoide.pt.Aptoide;
import cm.aptoide.pt.ManageRepos;
import cm.aptoide.pt.R;
import cm.aptoide.pt.Splash;
import cm.aptoide.pt.data.cache.ManagerCache;
import cm.aptoide.pt.data.cache.ViewCache;
import cm.aptoide.pt.data.database.ManagerDatabase;
import cm.aptoide.pt.data.display.ViewDisplayAppVersionsInfo;
import cm.aptoide.pt.data.display.ViewDisplayCategory;
import cm.aptoide.pt.data.display.ViewDisplayListApps;
import cm.aptoide.pt.data.display.ViewDisplayListRepos;
import cm.aptoide.pt.data.display.ViewDisplayRepo;
import cm.aptoide.pt.data.downloads.EnumDownloadType;
import cm.aptoide.pt.data.downloads.ManagerDownloads;
import cm.aptoide.pt.data.downloads.ViewDownloadStatus;
import cm.aptoide.pt.data.model.ViewApplication;
import cm.aptoide.pt.data.model.ViewManageRepos;
import cm.aptoide.pt.data.model.ViewRepository;
import cm.aptoide.pt.data.notifications.ManagerNotifications;
import cm.aptoide.pt.data.preferences.ManagerPreferences;
import cm.aptoide.pt.data.system.ManagerSystemSync;
import cm.aptoide.pt.data.system.ViewScreenDimensions;
import cm.aptoide.pt.data.xml.EnumInfoType;
import cm.aptoide.pt.data.xml.ManagerXml;
import cm.aptoide.pt.debug.AptoideLog;
import cm.aptoide.pt.debug.InterfaceAptoideLog;

/**
 * ServiceData, Aptoide's data I/O manager for the activity classes
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class AptoideServiceData extends Service implements InterfaceAptoideLog {

	private final String TAG = "Aptoide-ServiceData";
	private boolean isRunning = false;
	
	private HashMap<EnumServiceDataCallback, AIDLAptoideInterface> aptoideClient;
	private HashMap<Integer, AIDLAppInfo> appInfoClient;
	private AIDLReposInfo reposInfoClient;
//	private EnumServiceDataCall latestRequest;

	private ManagerPreferences managerPreferences;
	private ManagerSystemSync managerSystemSync;
	private ManagerDatabase managerDatabase;
	private ManagerDownloads managerDownloads;
	private ManagerNotifications managerNotifications;
	private ManagerXml managerXml;
	
	private ExecutorService cachedThreadPool;		//TODO in the future turn this into a priorityThreadPool, with a highest priority thread able to pause execution of other threads
	private ExecutorService scheduledThreadPool;
    
	private AtomicBoolean syncingInstalledApps;
    
	/**
	 * When binding to the service, we return an interface to our AIDL stub
	 * allowing clients to send requests to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		AptoideLog.d(AptoideServiceData.this, "binding new client");
		return aptoideServiceDataCallReceiver;
	}
	
	private final AIDLAptoideServiceData.Stub aptoideServiceDataCallReceiver = new AIDLAptoideServiceData.Stub() {
		
		@Override
		public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
			try {
				return super.onTransact(code, data, reply, flags);
			} catch (RuntimeException e) {
				Log.w("Aptoide-ServiceData", "Unexpected remote exception", e);
				throw e;
			}
		}

		@Override
		public void callSyncInstalledApps() throws RemoteException {
	    	syncInstalledApps();			
		}
		
		@Override
		public void callStoreScreenDimensions(ViewScreenDimensions screenDimensions) throws RemoteException {
			storeScreenDimensions(screenDimensions);	
		}

		@Override
		public void callRegisterAvailableAppsObserver(AIDLAptoideInterface availableAppsObserver) throws RemoteException {
			registerAvailableDataObserver(availableAppsObserver);
		}
		
		@Override
		public void callRegisterInstalledAppsObserver(AIDLAptoideInterface installedAppsObserver) throws RemoteException {
			registerInstalledDataObserver(installedAppsObserver);
		}
		
		@Override
		public ViewDisplayListApps callGetInstalledApps() throws RemoteException {
			return getInstalledApps();
		}

		@Override
		public void callRegisterReposObserver(AIDLReposInfo reposInfoObserver) throws RemoteException {
			registerReposObserver(reposInfoObserver);
		}

		@Override
		public void callAddRepo(ViewRepository repository) throws RemoteException {
			if(repoIsManaged(repository.getHashid())){
				//TODO check for delta
				updateAvailableLists();
			}else{
				addRepoBare(repository);			
			}
		}

		@Override
		public ViewDisplayListRepos callGetRepos() throws RemoteException {
			return getRepos();
		}
		

		@Override
		public boolean callAreListsByCategory() throws RemoteException {
			return getShowApplicationsByCategory();
		}

		@Override
		public void callSetListsBy(boolean byCategory) throws RemoteException {
			setShowApplicationsByCategory(byCategory);
		}

		@Override
		public ViewDisplayCategory callGetCategories() throws RemoteException {
			return getCategories();
		}

		@Override
		public ViewDisplayListApps callGetAvailableAppsByCategory(int offset, int range, int categoryHashid) throws RemoteException {
			return getAvailableApps(offset, range, categoryHashid);
		}

		@Override
		public ViewDisplayListApps callGetAvailableApps(int offset, int range) throws RemoteException {
			return getAvailableApps(offset, range);
		}

		@Override
		public ViewDisplayListApps callGetUpdatableApps() throws RemoteException {
			return getUpdatableApps();
		}

		@Override
		public ViewDisplayListApps callGetAppSearchResults(String searchString) throws RemoteException {
			return getAppSearchResults(searchString);
		}

		@Override
		public void callRegisterAppInfoObserver(AIDLAppInfo appInfoObserver, int appHashid) throws RemoteException {
			registerAppInfoObserver(appInfoObserver, appHashid);
		}

		@Override
		public void CallFillAppInfo(int appHashid) throws RemoteException {
			fillAppInfo(appHashid);
			
		}

		@Override
		public ViewDisplayAppVersionsInfo callGetAppInfo(int appHashid) throws RemoteException {
			return getAppInfo(appHashid);
		}

		@Override
		public void callManageRepos(ViewManageRepos reposToManage) throws RemoteException {
			manageRepos(reposToManage);
		}

		@Override
		public void callInstallApp(int appHashid) throws RemoteException {
			downloadApp(appHashid);
		}

		@Override
		public void callUninstallApp(int appHashid) throws RemoteException {
			uninstallApp(appHashid);
		}
		
	}; 

	public void registerAvailableDataObserver(AIDLAptoideInterface availableAppsObserver){
		aptoideClient.put(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST, availableAppsObserver);
		checkIfAnyReposInUse();
    	AptoideLog.d(AptoideServiceData.this, "Registered Available Data Observer");
	}
	
	public void registerInstalledDataObserver(AIDLAptoideInterface installedAppsObserver){
		aptoideClient.put(EnumServiceDataCallback.UPDATE_INSTALLED_LIST, installedAppsObserver);
		if(!syncingInstalledApps.get()){
			try {
				installedAppsObserver.newInstalledListDataAvailable();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	AptoideLog.d(AptoideServiceData.this, "Registered Installed Data Observer");
	}
	
	
	public void registerAppInfoObserver(AIDLAppInfo appInfoObserver, int appHashid){
		appInfoClient.put(appHashid, appInfoObserver);
    	AptoideLog.d(AptoideServiceData.this, "Registered App Info Observer: "+appHashid);
	}
	
	
	public void registerReposObserver(AIDLReposInfo reposInfoObserver){
		reposInfoClient = reposInfoObserver;
	}
	
	
	private BroadcastReceiver installedAppsChangeListener = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context receivedContext, Intent receivedIntent) {
			if(receivedIntent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
				String packageName = receivedIntent.getData().getEncodedSchemeSpecificPart();
				Log.d("Aptoide-ServiceData", "installedAppsChangeListener - package added: "+packageName);
				addApp(packageName);
				
			}else if(receivedIntent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)){
				String packageName = receivedIntent.getData().getEncodedSchemeSpecificPart();
				Log.d("Aptoide-ServiceData", "installedAppsChangeListener - package removed: "+packageName);
				removeApp(packageName);
			}
		}
	};
		
	
	
	public String getTag() {
		return TAG;
	}


	public ManagerPreferences getManagerPreferences() {
		return managerPreferences;
	}
	
	public ManagerSystemSync getManagerSystemSync() {
		return managerSystemSync;
	}	
	
	public ManagerDatabase getManagerDatabase() {
		return managerDatabase;
	}	
	
	public ManagerDownloads getManagerDownloads() {
		return managerDownloads;
	}
	
	public ManagerCache getManagerCache() {
		return managerDownloads.getManagerCache();
	}

	public ManagerNotifications getManagerNotifications() {
		return managerNotifications;
	}

	public ManagerXml getManagerXml(){
		return managerXml;
	}



	@Override
	public void onCreate() {
	    if(!isRunning){
	    	splash();
	    	
	    	cachedThreadPool = Executors.newCachedThreadPool();
	    	scheduledThreadPool = Executors.newScheduledThreadPool(Constants.MAX_PARALLEL_SERVICE_REQUESTS);
	    	
			aptoideClient = new HashMap<EnumServiceDataCallback, AIDLAptoideInterface>();
			appInfoClient = new HashMap<Integer, AIDLAppInfo>();
			
			managerPreferences = new ManagerPreferences(this);
			managerSystemSync = new ManagerSystemSync(this);
			managerDatabase = new ManagerDatabase(this);
			managerNotifications = new ManagerNotifications(this);
			managerDownloads = new ManagerDownloads(this);
			managerXml = new ManagerXml(this);
			
			syncingInstalledApps = new AtomicBoolean(false);
			
			IntentFilter installedAppsChangeFilter = new IntentFilter();
			installedAppsChangeFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
			installedAppsChangeFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			installedAppsChangeFilter.addDataScheme(Constants.URI_PACKAGE_PREFIX);
			registerReceiver(installedAppsChangeListener, installedAppsChangeFilter);
			
			checkForSelfUpdate();
			
			isRunning = true;
			Log.d("Aptoide ServiceData", "Service started");

	    }
		super.onCreate();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		if(intent.getData() != null){
			launchAptoide();
			
			//TODO myapp handling section -- move partly to download/xml/ classes
		    
//			private static final int LOAD_TABS = 0;			//TODO Probably unneeded
//		    private Vector<String> server_lst = null;	//TODO to dataService
//		    private Vector<String[]> get_apps = null;	//TODO to dataService
			
//			private Handler startHandler = new Handler() {
		//
//				@Override
//				public void handleMessage(Message msg) {
//					switch(msg.what){
//					case LOAD_TABS:
//						Intent i = new Intent(Aptoide.this, RemoteInTab.class);
//						Intent get = getIntent();
//						if(get.getData() != null){
//							String uri = get.getDataString();
//								Log.d("Aptoide-startHandler", "receiving a myapp file");
//								downloadMyappFile(uri);
//								try {
//									parseMyappFile(TMP_MYAPP_FILE);
//									i.putExtra("repos", server_lst);
//									if(get_apps.size() > 0){
//										//i.putExtra("uri", TMP_SRV_FILE);
//										i.putExtra("apps", get_apps);
		//	
//									}
//								} catch (Exception e) {
//									Toast.makeText(mctx, mctx.getString(R.string.failed_install), Toast.LENGTH_LONG);
//									onCreate(savedInstanceState);
//								}
//						}
//						startActivityForResult(i,0);
//						break;
//					}
//					super.handleMessage(msg);
//				} 
//		    }; 

			
			
//			private void downloadMyappFile(String myappUri){
//				try{
//					keepScreenOn.acquire();
//					
//					BufferedInputStream getit = new BufferedInputStream(new URL(myappUri).openStream());
		//
//					File file_teste = new File(TMP_MYAPP_FILE);
//					if(file_teste.exists())
//						file_teste.delete();
//					
//					FileOutputStream saveit = new FileOutputStream(TMP_MYAPP_FILE);
//					BufferedOutputStream bout = new BufferedOutputStream(saveit,1024);
//					byte data[] = new byte[1024];
//					
//					int readed = getit.read(data,0,1024);
//					while(readed != -1) {
//						bout.write(data,0,readed);
//						readed = getit.read(data,0,1024);
//					}
//					
//					keepScreenOn.release();
//					
//					bout.close();
//					getit.close();
//					saveit.close();
//				} catch(Exception e){
//					AlertDialog p = new AlertDialog.Builder(this).create();
//					p.setTitle(getText(R.string.top_error));
//					p.setMessage(getText(R.string.aptoide_error));
//					p.setButton(getText(R.string.btn_ok), new DialogInterface.OnClickListener() {
//					      public void onClick(DialogInterface dialog, int which) {
//					          return;
//					        } });
//					p.show();
//				}
//			}
		//	
//			private void parseMyappFile(String file){
//				SAXParserFactory spf = SAXParserFactory.newInstance();
//			    try {
//			    	keepScreenOn.acquire();
//			    	
//			    	SAXParser sp = spf.newSAXParser();
//			    	XMLReader xr = sp.getXMLReader();
//			    	NewServerRssHandler handler = new NewServerRssHandler(this);
//			    	xr.setContentHandler(handler);
//			    	
//			    	InputStreamReader isr = new FileReader(new File(file));
//			    	InputSource is = new InputSource(isr);
//			    	xr.parse(is);
//			    	File xml_file = new File(file);
//			    	xml_file.delete();
//			    	server_lst = handler.getNewSrvs();
//			    	get_apps = handler.getNewApps();
//			    	
//			    	keepScreenOn.release();
//			    	
//			    } catch (IOException e) {
//			    	e.printStackTrace();
//			    } catch (SAXException e) {
//			    	e.printStackTrace();
//			    } catch (ParserConfigurationException e) {
//					e.printStackTrace();
//				}
//			}
		//	
//			private void parseXmlString(String file){
//				SAXParserFactory spf = SAXParserFactory.newInstance();
//			    try {
//			    	keepScreenOn.acquire();
//			    	
//			    	SAXParser sp = spf.newSAXParser();
//			    	XMLReader xr = sp.getXMLReader();
//			    	NewServerRssHandler handler = new NewServerRssHandler(this);
//			    	xr.setContentHandler(handler);
//			    	
//			    	InputSource is = new InputSource();
//			    	is.setCharacterStream(new StringReader(file));
//			    	xr.parse(is);
//			    	server_lst = handler.getNewSrvs();
//			    	get_apps = handler.getNewApps();
//			    	
//			    	keepScreenOn.release();
//			    	
//			    } catch (IOException e) {
//			    } catch (SAXException e) {
//			    } catch (ParserConfigurationException e) {
//				}
//			}
			
			
		//--------------------------------------------------------------------------

			
		}
		super.onStart(intent, startId);
		
	}


	@Override
	public void onDestroy() {
		managerNotifications.destroy();
		Toast.makeText(this, R.string.aptoide_stopped, Toast.LENGTH_LONG).show();
		stopSelf();
		Log.d("Aptoide ServiceData", "Service stopped");
		super.onDestroy();
	}

	
	public void checkForSelfUpdate(){	//TODO use NotificationManager class to load Splash Activity with it's progress bar as selfupdate activity
//   		try{
//			if( this.managerSystemSync.getAptoideVersionInUse() < Integer.parseInt( getXmlElement("versionCode") ) ){
//				Log.d("Aptoide-VersionCode", "Using version "+pkginfo.versionCode+", suggest update!");
//				requestUpdateSelf();
//			}else{
//				proceed();
//			}
//   		}catch(Exception e){
//   			e.printStackTrace();
//   			proceed();
//   		}
	}
	
	public void checkIfAnyReposInUse(){
		if(managerDatabase.anyReposInUse()){
			resetAvailableLists();
			return;
		}else{
			manageRepos();  				
		}
	}
	
	public void storeScreenDimensions(ViewScreenDimensions screenDimensions){
		managerPreferences.setScreenDimensions(screenDimensions);
		AptoideLog.d(AptoideServiceData.this, "Stored Screen Dimensions: "+managerPreferences.getScreenDimensions());
	}
	
	public void syncInstalledApps(){
		syncingInstalledApps.set(true);
		cachedThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				managerDatabase.insertInstalledApplications(managerSystemSync.getInstalledApps());
				AptoideLog.d(AptoideServiceData.this, "Sync'ed Installed Apps");
				
				syncingInstalledApps.set(false);
				managerSystemSync.cacheInstalledIcons();
			}
		});
	}	
	
	
	public void updateInstalledLists(){
		try {
			aptoideClient.get(EnumServiceDataCallback.UPDATE_INSTALLED_LIST).newInstalledListDataAvailable(); 
//			Looper.prepare();
//			Toast.makeText(getApplicationContext(), "installed list now available in next -> tab", Toast.LENGTH_LONG).show();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void refreshAvailableDisplay(){
		try {
			aptoideClient.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).refreshAvailableDisplay();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateAvailableLists(){
		try {
			aptoideClient.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).newAvailableListDataAvailable();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void resetAvailableLists(){
		try {
			aptoideClient.get(EnumServiceDataCallback.UPDATE_AVAILABLE_LIST).resetAvailableListData();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateReposLists(){
		try {
			reposInfoClient.updateReposBasicInfo();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public ViewDisplayListRepos getRepos(){
		AptoideLog.d(AptoideServiceData.this, "Getting Repos");
		return managerDatabase.getReposDisplayInfo();
	}
	
	
	public boolean repoIsManaged(int repoHashid){
		return managerDatabase.repoIsManaged(repoHashid);
	}
	
	public void manageRepos(final ViewManageRepos reposToManage){
		cachedThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				if(reposToManage.hasLocalChanges()){
					if(reposToManage.hasReposToRemove()){
						managerDatabase.removeRepositories(reposToManage.getReposToRemove());
					}
					if(reposToManage.hasReposToSetInUse()){
						managerDatabase.toggleRepositoriesInUse(reposToManage.getReposToSetInUse(), true);
					}
					if(reposToManage.hasReposToUnsetInUse()){
						managerDatabase.toggleRepositoriesInUse(reposToManage.getReposToUnsetInUse(), false);
					}
					resetAvailableLists();
				}
			}
		});
		if(reposToManage.getReposToInsert().size()!=0){
			cachedThreadPool.execute(new Runnable() {
				@Override
				public void run() {	
					ArrayList<ViewRepository> reposToInsert = new ArrayList<ViewRepository>();
					for (ViewDisplayRepo displayRepo : reposToManage.getReposToInsert()) {
						ViewRepository repo = new ViewRepository(displayRepo.getUri());
						repo.setInUse(displayRepo.getInUse());
						if(displayRepo.requiresLogin()){
							repo.setLogin(displayRepo.getLogin());
						}
						if(repo.getInUse()){	//TODO optimize multiple repo insertion
							addRepoBare(repo);
						}else{
							reposToInsert.add(repo);
						}
					}
					managerDatabase.insertRepositories(reposToInsert);
				}
			});
		}
	}
	
	
	
	public void addRepoBare(final ViewRepository originalRepository){
		
		cachedThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				ViewRepository repository = originalRepository;
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				if(!managerDownloads.isConnectionAvailable()){
					AptoideLog.d(AptoideServiceData.this, "No connection");	//TODO raise exception to ask for what to do
				}
				if(!getManagerCache().isFreeSpaceInSdcard()){
					//TODO raise exception
				}
				ViewCache cache = managerDownloads.startRepoBareDownload(repository);
//				Looper.prepare();
//				Toast.makeText(getApplicationContext(), "finisehd downloading bare list", Toast.LENGTH_LONG).show();
				
				managerXml.repoBareParse(repository, cache);
				//TODO find some way to track global parsing completion status, probably in managerXml
			}
		});
		
	}
	
	public void parsingRepoBareFinished(ViewRepository repository){
		resetAvailableLists();
		addRepoIconsInfo(repository);
	}
	
	public void addRepoIconsInfo(final ViewRepository repository){

		cachedThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
				if(!managerDownloads.isConnectionAvailable()){
					AptoideLog.d(AptoideServiceData.this, "No connection");	//TODO raise exception to ask for what to do
				}
				if(!getManagerCache().isFreeSpaceInSdcard()){
					//TODO raise exception
				}
				ViewCache cache = managerDownloads.startRepoIconDownload(repository);

				managerXml.repoIconParse(repository, cache);
				//TODO find some way to track global parsing completion status, probably in managerXml
			}
		});

	}
	
	public void parsingRepoIconsFinished(ViewRepository repository){
		getRepoIcons(new ViewDownloadStatus(repository, Constants.FIRST_ELEMENT, EnumDownloadType.ICON));
		addRepoStats(repository);
	}
	
	public void getRepoIcons(final ViewDownloadStatus downloadStatus){
		
		if(downloadStatus.getRepository().getSize() < downloadStatus.getOffset()){
//			refreshAvailableDisplay();
			return;
		}else{
//			if(downloadStatus.getOffset() >  Constants.FIRST_ELEMENT){
//				refreshAvailableDisplay();
//			}
			
			scheduledThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
					if(!managerDownloads.isConnectionAvailable()){
						AptoideLog.d(AptoideServiceData.this, "No connection");	//TODO raise exception to ask for what to do
					}
					if(!getManagerCache().isFreeSpaceInSdcard()){
						//TODO raise exception
						return;
					}

					managerDownloads.getRepoIcons(downloadStatus, managerDatabase.getIconsDownloadInfo(downloadStatus.getRepository(), downloadStatus.getOffset(), Constants.DISPLAY_LISTS_CACHE_SIZE));
//					if(downloadStatus.getOffset() ==  Constants.FIRST_ELEMENT){//+Constants.DISPLAY_LISTS_CACHE_SIZE){
//						addRepoStats(downloadStatus.getRepository());
//					}
					//TODO find some way to track global parsing completion status, probably in managerXml
				}
			});
		}
		
	}
	
	public void addRepoStats(final ViewRepository repository){
	
		scheduledThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
				if(!managerDownloads.isConnectionAvailable()){
					AptoideLog.d(AptoideServiceData.this, "No connection");	//TODO raise exception to ask for what to do
				}
				if(!getManagerCache().isFreeSpaceInSdcard()){
					//TODO raise exception
				}
				ViewCache cache = managerDownloads.startRepoDownload(repository, EnumInfoType.STATS);
				
				managerXml.repoStatsParse(repository, cache);
				//TODO find some way to track global parsing completion status, probably in managerXml
			}
		});

	}
	
	public void parsingRepoStatsFinished(ViewRepository repository){
		updateAvailableLists();
//		Toast.makeText(AptoideServiceData.this, "app stats available", Toast.LENGTH_LONG).show();
	}
	
//	public void getRepoIconsExtraordinarily(final ViewRepository repository, final int offset){
//		try{
//
//			new Thread(){
//				public void run(){
//					this.setPriority(Thread.MAX_PRIORITY);
//					if(!managerDownloads.isConnectionAvailable()){
//						AptoideLog.d(AptoideServiceData.this, "No connection");	//TODO raise exception to ask for what to do
//					}
//					if(!getManagerCache().isFreeSpaceInSdcard()){
//						//TODO raise exception
//					}
//					managerDownloads.getRepoIconsExtraordinarily(repository, managerDatabase.getIconsDownloadInfo(repository, offset, Constants.SIZE_CACHE_OF_DISPLAY_LISTS));
//					//TODO find some way to track global parsing completion status, probably in managerXml
//				}
//			}.start();
//
//
//		} catch(Exception e){
//			/** this should never happen */
//			//TODO handle exception
//			e.printStackTrace();
//		}
//	}
	
//	public void addRepoDownloadsInfo(final ViewRepository repository){
//		try{
//
//			new Thread(){
//				public void run(){
//					this.setPriority(Thread.MAX_PRIORITY);
//					if(!managerDownloads.isConnectionAvailable()){
//						AptoideLog.d(AptoideServiceData.this, "No connection");	//TODO raise exception to ask for what to do
//					}
//					if(!getManagerCache().isFreeSpaceInSdcard()){
//						//TODO raise exception
//					}
//					ViewCache cache = managerDownloads.startRepoAppDownloads(repository);
//					
//					managerXml.repoDownloadParse(repository, cache);
//					//TODO find some way to track global parsing completion status, probably in managerXml
//				}
//			}.start();
//
//
//		} catch(Exception e){
//			/** this should never happen */
//			//TODO handle exception
//			e.printStackTrace();
//		}
//	}
	
	
	public void fillAppInfo(final int appHashid){
		ViewRepository repository = managerDatabase.getAppRepo(appHashid);
		if(repository == null){
			return;
		}
		//TODO parallel check icon, download immediately if necessary
		//TODO parallel check if the 3 types of info already exist, and only if not get them
		addRepoAppDownloadInfo(repository, appHashid);
		addRepoAppStats(repository, appHashid);
		addRepoAppExtras(repository, appHashid);
		//TODO parallel get Comments
	}
	
	
	public void addRepoAppDownloadInfo(final ViewRepository repository, final int appHashid){
	
		scheduledThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				if(!managerDownloads.isConnectionAvailable()){
					AptoideLog.d(AptoideServiceData.this, "No connection");	//TODO raise exception to ask for what to do
				}
				if(!getManagerCache().isFreeSpaceInSdcard()){
					//TODO raise exception
				}
				ViewCache cache = managerDownloads.startRepoAppDownload(repository, appHashid, EnumInfoType.DOWNLOAD);
				
				managerXml.repoAppDownloadParse(repository, cache, appHashid);
				//TODO find some way to track global parsing completion status, probably in managerXml
			}
		});
					
	}
	
	public void parsingRepoAppDownloadInfoFinished(ViewRepository repository, int appHashid){
		updateAppInfo(appHashid, EnumServiceDataCallback.UPDATE_APP_DOWNLOAD_INFO);
	}
	
	
	
	public void addRepoAppStats(final ViewRepository repository, final int appHashid){

		scheduledThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				if(!managerDownloads.isConnectionAvailable()){
					AptoideLog.d(AptoideServiceData.this, "No connection");	//TODO raise exception to ask for what to do
				}
				if(!getManagerCache().isFreeSpaceInSdcard()){
					//TODO raise exception
				}
				ViewCache cache = managerDownloads.startRepoAppDownload(repository, appHashid, EnumInfoType.STATS);
				
				managerXml.repoAppStatsParse(repository, cache, appHashid);
				//TODO find some way to track global parsing completion status, probably in managerXml
			}
		});
					
	}
	
	public void parsingRepoAppStatsFinished(ViewRepository repository, int appHashid){
		updateAppInfo(appHashid, EnumServiceDataCallback.UPDATE_APP_STATS);
	}
	
	
	
	public void addRepoAppExtras(final ViewRepository repository, final int appHashid){
		
		scheduledThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				if(!managerDownloads.isConnectionAvailable()){
					AptoideLog.d(AptoideServiceData.this, "No connection");	//TODO raise exception to ask for what to do
				}
				if(!getManagerCache().isFreeSpaceInSdcard()){
					//TODO raise exception
				}
				ViewCache cache = managerDownloads.startRepoAppDownload(repository, appHashid, EnumInfoType.EXTRAS);
				
				managerXml.repoAppExtrasParse(repository, cache, appHashid);
				//TODO find some way to track global parsing completion status, probably in managerXml
			}
		});
					
	}
	
	public void parsingRepoAppExtrasFinished(ViewRepository repository, int appHashid){
		updateAppInfo(appHashid, EnumServiceDataCallback.UPDATE_APP_EXTRAS);
	}
	
	public void updateAppInfo(int appHashid, EnumServiceDataCallback callBack){
		try {
			switch (callBack) {
				case REFRESH_ICON:
					appInfoClient.get(appHashid).refreshIcon();
					break;
					
				case UPDATE_APP_DOWNLOAD_INFO:
					appInfoClient.get(appHashid).newAppDownloadInfoAvailable();
					break;
					
				case UPDATE_APP_STATS:
					appInfoClient.get(appHashid).newStatsInfoAvailable();
					break;
					
				case UPDATE_APP_EXTRAS:
					appInfoClient.get(appHashid).newExtrasAvailable();
					break;
					
				case REFRESH_SCREENS:
					appInfoClient.get(appHashid).refreshScreens();
					break;
					
				default:
					break;
			}
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public ViewDisplayAppVersionsInfo getAppInfo(int appHashid){
		AptoideLog.d(AptoideServiceData.this, "Getting App Versions Info: "+appHashid);
		return managerDatabase.getAppDisplayInfo(appHashid);
	}
	
	public boolean getShowApplicationsByCategory(){
		return managerPreferences.getShowApplicationsByCategory();
	}
	
	public void setShowApplicationsByCategory(boolean byCategory){
		managerPreferences.setShowApplicationsByCategory(byCategory);
	}
	
	public ViewDisplayCategory getCategories(){
		AptoideLog.d(AptoideServiceData.this, "Getting Categories");
		return managerDatabase.getCategoriesDisplayInfo();
	}
	
	
	public ViewDisplayListApps getInstalledApps(){
		AptoideLog.d(AptoideServiceData.this, "Getting Installed Apps");
		return managerDatabase.getInstalledAppsDisplayInfo();
	}
	
	public ViewDisplayListApps getAvailableApps(int offset, int range){
		AptoideLog.d(AptoideServiceData.this, "Getting Available Apps");
		return managerDatabase.getAvailableAppsDisplayInfo(offset, range);
	}
	
	public ViewDisplayListApps getAvailableApps(int offset, int range, int categoryHashid){
		AptoideLog.d(AptoideServiceData.this, "Getting Available Apps for category: "+categoryHashid);
		return managerDatabase.getAvailableAppsDisplayInfo(offset, range, categoryHashid);
	}
	
	public ViewDisplayListApps getUpdatableApps(){
		AptoideLog.d(AptoideServiceData.this, "Getting Updatable Apps");
		return managerDatabase.getUpdatableAppsDisplayInfo();
	}
	
	public ViewDisplayListApps getAppSearchResults(String searchString){
		AptoideLog.d(AptoideServiceData.this, "Getting App Search Results: "+searchString);
		return managerDatabase.getAppSearchResultsDisplayInfo(searchString);
	}
	
	public void downloadApp(final int appHashid){
		scheduledThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				if(!managerDownloads.isConnectionAvailable()){
					AptoideLog.d(AptoideServiceData.this, "No connection");	//TODO raise exception to ask for what to do
				}
				if(!getManagerCache().isFreeSpaceInSdcard()){
					//TODO raise exception
				}
				ViewCache apk = managerDownloads.downloadApk(managerDatabase.getAppDownload(appHashid));
				installApp(apk, appHashid);
			}
		});
	}
	
	
	public void addApp(String packageName){
		ViewApplication installedApp = managerSystemSync.getInstalledApp(packageName);
		if(installedApp != null){
			managerDatabase.insertInstalledApplication(installedApp);
		}
	}
	
	public void removeApp(String packageName){
		managerDatabase.removeInstalledApplication(packageName);
	}
	
	public void installApp(ViewCache apk, int appHashid){
		Intent install = new Intent();
		install.setAction(android.content.Intent.ACTION_VIEW);
		install.setDataAndType(Uri.parse("file://" + apk.getLocalPath()), "application/vnd.android.package-archive");
		startActivity(install);
		AptoideLog.d(AptoideServiceData.this, "Installing app: "+appHashid);
	}
	
	public void uninstallApp(int appHashid){
		Uri uri = Uri.fromParts("package", managerDatabase.getInstalledAppPackageName(appHashid), null);
		Intent remove = new Intent(Intent.ACTION_DELETE, uri);
		startActivity(remove);
		AptoideLog.d(AptoideServiceData.this, "Removing app: "+appHashid);
	}
	
	
	public void splash(){
		Intent splash = new Intent(AptoideServiceData.this, Splash.class);
		splash.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(splash);    				
	}
	
	public void manageRepos(){
		Intent manageRepos = new Intent(AptoideServiceData.this, ManageRepos.class);
		manageRepos.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(manageRepos);  
	}
	

	public void launchAptoide() {
		Intent aptoide = new Intent(this, Aptoide.class);
		aptoide.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(aptoide);
	}
	
	
	public ViewClientStatistics getStatistics(){
		ViewClientStatistics statistics = new ViewClientStatistics(managerSystemSync.getAptoideVersionNameInUse());
		managerPreferences.completeStatistics(statistics);
		return statistics;
	}

}
