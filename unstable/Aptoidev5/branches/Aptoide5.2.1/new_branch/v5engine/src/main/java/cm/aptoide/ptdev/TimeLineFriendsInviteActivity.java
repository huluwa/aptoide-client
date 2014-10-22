package cm.aptoide.ptdev;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.manuelpeinado.multichoiceadapter.extras.actionbarcompat.MultiChoiceArrayAdapter;
import com.octo.android.robospice.SpiceManager;

import cm.aptoide.ptdev.adapters.InviteFriendsListAdapter;
import cm.aptoide.ptdev.fragments.GenericResponse;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.timeline.ListUserFriendsRequest;
import cm.aptoide.ptdev.webservices.timeline.RegisterUserFriendsInviteRequest;
import cm.aptoide.ptdev.webservices.timeline.TimelineRequestListener;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;

/**
 * Created by asantos on 20-10-2014.
 */
public class TimeLineFriendsInviteActivity extends ActionBarActivity {
    private InviteFriendsListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_timeline_invite_friends);
        setTitle(R.string.invite_friends);

        rebuildList(savedInstanceState);
        View invite =findViewById(R.id.timeline_invite);
        final Context c = this;
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterUserFriendsInviteRequest request = new RegisterUserFriendsInviteRequest();
                for(long id : adapter.getCheckedItems()){
                    Log.d("pois","id:"+id);
                    Log.d("pois","id:"+adapter.getItem((int)id).getEmail());
                    request.addEmail(adapter.getItem((int)id).getEmail());
                }
                manager.execute(request,new TimelineRequestListener<GenericResponse>(){
                    @Override
                    protected void caseOK(GenericResponse response) {
                        Toast.makeText(c, c.getString(R.string.facebook_timeline_friends_invited), Toast.LENGTH_LONG).show();
                        finish();
                    }
                });

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        manager.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        manager.shouldStop();
    }
    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);
    private void rebuildList(final Bundle savedInstanceState) {
        final TimeLineFriendsInviteActivity c = this;
        ListUserFriendsRequest request = new ListUserFriendsRequest();
        request.setOffset(0);
        request.setLimit(150);
        manager.execute(request, new TimelineRequestListener<ListUserFriendsJson>(){
            @Override
            protected void caseOK(ListUserFriendsJson response) {
                adapter = new InviteFriendsListAdapter(savedInstanceState, c, response.getFriends());
                //adapter.setOnItemClickListener(this);
                final ListView listView = c.getListView();
                adapter.setAdapterView(listView);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        final MultiChoiceArrayAdapter adapter = (InviteFriendsListAdapter) parent.getAdapter();
                        adapter.setItemChecked(position, !adapter.isChecked(position));

                    }
                });

            }
        });

    }
    private ListView getListView() {
        return (ListView) findViewById(android.R.id.list);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        adapter.save(outState);
    }
}
