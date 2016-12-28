package com.example.bm.photoview.widget;

import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;

/**
 * 普通ImageView获取ImageInfo的支持
 */
public class PhotoImageInfoUtil {


    /**
     * 获取当前ImageView的位置信息
     *
     * @return
     */
    public static PhotoImageView.ImageInfo getImageInfo(ImageView imageView) {
        if (imageView == null) {
            return null;
        }

        // -------ImageView的宽高(0 0 width height)-----
        RectF iViewRect = new RectF(0, 0, imageView.getWidth(), imageView.getHeight());
        // ------Drawable的相对于ImageView上的位置------
        Drawable drawable = imageView.getDrawable();
        int drawableW = drawable.getIntrinsicWidth();
        int drawableH = drawable.getIntrinsicHeight();
        float drawableLeft = (iViewRect.width() - drawableW) / 2f;
        float drawableRight = (iViewRect.height() - drawableH) / 2f;
        RectF iDrawableRect = new RectF(drawableLeft, drawableRight, drawableLeft + drawableW, drawableRight + drawableH);
        // -------Drawable在整个窗口的位置(left top right bottom)--------
        RectF iDrawableLocalOnScreenR = new RectF();
        // 获取在屏幕上的位置
        int[] location = new int[2];
        getLocation(imageView, location);
        iDrawableLocalOnScreenR.set(location[0] + iDrawableRect.left, location[1] + iDrawableRect.top,
                location[0] + iDrawableRect.right, location[1] + iDrawableRect.bottom);
        // ImageView的scaleType
        ImageView.ScaleType iScaleType = imageView.getScaleType();
        return new PhotoImageView.ImageInfo(iDrawableLocalOnScreenR, iDrawableRect, iViewRect, iScaleType);
    }

    /**
     * 获取屏幕上的位置，标题栏不算在location中
     *
     * @param position
     */
    private static void getLocation(ImageView imageView, int[] position) {

        position[0] += imageView.getLeft();
        position[1] += imageView.getTop();

        ViewParent viewParent = imageView.getParent();
        while (viewParent instanceof View) {
            final View view = (View) viewParent;

            if (view.getId() == android.R.id.content) return;

            position[0] -= view.getScrollX();
            position[1] -= view.getScrollY();

            position[0] += view.getLeft();
            position[1] += view.getTop();

            viewParent = view.getParent();
        }

        position[0] = (int) (position[0] + 0.5f);
        position[1] = (int) (position[1] + 0.5f);
    }

}