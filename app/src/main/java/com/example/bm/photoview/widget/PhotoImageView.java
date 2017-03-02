package com.example.bm.photoview.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.OverScroller;
import android.widget.Scroller;

/**
 *
 */
public class PhotoImageView extends ImageView {

    // 动画时间
    private final static int ANIMA_DURING = 300;
    // 放大的阈值
    private final static float MAX_SCALE = 2.5f;

    private int MAX_FLING_OVER_SCROLL = 0;
    private int MAX_OVER_RESISTANCE = 0;
    // 开启动画,最大的等待时间
    private int MAX_ANIM_DELAY = 500;

    private Matrix mTmpMatrix = new Matrix();


    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private OnClickListener mClickListener;


    private boolean hasMultiTouch;


    private boolean hasOverTranslate;
    // 是否可以点击放大等操作
    private boolean isScaleEnable = false;


    private float mScale = 1.0f;
    private int mTranslateX;
    private int mTranslateY;


    private RectF mDrawableRect = new RectF();
    private RectF mTmpRect = new RectF();
    private RectF mCommonRect = new RectF();

    // 缩放的中心
    private PointF mScaleCenter = new PointF();
    private PointF mRotateCenter = new PointF();

    private RectF mClip;
    private ImageInfo mInfo;
    private long mInfoTime;

    //-------Rect------
    // View的Rect(0, 0, w, h)
    private RectF mViewRect = new RectF();
    // 初始状态(0, 0, drawableWidth, drawableHeight) 应用于一个mInitDrawableMatrix矩阵
    private RectF mInitDrawableRect = new RectF();

    // 初始Drawable的一半大小
    private float mHalfInitDrawableWidth;
    private float mHalfInitDrawableHeight;

    //-------Point--------
    //View的中心(w/2, h/2)
    private PointF mViewCenter = new PointF();

    //-------Matrix------
    // Drawable对齐View中心点的矩阵
    private Matrix mInitDrawableMatrix = new Matrix();
    // 动画矩阵
    private Matrix mAnimaMatrix = new Matrix();
    // 最终应用与Drawable的变换矩阵
    private Matrix mFinalMatrix = new Matrix();


    /**
     * 数据
     */
    private ScaleType mScaleType;
    //---------------
    // drawable是否已经设置的标识
    private boolean isDrawableSetted;
    // 宽高是否知道
    private boolean isDrawableSizeGot;
    // 是否保持宽高比
    private boolean isAdjustViewBounds;
    // 当前是否处于放大状态
    private boolean isZoonUp;
    // Drawable大于View的宽度
    private boolean drawableLargerViewW;
    private boolean drawableLargerViewH;
    // 是否初始化的标识
    private boolean isInit;


    // 放大的Runable
    private TransformRunable mTransformRunable = new TransformRunable();

    /**
     * 放大和缩小动画的回调
     */
    // 放大动画回调
    private OnPhotoAnimaListener mZoomInListener;
    // 缩小动画回调
    private OnPhotoAnimaListener mZoomOutListener;

    public interface OnPhotoAnimaListener {
        void onAnimaFinish();
    }


    /**
     * 构造方法
     *
     * @param context
     */
    public PhotoImageView(Context context) {
        super(context);
        initData();
    }

    /**
     * 构造方法
     *
     * @param context
     * @param attrs
     */
    public PhotoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    /**
     * 构造方法
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public PhotoImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 没有图片,走这里
        if (isDrawableSetted == false) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        //---------------------
        // 获取drawable
        Drawable drawable = getDrawable();
        // Drawable的宽高
        int drawableW = getDrawableWidth(drawable);
        int drawableH = getDrawableHeight(drawable);

        // View的宽高
        int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        // mode
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //---------------------
        int finalWidth = 0;
        int finalHeight = 0;

        // 获取layoutParams
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // -----宽------
        if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
            // 宽可以任意大小
            if (widthMode == MeasureSpec.UNSPECIFIED) {
                // 以drawable的宽作为宽
                finalWidth = drawableW;
            } else {
                // 以View的宽作为宽
                finalWidth = viewWidth;
            }
        } else {
            // 已存在确定大小
            if (widthMode == MeasureSpec.EXACTLY) {
                // 以View的大小作为确定大小
                finalWidth = viewWidth;
            } else if (widthMode == MeasureSpec.AT_MOST) {
                // 取小
                finalWidth = Math.min(drawableW, viewWidth);
            } else {
                finalWidth = drawableW;
            }
        }
        // -----高------
        if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
            if (heightMode == MeasureSpec.UNSPECIFIED) {
                finalHeight = drawableH;
            } else {
                finalHeight = viewHeight;
            }
        } else {
            if (heightMode == MeasureSpec.EXACTLY) {
                finalHeight = viewHeight;
            } else if (heightMode == MeasureSpec.AT_MOST) {
                finalHeight = Math.min(drawableH, viewHeight);
            } else {
                finalHeight = drawableH;
            }
        }
        // 保持宽高比
        if (isAdjustViewBounds && (float) drawableW / drawableH != (float) finalWidth / finalHeight) {
            float hScale = (float) finalHeight / drawableH;
            float wScale = (float) finalWidth / drawableW;
            //
            float scale = Math.min(wScale, hScale);
            finalWidth = lp.width == ViewGroup.LayoutParams.MATCH_PARENT ? finalWidth : (int) (drawableW * scale);
            finalHeight = lp.height == ViewGroup.LayoutParams.MATCH_PARENT ? finalHeight : (int) (drawableH * scale);
        }
        setMeasuredDimension(finalWidth, finalHeight);
    }

    @Override
    public void setAdjustViewBounds(boolean adjustViewBounds) {
        super.setAdjustViewBounds(adjustViewBounds);
        // 是否保持宽高比
        this.isAdjustViewBounds = adjustViewBounds;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // View的Rect
        mViewRect.set(0, 0, w, h);
        //View的中心点
        mViewCenter.set(w / 2, h / 2);

        if (!isDrawableSizeGot) {
            isDrawableSizeGot = true;
            initDataByDrawable();
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        mClickListener = l;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        ScaleType old = mScaleType;
        mScaleType = scaleType;
        //
        if (old != scaleType) {
            initDataByDrawable();
        }
    }

    @Override
    public void setImageResource(int resId) {
        try {
            setImageDrawable(getResources().getDrawable(resId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        //
        if (drawable == null) {
            this.isDrawableSetted = false;
            return;
        }
        //
        if (!hasSize(drawable)) {
            return;
        }
        //
        this.isDrawableSetted = true;
        //
        initDataByDrawable();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mClip != null) {
            canvas.clipRect(mClip);
            mClip = null;
        }
        super.draw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isScaleEnable) {
            final int Action = event.getActionMasked();
            if (event.getPointerCount() >= 2) hasMultiTouch = true;

            mGestureDetector.onTouchEvent(event);
            mScaleDetector.onTouchEvent(event);
            if (Action == MotionEvent.ACTION_UP || Action == MotionEvent.ACTION_CANCEL) onUp(event);

            return true;
        } else {
            return super.dispatchTouchEvent(event);
        }
    }


    /**
     * 初始化
     */
    private void initData() {
        // 设置scaleType为matrix(设置当前的变换为矩阵变换)
        super.setScaleType(ScaleType.MATRIX);
        // mScaleType CENTER_INSIDE
        if (mScaleType == null) {
            mScaleType = ScaleType.CENTER_INSIDE;
        }
        //--------
        //
        float density = getResources().getDisplayMetrics().density;
        MAX_FLING_OVER_SCROLL = (int) (density * 30);
        MAX_OVER_RESISTANCE = (int) (density * 140);
        //--------
        //
        mGestureDetector = new GestureDetector(getContext(), mGestureListener);
        // scale gesture
        mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
    }

