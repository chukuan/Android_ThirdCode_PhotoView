package com.example.bm.photoview;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.bm.photoview.widget.PhotoImageView;

import java.util.ArrayList;


public class PreViewPagerAdapter extends PagerAdapter {


    Context mContext = null;
    LayoutInflater mInflater = null;


    /**
     * 数据(数据在退出时，清空)
     */
    // URL数据
    public ArrayList<ImageUrlData> mPhotoUrlDataList = new ArrayList<ImageUrlData>();
    // 屏幕位置数据
    public ArrayList<PhotoImageView.ImageInfo> mImageInfo = new ArrayList<PhotoImageView.ImageInfo>();

    private int mInitPosition = 0;

    public PreViewPagerAdapter(Context context, ArrayList<ImageUrlData> mPhotoUrlDataList, ArrayList<PhotoImageView.ImageInfo> mImageInfo, int position) {
        this.mContext = context;
        this.mInitPosition = position;

        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //
        this.mPhotoUrlDataList.clear();
        this.mPhotoUrlDataList.addAll(mPhotoUrlDataList);
        //
        this.mImageInfo.clear();
        this.mImageInfo.addAll(mImageInfo);
    }


    @Override
    public int getCount() {
        return mPhotoUrlDataList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        /**
         * UI
         */
        View view = mInflater.inflate(
                R.layout.pre_viewpager_item, null);
        PhotoImageView mBigImageView = (PhotoImageView) view.findViewById(R.id.b_scale_photoImageView);
        mBigImageView.enableScale();
        PhotoImageView mSmallImageView = (PhotoImageView) view.findViewById(R.id.s_scale_photoImageView);
        //
        if (position == mInitPosition) {
            zoomInImage(position, mSmallImageView, mBigImageView, true);
        } else {
            zoomInImage(position, mSmallImageView, mBigImageView, false);
        }


        //------
        view.setTag(position);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }


    private void zoomInImage(final int position, final PhotoImageView mSmallImageView, final PhotoImageView mBigImageView, final boolean needAnima) {

        /**
         * 1、数据
         */
        final ImageUrlData photoData = mPhotoUrlDataList.get(position);
        final PhotoImageView.ImageInfo imageInfo = mImageInfo.get(position);

        /**
         * 2、加载mScaleImageView的小图
         */
        // 小图
        if (TextUtils.isEmpty(photoData.s_Url) == false) {
            Glide.with(mContext).load(photoData.s_Url).diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate().into(mSmallImageView);
        } else if (photoData.s_Drawable != null) {
            mSmallImageView.setImageDrawable(photoData.s_Drawable.getConstantState().newDrawable());
        }
        if (needAnima) {
            mBigImageView.setVisibility(View.GONE);
            mSmallImageView.startAnimaFrom(imageInfo, new PhotoImageView.OnPhotoAnimaListener() {
                @Override
                public void onAnimaFinish() {
                    mBigImageView.setVisibility(View.VISIBLE);
                }
            });
        }
        /**
         * 3、加载mScaleImageView的大图
         */
        // 大图
        Glide.with(mContext).load(photoData.b_Url).diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate().listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                // 小图隐藏
                mSmallImageView.setVisibility(View.GONE);
                return false;
            }
        }).dontTransform().into(mBigImageView);
    }


}
