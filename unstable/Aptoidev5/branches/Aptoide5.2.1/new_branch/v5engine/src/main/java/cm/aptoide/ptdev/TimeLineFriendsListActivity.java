package cm.aptoide.ptdev;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.octo.android.robospice.SpiceManager;

import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.webservices.timeline.ListUserFriendsRequest;
import cm.aptoide.ptdev.webservices.timeline.TimelineRequestListener;
import cm.aptoide.ptdev.webservices.timeline.json.ListUserFriendsJson;

/**
 * Created by asantos on 29-09-2014.
 */
public class TimeLineFriendsListActivity extends ActionBarActivity {
    SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);
    private TextView friends_using_timeline;
    private LinearLayout friends_list;
    private Button start_timeline;


    public void setFriends(ListUserFriendsJson friends){
        StringBuilder friendsString;
        int i = 0;
        if(!friends.getFriends().isEmpty()){
            friendsString = new StringBuilder(friends.getFriends().get(0).getUsername());

            for(i = 1; i!=friends.getFriends().size() && i < 3; i++){
                friendsString.append(", ").append(friends.getFriends().get(i).getUsername());
            }
            String text;
            text = getString(R.string.facebook_friends_list_using_timeline);
            if ( i == 0){
                text= friendsString.toString() +text;
            }else{
                text=friendsString.toString()
                        +" "+ getString(R.string.and)
                        +" "+ String.valueOf(friends.getFriends().size() - i)
                        +" "+ text;
            }

            friends_using_timeline.setText(text);

            for(ListUserFriendsJson.Friend friend : friends.getFriends()){
                String avatar = friend.getAvatar();
                View v = LayoutInflater.from(this).inflate(R.layout.row_facebook_friends_on_timeline, null);
                ImageView avatarIv = (ImageView) v.findViewById(R.id.user_avatar);
                ImageLoader.getInstance().displayImage(avatar, avatarIv);
                friends_list.addView(v);

            }



        }else{

            friends_using_timeline.setVisibility(View.GONE);

        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Aptoide.getThemePicker().setAptoideTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_timeline_logged_in);
        friends_using_timeline = (TextView) findViewById(R.id.friends_using_timeline);

        getFriends();

//        lv = (ListView) findViewById(R.id.TimeLineListView);
        friends_list = (LinearLayout) findViewById(R.id.friends_list);

        start_timeline = (Button) findViewById(R.id.start_timeline);
        start_timeline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK);
                finish();
            }
        });

        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.social_timeline);

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
    public void getFriends(){
        ListUserFriendsRequest request = new ListUserFriendsRequest();
        manager.execute(request, new TimelineRequestListener<ListUserFriendsJson>() {
            @Override
            protected void caseOK(ListUserFriendsJson response) {
                setFriends((response));
            }
        });
    }

}