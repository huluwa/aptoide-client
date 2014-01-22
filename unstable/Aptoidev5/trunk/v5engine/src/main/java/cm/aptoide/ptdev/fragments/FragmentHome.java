package cm.aptoide.ptdev.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.adapters.HomeBucketAdapter;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.database.Database;
import cm.aptoide.ptdev.events.BusProvider;
import cm.aptoide.ptdev.fragments.callbacks.RepoCompleteEvent;
import com.commonsware.cwac.merge.MergeAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentHome extends ListFragment implements LoaderManager.LoaderCallbacks<ArrayList<HomeItem>> {


    private MergeAdapter adapter;
    private List<HomeItem> editorsChoice = new ArrayList<HomeItem>();
    private List<HomeItem> top = new ArrayList<HomeItem>();
    private int editorsChoiceBucketSize;

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(50, null, this);
        getLoaderManager().initLoader(51, null, this);
    }

    @Override
    public void onStart() {
        BusProvider.getInstance().register(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onStoreCompleted(RepoCompleteEvent event) {
        Log.d("Aptoide-Home", "OnRefresh");

        if (event.getRepoId() == -2) {
            refreshEditorsList();
        }else if(event.getRepoId() == -1){
            refreshTopList();
        }
    }

    private void refreshEditorsList() {
        //editorsChoice.clear();
        adapter.notifyDataSetChanged();
        getLoaderManager().restartLoader(50, null, this);
    }

    private void refreshTopList() {
        //top.clear();
        adapter.notifyDataSetChanged();
        getLoaderManager().restartLoader(51, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new MergeAdapter();

        HomeBucketAdapter homeBucketAdapter = new HomeBucketAdapter(getActivity(), editorsChoice);
        View editorsView = View.inflate(getActivity(), R.layout.separator_home_header, null);
        ((TextView) editorsView.findViewById(R.id.separator_label)).setText(getString(R.string.editors_choice));
        editorsView.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://m.aptoide.com/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        adapter.addView(editorsView);
        editorsChoiceBucketSize = homeBucketAdapter.getBucketSize();
        adapter.addAdapter(homeBucketAdapter);
        getListView().setCacheColorHint(0);

        HomeBucketAdapter homeBucketAdapter2 = new HomeBucketAdapter(getActivity(), top);
        View v = View.inflate(getActivity(), R.layout.separator_home_header, null);
        ((TextView)v.findViewById(R.id.separator_label)).setText(getString(R.string.top_apps));
        v.findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://m.aptoide.com/more/topapps");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        adapter.addView(v);
        adapter.addAdapter(homeBucketAdapter2);
//
//        HomeBucketAdapter homeBucketAdapter3 = new HomeBucketAdapter(getSherlockActivity(), Arrays.asList(new HomeItem[]{new HomeItem(), new HomeItem(), new HomeItem(), new HomeItem()}));
//        adapter.addView(View.inflate(getSherlockActivity(), R.layout.separator_home_header, null));
//        adapter.addAdapter(homeBucketAdapter3);

        //getListView().setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_anim));

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        ImageLoader.getInstance().resume();
                        break;
                    default:
                        ImageLoader.getInstance().pause();
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }



    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Log.d("Aptoide-Home", "clicked");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public Loader<ArrayList<HomeItem>> onCreateLoader(final int id, final Bundle args) {



        AsyncTaskLoader<ArrayList<HomeItem>> asyncTaskLoader = new AsyncTaskLoader<ArrayList<HomeItem>>(getActivity()) {
            @Override
            public ArrayList<HomeItem> loadInBackground() {


                switch (id){
                    case 50:
                        return new Database(Aptoide.getDb()).getFeatured(2, editorsChoiceBucketSize);
                    case 51:
                        return new Database(Aptoide.getDb()).getFeatured(1, editorsChoiceBucketSize);
                    default:
                        return new Database(Aptoide.getDb()).getFeatured(1, editorsChoiceBucketSize);
                }


            }
        };

        asyncTaskLoader.forceLoad();

        return asyncTaskLoader;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<HomeItem>> loader, ArrayList<HomeItem> data) {


        switch (loader.getId()) {
            case 50:
                //editorsChoice.clear();
                editorsChoice.addAll(data);
                break;
            case 51:
                //top.clear();

                top.addAll(data);
                break;
        }

        if(getListView().getAdapter()==null){
            if(!data.isEmpty()) setListAdapter(adapter);
        }else{
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onLoaderReset(Loader<ArrayList<HomeItem>> loader) {

        switch (loader.getId()) {
            case 50:
                editorsChoice.clear();
                break;
            case 51:
                top.clear();
                break;
        }

        adapter.notifyDataSetChanged();

    }
}
