package xiaoyalllove.xyeturnplatedemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by xiaoyalllove on 2018/2/6.
 */

public class LuckyTurnplate extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    /**
     * 盘块的奖项
     */
    private String[] mNames = new String[]{"收音机", "钢琴", "手风琴", "架子鼓", "小鼓", "萨克斯"};
    /**
     * 奖项的图片
     */
    private int[] mImgs = new int[]{R.mipmap.shouyingji, R.mipmap.gangq, R.mipmap.shoufq, R.mipmap.jiazig, R.mipmap.xiaogu, R.mipmap.sakesi};
    /**
     * 与图片对应的bitmap
     */
    private Bitmap[] mImgsBitmap;
    /**
     * 盘块的颜色
     */
    private int[] mColors = new int[]{0xFFFFC300, 0XFFF17E01, 0xFFFFC300, 0XFFF17E01, 0xFFFFC300, 0XFFF17E01};
    private int mItemCount = 6;
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    /**
     * 用于线程绘制
     */
    private Thread t;
    /**
     * 线程的开关控制
     */
    private boolean isRunning;

    /**
     * 盘块的整个范围
     */
    private RectF mRange = new RectF();
    /**
     * 整个盘块的直径
     */
    private int mRadius;
    /**
     * 背景的半径
     */
    private int mCircleRadius;
    /**
     * 绘制背景的画笔
     */
    private Paint mCirclePaint;
    /**
     * 绘制盘块的画笔
     */
    private Paint mArcPaint;
    /**
     * 绘制文本的画笔
     */
    private Paint mTextPaint;

    /**
     * 盘块转动的速度
     */
    private double mSpeed = 0;

    /**
     * 转动角度  volatile保证该属性在线程间的可见性
     */
    private volatile float mStartAngle = 0;
    /**
     * 判断是否点击了停止按钮
     */
    private boolean isShouldEnd;

    /**
     * 转盘中心位置
     */
    private int mCenter;
    /**
     * 直接以paddingLeft为准
     */
    private int mPadding;

//    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.background);

    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20,
            getResources().getDisplayMetrics());

    public LuckyTurnplate(Context context) {
        this(context, null);
    }

    public LuckyTurnplate(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);

        //可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        //设置常量
        setKeepScreenOn(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * view 的区域设置为矩形
         */

        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());

        mPadding = getPaddingLeft();
        /**
         * 圆盘背景的半径
         */
        mCircleRadius = (getMeasuredWidth() - mPadding) / 2;
        /**
         * 直径
         */
        mRadius = width - mPadding * 2;
        /**
         * 中心点
         */
        mCenter = width / 2;

        setMeasuredDimension(width, width);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        /**
         * 初始化盘块的画笔
         */
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);


        /**
         * 初始化背景的画笔
         */
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setDither(true);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setColor(Color.parseColor("#ad1f02"));

        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);

        /**
         * 初始化盘块绘制的范围
         */
        mRange = new RectF(mPadding, mPadding, mPadding + mRadius, mPadding + mRadius);
        /**
         * 初始化图片
         */
        mImgsBitmap = new Bitmap[mItemCount];

        for (int i = 0; i < mItemCount; i++) {
            mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(), mImgs[i]);
        }
        isRunning = true;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;

    }

    @Override
    public void run() {
        /**
         * 不断进行绘制
         */
        while (isRunning) {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            if (end - start < 50) {
                try {
                    Thread.sleep(end - start);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null) {
                //绘制背景
                drawBg();
                drawAngle();
            }
        } catch (Exception e) {
        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    /**
     * 绘制盘块
     */
    private void drawAngle() {
        float tmpAngle = mStartAngle;
        float sweepAngle = 360 / mItemCount;
        for (int i = 0; i < mItemCount; i++) {
            mArcPaint.setColor(mColors[i]);
            //绘制盘块
            mCanvas.drawArc(mRange, tmpAngle, sweepAngle, true, mArcPaint);
            //绘制文本
            drawText(tmpAngle, sweepAngle, mNames[i]);

            //绘制图片
            drawIcon(tmpAngle, mImgsBitmap[i]);
            tmpAngle += sweepAngle;
        }
        mStartAngle += mSpeed;

        //如果点击停止按钮
        if (isShouldEnd) {
            mSpeed -= 1;
        }

        if (mSpeed <= 0) {
            mSpeed = 0;
            isShouldEnd = false;
        }
    }

    /**
     * 启动旋转按钮
     */
    public void luckyStart(int index) {
        //计算每一项的角度
        float angle = 360 / mItemCount;
        //计算每一项的范围
        float from = 270 - (index + 1) * angle;
        float end = from + angle;

        //设置旋转停下来的距离
        float targetFrom = 4 * 360 + from;
        float targetEnd = 4 * 360 + end;


        float v1 = (float) ((-1 + Math.sqrt(1 + 8 * targetFrom)) / 2);
        float v2 = (float) ((-1 + Math.sqrt(1 + 8 * targetEnd)) / 2);

        mSpeed = v1 + Math.random() * (v2 - v1);

        isShouldEnd = false;
    }

    public void luckyEnd() {
        mStartAngle = 0;
        isShouldEnd = true;
    }

    /**
     * 判断转盘是否在旋转
     *
     * @return
     */
    public boolean isStart() {
        return mSpeed != 0;
    }

    public boolean isShouldEnd() {
        return isShouldEnd;
    }

    /**
     * 绘制图片
     *
     * @param tmpAngle 起始角度
     * @param bitmap
     */
    private void drawIcon(float tmpAngle, Bitmap bitmap) {
        //设置图片的宽度  为直径的1/8
        int imgWidth = mRadius / 8;

        // 每块对应的弧度值
        float angle = (float) ((tmpAngle + 360 / mItemCount / 2) * Math.PI / 180);

        int x = (int) (mCenter + mRadius / 2 / 2 * Math.cos(angle));
        int y = (int) (mCenter + mRadius / 2 / 2 * Math.sin(angle));

        //确定图片的位置 int left, int top, int right, int bottom
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
        mCanvas.drawBitmap(bitmap, null, rect, null);
    }

    /**
     * 绘制文本
     *
     * @param tmpAngle
     * @param sweepAngle
     * @param mName
     */
    private void drawText(float tmpAngle, float sweepAngle, String mName) {
        Path path = new Path();
        path.addArc(mRange, tmpAngle, sweepAngle);
        //利用水平偏移量让文字居中 hOffset（圆弧弧度的一半 - 文字的一半=偏移量）
        //(圆形的周 dπ)
        float textWidth = mTextPaint.measureText(mName);
        int hOffset = (int) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);

        //垂直偏移量 vOffset
        int vOffset = mRadius / 2 / 6;
        mCanvas.drawTextOnPath(mName, path, hOffset, vOffset, mTextPaint);
    }

    /**
     * 绘制背景
     */
    private void drawBg() {
        /**
         * 设置canvas画板的颜色--默认黑色
         */
        mCanvas.drawColor(0xFFFFFFFF);
        //int left, int top, int right, int bottom
//        mCanvas.drawBitmap(mBgBitmap, null, new Rect(mPadding / 2, mPadding / 2,
//                getMeasuredWidth() - mPadding / 2, getMeasuredHeight() - mPadding / 2), null);

        //float cx, float cy, float radius, @NonNull Paint paint
        float x = getWidth() >> 1;
        float y = getHeight() >> 1;
        mCanvas.drawCircle(x, y, mCircleRadius, mCirclePaint);
    }
}
