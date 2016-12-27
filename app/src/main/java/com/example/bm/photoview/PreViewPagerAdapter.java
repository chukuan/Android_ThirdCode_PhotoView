package com.example.bm.photoview;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
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
        mSmallImageView.setImageDrawable(photoData.s_Drawable.getConstantState().newDrawable());

        // 大图
        final long loadStartTime = System.currentTimeMillis();
        Glide.with(mContext).load(photoData.b_Url).diskCacheStrategy(DiskCacheStrategy.ALL).dontAnimate().listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                mSmallImageView.setVisibility(View.GONE);
                // 没有快速加载出来，算是网络下载图片
                if (needAnima) {
                    if ((System.currentTimeMillis() - loadStartTime) >= 120) {
                        mBigImageView.startAnimaFrom(mSmallImageView.getImageInfo());
                    } else {
                        mBigImageView.startAnimaFrom(imageInfo);
                    }
                }
                return false;
            }
        }).dontTransform().into(mBigImageView);
    }


}
