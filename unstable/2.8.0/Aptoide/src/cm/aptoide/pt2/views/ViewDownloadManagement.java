/*
 * ViewDownloadManagement, part of Aptoide
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
package cm.aptoide.pt2.views;

import java.util.HashMap;

import android.os.Handler;
import android.util.Log;
import cm.aptoide.pt2.ApplicationServiceManager;
import cm.aptoide.pt2.R;

/**
 * ViewDownloadManagement
 *
 * @author dsilveira
 *
 */
public class ViewDownloadManagement {
	private ApplicationServiceManager serviceManager;
	private HashMap<Integer, Handler> observers;
	
	private ViewApk appInfo;
	
	private ViewDownload viewDownload;
	private ViewCache cache;
	
	private boolean isLoginRequired;
	private ViewLogin login;
	
	private boolean isNull;
	
	
	/**
	 * 
	 * ViewDownloadManagement null object Constructor
	 *
	 */
	public ViewDownloadManagement(){
		this.isNull = true;
	}
	
	/**
	 * 
	 * ViewDownloadManagement skeleton Constructor
	 *
	 * @param serviceManager
	 */
	public ViewDownloadManagement(ApplicationServiceManager serviceManager){
		this.isNull = false;
		this.serviceManager = serviceManager;
		this.observers = new HashMap<Integer, Handler>();
	}
	
	/**
	 * 
	 * ViewDownloadManagement Constructor
	 *
	 * @param serviceManager
	 * @param url
	 * @param appInfo
	 * @param cache
	 */
	public ViewDownloadManagement(ApplicationServiceManager serviceManager, String remoteUrl, ViewApk appInfo, ViewCache cache) {
		this(serviceManager);
		this.viewDownload = new ViewDownload(remoteUrl);
		this.cache = cache;
		this.appInfo = appInfo;
	}
	
	/**
	 * 
	 * ViewDownloadManagement Constructor
	 *
	 * @param serviceManager
	 * @param url
	 * @param appInfo
	 * @param cache
	 * @param login
	 */
	public ViewDownloadManagement(ApplicationServiceManager serviceManager, String remoteUrl, ViewApk appInfo, ViewCache cache, ViewLogin login) {
		this(serviceManager,remoteUrl, appInfo, cache);
		if(login != null){
			this.isLoginRequired = true;
			this.login = login;
		}
	}
	
	
	public boolean isNull(){
		return this.isNull;
	}

	/**
	 * getProgress, in percentage
	 * 
	 * @return percentage int
	 */
	public int getProgress() {
		return (viewDownload == null?0:viewDownload.getProgressPercentage());
	}

	/**
	 * getProgressString, in percentage
	 * 
	 * @return percentage string
	 */
	public String getProgressString() {
		return getProgress()+"%";
	}
	
	public void updateProgress(ViewDownload update){
		Log.d("Aptoide-ViewDownloadManagement", "update: "+update+" downloadStatus: "+update.getStatus());
		this.viewDownload.setProgressTarget(update.getProgressTarget());
		this.viewDownload.setProgress(update.getProgress());
		this.viewDownload.setSpeedInKBps(update.getSpeedInKBps());
		this.viewDownload.setStatus(update.getStatus());
		switch (viewDownload.getStatus()) {
			case FAILED:
				this.viewDownload.setFailReason(update.getFailReason());
				notifyObservers(EnumDownloadProgressUpdateMessages.FAILED);
				break;
				
			case PAUSED:
				notifyObservers(EnumDownloadProgressUpdateMessages.PAUSED);
				break;
				
			case RESUMING:
				notifyObservers(EnumDownloadProgressUpdateMessages.RESUMING);
				break;
				
			case STOPPED:
				notifyObservers(EnumDownloadProgressUpdateMessages.STOPPED);
				break;
				
			case COMPLETED:
				notifyObservers(EnumDownloadProgressUpdateMessages.COMPLETED);
				break;
	
			default:
				notifyObservers(EnumDownloadProgressUpdateMessages.UPDATE);
				break;
		}
	}
	
	public void pause(){
		viewDownload.setStatus(EnumDownloadStatus.PAUSED);
		notifyObservers(EnumDownloadProgressUpdateMessages.PAUSED);
		serviceManager.pauseDownload(hashCode());
	}
	
	public void resume(){
		viewDownload.setStatus(EnumDownloadStatus.RESUMING);
		notifyObservers(EnumDownloadProgressUpdateMessages.RESUMING);
		serviceManager.resumeDownload(hashCode());
	}
	
	public void stop(){
		viewDownload.setStatus(EnumDownloadStatus.STOPPED);
		notifyObservers(EnumDownloadProgressUpdateMessages.STOPPED);
		serviceManager.stopDownload(hashCode());
	}
	