    /**
     * 初始化数据by Drawable
     */
    private void initDataByDrawable() {
        // drawable
        if (!isDrawableSetted) {
            return;
        }
        // isKnowSize
        if (!isDrawableSizeGot) {
            return;
        }
        // 重置矩阵
        mInitDrawableMatrix.reset();
        mAnimaMatrix.reset();
        // 当前是否处于放大状态?????
        isZoonUp = false;

        Drawable drawable = getDrawable();
        // View的宽高
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        // imageView的宽高
        int drawableWidth = getDrawableWidth(drawable);
        int drawableHeight = getDrawableHeight(drawable);
        // baseRect是Drawable的宽高
        mInitDrawableRect.set(0, 0, drawableWidth, drawableHeight);

        // 以图片中心点居中位移
        int dx = (viewWidth - drawableWidth) / 2;
        int dy = (viewHeight - drawableHeight) / 2;

        /**
         * 如果图片的大小超过View的大小,缩小图片
         */
        float sx = 1;
        float sy = 1;
        // 缩放，默认不超过屏幕大小
        if (drawableWidth > viewWidth) {
            sx = (float) viewWidth / drawableWidth;
        }
        if (drawableHeight > viewHeight) {
            sy = (float) viewHeight / drawableHeight;
        }
        float scale = Math.min(sx, sy);

        /**
         * 将Drawable移动的View的中心，并缩小
         */
        mInitDrawableMatrix.reset();
        mInitDrawableMatrix.postTranslate(dx, dy);
        mInitDrawableMatrix.postScale(scale, scale, mViewCenter.x, mViewCenter.y);
        // 对mBaseDrawableRect进行坐标变换
        mInitDrawableMatrix.mapRect(mInitDrawableRect);

        // baseRect一半的宽高
        mHalfInitDrawableWidth = mInitDrawableRect.width() / 2;
        mHalfInitDrawableHeight = mInitDrawableRect.height() / 2;

        // 缩放的中心
        mScaleCenter.set(mViewCenter);
        mRotateCenter.set(mScaleCenter);

        //------应用矩阵------
        setFinalMztrix2ImageView();
        //
        switch (mScaleType) {
            // 居中显示,不缩放,不裁减
            case CENTER:
                initCenter();
                break;
            case CENTER_CROP:
                initCenterCrop();
                break;
            case CENTER_INSIDE:
                initCenterInside();
                break;
            case FIT_CENTER:
                initFitCenter();
                break;
            case FIT_START:
                initFitStart();
                break;
            case FIT_END:
                initFitEnd();
                break;
            case FIT_XY:
                initFitXY();
                break;
        }
        //
        isInit = true;

        //----------判断动画是否开启-----------
        if (mInfo != null && System.currentTimeMillis() - mInfoTime < MAX_ANIM_DELAY) {
            startAnimaFrom(mInfo, mZoomInListener);
        }
        mInfo = null;
    }


    public static int getDefaultAnimaDuring() {
        return ANIMA_DURING;
    }


    /**
     * 启用scale
     */
    public void enableScale() {
        isScaleEnable = true;
    }

    /**
     * 禁用scale
     */
    public void disableScale() {
        isScaleEnable = false;
    }


    /**
     * 开启动画的最大等待时间
     *
     * @param wait
     */
    public void setMaxAnimDelayTime(int wait) {
        MAX_ANIM_DELAY = wait;
    }


