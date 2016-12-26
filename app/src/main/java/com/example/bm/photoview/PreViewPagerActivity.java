package com.example.bm.photoview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.example.bm.photoview.widget.PhotoImageView;

import java.util.ArrayList;


public class PreViewPagerActivity extends Activity {


    private View mMaskView;
    // ViewPager
    private ViewPager mPager;

    PreViewPagerAdapter mPreViewPagerAdapter = null;


    /**
     * 数据(数据在退出时，清空)
     */
    // URL数据
    public static ArrayList<PhotoUrlData> mPhotoUrlDataList = new ArrayList<PhotoUrlData>();
    // 屏幕位置数据
    public static ArrayList<PhotoImageView.ImageInfo> mImageInfo = new ArrayList<PhotoImageView.ImageInfo>();


    /**
     *
     */
    private int mSelectPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_viewpager_activity);

        initData();
        initUI();
    }

    private void initData() {
        //
        Intent intent = getIntent();
        mSelectPosition = intent.getIntExtra("selectPosition", 0);
    }

    private void initUI() {


        mMaskView = findViewById(R.id.mask_View);

        // ViewPager
        mPager = (ViewPager) findViewById(R.id.pager);
        mPreViewPagerAdapter = new PreViewPagerAdapter(this, mPhotoUrlDataList, mImageInfo, mSelectPosition);
        mPager.setAdapter(mPreViewPagerAdapter);
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mSelectPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //
        mPager.setCurrentItem(mSelectPosition);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPhotoUrlDataList.clear();
        mImageInfo.clear();
    }

    @Override
    public void finish() {
        super.finish();
        //
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startZoomOutAnim();
        }
        return true;
    }

    boolean flag = false;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (flag == false) {
            startZoomInAnim();
            flag = true;
        }
    }

    public void startZoomInAnim() {

        //-------Alpaha--------
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(mMaskView, "alpha", 0, 1));
        set.setDuration(300);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
    }

    public void startZoomOutAnim() {

        //-------Alpaha--------
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(mMaskView, "alpha", 1, 0));
        set.setDuration(300);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
        //-------Scale--------
        for (int i = 0; i < mPager.getChildCount(); i++) {
            View view = mPager.getChildAt(i);
            int position = (int) view.getTag();
            if (position == mSelectPosition) {
                PhotoImageView phtoImage = (PhotoImageView) view.findViewById(R.id.b_scale_photoImageView);
                //
                zoomOutImage(position, phtoImage, new PhotoImageView.OnPhotoAnimaListener() {
                    @Override
                    public void onAnimaFinish() {
                        PreViewPagerActivity.this.finish();
                    }
                });
            }
        }
    }


    /**
     * @param position
     */
    public void zoomOutImage(final int position, final PhotoImageView mBigImageView, final PhotoImageView.OnPhotoAnimaListener listener) {

        /**
         * 1、数据
         */
        final PhotoUrlData photoData = mPhotoUrlDataList.get(position);
        final PhotoImageView.ImageInfo imageInfo = mImageInfo.get(position);

        mBigImageView.startAnimaTo(imageInfo, new PhotoImageView.OnPhotoAnimaListener() {
            @Override
            public void onAnimaFinish() {
                if (listener != null) {
                    listener.onAnimaFinish();
                }
            }
        });
    }

}
