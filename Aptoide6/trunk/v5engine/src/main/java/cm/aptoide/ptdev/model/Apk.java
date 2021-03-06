package cm.aptoide.ptdev.model;

import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.StatementHelper;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Filters;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: brutus
 * Date: 14-10-2013
 * Time: 16:08
 * To change this template use File | Settings | File Templates.
 */
public abstract class Apk implements Cloneable {

    public Apk() {
        signature = "";
        name = "";
        remotePath = "";
        versionName = "";
        packageName = "";
        md5h = "";
        category1 = "";
        category2 = "";
        categoryId = new ArrayList<Integer>();
        age =Filters.Age.All;
        minSdk = 0;
        minScreen = Filters.Screen.notfound;
        price = 0.0;
        minGlEs = "0.0";
        server = new Server() {
            @Override
            public String getApkpath() {
                return super.getApkpath();
            }
        };
        isRunning = true;
    }

    public Apk(Apk apk) {
        this.children = apk.children;
        this.signature = apk.signature;
        this.path = apk.path;
        this.repoId = apk.repoId;
        this.name = apk.name;
        this.remotePath = apk.remotePath;
        this.versionName = apk.versionName;
        this.versionCode = apk.versionCode;
        this.packageName = apk.packageName;
        this.iconPath = apk.iconPath;
        this.md5h = apk.md5h;
        this.downloads = apk.downloads;
        this.rating = apk.rating;
        this.category1 = apk.category1;
        this.category2 = apk.category2;
        this.categoryId = apk.categoryId;
        this.size = apk.size;
        this.age = apk.age;
        this.minSdk = apk.minSdk;
        this.minScreen = apk.minScreen;
        this.minGlEs = apk.minGlEs;
        this.screenCompat = apk.screenCompat;
        this.date = apk.date;
        this.price = apk.price;
        this.server = apk.server;
        this.cpuAbi = apk.cpuAbi;
        this.isRunning = apk.isRunning;
    }

    private ArrayList<Apk> children;
    private String signature ;
    private String path;
    private long repoId;
    private String name ;
    private String remotePath ;
    private String versionName ;
    private int versionCode;
    private String packageName;
    private String iconPath;
    private String md5h ;
    private int downloads;
    private double rating;
    private String category1 ;
    private String category2 ;
    private List<Integer> categoryId;
    private long size;
    private Filters.Age age;
    private int minSdk ;
    private Filters.Screen minScreen;
    private String screenCompat;
    private Date date;
    private double price;
    private String minGlEs;
    private Server server ;
    private String cpuAbi;
    boolean isRunning ;

    public long getRepoId() {
        return repoId;
    }

    public void setRepoId(long repoId) {
        this.repoId = repoId;
    }

    public String getCpuAbi() {
        return cpuAbi;
    }

    public void setCpuAbi(String cpuAbi) {
        this.cpuAbi = cpuAbi;
    }


    public String getCategory2() {
        return category2;
    }

    public void setCategory2(String category2) {
        this.category2 = category2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getMd5h() {
        return md5h;
    }

    public void setMd5h(String md5h) {
        this.md5h = md5h;
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getCategory1() {
        return category1;
    }

    public void setCategory1(String category1) {
        this.category1 = category1;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Filters.Age getAge() {
        return age;
    }

    public void setAge(Filters.Age age) {
        this.age = age;
    }

    public int getMinSdk() {
        return minSdk;
    }

    public void setMinSdk(int minSdk) {
        this.minSdk = minSdk;
    }

    public Filters.Screen getMinScreen() {
        return minScreen;
    }

    public void setMinScreen(Filters.Screen minScreen) {
        this.minScreen = minScreen;
    }


    public void setScreenCompat(String screenCompat) {
        this.screenCompat = screenCompat;
    }

    public String getScreenCompat() {
        return screenCompat;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setMinGlEs(String minGlEs) {
        this.minGlEs = minGlEs;
    }

    public String getMinGlEs() {
        return minGlEs;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public List<Integer> getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(List<Integer> categoryId) {
        this.categoryId = categoryId;
    }

    public void addCategoryId(int id) {
        categoryId.add(id);
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getSignature() {
        return signature.replaceAll(":", "");
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public ArrayList<Apk> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Apk> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return name + " " + versionCode;
    }


    public abstract void addApkToChildren();

    public abstract List<String> getStatements();

    public abstract void databaseDelete(Database db);

    public abstract void databaseInsert(List<SQLiteStatement> sqLiteStatements, HashMap<Integer, Integer> categoriesIds);

    protected ArrayList<String> getValues(){
        ArrayList<String> values = new ArrayList<String>();

        values.add(Schema.Apk.COLUMN_APKID);
        values.add(Schema.Apk.COLUMN_NAME);
        values.add(Schema.Apk.COLUMN_VERCODE);
        values.add(Schema.Apk.COLUMN_VERNAME);
        values.add(Schema.Apk.COLUMN_REPO_ID);
        values.add(Schema.Apk.COLUMN_DATE);
        values.add(Schema.Apk.COLUMN_DOWNLOADS);
        values.add(Schema.Apk.COLUMN_RATING);
        values.add(Schema.Apk.COLUMN_MATURE);
        values.add(Schema.Apk.COLUMN_SDK);
        values.add(Schema.Apk.COLUMN_SCREEN);
        values.add(Schema.Apk.COLUMN_GLES);
        values.add(Schema.Apk.COLUMN_ICON);
        values.add(Schema.Apk.COLUMN_IS_COMPATIBLE);
        values.add(Schema.Apk.COLUMN_SIGNATURE);
        values.add(Schema.Apk.COLUMN_PATH);
        values.add(Schema.Apk.COLUMN_MD5);
        values.add(Schema.Apk.COLUMN_PRICE);
        return values;
    }

}
