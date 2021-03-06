package cm.aptoide.ptdev.model;

import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.database.StatementHelper;
import cm.aptoide.ptdev.database.schema.Schema;
import cm.aptoide.ptdev.model.Apk;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.utils.Filters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 29-11-2013
 * Time: 18:20
 * To change this template use File | Settings | File Templates.
 */
public class ApkEditorsChoice extends Apk {
    private String featuredGraphic;
    private int order = -1;

    public ApkEditorsChoice(){
        super();
    }

    public ApkEditorsChoice(ApkEditorsChoice apkEditorsChoice) {
        super(apkEditorsChoice);
        this.featuredGraphic = apkEditorsChoice.featuredGraphic;
        this.order = apkEditorsChoice.order;
    }

    @Override
    public List<String> getStatements() {

        ArrayList<String> statements = new ArrayList<String>(10);
        ArrayList<String> values = getValues();

        statements.add(0, StatementHelper.getInsertStatment(Schema.Apk.getName(), values));

        values.add(Schema.Category_Apk.COLUMN_APK_ID);
        values.add(Schema.Category_Apk.COLUMN_CATEGORY_ID);
        values.add(Schema.Category_Apk.COLUMN_REPO_ID);

        statements.add(1, StatementHelper.getInsertStatment(Schema.Category_Apk.getName(), values));

        statements.add(2, "select id_apk from apk where id_repo = ? and package_name = ? and version_code = ?");


        values.add(Schema.FeaturedEditorsChoice.COLUMN_FEATURED_GRAPHIC_PATH);
        values.add(Schema.FeaturedEditorsChoice.COLUMN_ID);
        values.add(Schema.FeaturedEditorsChoice.COLUMN_ORDER);

        statements.add(3, StatementHelper.getInsertStatment(Schema.FeaturedEditorsChoice.getName(), values));


        return statements;
    }

    @Override
    public void databaseDelete(Database db) {

    }

    @Override
    public void databaseInsert(List<SQLiteStatement> sqLiteStatements, HashMap<Integer, Integer> categoriesIds) {

        long apkid;

        try {

            StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(0),
                    new String[]{getPackageName(),
                            getName(),
                            String.valueOf(getVersionCode()),
                            String.valueOf(getVersionName()),
                            String.valueOf(getRepoId()),
                            String.valueOf(getDate().getTime()),
                            String.valueOf(getDownloads()),
                            String.valueOf(getRating()),
                            String.valueOf(getAge().equals(Filters.Age.Mature)?1:0),
                            String.valueOf(getMinSdk()),
                            String.valueOf(getMinScreen()),
                            getMinGlEs(),
                            getIconPath(),
                            String.valueOf(AptoideUtils.isCompatible(this) ? 1 : 0),
                            getSignature(),
                            getPath(),
                            getMd5h(),
                            String.valueOf(getPrice())


                    });
            apkid = sqLiteStatements.get(0).executeInsert();

        } catch (SQLiteException e) {
            if(Aptoide.DEBUG_MODE) e.printStackTrace();

            //Log.d("RepoParser-ApkInfo-Insert", "Conflict: " + e.getMessage() + " on " + getPackageName() + " " + getRepoId() + " " + getVersionCode());
            StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(2), new String[]{ String.valueOf(getRepoId()), getPackageName(), String.valueOf(getVersionCode()) });
            apkid = sqLiteStatements.get(2).simpleQueryForLong();

        }

        for (Integer catid : getCategoryId()) {
            try {
                StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(1), new String[]{
                        String.valueOf(apkid),
                        String.valueOf(catid),
                });
                sqLiteStatements.get(1).executeInsert();
            } catch (SQLiteException e) {
                throw e;
            }
        }

        if(featuredGraphic != null){
            StatementHelper.bindAllArgsAsStrings(sqLiteStatements.get(3), new String[]{
                    String.valueOf(featuredGraphic),
                    String.valueOf(apkid),
                    String.valueOf(order)
            });
            sqLiteStatements.get(3).executeInsert();
        }


    }

    @Override
    public void addApkToChildren() {
        getChildren().add(new ApkEditorsChoice(this));
    }


    public void setFeaturedGraphic(String featuredGraphic) {
        this.featuredGraphic = featuredGraphic;
    }


    public void setOrder(int order) {
        this.order = order;
    }
}
