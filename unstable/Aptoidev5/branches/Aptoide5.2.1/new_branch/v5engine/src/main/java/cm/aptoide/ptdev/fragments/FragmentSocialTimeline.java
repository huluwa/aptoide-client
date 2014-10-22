package cm.aptoide.ptdev.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.FeedBackActivity;
import cm.aptoide.ptdev.LoginActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.TimeLineFriendsListActivity;
import cm.aptoide.ptdev.adapters.EndlessWrapperAdapter;
import cm.aptoide.ptdev.dialogs.TimeLineCommentsDialog;
import cm.aptoide.ptdev.fragments.callbacks.GetStartActivityCallback;
import cm.aptoide.ptdev.preferences.Preferences;
import cm.aptoide.ptdev.services.HttpClientSpiceService;
import cm.aptoide.ptdev.utils.AptoideUtils;
import cm.aptoide.ptdev.webservices.timeline.AddUserApkInstallCommentRequest;
import cm.aptoide.ptdev.webservices.timeline.AddUserApkInstallLikeRequest;
import cm.aptoide.ptdev.webservices.timeline.ChangeUserApkInstallStatusRequest;
import cm.aptoide.ptdev.webservices.timeline.ChangeUserSettingsRequest;
import cm.aptoide.ptdev.webservices.timeline.GetUserApkInstallCommentsRequest;
import cm.aptoide.ptdev.webservices.timeline.GetUserSettingsRequest;
import cm.aptoide.ptdev.webservices.timeline.ListApksInstallsRequest;
import cm.aptoide.ptdev.webservices.timeline.TimeLineManager;
import cm.aptoide.ptdev.webservices.timeline.TimelineRequestListener;
import cm.aptoide.ptdev.webservices.timeline.json.ApkInstallComments;
import cm.aptoide.ptdev.webservices.timeline.json.GetUserSettingsJson;
import cm.aptoide.ptdev.webservices.timeline.json.TimelineListAPKsJson;

/**
 * Created by rmateus on 21-10-2014.
 */
public class FragmentSocialTimeline extends Fragment implements FragmentSignIn.Callback, FragmentSocialTimelineLayouts.Callback {


    public void loginError() {
        init();
    }

    public void loginEnded() {
        startTimeline();
    }

    private void startTimeline() {
        SubFragmentSocialTimeline fragment = new SubFragmentSocialTimeline();
        //fragment.setTargetFragment(this, 0);
        getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
    }


    private UiLifecycleHelper fbhelper;

    @Override
    public void onStartTimeline() {
        startTimeline();
    }

    public class GetUserSettingsRequestListener extends TimelineRequestListener<GetUserSettingsJson> {
        @Override
        protected void caseOK(GetUserSettingsJson response) {
            if (response.getResults() != null) {
                boolean serverTimelineActive = response.getResults().getTimeline().equals("active");
                if (serverTimelineActive) {
                    startTimeline();
                } else {
                    Fragment fragment = new FragmentSocialTimelineLayouts();
                    Bundle args = new Bundle();
                    args.putBoolean(FragmentSocialTimelineLayouts.LOGGED_IN_ARG, true);
                    fragment.setArguments(args);
                    getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                }
            }
        }

        @Override
        protected void caseFAIL() {
            //finish();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if(savedInstanceState == null || !PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getBoolean(Preferences.TIMELINE_ACEPTED_BOOL, false)) {
            Fragment fragment = null;
            init();
        }