    /**
     * 是否可以获取到Drawable的宽度
     *
     * @param d
     * @return
     */
    private boolean hasSize(Drawable d) {
        if ((d.getIntrinsicHeight() <= 0 || d.getIntrinsicWidth() <= 0)
                && (d.getMinimumWidth() <= 0 || d.getMinimumHeight() <= 0)
                && (d.getBounds().width() <= 0 || d.getBounds().height() <= 0)) {
            return false;
        }
        return true;
    }

    /**
     * 获取drawable的宽
     *
     * @param d
     * @return
     */
    private int getDrawableWidth(Drawable d) {
        int width = d.getIntrinsicWidth();
        if (width <= 0) {
            width = d.getMinimumWidth();
        }
        if (width <= 0) {
            width = d.getBounds().width();
        }
        return width;
    }

    /**
     * 获取drawable的高
     *
     * @param d
     * @return
     */
    private int getDrawableHeight(Drawable d) {
        int height = d.getIntrinsicHeight();
        if (height <= 0) {
            height = d.getMinimumHeight();
        }
        if (height <= 0) {
            height = d.getBounds().height();
        }
        return height;
    }

    /**
     * 居中显示,不缩放,不裁减
     */
    private void initCenter() {
        if (!isDrawableSetted) {
            return;
        }
        if (!isDrawableSizeGot) {
            return;
        }

        Drawable img = getDrawable();

        int drawableWidth = getDrawableWidth(img);
        int drawableHeight = getDrawableHeight(img);
        //
        if (drawableWidth > mViewRect.width() || drawableHeight > mViewRect.height()) {
            float scaleX = drawableWidth / mDrawableRect.width();
            float scaleY = drawableHeight / mDrawableRect.height();
            mScale = Math.max(scaleX, scaleY);
            // 居中显示,不缩放,不裁减
            mAnimaMatrix.postScale(mScale, mScale, mViewCenter.x, mViewCenter.y);
            // 应用矩阵
            setFinalMztrix2ImageView();
            // 恢复数据
            resetBase();
        }
    }

    private void initCenterCrop() {
        if (mDrawableRect.width() < mViewRect.width() || mDrawableRect.height() < mViewRect.height()) {
            float scaleX = mViewRect.width() / mDrawableRect.width();
            float scaleY = mViewRect.height() / mDrawableRect.height();

            mScale = scaleX > scaleY ? scaleX : scaleY;

            mAnimaMatrix.postScale(mScale, mScale, mViewCenter.x, mViewCenter.y);

            setFinalMztrix2ImageView();
            resetBase();
        }
    }

    private void initCenterInside() {
        if (mDrawableRect.width() > mViewRect.width() || mDrawableRect.height() > mViewRect.height()) {
            float scaleX = mViewRect.width() / mDrawableRect.width();
            float scaleY = mViewRect.height() / mDrawableRect.height();

            mScale = scaleX < scaleY ? scaleX : scaleY;

            mAnimaMatrix.postScale(mScale, mScale, mViewCenter.x, mViewCenter.y);

            setFinalMztrix2ImageView();
            resetBase();
        }
    }

    private void initFitCenter() {
        if (mDrawableRect.width() < mViewRect.width()) {
            mScale = mViewRect.width() / mDrawableRect.width();

            mAnimaMatrix.postScale(mScale, mScale, mViewCenter.x, mViewCenter.y);

            setFinalMztrix2ImageView();
            resetBase();
        }
    }

    private void initFitStart() {
        initFitCenter();

        float ty = -mDrawableRect.top;
        mTranslateY += ty;
        mAnimaMatrix.postTranslate(0, ty);
        setFinalMztrix2ImageView();
        resetBase();
    }

    private void initFitEnd() {
        initFitCenter();

        float ty = (mViewRect.bottom - mDrawableRect.bottom);
        mTranslateY += ty;
        mAnimaMatrix.postTranslate(0, ty);
        setFinalMztrix2ImageView();
        resetBase();
    }

    private void initFitXY() {
        float scaleX = mViewRect.width() / mDrawableRect.width();
        float scaleY = mViewRect.height() / mDrawableRect.height();

        mAnimaMatrix.postScale(scaleX, scaleY, mViewCenter.x, mViewCenter.y);

        setFinalMztrix2ImageView();
        resetBase();
    }

    /**
     * 重置数据
     */
    private void resetBase() {
        //
        Drawable img = getDrawable();
        int imgw = getDrawableWidth(img);
        int imgh = getDrawableHeight(img);
        mInitDrawableRect.set(0, 0, imgw, imgh);
        // 移动到中心点
        mInitDrawableMatrix.set(mFinalMatrix);
        mInitDrawableMatrix.mapRect(mInitDrawableRect);
        //
        mHalfInitDrawableWidth = mInitDrawableRect.width() / 2;
        mHalfInitDrawableHeight = mInitDrawableRect.height() / 2;
        //
        mScale = 1;
        mTranslateX = 0;
        mTranslateY = 0;
        //
        mAnimaMatrix.reset();
    }

    /**
     * 将最终变换矩阵，赋值给当前ImageView
     */
    private void setFinalMztrix2ImageView() {
        // 将mBaseMatrix赋值给mSynthesisMatrix
        mFinalMatrix.set(mInitDrawableMatrix);
        // 设置变化矩阵
        mFinalMatrix.postConcat(mAnimaMatrix);
        // 给imageView设置matrix
        this.setImageMatrix(mFinalMatrix);


        // mInitDrawableRect根据mAnimaMatrix变换，并把变换后的结果应用于mDrawableRect
        mAnimaMatrix.mapRect(mDrawableRect, mInitDrawableRect);
        // -----------
        drawableLargerViewW = mDrawableRect.width() > mViewRect.width();
        drawableLargerViewH = mDrawableRect.height() > mViewRect.height();
    }


