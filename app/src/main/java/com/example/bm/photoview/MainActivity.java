package com.example.bm.photoview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.bm.photoview.widget.PhotoImageInfoUtil;
import com.example.bm.photoview.widget.PhotoImageView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    String[] s_picArray = {"https://c1c2133e2cc13.cdn.sohucs.com/s_h_z/pic/20161227/189409970494645120",
            "https://c1c2133e2cc13.cdn.sohucs.com/s_h_z/pic/20161227/189409971035710336",
            "http://d040779c2cd49.scdn.itc.cn/s_w_z/pic/20161213/184474628071099136.jpg"
    };
    String[] b_picArray = {"https://c1c2133e2cc13.cdn.sohucs.com/s_big/pic/20161227/189409970494645120",
            "https://c1c2133e2cc13.cdn.sohucs.com/s_big/pic/20161227/189409971035710336",
            "http://d040779c2cd49.scdn.itc.cn/s_big/pic/20161213/184474628071099136.jpg"
    };


    ArrayList<String> s_PhotoList = new ArrayList<String>();
    ArrayList<String> b_PhotoList = new ArrayList<String>();


    ImageView mPhotoImageView01 = null;
    ImageView mPhotoImageView02 = null;
    ImageView mPhotoImageView03 = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //
        initData();
        //
        initUI();
    }


    /**
     * 初始化数据
     */
    private void initData() {
        // 初始化小图数据
        for (int i = 0; i < s_picArray.length; i++) {
            s_PhotoList.add(s_picArray[i]);
        }
        // 初始化大图
        for (int i = 0; i < b_picArray.length; i++) {
            b_PhotoList.add(b_picArray[i]);
        }
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        //
        mPhotoImageView01 = (ImageView) findViewById(R.id.imageView01);
        mPhotoImageView01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                onImageClick(0);
            }
        });
        //
        mPhotoImageView02 = (ImageView) findViewById(R.id.imageView02);
        mPhotoImageView02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                onImageClick(1);
            }
        });
        //
        mPhotoImageView03 = (ImageView) findViewById(R.id.imageView03);
        mPhotoImageView03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                onImageClick(2);
            }
        });

        // glide加载图片
        Glide.with(MainActivity.this).load(s_picArray[0]).diskCacheStrategy(DiskCacheStrategy.ALL).into(mPhotoImageView01);
        Glide.with(MainActivity.this).load(s_picArray[1]).diskCacheStrategy(DiskCacheStrategy.ALL).into(mPhotoImageView02);
        Glide.with(MainActivity.this).load(s_picArray[2]).diskCacheStrategy(DiskCacheStrategy.ALL).into(mPhotoImageView03);
    }


    private void onImageClick(int position) {

        //------屏幕位置数据------
        ArrayList<PhotoImageView.ImageInfo> mImageLocList = new ArrayList<PhotoImageView.ImageInfo>();
        mImageLocList.add(PhotoImageInfoUtil.getImageInfo(mPhotoImageView01));
        mImageLocList.add(PhotoImageInfoUtil.getImageInfo(mPhotoImageView02));
        mImageLocList.add(PhotoImageInfoUtil.getImageInfo(mPhotoImageView03));
        //------URL数据------
        ArrayList<ImageUrlData> mPhotoDataList = new ArrayList<ImageUrlData>();
        // url
        for (int i = 0; i < mImageLocList.size(); i++) {
            ImageUrlData photoData = new ImageUrlData();
            photoData.b_Url = b_picArray[i];
            mPhotoDataList.add(photoData);
        }
        // drawable
        mPhotoDataList.get(0).s_Drawable = mPhotoImageView01.getDrawable();
        mPhotoDataList.get(1).s_Drawable = mPhotoImageView02.getDrawable();
        mPhotoDataList.get(2).s_Drawable = mPhotoImageView03.getDrawable();

        //
        startViewPagerActivity(position, mPhotoDataList, mImageLocList);
    }


    /**
     * @param position
     * @param imageUrlList url
     * @param imageLocList location
     */
    private void startViewPagerActivity(int position, ArrayList<ImageUrlData> imageUrlList, ArrayList<PhotoImageView.ImageInfo> imageLocList) {
        // 判断
        if (imageUrlList == null || imageUrlList.size() == 0) {
            return;
        }
        if (imageLocList == null || imageLocList.size() == 0) {
            return;
        }
        if (imageUrlList.size() != imageLocList.size()) {
            return;
        }
        //------图片Url数据------
        PreViewPagerActivity.mImageUrlList.clear();
        PreViewPagerActivity.mImageUrlList.addAll(imageUrlList);
        //------图片loc数据------
        PreViewPagerActivity.mImageLocList.clear();
        PreViewPagerActivity.mImageLocList.addAll(imageLocList);
        // --------position-------
        Intent intent = new Intent(MainActivity.this, PreViewPagerActivity.class);
        intent.putExtra("selectPosition", position);
        MainActivity.this.startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
