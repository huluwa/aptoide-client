/**
 * DynamicAvailableAppsListAdapter,		part of Aptoide's data model
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

package cm.aptoide.pt.ifaceutil;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import cm.aptoide.pt.EnumAptoideInterfaceTasks;
import cm.aptoide.pt.R;
import cm.aptoide.pt.data.AIDLAptoideServiceData;
import cm.aptoide.pt.data.display.ViewDisplayApplication;
import cm.aptoide.pt.data.display.ViewDisplayApplicationAvailable;
import cm.aptoide.pt.data.display.ViewDisplayCategory;
import cm.aptoide.pt.data.display.ViewDisplayListApps;
import cm.aptoide.pt.data.display.ViewDisplayListsDimensions;
import cm.aptoide.pt.data.util.Constants;

 /**
 * DynamicAvailableAppsListAdapter, models a dynamic loading, available apps list adapter
 * 									extends arrayAdapter
 * 
 * @author dsilveira
 * @since 3.0
 *
 */
public class DynamicAvailableAppsListAdapter extends BaseAdapter{
	
	private ListView listView;
	private LayoutInflater layoutInflater;
	
	private View topProgressBar;
	private View bottomProgressBar;

	private ViewDisplayListsDimensions displayListsDimensions;
	private ViewDisplayCategory category = null;
	
	private int totalAvailableApps = Constants.EMPTY_INT;
	
	private ViewDisplayListApps apps = null;
	private ViewDisplayListApps freshApps = null;
	private ViewDisplayListApps freshTopApps = null;
	private ViewDisplayListApps freshBottomApps = null;
	private AtomicInteger appsTrimTopAmount;
	private AtomicInteger appsTrimBottomAmount;
	
	private AtomicInteger cacheListOffset;
	private AtomicInteger cacheAppsTrimmed;

	private AtomicInteger scrollDirectionOrigin;
	private AtomicBoolean directionDown;
	private AtomicBoolean changingList;

	private AvailableAppsManager appsManager;
	