    private void onUp(MotionEvent ev) {
        if (mTransformRunable.isRuning) {
            return;
        }

        float scale = mScale;

        if (mScale < 1) {
            scale = 1;
            mTransformRunable.withScale(mScale, 1);
        } else if (mScale > MAX_SCALE) {
            scale = MAX_SCALE;
            mTransformRunable.withScale(mScale, MAX_SCALE);
        }

        float cx = mDrawableRect.left + mDrawableRect.width() / 2;
        float cy = mDrawableRect.top + mDrawableRect.height() / 2;

        mScaleCenter.set(cx, cy);
        mRotateCenter.set(cx, cy);
        mTranslateX = 0;
        mTranslateY = 0;

        mTmpMatrix.reset();
        mTmpMatrix.postTranslate(-mInitDrawableRect.left, -mInitDrawableRect.top);
        mTmpMatrix.postTranslate(cx - mHalfInitDrawableWidth, cy - mHalfInitDrawableHeight);
        mTmpMatrix.postScale(scale, scale, cx, cy);
        mTmpMatrix.mapRect(mTmpRect, mInitDrawableRect);

        doTranslateReset(mTmpRect);
        mTransformRunable.start();
    }

    private void doTranslateReset(RectF imgRect) {
        int tx = 0;
        int ty = 0;

        if (imgRect.width() <= mViewRect.width()) {
            if (!isImageCenterWidth(imgRect)) {
                tx = -(int) ((mViewRect.width() - imgRect.width()) / 2 - imgRect.left);
            }
        } else {
            if (imgRect.left > mViewRect.left) {
                tx = (int) (imgRect.left - mViewRect.left);
            } else if (imgRect.right < mViewRect.right) {
                tx = (int) (imgRect.right - mViewRect.right);
            }
        }

        if (imgRect.height() <= mViewRect.height()) {
            if (!isImageCenterHeight(imgRect)) {
                ty = -(int) ((mViewRect.height() - imgRect.height()) / 2 - imgRect.top);
            }
        } else {
            if (imgRect.top > mViewRect.top) {
                ty = (int) (imgRect.top - mViewRect.top);
            } else if (imgRect.bottom < mViewRect.bottom) {
                ty = (int) (imgRect.bottom - mViewRect.bottom);
            }
        }

        if (tx != 0 || ty != 0) {
            if (!mTransformRunable.mFlingScroller.isFinished()) {
                mTransformRunable.mFlingScroller.abortAnimation();
            }
            mTransformRunable.withTranslate(mTranslateX, mTranslateY, -tx, -ty);
        }
    }

    private boolean isImageCenterHeight(RectF rect) {
        return Math.abs(Math.round(rect.top) - (mViewRect.height() - rect.height()) / 2) < 1;
    }

    private boolean isImageCenterWidth(RectF rect) {
        return Math.abs(Math.round(rect.left) - (mViewRect.width() - rect.width()) / 2) < 1;
    }


    private ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();

