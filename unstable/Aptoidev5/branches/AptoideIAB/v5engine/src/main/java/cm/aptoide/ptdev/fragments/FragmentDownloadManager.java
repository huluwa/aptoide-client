package cm.aptoide.ptdev.fragments;

import android.app.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import cm.aptoide.ptdev.DownloadServiceConnected;
import cm.aptoide.ptdev.Start;
import cm.aptoide.ptdev.R;

import cm.aptoide.ptdev.adapters.DownloadManagerSectionAdapter;
import cm.aptoide.ptdev.adapters.DownloadSimpleSectionAdapter;
import cm.aptoide.ptdev.adapters.NotOngoingAdapter;
import cm.aptoide.ptdev.adapters.OngoingAdapter;

import cm.aptoide.ptdev.dialogs.AptoideDialog;
import cm.aptoide.ptdev.downloadmanager.event.DownloadEvent;

import cm.aptoide.ptdev.events.BusProvider;

import cm.aptoide.ptdev.model.Download;
import cm.aptoide.ptdev.services.DownloadService;
import com.commonsware.cwac.merge.MergeAdapter;
import com.flurry.android.FlurryAgent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;


/**
 * Created with IntelliJ IDEA.
 * User: rmateus
 * Date: 28-10-2013
 * Time: 11:40
 * To change this template use File | Settings | File Templates.
 */
public class FragmentDownloadManager extends ListFragment {

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        Object type = getListView().getAdapter().getItem((info).position);
        MenuInflater inflater = this.getActivity().getMenuInflater();

        Log.d("onCreateContextMenu", "OnCreate");

        if (type instanceof Download) {

            switch (((Download) type).getDownloadState()) {
                case ERROR:
                    inflater.inflate(R.menu.menu_download_error, menu);
                    break;
            }

        }

    }

    Start callback;
    DownloadService service;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callback = (Start) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.callback = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        initAdapters(null);
    }

    @Subscribe
    public synchronized void  initAdapters(DownloadServiceConnected event) {


        service = callback.getDownloadService();

        if (service != null) {
            ongoingList.clear();
            notOngoingList.clear();
            ongoingList.addAll(service.getAllActiveDownloads());
            notOngoingList.addAll(service.getAllNotActiveDownloads());
            sectionAdapter.notifyDataSetChanged();
            getActivity().supportInvalidateOptionsMenu();
        }

    }


    @Override
    public void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    MergeAdapter adapter;

    OngoingAdapter ongoingAdapter;
    NotOngoingAdapter notOngoingAdapter;
    ArrayList<Download> ongoingList = new ArrayList<Download>();
    ArrayList<Download> notOngoingList = new ArrayList<Download>();
    DownloadSimpleSectionAdapter<Download> sectionAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        adapter = new MergeAdapter();

        ongoingAdapter = new OngoingAdapter(getActivity(), ongoingList);
        notOngoingAdapter = new NotOngoingAdapter(getActivity(), notOngoingList);

        adapter.addAdapter(ongoingAdapter);
        adapter.addAdapter(notOngoingAdapter);

        sectionAdapter = new DownloadSimpleSectionAdapter<Download>(getActivity(),  adapter);

        setListAdapter(sectionAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_download_manager, menu);
        if (!notOngoingList.isEmpty()) {
            menu.findItem(R.id.menu_clear_downloads).setVisible(true);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_retry) {
            ((Download) getListAdapter().getItem(((AdapterView.AdapterContextMenuInfo) item.getMenuInfo()).position)).getParent().download();
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.menu_clear_downloads) {
            final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_clear_downloads, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity()).setView(view);
            final AlertDialog clearDownloadsDialog = alertDialogBuilder.create();
            clearDownloadsDialog.setTitle(getString(R.string.clear));
            clearDownloadsDialog.setIcon(android.R.drawable.ic_dialog_alert);
            clearDownloadsDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            clearDownloadsDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean clearApks = ((CheckBox)view.findViewById(R.id.checkbox_clear)).isChecked();
                    service.removeNonActiveDownloads(clearApks);
                    if(Build.VERSION.SDK_INT >= 10) FlurryAgent.logEvent("Download_Manager_Cleared_Apks");
                }
            });

            clearDownloadsDialog.show();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        callback.installAppFromManager(id);
    }

    @Subscribe
    public synchronized void onDownloadStatus(DownloadEvent event) {


        if (service != null && getActivity() != null) {
            ongoingList.clear();
            notOngoingList.clear();
            ongoingList.addAll(service.getAllActiveDownloads());
            notOngoingList.addAll(service.getAllNotActiveDownloads());
            sectionAdapter.notifyDataSetChanged();

            getActivity().supportInvalidateOptionsMenu();
            Log.d("Aptoide-DownloadManager", "On Download Status");
        }


    }


    @Subscribe
    public synchronized void onDownloadUpdate(Download download) {
        Log.d("Aptoide-DownloadManager", "onDownloadUpdate " + download.getId());

        try {
            int start = getListView().getFirstVisiblePosition();
            for (int i = start, j = getListView().getLastVisiblePosition(); i <= j; i++) {
                if (download.equals((getListView().getItemAtPosition(i)))) {
                    View view = getListView().getChildAt(i - start);
                    getListView().getAdapter().getView(i, view, getListView());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setDivider(null);
        getListView().setCacheColorHint(getResources().getColor(android.R.color.transparent));
        setEmptyText(getString(R.string.no_downloads));
        registerForContextMenu(getListView());
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
}