	private AIDLAptoideServiceData serviceDataCaller = null;
	private Handler aptoideTasksHandler;

	
	private Handler interfaceTasksHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	EnumAptoideInterfaceTasks task = EnumAptoideInterfaceTasks.reverseOrdinal(msg.what);
        	switch (task) {
				
				case RESET_AVAILABLE_LIST_DISPLAY:
					resetDisplay();
					break;
					
				case TRIM_TOP_AND_UPDATE_AVAILABLE_LIST_DISPLAY:
					trimTopAppsList(appsTrimTopAmount.get());					
					break;
					
				case TRIM_BOTTOM_AND_UPDATE_AVAILABLE_LIST_DISPLAY:
					trimBottomAppsList(appsTrimBottomAmount.get());					
					break;
					
				case PREPEND_AND_UPDATE_AVAILABLE_LIST_DISPLAY:
					prependAndUpdateDisplay(freshTopApps);
					break;
					
				case APPEND_AND_UPDATE_AVAILABLE_LIST_DISPLAY:
					appendAndUpdateDisplay(freshBottomApps);
					break;
					
//				case TRIM_PREPEND_AND_UPDATE_AVAILABLE_LIST_DISPLAY:
//					trimPrependAndUpdateDisplay(freshTopApps, appsTrimBottomAmount.get());
//					break;
//					
//				case TRIM_APPEND_AND_UPDATE_AVAILABLE_LIST_DISPLAY:
//					trimAppendAndUpdateDisplay(appsTrimTopAmount.get(), freshBottomApps);
//					break;
	
				default:
					break;
			}
        }
    };
    
    

    
    
   
    
    private class AvailableAppsManager{
    	
    	private ExecutorService dataColector;


    	public AvailableAppsManager() {
    		dataColector = Executors.newSingleThreadExecutor();
    	}
		

		/** 
		 * scrollDown - MOVE FORWARD IN AVAILABLE APPS LIST
		 * 
		 */
		public void scrollDown(){
			dataColector.execute(new ScrollDown());
		}

		/** 
		 * scrollUp - MOVE BACKWARD IN AVAILABLE APPS LIST
		 * 
		 */
		public void scrollUp(){
			dataColector.execute(new ScrollUp());
		}

		/** 
		 * reload - RELOAD AVAILABLE APPS
		 * 
		 */
		public void reload(){
			dataColector.execute(new Reload());
		}

		/** 
		 * reset - RESET AVAILABLE APPS TO ZERO
		 * 
		 */
		public void reset(){
			dataColector.execute(new Reset());
		}
    	
		private class ScrollDown implements Runnable {

			@Override
			public void run() {
				int range = displayListsDimensions.getPageSize();
				
//				boolean trim_top = (apps.size() >= (displayListsDimensions.getCacheSize()));
//				if(trim_top){
//					cacheAppsTrimmed.incrementAndGet();
//				}
				boolean append = true;
				int offset = cacheListOffset.incrementAndGet()*displayListsDimensions.getPageSize();

				try {
					if( category != null && !category.hasChildren()){
						Log.d("Aptoide","scrolling down available list.  offset: "+offset+" range: "+range+" category: "+category);
						setBottomFreshAvailableApps(serviceDataCaller.callGetAvailableAppsByCategory(offset, range, category.getCategoryHashid()));
					}else{
						Log.d("Aptoide","scrolling down available list.  offset: "+offset+" range: "+range);
						setBottomFreshAvailableApps(serviceDataCaller.callGetAvailableApps(offset, range));
					}
					
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(freshBottomApps.size() == 0){
					append = false;
				}
				
//				if(trim_top){
//					appsTrimTopAmount.addAndGet(range);
//					if(append){
//						interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.TRIM_APPEND_AND_UPDATE_AVAILABLE_LIST_DISPLAY.ordinal());
//					}else{
//						interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.TRIM_TOP_AND_UPDATE_AVAILABLE_LIST_DISPLAY.ordinal());
//					}
//				}else{
					if(append){
						interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.APPEND_AND_UPDATE_AVAILABLE_LIST_DISPLAY.ordinal());
					}
//				}
				
			}
			
		}
    	
		private class ScrollUp implements Runnable {

			@Override
			public void run() {
				int range = displayListsDimensions.getPageSize();
				
//				boolean trim_bottom = (apps.size() >= (displayListsDimensions.getCacheSize()));
//				if(trim_bottom){
//					cacheListOffset.decrementAndGet();					
//				}
				boolean prepend = (cacheAppsTrimmed.get() > 0);
				if(prepend){
					int offset = cacheAppsTrimmed.get()-displayListsDimensions.getPageSize();
					
					try {
						if(category != null && !category.hasChildren()){
							Log.d("Aptoide","scrolling up available list.  offset: "+offset+" range: "+range+" category: "+category);
							setTopFreshAvailableApps(serviceDataCaller.callGetAvailableAppsByCategory(offset, range, category.getCategoryHashid()));
						}else{
							Log.d("Aptoide","scrolling up available list.  offset: "+offset+" range: "+range);
							setTopFreshAvailableApps(serviceDataCaller.callGetAvailableApps(offset, range));
						}	
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
//				}

//				if(trim_bottom){
//					appsTrimBottomAmount.addAndGet(range);
//					if(prepend){
//						interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.TRIM_PREPEND_AND_UPDATE_AVAILABLE_LIST_DISPLAY.ordinal());						
//					}else{
//						interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.TRIM_BOTTOM_AND_UPDATE_AVAILABLE_LIST_DISPLAY.ordinal());
//					}
//				}else{
//					if(prepend){
						interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.PREPEND_AND_UPDATE_AVAILABLE_LIST_DISPLAY.ordinal());							
					}
//				}
				
			}
			
		}
    	
		private class Reload implements Runnable {
			@Override
			public void run() {
				int offset = cacheListOffset.get()*displayListsDimensions.getPageSize();	//TODO broken, needs fine detection of current position
				int range = displayListsDimensions.getPageSize();
								
				try {
					if(category != null && !category.hasChildren()){
						Log.d("Aptoide","reloading available list.  offset: "+offset+" range: "+range);
						setFreshAvailableApps(serviceDataCaller.callGetAvailableAppsByCategory(offset, range, category.getCategoryHashid()));
						interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_AVAILABLE_LIST_DISPLAY.ordinal());
					}else{
						Log.d("Aptoide","reloading available list.  offset: "+offset+" range: "+range);
						setFreshAvailableApps(serviceDataCaller.callGetAvailableApps(offset, range));
						interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_AVAILABLE_LIST_DISPLAY.ordinal());
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
    	
		private class Reset implements Runnable {

			@Override
			public void run() {
				cacheListOffset.set(0);
				int offset = 0;
				int range = displayListsDimensions.getFastReset();
				try {
					if(category != null && !category.hasChildren()){
						Log.d("Aptoide","resetting available list.  offset: "+offset+" range: "+range+" "+category);
						setFreshAvailableApps(serviceDataCaller.callGetAvailableAppsByCategory(offset, range, category.getCategoryHashid()));
						interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_AVAILABLE_LIST_DISPLAY.ordinal());
					}else{
						Log.d("Aptoide","resetting available list.  offset: "+offset+" range: "+range);
						setFreshAvailableApps(serviceDataCaller.callGetAvailableApps(offset, range));
						interfaceTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_AVAILABLE_LIST_DISPLAY.ordinal());
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}

    }
    
    
	private synchronized void detectMoreAppsNeeded(int position){
		directionDown.set((position - scrollDirectionOrigin.get()) > 0);
		if( directionDown.get() /*&& (cacheAppsTrimmed.get()+getCount()) != totalAvailableApps*/ && (getCount()-position) < displayListsDimensions.getIncreaseTrigger() ){
			setLoadingFooter(true);
			if(!changingList.get()){
				scrollDirectionOrigin.set(position);
				changingList.set(true);
				Log.d("Aptoide-DynamicAvailableAppsListAdapter", "scrollind down, position: "+position);
				appsManager.scrollDown();
				if(position > displayListsDimensions.getDecreaseTrigger()){
					trimTopAppsList(displayListsDimensions.getIncreaseTrigger());
				}
			}else if( (getCount()-position) < (displayListsDimensions.getPageSize()/Constants.DISPLAY_LISTS_PAGE_SIZE_MULTIPLIER) ){
				scrollDirectionOrigin.set(position);
			}
		}else if( !directionDown.get() && (cacheAppsTrimmed.get()+getCount()) > displayListsDimensions.getCacheSize() && position < displayListsDimensions.getIncreaseTrigger() ){
			setLoadingHeader(true);
			if(!changingList.get()){
				scrollDirectionOrigin.set(position);
				changingList.set(true);
				Log.d("Aptoide-DynamicAvailableAppsListAdapter", "srolling up, position: "+position);
				appsManager.scrollUp();
				if((getCount()-position) > displayListsDimensions.getDecreaseTrigger()){
					trimBottomAppsList(displayListsDimensions.getIncreaseTrigger());
				}
			}else if( position < (displayListsDimensions.getPageSize()/Constants.DISPLAY_LISTS_PAGE_SIZE_MULTIPLIER) ){
				scrollDirectionOrigin.set(position);
			}
		}
	}
	
	
	private void setLoadingHeader(boolean on){
		if(on){
			topProgressBar.setVisibility(View.VISIBLE);
		}else{
			topProgressBar.setVisibility(View.GONE);
		}
	}
	
	private void setLoadingFooter(boolean on){
		if(on){
			bottomProgressBar.setVisibility(View.VISIBLE);
		}else{
			bottomProgressBar.setVisibility(View.GONE);
		}
	}
	
	
	
	public static class AvailableRowViewHolder{
		int appHashid;
		
		ImageView app_icon;
		
		TextView app_name;
		TextView uptodate_versionname;
		
		TextView downloads;
		RatingBar stars;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		detectMoreAppsNeeded(position);
		
		AvailableRowViewHolder rowViewHolder;
		
		if(convertView == null){
			convertView = layoutInflater.inflate(R.layout.row_app_available, null);
			
			rowViewHolder = new AvailableRowViewHolder();
			rowViewHolder.app_icon = (ImageView) convertView.findViewById(R.id.app_icon);
			rowViewHolder.app_name = (TextView) convertView.findViewById(R.id.app_name);
			rowViewHolder.uptodate_versionname = (TextView) convertView.findViewById(R.id.uptodate_versionname);
			rowViewHolder.downloads = (TextView) convertView.findViewById(R.id.downloads);
			rowViewHolder.stars = (RatingBar) convertView.findViewById(R.id.stars);
			
			convertView.setTag(rowViewHolder);
		}else{
			rowViewHolder = (AvailableRowViewHolder) convertView.getTag();
		}
		
		rowViewHolder.appHashid = apps.get(position).getAppHashid();
		
		File iconCache = new File(apps.get(position).getIconCachePath());
		if(iconCache.exists() && iconCache.length() > 0){
			rowViewHolder.app_icon.setImageURI(Uri.parse(apps.get(position).getIconCachePath()));
		}else{
			rowViewHolder.app_icon.setImageResource(android.R.drawable.sym_def_app_icon);
		}
		
		rowViewHolder.app_name.setText(apps.get(position).getAppName());
		rowViewHolder.uptodate_versionname.setText(apps.get(position).getVersionName());
		
		rowViewHolder.downloads.setText(Integer.toString(((ViewDisplayApplicationAvailable) apps.get(position)).getDownloads()));
		rowViewHolder.stars.setRating(((ViewDisplayApplicationAvailable) apps.get(position)).getStars());
		
		return convertView;
	}

	
	@Override
	public int getCount() {
		return apps.size();
	}

	@Override
	public ViewDisplayApplication getItem(int position) {
		return apps.get(position);
	}

	@Override
	public long getItemId(int position) {
		return apps.get(position).getAppHashid();
	}
	
	
	
	/**
	 * DynamicAppsListAdapter Constructor
	 *
	 * @param context
	 * @param textViewResourceId
	 */
	public DynamicAvailableAppsListAdapter(Context context, ListView listView, AIDLAptoideServiceData serviceDataCaller, Handler interfaceTasksHandler) {
		
		this.serviceDataCaller = serviceDataCaller;
		this.aptoideTasksHandler = interfaceTasksHandler;
		
		apps = new ViewDisplayListApps();
		freshApps = new ViewDisplayListApps();
		freshBottomApps = new ViewDisplayListApps();
		freshTopApps = new ViewDisplayListApps();

		appsManager = new AvailableAppsManager();


		this.listView = listView;
		layoutInflater = LayoutInflater.from(context);
		
		this.topProgressBar = new ProgressBar(context);
		this.bottomProgressBar = new ProgressBar(context);
		
		listView.addFooterView(bottomProgressBar);
//		listView.addHeaderView(topProgressBar);
		
		setLoadingHeader(false);
		
		
		cacheListOffset = new AtomicInteger(0);
		cacheAppsTrimmed = new AtomicInteger(0);
		appsTrimTopAmount = new AtomicInteger(0);
		appsTrimBottomAmount = new AtomicInteger(0);
		
		scrollDirectionOrigin = new AtomicInteger(0);
		directionDown = new AtomicBoolean(true);
		changingList = new AtomicBoolean(false);

        try {

            displayListsDimensions = serviceDataCaller.callGetDisplayListsDimensions();
        } catch (RemoteException e) {
			// TODO Auto-generated catch block
            e.printStackTrace();
        }
        
	} 
	
	
	
	public void resetDisplay(ViewDisplayCategory category){
		this.category = category;
		try {
			if(category != null && category.getCategoryHashid() != Constants.TOP_CATEGORY){
				totalAvailableApps = serviceDataCaller.callGetTotalAvailableAppsInCategory(category.getCategoryHashid());
			}else{
				totalAvailableApps = serviceDataCaller.callGetTotalAvailableApps();
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		appsManager.reset();
	}	
    
	public void reloadDisplay(){
//		appsManager.reload();
		appsManager.reset();
	}
	
	public void refreshDisplayAvailable(){
		notifyDataSetChanged();
	}
	
	
	
	private synchronized void setFreshAvailableApps(ViewDisplayListApps freshAvailableApps){
		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "setFreshAvailableList "+freshAvailableApps.size()+" count: "+getCount()+" cachesize: "+displayListsDimensions.getCacheSize());
		this.freshApps = freshAvailableApps;
	}
	
	private synchronized void setTopFreshAvailableApps(ViewDisplayListApps freshAvailableApps){
		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "addTopFreshAvailableList "+freshAvailableApps.size()+" count: "+getCount()+" cachesize: "+displayListsDimensions.getCacheSize());
		this.freshTopApps = freshAvailableApps;
	}
	
	private synchronized void setBottomFreshAvailableApps(ViewDisplayListApps freshAvailableApps){
		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "addBottomFreshAvailableList "+freshAvailableApps.size()+" count: "+getCount()+" cachesize: "+displayListsDimensions.getCacheSize());
		this.freshBottomApps = freshAvailableApps;
	}
	


	private void initDisplayAvailable(){
		listView.setAdapter(this);
    }
	
	private void resetDisplay(){
		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "new AvailableList: "+freshApps.size());
		
		if(freshApps.isEmpty()){
			aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_NO_APPS.ordinal());
		}else{
			aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.SWITCH_AVAILABLE_TO_LIST.ordinal());
		}

		
