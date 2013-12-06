package cm.aptoide.ptdev.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import cm.aptoide.ptdev.AppViewActivity;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import cm.aptoide.ptdev.utils.SimpleCursorLoader;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.commonsware.cwac.merge.MergeAdapter;
import com.squareup.otto.Subscribe;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentUpdates extends SherlockListFragment {

    private InstalledAdapter installedAdapter;
    private UpdatesAdapter updatesAdapter;
    private Database db;
    private RecentlyUpdated recentUpdates;

    private MergeAdapter adapter;

    @Subscribe
    public void RefreshStoresEvent(RepoCompleteEvent event){

        Log.d("Aptoide-", "OnEvent");


    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new MergeAdapter();

        this.db = new Database(Aptoide.getDb());

        updatesAdapter = new UpdatesAdapter(getSherlockActivity());
        adapter.addAdapter(updatesAdapter);

        recentUpdates = new RecentlyUpdated(getSherlockActivity());
        adapter.addAdapter(recentUpdates);

        installedAdapter = new InstalledAdapter(getSherlockActivity());
        adapter.addAdapter(installedAdapter);

        setHasOptionsMenu(true);

    }



    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if(id>0){
            Intent i = new Intent(getSherlockActivity(), AppViewActivity.class);
            i.putExtra("id", id);
            startActivity(i);
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_rollback, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        getLoaderManager().initLoader(91, null, new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {

                return new SimpleCursorLoader(getSherlockActivity()) {
                    @Override
                    public Cursor loadInBackground() {
                        return db.getUpdates();
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                updatesAdapter.swapCursor(data);
                if (getListView().getAdapter() == null)
                    setListAdapter(adapter);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });


        getLoaderManager().initLoader(90, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {

                return new SimpleCursorLoader(getSherlockActivity()) {
                    @Override
                    public Cursor loadInBackground() {
                        return db.getInstalled();
                    }
                };
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                installedAdapter.swapCursor(data);
                if (getListView().getAdapter() == null)
                    setListAdapter(adapter);

            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        });


    }


}
