package cm.aptoide.ptdev;

/**
 * Created by rmateus on 20-03-2014.
 */
public interface CategoryCallback{
    StoreActivity.SortObject getSort();

    void onRefreshStarted();

    void installApp(long id);
}