//		int scrollRestorePosition = listView.getFirstVisiblePosition();
//		int partialScrollRestorePosition = (listView.getChildAt(0)==null?0:listView.getChildAt(0).getTop());
    	this.apps = freshApps;
		initDisplayAvailable();
		refreshDisplayAvailable();
		if( directionDown.get() && !freshApps.isEmpty() && getCount() < displayListsDimensions.getCacheSize()){
			appsManager.scrollDown();
		}else {
			setLoadingFooter(false);
		}
//    	listView.setSelectionFromTop(scrollRestorePosition, partialScrollRestorePosition);
    	
    	aptoideTasksHandler.sendEmptyMessage(EnumAptoideInterfaceTasks.RESET_UPDATABLE_LIST_DISPLAY.ordinal());
	}
	
	private synchronized void trimTopAppsList(int trimAmount){
//		appsTrimTopAmount.addAndGet(trimAmount);
//		int adjustAmount = trimAmount;
//		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "trimTopAvailableList: "+trimAmount);
//		int scrollRestorePosition = listView.getFirstVisiblePosition();
//		int partialScrollRestorePosition = (listView.getChildAt(0)==null?0:listView.getChildAt(0).getTop());
//		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "list size before: "+apps.size()+"   original scroll position: "+scrollRestorePosition);
//		do{
//			this.apps.removeFirst();
//			trimAmount--;
//		}while(trimAmount>0);
//		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "list size after: "+apps.size());
//		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "restoring scroll position, currentPosition:"+listView.getFirstVisiblePosition()+" firstVisiblePosition: "+(scrollRestorePosition-adjustAmount)+" top: "+partialScrollRestorePosition);
//    	listView.setSelectionFromTop((scrollRestorePosition-adjustAmount), partialScrollRestorePosition);
	}
	
	private synchronized void trimBottomAppsList(int trimAmount){
//		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "trimEndAvailableList: "+trimAmount);
//		do{
//			this.apps.removeLast();
//			trimAmount--;
//		}while(trimAmount>0);
//		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "list size after: "+apps.size());
	}
	
	private void prependAndUpdateDisplay(ViewDisplayListApps freshAvailableApps){	
		appsTrimTopAmount.addAndGet(-freshAvailableApps.size());
		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "prepending freshAvailableList: "+freshAvailableApps.size());
		if(freshAvailableApps.isEmpty()){
			return;
		}
		
    	int adjustAmount = freshAvailableApps.size();
    	boolean newList = this.apps.isEmpty();
    	if(newList){
    		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "prepending to empty available list");
    		this.apps = freshAvailableApps;
    		
    		initDisplayAvailable();
    	}else{	
    		int scrollRestorePosition = listView.getFirstVisiblePosition();
    		int partialScrollRestorePosition = (listView.getChildAt(0)==null?0:listView.getChildAt(0).getTop());
    		
    		this.apps.addAll(0,freshAvailableApps);
    		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "new displayList size: "+this.apps.size());
    		
    		refreshDisplayAvailable();

        	listView.setSelectionFromTop(scrollRestorePosition+adjustAmount, partialScrollRestorePosition);
    	}
    	changingList.set(false);
		if(!directionDown.get() && appsTrimTopAmount.get() > 0 && getCount() < displayListsDimensions.getCacheSize()){
			appsManager.scrollUp();
		}else {
			setLoadingHeader(false);
		}
	}
	
	private void appendAndUpdateDisplay(ViewDisplayListApps freshAvailableApps){	
		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "appending freshAvailableList: "+freshAvailableApps.size());
		if(freshAvailableApps.isEmpty()){
			return;
		}
		
		boolean newList = this.apps.isEmpty();
    	if(newList){
    		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "appending to empty available list");
    		this.apps = freshAvailableApps;
    		
    		initDisplayAvailable();
    	}else{	
    		this.apps.addAll(freshAvailableApps);
    		Log.d("Aptoide-DynamicAvailableAppsListAdapter", "new displayList size: "+this.apps.size());
    		
    		refreshDisplayAvailable();
    	}
    	changingList.set(false);
		if( directionDown.get() && !freshAvailableApps.isEmpty() && getCount() < displayListsDimensions.getCacheSize()){
			appsManager.scrollDown();
		}else {
			setLoadingFooter(false);
		}
	}
	
//	private void trimPrependAndUpdateDisplay(ViewDisplayListApps freshAvailableApps, int trimAmount){
//		prependAndUpdateDisplay(freshAvailableApps);
//		trimBottomAppsList(trimAmount);
//	}
//	
//	private void trimAppendAndUpdateDisplay(int trimAmount, ViewDisplayListApps freshAvailableApps){
//		trimTopAppsList(trimAmount);
//		appendAndUpdateDisplay(freshAvailableApps);
//	}
	
}