	public void restart(){
		viewDownload.setStatus(EnumDownloadStatus.RESTARTING);
		notifyObservers(EnumDownloadProgressUpdateMessages.RESTARTING);
		serviceManager.restartDownload(hashCode());
	}
	
	public EnumDownloadStatus getDownloadStatus(){
		return viewDownload.getStatus();
	}
	
	public int getSpeedInKBps(){
		return viewDownload.getSpeedInKBps();
	}
	
	public String getSpeedInKBpsString(){
		switch (viewDownload.getStatus()) {
			case PAUSED:
				return serviceManager.getString(R.string.paused);
	
			case FAILED:
			case STOPPED:
				return serviceManager.getString(R.string.stopped);
				
			default:
				if(viewDownload.getSpeedInKBps() == 0){
					return serviceManager.getString(R.string.slow);
				}else{
					return viewDownload.getSpeedInKBps()+" KBps";
				}
		}
		
		
	}
	
	public boolean isComplete(){
		return viewDownload.isComplete();
	}
	
	public ViewDownload getDownload(){
		return this.viewDownload;
	}

	public ViewCache getCache() {
		return cache;
	}
	
	public void setCache(ViewCache cache){
		this.cache = cache;
	}

	public String getRemoteUrl() {
		return viewDownload.getRemotePath();
	}

	public ViewApk getAppInfo() {
		return appInfo;
	}
	
	public void setAppInfo(ViewApk appInfo){
		this.appInfo = appInfo;
	}
	
	public boolean isLoginRequired(){
		return isLoginRequired;
	}
	
	public ViewLogin getLogin() {
		return login;
	}

	public void setLogin(ViewLogin login) {
		if(login != null){
			this.isLoginRequired = true;
			this.login = login;
		}else{
			this.isLoginRequired = false;
			this.login = null;
		}
	}

	public void registerObserver(int appId, Handler observerDownloadProgress){
		observers.put(appId, observerDownloadProgress);
	}

	public void unregisterObserver(int appId){
		try {
			observers.remove(appId);
		} catch (Exception e) { }
	}
	
	private void notifyObservers(EnumDownloadProgressUpdateMessages progressUpdate){
//	private void notifyObservers(){
		for (Handler listenerDownloadProgress : observers.values()) {
			listenerDownloadProgress.sendEmptyMessage(progressUpdate.ordinal());
//			listenerDownloadProgress.sendEmptyMessage(hashCode());
		}
	}
	
	
	/**
	 * startDownloadManagement, starts the download of the apk for the app described within this view
	 * 
	 * @throws AptoideDownloadException (runtimeException)
	 */
	public void startDownload(){
		serviceManager.startDownload(this);
	}
	
	
	
	/**
	 * ViewDownloadManagement object reuse clean references
	 *
	 */
	public void clean(){
		this.serviceManager = null;
		this.observers = null;
		this.appInfo = null;
		this.cache = null;
	}
	
	/**
	 * ViewDownloadManagement object skeleton reuse reConstructor
	 *
	 * @param serviceManager
	 */
	public void reuse(ApplicationServiceManager serviceManager) {
		this.serviceManager = serviceManager;
		this.observers = new HashMap<Integer, Handler>();
	}
	
	/**
	 * ViewDownloadManagement object reuse reConstructor
	 *
	 * @param serviceManager
	 * @param remoteUrl
	 * @param appInfo
	 * @param cache
	 */
	public void reuse(ApplicationServiceManager serviceManager, String remoteUrl, ViewApk appInfo, ViewCache cache) {
		reuse(serviceManager);
		this.viewDownload = new ViewDownload(remoteUrl);
		this.cache = cache;
		this.appInfo = appInfo;
	}
	
	/**
	 * ViewDownloadManagement object reuse reConstructor
	 *
	 * @param serviceManager
	 * @param remoteUrl
	 * @param appInfo
	 * @param cache
	 * @param login
	 */
	public void reuse(ApplicationServiceManager serviceManager, String remoteUrl, ViewApk appInfo, ViewCache cache, ViewLogin login) {
		reuse(serviceManager);
		this.viewDownload = new ViewDownload(remoteUrl);
		this.cache = cache;
		this.login = login;
		this.appInfo = appInfo;
	}


	@Override
	public int hashCode() {
		return this.appInfo.hashCode();
	}


	@Override
	public boolean equals(Object object) {
		if(object instanceof ViewDownloadManagement){
			ViewDownloadManagement download = (ViewDownloadManagement) object;
			if(download.hashCode() == this.hashCode()){
				return true;
			}
		}
		return false;
	}


	@Override
	public String toString() {
		return appInfo+" downloadStatus: "+viewDownload.getStatus()+viewDownload;
	}
	
}