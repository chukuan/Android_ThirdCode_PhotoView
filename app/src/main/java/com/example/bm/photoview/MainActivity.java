package com.example.bm.photoview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.bm.photoview.widget.PhotoImageView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    String[] s_picArray = {"http://d040779c2cd49.scdn.itc.cn/s_w_z/pic/20161213/184474627873966848.jpg",
            "http://d040779c2cd49.scdn.itc.cn/s_w_z/pic/20161213/184474627999795968.jpg",
            "http://d040779c2cd49.scdn.itc.cn/s_w_z/pic/20161213/184474628071099136.jpg"
    };
    String[] b_picArray = {"http://d040779c2cd49.scdn.itc.cn/s_big/pic/20161213/184474627873966848.jpg",
            "http://d040779c2cd49.scdn.itc.cn/s_big/pic/20161213/184474627999795968.jpg",
            "http://d040779c2cd49.scdn.itc.cn/s_big/pic/20161213/184474628071099136.jpg"
    };


    ArrayList<String> s_PhotoList = new ArrayList<String>();
    ArrayList<String> b_PhotoList = new ArrayList<String>();


    PhotoImageView mPhotoImageView01 = null;
    PhotoImageView mPhotoImageView02 = null;
    PhotoImageView mPhotoImageView03 = null;


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
        mPhotoImageView01 = (PhotoImageView) findViewById(R.id.imageView01);
        mPhotoImageView01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                startViewPagerActivity(0);
            }
        });
        //
        mPhotoImageView02 = (PhotoImageView) findViewById(R.id.imageView02);
        mPhotoImageView02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                startViewPagerActivity(1);
            }
        });
        //
        mPhotoImageView03 = (PhotoImageView) findViewById(R.id.imageView03);
        mPhotoImageView03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                startViewPagerActivity(2);
            }
        });

        // glide加载图片
        Glide.with(MainActivity.this).load(s_picArray[0]).diskCacheStrategy(DiskCacheStrategy.ALL).into(mPhotoImageView01);
        Glide.with(MainActivity.this).load(s_picArray[1]).diskCacheStrategy(DiskCacheStrategy.ALL).into(mPhotoImageView02);
        Glide.with(MainActivity.this).load(s_picArray[2]).diskCacheStrategy(DiskCacheStrategy.ALL).into(mPhotoImageView03);
    }


    /**
     * @param position
     */
    private void startViewPagerActivity(int position) {

        //------屏幕位置数据------
        PreViewPagerActivity.mImageInfo.clear();
        PreViewPagerActivity.mImageInfo.add(mPhotoImageView01.getImageInfo());
        PreViewPagerActivity.mImageInfo.add(mPhotoImageView02.getImageInfo());
        PreViewPagerActivity.mImageInfo.add(mPhotoImageView03.getImageInfo());
        //------URL数据------
        // ------初始化数据-------
        ArrayList<PhotoUrlData> mPhotoDataList = new ArrayList<PhotoUrlData>();
        for (int i = 0; i < s_PhotoList.size(); i++) {
            PhotoUrlData photoData = new PhotoUrlData();
            // ---Url数据---
            photoData.b_Url = b_picArray[i];
            //
            mPhotoDataList.add(photoData);
        }


        mPhotoDataList.get(0).s_Drawable = mPhotoImageView01.getDrawable();
        mPhotoDataList.get(1).s_Drawable = mPhotoImageView02.getDrawable();
        mPhotoDataList.get(2).s_Drawable = mPhotoImageView03.getDrawable();


        // 设置图片数据
        PreViewPagerActivity.mPhotoUrlDataList.clear();
        PreViewPagerActivity.mPhotoUrlDataList.addAll(mPhotoDataList);

        //
        Intent intent = new Intent(MainActivity.this, PreViewPagerActivity.class);
        intent.putExtra("selectPosition", position);
        MainActivity.this.startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
