package cm.aptoide.ptdev;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import cm.aptoide.ptdev.adapters.HomeLayoutAdapter;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.model.Collection;
import cm.aptoide.ptdev.services.DownloadService;

import java.util.ArrayList;

/**
 * Created by rmateus on 28-01-2014.
 */
public class MoreEditorsChoiceActitivy extends ActionBarActivity implements DownloadInterface {


    private DownloadService downloadService;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            downloadService = ((DownloadService.LocalBinder) binder).getService();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_store);

        Fragment fragment = new MoreEditorsChoiceFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        bindService(new Intent(this, DownloadService.class), conn, BIND_AUTO_CREATE);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.more_editors_choice));
    }


    public static class MoreEditorsChoiceFragment extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<Collection>> {
        private boolean mWasEndedAlready;



        private HomeLayoutAdapter adapter;
        private ArrayList<Collection> editorsChoice;

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            editorsChoice = new ArrayList<Collection>();
            adapter = new HomeLayoutAdapter(getActivity(), editorsChoice, false);
            getLoaderManager().restartLoader(0, getArguments(), this);

            getListView().setDivider(null);
            getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
            getListView().setItemsCanFocus(true);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);

            //page_collection.setExpanded(!page_collection.isExpanded());

        }

        @Override
        public Loader<ArrayList<Collection>> onCreateLoader(final int id, final Bundle args) {
            AsyncTaskLoader<ArrayList<Collection>> asyncTaskLoader = new AsyncTaskLoader<ArrayList<Collection>>(getActivity()) {
                @Override
                public ArrayList<Collection> loadInBackground() {
                    Log.d("Aptoide-Home", String.valueOf(args.getInt("parentId")));
                    return new Database(Aptoide.getDb()).getSpecificFeatured(args.getInt("parentId"), adapter.getBucketSize());

                }
            };

            asyncTaskLoader.forceLoad();
            return asyncTaskLoader;
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<Collection>> loader, ArrayList<Collection> data) {

            editorsChoice.clear();
            editorsChoice.addAll(data);
            adapter.notifyDataSetChanged();
            setListAdapter(adapter);

        }

        @Override
        public void onLoaderReset(Loader<ArrayList<Collection>> loader) {

        }

    }

    public void installApp(long id) {
        downloadService.startDownloadFromAppId(id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
        } else if (i == R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

}