        fbhelper = new UiLifecycleHelper(getActivity(), new Session.StatusCallback() {
            @Override
            public void call(final Session session, SessionState state, Exception exception) {
                if (!AptoideUtils.isLoggedIn(Aptoide.getContext())) {
                    if (session.isOpened()) {
                        Toast.makeText(Aptoide.getContext(), "New me request " + state.name(), Toast.LENGTH_LONG).show();
                        Request.newMeRequest(session, new Request.GraphUserCallback() {

                            @Override
                            public void onCompleted(GraphUser user, Response response) {

                                try {

                                    Fragment fragment = new FragmentSignIn();
                                    Bundle args = new Bundle();
                                    args.putInt(FragmentSignIn.LOGIN_MODE_ARG, LoginActivity.Mode.FACEBOOK.ordinal());
                                    args.putString(FragmentSignIn.LOGIN_PASSWORD_OR_TOKEN_ARG, session.getAccessToken());
                                    args.putString(FragmentSignIn.LOGIN_USERNAME_ARG, (String) user.getProperty("email"));
                                    fragment.setArguments(args);
                                    //fragment.setTargetFragment(FragmentSocialTimeline.this, 0);
                                    getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).executeAsync();
                    }
                }
            }
        });

        fbhelper.onCreate(savedInstanceState);


    }

    private void init() {
        Fragment fragment;
        if (AptoideUtils.isLoggedIn(Aptoide.getContext())) {
            if ("FACEBOOK".equals(PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).getString("loginType", null))) {
                GetUserSettingsRequest request = new GetUserSettingsRequest();
                request.addSetting(GetUserSettingsRequest.TIMELINE);
                ((GetStartActivityCallback)getActivity()).getSpiceManager().execute(request, "timeline-status", DurationInMillis.ONE_HOUR / 2, new GetUserSettingsRequestListener());
            } else {
                fragment = new FragmentSocialTimelineLayouts();
                Bundle args = new Bundle();
                args.putBoolean(FragmentSocialTimelineLayouts.LOGOUT_FIRST_ARG, true);
                fragment.setArguments(args);
                getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();

            }
        } else {
            fragment = new FragmentSocialTimelineLayouts();
            Bundle args = new Bundle();
            args.putBoolean(FragmentSocialTimelineLayouts.LOGOUT_FIRST_ARG, false);
            fragment.setArguments(args);
            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.page_store, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        fbhelper.onResume();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


//        if(savedInstanceState == null){
//            Fragment fragment = new SubFragmentSocialTimeline();
//            fragment.setTargetFragment(this, 0);
//            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
//        }



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fbhelper.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        fbhelper.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbhelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onStop() {
        super.onStop();
        fbhelper.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        fbhelper.onSaveInstanceState(outState);
    }

    public static class SubFragmentSocialTimeline extends ListFragment implements SwipeRefreshLayout.OnRefreshListener, TimeLineManager, EndlessWrapperAdapter.Callback {

        private static final int COMMENTSLIMIT = 10;
        private static final String COMMENTSDIALOGTAG = "CD";
        private static final String TIMELINEFRIENDSLISTDIALOGTAG = "TLFLD";

        private ArrayList<TimelineListAPKsJson.UserApk> apks = new ArrayList<TimelineListAPKsJson.UserApk>();
        private EndlessWrapperAdapter adapter;
        private Number lastId;
        private Number firstId;
        private SwipeRefreshLayout mSwipeRefreshLayout;
        private boolean mListShown = false;
        private View mProgressContainer;
        private boolean showDialog;
        private boolean serverTimelineActive;

        private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

            public ListFragmentSwipeRefreshLayout(Context context) {
                super(context);
            }

            /**
             * As mentioned above, we need to override this method to properly signal when a
             * 'swipe-to-refresh' is possible.
             *
             * @return true if the {@link android.widget.ListView} is visible and can scroll up.
             */
            @Override
            public boolean canChildScrollUp() {
                final ListView listView = getListView();
                if (listView.getVisibility() == View.VISIBLE) {
                    return canListViewScrollUp(listView);
                } else {
                    return false;
                }
            }

        }



        private static boolean canListViewScrollUp(ListView listView) {
            if (android.os.Build.VERSION.SDK_INT >= 14) {
                // For ICS and above we can call canScrollVertically() to determine this
                return ViewCompat.canScrollVertically(listView, -1);
            } else {
                // Pre-ICS we need to manually check the first visible item and the child view's top
                // value
                return listView.getChildCount() > 0 &&
                        (listView.getFirstVisiblePosition() > 0
                                || listView.getChildAt(0).getTop() < listView.getPaddingTop());
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // Create the list fragment's content view by calling the super method
            final FrameLayout listFragmentView = (FrameLayout) super.onCreateView(inflater, container, savedInstanceState);

            // Now create a SwipeRefreshLayout to wrap the fragment's content view
            mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());

            mSwipeRefreshLayout.setOnRefreshListener(this);

            // Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
            // the SwipeRefreshLayout
            mSwipeRefreshLayout.addView(listFragmentView,
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            // Make sure that the SwipeRefreshLayout will fill the fragment
            mSwipeRefreshLayout.setLayoutParams(
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));

            // Now return the SwipeRefreshLayout as this fragment's content view
            return mSwipeRefreshLayout;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            init();


        }

        public void onItemsReady(ArrayList<TimelineListAPKsJson.UserApk> data) {
            if (data.isEmpty()) {
                adapter.stopAppending();
            } else {
                if (firstId == null) {
                    firstId = data.get(0).getInfo().getId();
                }
                data.get(0).animate = true;

                if(AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true)){

                    for(TimelineListAPKsJson.UserApk apk : data){
                        if(!apk.getApk().getAge().equals("Mature")){
                            apks.add(apk);
                        }
                    }

                }else{
                    apks.addAll(data);
                }


                lastId = apks.get(apks.size() - 1).getInfo().getId();
            }   // Tell the EndlessAdapter to
            // remove it's pending
            // view and call
            // notifyDataSetChanged()
            adapter.onDataReady();

        }

        public void onItemsReadyRefresh(ArrayList<TimelineListAPKsJson.UserApk> data) {

            if(AptoideUtils.getSharedPreferences().getBoolean("matureChkBox", true)){

                for(TimelineListAPKsJson.UserApk apk : data){
                    if(!apk.getApk().getAge().equals("Mature")){
                        apks.add(apk);
                    }
                }

            }else{
                apks.addAll(0, data);
            }


            if (apks.size() > 0) {
                firstId = apks.get(0).getInfo().getId();
                lastId = apks.get(apks.size()-1).getInfo().getId();
                adapter.onDataReady();
            }
            // remove it's pending
            // view and call
            // notifyDataSetChanged()
        }


        SpiceManager manager = new SpiceManager(HttpClientSpiceService.class);

        private ListApksInstallsRequest listAPKsInstallsRequest;


        private RequestListener<TimelineListAPKsJson> listener = new RequestListener<TimelineListAPKsJson>() {

            @Override
            public void onRequestFailure(SpiceException spiceException) {

            }

            @Override
            public void onRequestSuccess(TimelineListAPKsJson timelineListAPKsJson) {

                if(timelineListAPKsJson!=null && timelineListAPKsJson.getUsersapks()!=null){
                    onItemsReady(new ArrayList<TimelineListAPKsJson.UserApk>(timelineListAPKsJson.getUsersapks()));
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (!mListShown) setListShown(true);
                }



            }

        };

        private RequestListener<TimelineListAPKsJson> listenerRefresh = new RequestListener<TimelineListAPKsJson>() {

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                //swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onRequestSuccess(TimelineListAPKsJson timelineListAPKsJson) {
                apks.clear();
                onItemsReadyRefresh(new ArrayList<TimelineListAPKsJson.UserApk>(timelineListAPKsJson.getUsersapks()));
                mSwipeRefreshLayout.setRefreshing(false);

                if (!mListShown) setListShown(true);

            }

        };

        public void runRequest() {
            listAPKsInstallsRequest = new ListApksInstallsRequest();

            if (lastId != null) {
                listAPKsInstallsRequest.setOffset_id(String.valueOf(lastId.intValue()));
                listAPKsInstallsRequest.setDownwardsDirection();
            }

            manager.execute(listAPKsInstallsRequest,"timeline-posts-id" + (lastId != null ? lastId.intValue() : "") + username, DurationInMillis.ONE_HOUR / 2, listener);
        }

        public void refreshRequest() {
            listAPKsInstallsRequest = new ListApksInstallsRequest();

            //listAPKsInstallsRequest.setOffset(String.valueOf(firstId.intValue()));
            //listAPKsInstallsRequest.setUpwardsDirection();
            adapter.notifyDataSetChanged();
            adapter.restartAppending();

            try {
                manager.removeDataFromCache(TimelineListAPKsJson.class, "timeline-posts-id" + username).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            manager.execute(listAPKsInstallsRequest, "timeline-posts-id" + username , DurationInMillis.ONE_HOUR / 2, listenerRefresh);
        }




        @Override
        public void onCreate(Bundle savedInstanceState) {
            //Aptoide.getThemePicker().setAptoideTheme(this);
            super.onCreate(savedInstanceState);
            //setContentView(R.layout.page_timeline);
            Bundle addAccountOptions = null;


//            if (AptoideUtils.isLoggedIn(getActivity())) {
//                if ("FACEBOOK".equals(PreferenceManager.getDefaultSharedPreferences(this).getString("loginType", null))) {
//                    GetUserSettingsRequest request = new GetUserSettingsRequest();
//                    request.addSetting(GetUserSettingsRequest.TIMELINE);
//                    spiceManager.execute(request, "timeline-status", DurationInMillis.ONE_HOUR / 2, new GetUserSettingsRequestListener());
//                    return;
//                } else {
//                    addAccountOptions = new Bundle();
//                    addAccountOptions.putBoolean(LoginActivity.OPTIONS_LOGOUT_BOOL, true);
//                }
//            }
//
//            if (addAccountOptions == null)
//                addAccountOptions = new Bundle();
//            addAccountOptions.putBoolean(LoginActivity.OPTIONS_FASTBOOK_BOOL, true);
//            AccountManager.get(this).addAccount(
//                    Aptoide.getConfiguration().getAccountType(),
//                    AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,
//                    null, addAccountOptions, this,
//                    new AccountManagerCallback<Bundle>() {
//                        @Override
//                        public void run(AccountManagerFuture<Bundle> future) {
//                            String name ;
//
//                            try {
//                                name = future.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);
//
//                                if (TextUtils.isEmpty(name)) {
//                                    //finish();
//                                } else {
//                                    init();
//                                }
//                            } catch (Exception e) {
//                               // finish();
//                            }
//
//                        }
//                    },
//                    new Handler(Looper.getMainLooper())
//            );
        }

        String username = "";


        private void init() {

            adapter = new EndlessWrapperAdapter(this, getActivity(), apks);
            adapter.setRunInBackground(false);
            username = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("username", "");

            View inviteFriends = LayoutInflater.from(getActivity()).inflate(R.layout.separator_invite_friends, null);
            getListView().addHeaderView(inviteFriends);
            getListView().setItemsCanFocus(true);
            setListAdapter(adapter);
            setListShown(false);
            //force loading
            adapter.getView(0, null, null);
            if (!Preferences.getBoolean(Preferences.TIMELINE_ACEPTED_BOOL, false)) {
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {

                        try {


                            ChangeUserSettingsRequest request = new ChangeUserSettingsRequest();
                            request.addTimeLineSetting(ChangeUserSettingsRequest.TIMELINEACTIVE);
                            request.setHttpRequestFactory(AndroidHttp.newCompatibleTransport().createRequestFactory());
                            request.loadDataFromNetwork();


                            Preferences.putBooleanAndCommit(Preferences.TIMELINE_ACEPTED_BOOL, true);

                            if (!PreferenceManager.getDefaultSharedPreferences(Aptoide.getContext()).contains(Preferences.SHARE_TIMELINE_DOWNLOAD_BOOL)) {
                                Preferences.putBooleanAndCommit(Preferences.SHARE_TIMELINE_DOWNLOAD_BOOL, true);
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });
            }

        }

        @Override
        public void onResume() {
            super.onResume();
        }


        @Override
        public void onStart() {
            super.onStart();
            manager.start(getActivity());
        }

        @Override
        public void onStop() {
            super.onStop();
            manager.shouldStop();
        }

        @Override
        public void onRefresh() {
            refreshRequest();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int i = item.getItemId();
            if (i == android.R.id.home) {
//        } else if (i == R.id.menu_invite_friends) {

            } else if( i == R.id.menu_SendFeedBack){
                FeedBackActivity.screenshot(getActivity());
                startActivity(new Intent(getActivity(),FeedBackActivity.class));
            }
            return super.onOptionsItemSelected(item);
        }

    /* *************** Methods of the TimeLineManager Interface *************** */

        @Override
        public void hidePost(long id) {
            changeUserApkInstallStatusPost(id, ChangeUserApkInstallStatusRequest.STATUSHIDDEN);
        }

        @Override
        public void unHidePost(long id) {
            changeUserApkInstallStatusPost(id, ChangeUserApkInstallStatusRequest.STATUSACTIVE);
        }

        private void changeUserApkInstallStatusPost(long id, String status) {
            ChangeUserApkInstallStatusRequest request = new ChangeUserApkInstallStatusRequest();
            request.setPostStatus(status);
            request.setPostId(id);
            manager.execute(request, new TimelineRequestListener<GenericResponse>());
        }

        @Override
        public void likePost(long id) {
            likeRequestPost(id, AddUserApkInstallLikeRequest.LIKE);
        }

        @Override
        public void unlikePost(long id) {
            likeRequestPost(id, AddUserApkInstallLikeRequest.UNLIKE);
        }

        private void likeRequestPost(long id, String like) {
            AddUserApkInstallLikeRequest request = new AddUserApkInstallLikeRequest();
            request.setLike(like);
            request.setPostId(id);
            manager.execute(request, new TimelineRequestListener<GenericResponse>());
        }

        @Override
        public void commentPost(long id, String comment, int position) {
            AddUserApkInstallCommentRequest request = new AddUserApkInstallCommentRequest();
            request.setPostId(id);
            request.setComment(comment);
            manager.execute(request, new SetUserApkInstallCommentsRequestListener(id, position));
        }

        @Override
        public void getComments(long id) {
            GetUserApkInstallCommentsRequest request = new GetUserApkInstallCommentsRequest();
            request.setPostID(id);
            request.setPostLimit(COMMENTSLIMIT);
            manager.execute(request, new GetUserApkInstallCommentsRequestListener());
        }

        @Override
        public void openCommentsDialog(long id, int position){
            Bundle args = new Bundle();

            args.putString(TimeLineCommentsDialog.LIKES, String.valueOf(((TimelineListAPKsJson.UserApk)adapter.getItem(position)).getInfo().getLikes()));

            args.putLong(TimeLineCommentsDialog.POSTID, id);
            args.putInt(TimeLineCommentsDialog.POSITION, position);

            TimeLineCommentsDialog commentsDialog = new TimeLineCommentsDialog();
            commentsDialog.setCallback(this);
            commentsDialog.setArguments(args);
            commentsDialog.show(getChildFragmentManager(), COMMENTSDIALOGTAG);
        }

    /* *************** Methods of the TimeLineManager Interface *************** */



        private void startTimeLineFriendsListActivity() {
            startActivityForResult(new Intent(getActivity(), TimeLineFriendsListActivity.class), 0);
        }

        public class SetUserApkInstallCommentsRequestListener extends TimelineRequestListener<GenericResponse> {
            private final long postid;
            private final int position;

            public SetUserApkInstallCommentsRequestListener(long postid, int position) {
                this.postid = postid;
                this.position = position;
            }

            @Override
            protected void caseOK(GenericResponse response) {

                if(response.getStatus().equals("OK")){
                    refreshRequest();
                    TimeLineCommentsDialog fragmentByTag = (TimeLineCommentsDialog) getChildFragmentManager().findFragmentByTag(COMMENTSDIALOGTAG);
                    listAPKsInstallsRequest = new ListApksInstallsRequest();
                    listAPKsInstallsRequest.setPostId(postid);
                    manager.execute(listAPKsInstallsRequest, new TimelineRequestListener<TimelineListAPKsJson>(){
                        @Override
                        protected void caseOK(TimelineListAPKsJson response) {
                            TimelineListAPKsJson.UserApk apk = response.getUsersapks().get(0);
                            apks.set(position,apk);
                            adapter.onDataReady();
                        }
                    });
                    if(fragmentByTag!=null){
                        fragmentByTag.dismiss();
                    }
                }else{
                    Toast.makeText(Aptoide.getContext(), R.string.error_occured, Toast.LENGTH_LONG).show();
                }

            }
        }

        public class GetUserApkInstallCommentsRequestListener extends TimelineRequestListener<ApkInstallComments> {
            @Override
            protected void caseOK(ApkInstallComments response) {
                if ((response).getComments() != null) {
                    TimeLineCommentsDialog timeLineCommentsDialog = (TimeLineCommentsDialog) getChildFragmentManager().findFragmentByTag(COMMENTSDIALOGTAG);
                    if(timeLineCommentsDialog != null){
                        timeLineCommentsDialog.SetComments((response).getComments());
                    }
                }
            }
        }

    }



}
