package cm.aptoide.ptdev.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import cm.aptoide.ptdev.Aptoide;
import cm.aptoide.ptdev.MoreTopAppsActivity;
import cm.aptoide.ptdev.R;
import cm.aptoide.ptdev.fragments.HomeItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

/**
 * Created by rmateus on 27-06-2014.
 */
public  class HomeTopAdapter extends BaseAdapter {


    private final int bucketSize;
    private final LayoutInflater inflater;

    public HomeTopAdapter(Context context, final ArrayList<HomeItem> items, int bucketSize) {
        this.context = context;
        this.items = items;
        this.bucketSize = bucketSize;
        inflater = LayoutInflater.from(context);
    }



    private Context context;

    ArrayList<HomeItem> items = new ArrayList<HomeItem>();


    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {

        final int count = getCount();
        if (count > 1) {
            if (position < count - 2) {
                return 2;
            } else if (position == count - 2) {
                return 1;
            } else if (position == count - 1) {
                return 0;
            }
        } else {
            return 2;
        }

        return 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        int count = ((items.size() + bucketSize - 1 ) / bucketSize);
        return  count == 0 ? count : count+ 1 ;
    }

    @Override
    public HomeItem getItem(int position) {

        return items.get(position);

    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View item = null;

        switch (getItemViewType(position)){

            case 1:
            case 2:
                item = createRow(position, convertView);
                break;
            case 0:
                //Toast.makeText(context, "Inflating Footer", Toast.LENGTH_LONG).show();
                if(convertView==null){
                    item = View.inflate(context, R.layout.separator_home_footer, null);
                }else{
                    item = convertView;
                }

                TextView moreTopTv = (TextView) item.findViewById(R.id.more);
                moreTopTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(context, MoreTopAppsActivity.class);
                        context.startActivity(i);
                    }
                });
                break;
        }

        return item;
    }

    private View createRow(int position, View convertView){

        final int initialSize = position * bucketSize;
        final int maxSize = (position * bucketSize) + bucketSize;
        View item;
        if(convertView==null){
            Log.d("Sup", "Init view ");
            item = new LinearLayout(context);
            LinearLayout layout = (LinearLayout) item;
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.MATCH_PARENT));

            for(int i = initialSize ;  i < maxSize && i < items.size() ; i++ ){
                layout.addView(inflater.inflate(R.layout.row_app_home, layout, false));
            }

        }else{
            Log.d("Sup", "Reusing view ");
            item = convertView;
        }


        for(int i = initialSize, j=0;  i < items.size() && i < maxSize; i++, j++ ){
            View viewById = ((LinearLayout) item).getChildAt(j);

            TextView tv = (TextView) viewById.findViewById(R.id.app_name);
            tv.setText(getItem(i).getName());
            Toast.makeText(Aptoide.getContext(), getItem(i).getIcon(), Toast.LENGTH_LONG).show();
            ImageView iv = (ImageView) viewById.findViewById(R.id.app_icon);
            ImageLoader.getInstance().displayImage(getItem(i).getIcon(), iv);

        }

        return item;
    }

    public int getRealCount() {
        return items.size();
    }
}