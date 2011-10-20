/*
 * Constants.java, part of Aptoide
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

import android.os.Environment;

/**
 * Constants
 *
 * @author dsilveira
 *
 */
public class Constants {
	public static final int KBYTES_TO_BYTES = 1024;
	public static final String CACHE_PATH = Environment.getExternalStorageDirectory().getPath() + "/.aptoide/";
	public static final String SELF_UPDATE_FILE = CACHE_PATH + "latestSelfUpdate.apk";	//TODO possibly change apk name to reflect version code
	public static final String LATEST_VERSION_CODE_URI = "http://aptoide.com/latest_version.xml";
	
}