            if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
                return false;
            // mscale
            mScale *= scaleFactor;
            // 更改变化矩阵
            mAnimaMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            //
            setFinalMztrix2ImageView();
            return true;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    };

    private float resistanceScrollByX(float overScroll, float detalX) {
        float s = detalX * (Math.abs(Math.abs(overScroll) - MAX_OVER_RESISTANCE) / (float) MAX_OVER_RESISTANCE);
        return s;
    }

    private float resistanceScrollByY(float overScroll, float detalY) {
        float s = detalY * (Math.abs(Math.abs(overScroll) - MAX_OVER_RESISTANCE) / (float) MAX_OVER_RESISTANCE);
        return s;
    }

    /**
     * 匹配两个Rect的共同部分输出到out，若无共同部分则输出0，0，0，0
     */
    private void mapRect(RectF r1, RectF r2, RectF out) {

        float l, r, t, b;

        l = r1.left > r2.left ? r1.left : r2.left;
        r = r1.right < r2.right ? r1.right : r2.right;

        if (l > r) {
            out.set(0, 0, 0, 0);
            return;
        }

        t = r1.top > r2.top ? r1.top : r2.top;
        b = r1.bottom < r2.bottom ? r1.bottom : r2.bottom;

        if (t > b) {
            out.set(0, 0, 0, 0);
            return;
        }

        out.set(l, t, r, b);
    }

    private void checkRect() {
        if (!hasOverTranslate) {
            mapRect(mViewRect, mDrawableRect, mCommonRect);
        }
    }

    private Runnable mClickRunnable = new Runnable() {
        @Override
        public void run() {
            if (mClickListener != null) {
                mClickListener.onClick(PhotoImageView.this);
            }
        }
    };

    /**
     *
     */
    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            hasOverTranslate = false;
            hasMultiTouch = false;

            removeCallbacks(mClickRunnable);
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (hasMultiTouch) {
                return false;
            }
            if (!drawableLargerViewW && !drawableLargerViewH) {
                return false;
            }
            if (mTransformRunable.isRuning) {
                return false;
            }

            float vx = velocityX;
            float vy = velocityY;

            if (Math.round(mDrawableRect.left) >= mViewRect.left || Math.round(mDrawableRect.right) <= mViewRect.right) {
                vx = 0;
            }

            if (Math.round(mDrawableRect.top) >= mViewRect.top || Math.round(mDrawableRect.bottom) <= mViewRect.bottom) {
                vy = 0;
            }


            doTranslateReset(mDrawableRect);
            mTransformRunable.withFling(vx, vy);

            onUp(e2);
            mTransformRunable.start();
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (mTransformRunable.isRuning) {
                mTransformRunable.stop();
            }

            if (canScrollHorizontallySelf(distanceX)) {
                if (distanceX < 0 && mDrawableRect.left - distanceX > mViewRect.left)
                    distanceX = mDrawableRect.left;
                if (distanceX > 0 && mDrawableRect.right - distanceX < mViewRect.right)
                    distanceX = mDrawableRect.right - mViewRect.right;

                mAnimaMatrix.postTranslate(-distanceX, 0);
                mTranslateX -= distanceX;
            } else if (drawableLargerViewW || hasMultiTouch || hasOverTranslate) {
                checkRect();
                if (!hasMultiTouch) {
                    if (distanceX < 0 && mDrawableRect.left - distanceX > mCommonRect.left)
                        distanceX = resistanceScrollByX(mDrawableRect.left - mCommonRect.left, distanceX);
                    if (distanceX > 0 && mDrawableRect.right - distanceX < mCommonRect.right)
                        distanceX = resistanceScrollByX(mDrawableRect.right - mCommonRect.right, distanceX);
                }

                mTranslateX -= distanceX;
                mAnimaMatrix.postTranslate(-distanceX, 0);
                hasOverTranslate = true;
            }

            if (canScrollVerticallySelf(distanceY)) {
                if (distanceY < 0 && mDrawableRect.top - distanceY > mViewRect.top)
                    distanceY = mDrawableRect.top;
                if (distanceY > 0 && mDrawableRect.bottom - distanceY < mViewRect.bottom)
                    distanceY = mDrawableRect.bottom - mViewRect.bottom;

                mAnimaMatrix.postTranslate(0, -distanceY);
                mTranslateY -= distanceY;
            } else if (drawableLargerViewH || hasOverTranslate || hasMultiTouch) {
                checkRect();
                if (!hasMultiTouch) {
                    if (distanceY < 0 && mDrawableRect.top - distanceY > mCommonRect.top)
                        distanceY = resistanceScrollByY(mDrawableRect.top - mCommonRect.top, distanceY);
                    if (distanceY > 0 && mDrawableRect.bottom - distanceY < mCommonRect.bottom)
                        distanceY = resistanceScrollByY(mDrawableRect.bottom - mCommonRect.bottom, distanceY);
                }

                mAnimaMatrix.postTranslate(0, -distanceY);
                mTranslateY -= distanceY;
                hasOverTranslate = true;
            }

            setFinalMztrix2ImageView();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            postDelayed(mClickRunnable, 250);
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

            mTransformRunable.stop();

            float from = 1;
            float to = 1;

            float imgcx = mDrawableRect.left + mDrawableRect.width() / 2;
            float imgcy = mDrawableRect.top + mDrawableRect.height() / 2;

            mScaleCenter.set(imgcx, imgcy);
            mRotateCenter.set(imgcx, imgcy);
            mTranslateX = 0;
            mTranslateY = 0;

            if (isZoonUp) {
                from = mScale;
                to = 1;
            } else {
                from = mScale;
                to = MAX_SCALE;

                mScaleCenter.set(e.getX(), e.getY());
            }

            mTmpMatrix.reset();
            mTmpMatrix.postTranslate(-mInitDrawableRect.left, -mInitDrawableRect.top);
            mTmpMatrix.postTranslate(mRotateCenter.x, mRotateCenter.y);
            mTmpMatrix.postTranslate(-mHalfInitDrawableWidth, -mHalfInitDrawableHeight);
            mTmpMatrix.postScale(to, to, mScaleCenter.x, mScaleCenter.y);
            mTmpMatrix.postTranslate(mTranslateX, mTranslateY);
            mTmpMatrix.mapRect(mTmpRect, mInitDrawableRect);
            doTranslateReset(mTmpRect);

            isZoonUp = !isZoonUp;
            mTransformRunable.withScale(from, to);
            mTransformRunable.start();

            return false;
        }
    };

    public boolean canScrollHorizontallySelf(float direction) {
        if (mDrawableRect.width() <= mViewRect.width()) return false;
        if (direction < 0 && Math.round(mDrawableRect.left) - direction >= mViewRect.left)
            return false;
        if (direction > 0 && Math.round(mDrawableRect.right) - direction <= mViewRect.right)
            return false;
        return true;
    }

    public boolean canScrollVerticallySelf(float direction) {
        if (mDrawableRect.height() <= mViewRect.height()) return false;
        if (direction < 0 && Math.round(mDrawableRect.top) - direction >= mViewRect.top)
            return false;
        if (direction > 0 && Math.round(mDrawableRect.bottom) - direction <= mViewRect.bottom)
            return false;
        return true;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        if (hasMultiTouch) {
            return true;
        }
        return canScrollHorizontallySelf(direction);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (hasMultiTouch) {
            return true;
        }
        return canScrollVerticallySelf(direction);
    }

    /**
     * 重置变换参数
     */
    private void resetTransformData() {
        // 重置 mAnimaMatrix
        mAnimaMatrix.reset();
        // 将最终变换矩阵传给ImageView
        setFinalMztrix2ImageView();
        // 重置缩放比例
        mScale = 1;
        // ---移动距离---
        mTranslateX = 0;
        mTranslateY = 0;
    }

    public void startAnimaFrom(ImageInfo fromeInfo) {
        startAnimaFrom(fromeInfo, null);
    }


    /**
     * 在PhotoView内部还没有图片的时候同样可以调用该方法
     * <p></p>
     * 此时并不会播放动画，当给PhotoView设置图片后会自动播放动画。
     * <p></p>
     * 若等待时间过长也没有给控件设置图片，则会忽略该动画，若要再次播放动画则需要重新调用该方法
     * (等待的时间默认500毫秒，可以通过setMaxAnimFromWaiteTime(int)设置最大等待时间)
     */
    public void startAnimaFrom(ImageInfo fromeInfo, OnPhotoAnimaListener zoomInListener) {
        this.mZoomInListener = zoomInListener;
        if (isInit) {
            //
            mTransformRunable.stop();

            // 重置变换参数
            resetTransformData();
            // 获取当前
            ImageInfo toInfo = getImageInfo();
            // drawable的宽和高，计算缩放比例(取比例差距大的)
            float scaleX = fromeInfo.iDrawableRect.width() / toInfo.iDrawableRect.width();
            float scaleY = fromeInfo.iDrawableRect.height() / toInfo.iDrawableRect.height();
            float scale = Math.min(scaleX, scaleY);
            //-----from在屏幕上 Drawable的中心点-----
            float fromDrawableLocOnScreenCenterX = fromeInfo.iDrawableLocalOnScreenR.left + fromeInfo.iDrawableLocalOnScreenR.width() / 2;
            float fromDrawableLocOnScreenCenterY = fromeInfo.iDrawableLocalOnScreenR.top + fromeInfo.iDrawableLocalOnScreenR.height() / 2;
            //---------AnimaMatrix----------
            // 重置AnimaMatrix
            mAnimaMatrix.reset();
            mAnimaMatrix.postTranslate(-mInitDrawableRect.left, -mInitDrawableRect.top);
            mAnimaMatrix.postTranslate(fromDrawableLocOnScreenCenterX - mInitDrawableRect.width() / 2, fromDrawableLocOnScreenCenterY - mInitDrawableRect.height() / 2);
            mAnimaMatrix.postScale(scale, scale, fromDrawableLocOnScreenCenterX, fromDrawableLocOnScreenCenterY);
            //---------setFinalMztrix2ImageView----------
            setFinalMztrix2ImageView();
            //---------“旋转”与“缩放”的中心----------
            mScaleCenter.set(fromDrawableLocOnScreenCenterX, fromDrawableLocOnScreenCenterY);
            mRotateCenter.set(fromDrawableLocOnScreenCenterX, fromDrawableLocOnScreenCenterY);
            //---------------移动动画--------------
            // 中心点的移动
            mTransformRunable.withTranslate(0, 0, (int) (mViewCenter.x - fromDrawableLocOnScreenCenterX), (int) (mViewCenter.y - fromDrawableLocOnScreenCenterY));
            //---------------缩放动画--------------
            // scale的变化
            mTransformRunable.withScale(scale, 1);
            //---------------剪裁动画--------------
            // drawable大于View时，才会有剪裁策略
            if (fromeInfo.iViewRect.width() < fromeInfo.iDrawableRect.width() || fromeInfo.iViewRect.height() < fromeInfo.iDrawableRect.height()) {
                float clipRatioX = fromeInfo.iViewRect.width() / fromeInfo.iDrawableRect.width();
                float clipRatioY = fromeInfo.iViewRect.height() / fromeInfo.iDrawableRect.height();
                clipRatioX = Math.min(clipRatioX, 1);
                clipRatioY = Math.min(clipRatioY, 1);

                ClipCalculate c = fromeInfo.iScaleType == ScaleType.FIT_START ? new ClipTypeSTART() : fromeInfo.iScaleType == ScaleType.FIT_END ? new ClipTypeEND() : new ClipTypeOTHER();

                mTransformRunable.withClip(clipRatioX, clipRatioY, 1 - clipRatioX, 1 - clipRatioY, ANIMA_DURING / 3, c);

                mTmpMatrix.setScale(clipRatioX, clipRatioY, (mDrawableRect.left + mDrawableRect.right) / 2, c.calculateTop());
                mTmpMatrix.mapRect(mTransformRunable.mClipRect, mDrawableRect);
                mClip = mTransformRunable.mClipRect;
            }
            //---------------开启动画--------------
            mTransformRunable.start();
        } else {
            mInfo = fromeInfo;
            mInfoTime = System.currentTimeMillis();
        }
    }

    public void startAnimaTo(ImageInfo info, OnPhotoAnimaListener zoomOutListener) {
        if (isInit) {
            //
            mTransformRunable.stop();

            mTranslateX = 0;
            mTranslateY = 0;

            float tcx = info.iDrawableLocalOnScreenR.left + info.iDrawableLocalOnScreenR.width() / 2;
            float tcy = info.iDrawableLocalOnScreenR.top + info.iDrawableLocalOnScreenR.height() / 2;

            mScaleCenter.set(mDrawableRect.left + mDrawableRect.width() / 2, mDrawableRect.top + mDrawableRect.height() / 2);
            mRotateCenter.set(mScaleCenter);

            // 将图片旋转回正常位置，用以计算
            //mAnimaMatrix.postRotate(-mDegrees, mScaleCenter.x, mScaleCenter.y);
            mAnimaMatrix.mapRect(mDrawableRect, mInitDrawableRect);

            // 缩放
            float scaleX = info.iDrawableRect.width() / mInitDrawableRect.width();
            float scaleY = info.iDrawableRect.height() / mInitDrawableRect.height();
            float scale = scaleX > scaleY ? scaleX : scaleY;


            mAnimaMatrix.mapRect(mDrawableRect, mInitDrawableRect);


            mTransformRunable.withTranslate(0, 0, (int) (tcx - mScaleCenter.x), (int) (tcy - mScaleCenter.y));
            mTransformRunable.withScale(mScale, scale);


            if (info.iViewRect.width() < info.iDrawableLocalOnScreenR.width() || info.iViewRect.height() < info.iDrawableLocalOnScreenR.height()) {
                float clipX = info.iViewRect.width() / info.iDrawableLocalOnScreenR.width();
                float clipY = info.iViewRect.height() / info.iDrawableLocalOnScreenR.height();
                clipX = clipX > 1 ? 1 : clipX;
                clipY = clipY > 1 ? 1 : clipY;

                final float cx = clipX;
                final float cy = clipY;
                final ClipCalculate c = info.iScaleType == ScaleType.FIT_START ? new ClipTypeSTART() : info.iScaleType == ScaleType.FIT_END ? new ClipTypeEND() : new ClipTypeOTHER();

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mTransformRunable.withClip(1, 1, -1 + cx, -1 + cy, ANIMA_DURING / 2, c);
                    }
                }, ANIMA_DURING / 2);
            }

            this.mZoomOutListener = zoomOutListener;
            mTransformRunable.start();
        } else {
            this.mZoomOutListener = zoomOutListener;
            if (mZoomOutListener != null) {
                mZoomOutListener.onAnimaFinish();
                mZoomOutListener = null;
            }
        }
    }


    /**
     * ##########################剪裁时，ScaleType的属性##########################
     */
    // 剪裁计算
    public interface ClipCalculate {
        float calculateTop();
    }

    public class ClipTypeSTART implements ClipCalculate {
        public float calculateTop() {
            return mDrawableRect.top;
        }
    }

    public class ClipTypeEND implements ClipCalculate {
        public float calculateTop() {
            return mDrawableRect.bottom;
        }
    }

    public class ClipTypeOTHER implements ClipCalculate {
        public float calculateTop() {
            return (mDrawableRect.top + mDrawableRect.bottom) / 2;
        }
    }


    /**
     * ##########################ImgeView的参数信息都在这里##########################
     */

    /**
     * 获取当前ImageView的位置信息
     *
     * @return
     */
    public ImageInfo getImageInfo() {
        // 获取在屏幕上的位置
        int[] location = new int[2];
        // 5.0以下
        if (Build.VERSION.SDK_INT < PhotoImageInfoUtil.HIDE_STATE_BAR) {
            getLocation(location);
        } else {
            // 5.0以上
            this.getLocationOnScreen(location);
        }
        //---------------Drawable在整个窗口上的View---------------
        RectF drawableLocalOnScreenR = new RectF();
        drawableLocalOnScreenR.set(location[0] + mDrawableRect.left, location[1] + mDrawableRect.top,
                location[0] + mDrawableRect.right, location[1] + mDrawableRect.bottom);
        return new ImageInfo(drawableLocalOnScreenR, mDrawableRect, mViewRect, mScaleType);
    }

    /**
     * 获取屏幕上的位置，标题栏不算在location中
     *
     * @param position
     */
    private void getLocation(int[] position) {

        position[0] += getLeft();
        position[1] += getTop();

        ViewParent viewParent = getParent();
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


    /**
     * 本ImgeView的参数信息都在这里
     */
    public static class ImageInfo {
        // Drawable在整个窗口的位置(left top right bottom)
        RectF iDrawableLocalOnScreenR = new RectF();
        // Drawable的相对于ImageView上的位置
        RectF iDrawableRect = new RectF();
        // ImageView的宽高(0 0 width height)
        RectF iViewRect = new RectF();
        // ImageView的scaleType
        ImageView.ScaleType iScaleType;

        /**
         * 构造方法(缩放ImageView的信息)
         *
         * @param drawableLocalOnScreenR Drawable在整个窗口的位置(left top right bottom)
         * @param drawableRectF          Drawable的相对于ImageView的位置
         * @param viewRectF              ImageView的宽高(0 0 width height)
         * @param scaleType              ImageView的scaleType
         */
        public ImageInfo(RectF drawableLocalOnScreenR, RectF drawableRectF, RectF viewRectF, ImageView.ScaleType scaleType) {
            this.iDrawableLocalOnScreenR.set(drawableLocalOnScreenR);
            this.iDrawableRect.set(drawableRectF);
            this.iViewRect.set(viewRectF);
            this.iScaleType = scaleType;

            Log.d("xiaxl: ", "---ImageInfo---");
            Log.d("xiaxl: ", "iDrawableLocalOnScreenR: " + iDrawableLocalOnScreenR);
            Log.d("xiaxl: ", "iDrawableRect: " + iDrawableRect);
            Log.d("xiaxl: ", "iViewRect: " + iViewRect);


        }
    }


    /**
     *
     */
    private class TransformRunable implements Runnable {

        boolean isRuning;

        // 移动的Scroller
        OverScroller mTranslateScroller;
        OverScroller mFlingScroller;
        Scroller mScaleScroller;
        Scroller mClipScroller;

        ClipCalculate C;

        int mLastFlingX;
        int mLastFlingY;

        int mLastTranslateX;
        int mLastTranslateY;

        RectF mClipRect = new RectF();

        TransformRunable() {
            Context ctx = getContext();
            DecelerateInterpolator i = new DecelerateInterpolator();
            mTranslateScroller = new OverScroller(ctx, i);
            mScaleScroller = new Scroller(ctx, i);
            mFlingScroller = new OverScroller(ctx, i);
            mClipScroller = new Scroller(ctx, i);
        }

        void withTranslate(int startX, int startY, int deltaX, int deltaY) {
            mLastTranslateX = 0;
            mLastTranslateY = 0;
            mTranslateScroller.startScroll(0, 0, deltaX, deltaY, ANIMA_DURING);
        }

        void withScale(float form, float to) {
            mScaleScroller.startScroll((int) (form * 10000), 0, (int) ((to - form) * 10000), 0, ANIMA_DURING);
        }

        void withClip(float fromX, float fromY, float deltaX, float deltaY, int d, ClipCalculate c) {
            mClipScroller.startScroll((int) (fromX * 10000), (int) (fromY * 10000), (int) (deltaX * 10000), (int) (deltaY * 10000), d);
            C = c;
        }

        void withFling(float velocityX, float velocityY) {
            mLastFlingX = velocityX < 0 ? Integer.MAX_VALUE : 0;
            int distanceX = (int) (velocityX > 0 ? Math.abs(mDrawableRect.left) : mDrawableRect.right - mViewRect.right);
            distanceX = velocityX < 0 ? Integer.MAX_VALUE - distanceX : distanceX;
            int minX = velocityX < 0 ? distanceX : 0;
            int maxX = velocityX < 0 ? Integer.MAX_VALUE : distanceX;
            int overX = velocityX < 0 ? Integer.MAX_VALUE - minX : distanceX;

            mLastFlingY = velocityY < 0 ? Integer.MAX_VALUE : 0;
            int distanceY = (int) (velocityY > 0 ? Math.abs(mDrawableRect.top) : mDrawableRect.bottom - mViewRect.bottom);
            distanceY = velocityY < 0 ? Integer.MAX_VALUE - distanceY : distanceY;
            int minY = velocityY < 0 ? distanceY : 0;
            int maxY = velocityY < 0 ? Integer.MAX_VALUE : distanceY;
            int overY = velocityY < 0 ? Integer.MAX_VALUE - minY : distanceY;

            if (velocityX == 0) {
                maxX = 0;
                minX = 0;
            }

            if (velocityY == 0) {
                maxY = 0;
                minY = 0;
            }

            mFlingScroller.fling(mLastFlingX, mLastFlingY, (int) velocityX, (int) velocityY, minX, maxX, minY, maxY, Math.abs(overX) < MAX_FLING_OVER_SCROLL * 2 ? 0 : MAX_FLING_OVER_SCROLL, Math.abs(overY) < MAX_FLING_OVER_SCROLL * 2 ? 0 : MAX_FLING_OVER_SCROLL);
        }

        void start() {
            isRuning = true;
            postExecute();
        }

        void stop() {
            removeCallbacks(this);
            mTranslateScroller.abortAnimation();
            mScaleScroller.abortAnimation();
            mFlingScroller.abortAnimation();
            isRuning = false;
        }

        @Override
        public void run() {

            if (!isRuning) {
                return;
            }

            boolean endAnima = true;

            if (mScaleScroller.computeScrollOffset()) {
                mScale = mScaleScroller.getCurrX() / 10000f;
                endAnima = false;
            }

            if (mTranslateScroller.computeScrollOffset()) {
                int tx = mTranslateScroller.getCurrX() - mLastTranslateX;
                int ty = mTranslateScroller.getCurrY() - mLastTranslateY;
                mTranslateX += tx;
                mTranslateY += ty;
                mLastTranslateX = mTranslateScroller.getCurrX();
                mLastTranslateY = mTranslateScroller.getCurrY();
                endAnima = false;
            }

            if (mFlingScroller.computeScrollOffset()) {
                int x = mFlingScroller.getCurrX() - mLastFlingX;
                int y = mFlingScroller.getCurrY() - mLastFlingY;

                mLastFlingX = mFlingScroller.getCurrX();
                mLastFlingY = mFlingScroller.getCurrY();

                mTranslateX += x;
                mTranslateY += y;
                endAnima = false;
            }

            if (mClipScroller.computeScrollOffset() || mClip != null) {
                float sx = mClipScroller.getCurrX() / 10000f;
                float sy = mClipScroller.getCurrY() / 10000f;
                mTmpMatrix.setScale(sx, sy, (mDrawableRect.left + mDrawableRect.right) / 2, C.calculateTop());
                mTmpMatrix.mapRect(mClipRect, mDrawableRect);

                if (sx == 1) {
                    mClipRect.left = mViewRect.left;
                    mClipRect.right = mViewRect.right;
                }

                if (sy == 1) {
                    mClipRect.top = mViewRect.top;
                    mClipRect.bottom = mViewRect.bottom;
                }

                mClip = mClipRect;
            }

            if (!endAnima) {
                mAnimaMatrix.reset();
                mAnimaMatrix.postTranslate(-mInitDrawableRect.left, -mInitDrawableRect.top);
                mAnimaMatrix.postTranslate(mRotateCenter.x, mRotateCenter.y);
                mAnimaMatrix.postTranslate(-mHalfInitDrawableWidth, -mHalfInitDrawableHeight);

                mAnimaMatrix.postScale(mScale, mScale, mScaleCenter.x, mScaleCenter.y);
                mAnimaMatrix.postTranslate(mTranslateX, mTranslateY);
                setFinalMztrix2ImageView();
                postExecute();
            } else {
                isRuning = false;
                invalidate();


                if (mZoomInListener != null) {
                    mZoomInListener.onAnimaFinish();
                    mZoomInListener = null;
                }

                if (mZoomOutListener != null) {
                    mZoomOutListener.onAnimaFinish();
                    mZoomOutListener = null;
                }


            }
        }

        private void postExecute() {
            if (isRuning) post(this);
        }
    }

}