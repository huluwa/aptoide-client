package cm.aptoide.ptdev.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import cm.aptoide.ptdev.AllCommentsActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.model.Comment;
import cm.aptoide.ptdev.webservices.AllCommentsRequest;
import cm.aptoide.ptdev.webservices.json.AllCommentsJson;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.List;

/**
 * Created by rmateus on 26-12-2013.
 */
public class FragmentComments extends ListFragment {


    private RequestListener<AllCommentsJson> requestListener = new RequestListener<AllCommentsJson>() {
        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(getActivity(), "Error request", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestSuccess(AllCommentsJson allCommentsJson) {
            setListAdapter(new AllCommentsAdapter(getActivity(), allCommentsJson.getListing()));
        }
    };
    private SpiceManager spiceManager;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        spiceManager = ((AllCommentsActivity)getActivity()).getSpice();

        AllCommentsRequest request = new AllCommentsRequest();

        request.setRepoName(getActivity().getIntent().getStringExtra("repoName"));
        request.setVersionName(getActivity().getIntent().getStringExtra("versionName"));
        request.setPackageName(getActivity().getIntent().getStringExtra("packageName"));

        spiceManager.execute(request, requestListener);
    }

    public class AllCommentsAdapter extends ArrayAdapter<Comment>{

        public AllCommentsAdapter(Context context,  List<Comment> objects) {
            super(context, 0, objects);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v;

            if(convertView == null){
                v = LayoutInflater.from(getContext()).inflate(R.layout.row_comment, parent, false);
            }else{
                v= convertView;
            }
            Comment comment = getItem(position);

            TextView content = (TextView) v.findViewById(R.id.content);
            TextView date = (TextView) v.findViewById(R.id.date);
            TextView author = (TextView) v.findViewById(R.id.author);

            content.setText(comment.getText());
            date.setText(comment.getTimestamp());
            author.setText(comment.getUsername());

            return v;
        }
    }
}
