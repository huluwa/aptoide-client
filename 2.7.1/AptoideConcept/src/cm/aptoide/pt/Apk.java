package cm.aptoide.pt;

import java.util.Vector;

import android.content.ContentValues;

public class Apk {
	public Apk(int vercode, long repo_id) {
		this.vercode=vercode;
		this.repo_id=repo_id;
	}
	public Apk() {}
	
	public Apk(ContentValues values) {
		
	}
	long id;
	public String apkid;
	public String name;
	public String vername;
	public int vercode;
	public String downloads;
	public String size;
	public String category1 = "Others";
	public String category2 = "Others";
	public String stars;
	public int age;
	public String md5;
	public String path;
	public String icon;
	public String date;
	public String minSdk = "0";
	public long repo_id;
	public int minScreenSize = 0;
	public String featuregraphic;
	public String highlighted;
	public Vector<String> screenshots = new Vector<String>();

}
