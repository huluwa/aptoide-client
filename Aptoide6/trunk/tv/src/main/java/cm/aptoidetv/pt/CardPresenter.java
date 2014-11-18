/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package cm.aptoidetv.pt;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import cm.aptoidetv.pt.Model.BindInterface;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.net.URI;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand. 
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";

    private static Context mContext;
    private static int CARD_WIDTH = 313;
    private static int CARD_HEIGHT = 176;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");
        mContext = parent.getContext();

        ImageCardView cardView = new ImageCardView(mContext);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        TypedArray typedArray = mContext.getTheme().obtainStyledAttributes(ThemePicker.getThemePicker(), new int[]{R.attr.brandColor});
        int brandColorResourceId = typedArray.getResourceId(0, 0);
        typedArray.recycle();

        cardView.setBackgroundColor(mContext.getResources().getColor(brandColorResourceId));
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        BindInterface application = (BindInterface) item;

        if(application.getCategory().equals("Editors' Choice")) {

            if (!TextUtils.isEmpty(application.getImage())) {
                ((ViewHolder) viewHolder).mCardView.setTitleText(Html.fromHtml(application.getName()));
                ((ViewHolder) viewHolder).mCardView.setContentText("Version: "+application.getVersion());
                ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(((ViewHolder) viewHolder).mCardView.getContext().getResources().getInteger(R.integer.card_presenter_width),((ViewHolder) viewHolder).mCardView.getContext().getResources().getInteger(R.integer.card_presenter_height));
                ((ViewHolder) viewHolder).updateCardViewImage(application.getImage());
            }

        } else {
            if (!TextUtils.isEmpty(application.getImage())) {
                ((ViewHolder) viewHolder).mCardView.setTitleText(Html.fromHtml(application.getName()));
                ((ViewHolder) viewHolder).mCardView.setContentText("Version: " + application.getVersion());
                ((ViewHolder) viewHolder).mCardView.setMainImageDimensions(((ViewHolder) viewHolder).mCardView.getContext().getResources().getInteger(R.integer.icon_width), ((ViewHolder) viewHolder).mCardView.getContext().getResources().getInteger(R.integer.icon_height));
                ((ViewHolder) viewHolder).updateCardViewImage(application.getImage());
            }
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
        ViewHolder vh = (ViewHolder) viewHolder;
        // Remove references to images so that the garbage collector can free up memory
        vh.mCardView.setBadgeImage(null);
        vh.mCardView.setMainImage(null);
    }

    @Override
    public void onViewAttachedToWindow(Presenter.ViewHolder viewHolder) {
        // TO DO
    }

    static class ViewHolder extends Presenter.ViewHolder {
        private Movie mMovie;
        private ImageCardView mCardView;
        private Drawable mDefaultCardImage;
        private PicassoImageCardViewTarget mImageCardViewTarget;

        public ViewHolder(View view) {
            super(view);
            mCardView = (ImageCardView) view;
            mImageCardViewTarget = new PicassoImageCardViewTarget(mCardView);
            mDefaultCardImage = mContext.getResources().getDrawable(R.drawable.movie);
        }

        public Movie getMovie() {
            return mMovie;
        }

        public void setMovie(Movie m) {
            mMovie = m;
        }

        public ImageCardView getCardView() {
            return mCardView;
        }

        protected void updateCardViewImage(URI uri) {
            Picasso.with(mContext)
                    .load(uri.toString())
                    .resize(Utils.convertDpToPixel(mContext, CARD_WIDTH),
                            Utils.convertDpToPixel(mContext, CARD_HEIGHT))
                    .error(mDefaultCardImage)
                    .into(mImageCardViewTarget);
        }
        protected void updateCardViewImage(String uri) {
            Picasso.with(mContext)
                    .load(uri)
                    .placeholder(android.R.drawable.sym_def_app_icon)
                    .centerInside()
                    .resize(Utils.convertDpToPixel(mContext, CARD_WIDTH),
                            Utils.convertDpToPixel(mContext, CARD_HEIGHT))
                    .error(mDefaultCardImage)
                    .into(mImageCardViewTarget);
        }
    }

    public static class PicassoImageCardViewTarget implements Target {
        private ImageCardView mImageCardView;

        public PicassoImageCardViewTarget(ImageCardView imageCardView) {
            mImageCardView = imageCardView;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            Drawable bitmapDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
            mImageCardView.setMainImage(bitmapDrawable);
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            mImageCardView.setMainImage(drawable);
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {
            // Do nothing, default_background manager has its own transitions
        }
    }
}