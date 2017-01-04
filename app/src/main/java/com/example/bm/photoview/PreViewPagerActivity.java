package com.example.bm.photoview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;

import com.example.bm.photoview.widget.PhotoImageInfoUtil;
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
    public static ArrayList<ImageUrlData> mImageUrlList = new ArrayList<ImageUrlData>();
    // 屏幕位置数据
    public static ArrayList<PhotoImageView.ImageInfo> mImageLocList = new ArrayList<PhotoImageView.ImageInfo>();


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


        setImmersiveStatusBar();

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
        mPreViewPagerAdapter = new PreViewPagerAdapter(this, mImageUrlList, mImageLocList, mSelectPosition);
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
        mImageUrlList.clear();
        mImageLocList.clear();
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
            // 开启放大动画
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
        //-------Scale动画在ViewPager的Adapter中--------
    }

    public void startZoomOutAnim() {

        showSystemUI();

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
        final ImageUrlData photoData = mImageUrlList.get(position);
        final PhotoImageView.ImageInfo imageInfo = mImageLocList.get(position);

        mBigImageView.startAnimaTo(imageInfo, new PhotoImageView.OnPhotoAnimaListener() {
            @Override
            public void onAnimaFinish() {
                if (listener != null) {
                    listener.onAnimaFinish();
                }
            }
        });
    }


    //------------------5.0以上更改状态栏-------------------
    protected void setImmersiveStatusBar() {
        // http://www.jianshu.com/p/11a2b780fd9b
        if (Build.VERSION.SDK_INT >= PhotoImageInfoUtil.HIDE_STATE_BAR) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    // 保持整个View稳定, 使View不会因为System UI的变化而重新layout API 16
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            // 视图延伸至状态栏区域，状态栏上浮于视图之上 API 16
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            // 状态栏隐藏，效果同设置WindowManager.LayoutParams.FLAG_FULLSCREEN API 16
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            // API 19
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }


    private void showSystemUI() {
        if (Build.VERSION.SDK_INT >= PhotoImageInfoUtil.HIDE_STATE_BAR) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    // 保持整个View稳定, 使View不会因为System UI的变化而重新layout API 16
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            // 视图延伸至状态栏区域，状态栏上浮于视图之上 API 16
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }


}
