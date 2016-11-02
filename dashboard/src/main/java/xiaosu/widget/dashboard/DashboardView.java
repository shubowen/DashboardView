package xiaosu.widget.dashboard;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * 仪表盘控件
 */
public class DashboardView extends View implements GestureDetector.OnGestureListener {

    private static final String TAG = "RulerView";

    private Option mOption;

    private int mWidth;
    private int mHeight;
    private float mRadius;
    private int mCenterX;
    private int mCenterY;
    private Paint mCirclePaint;
    private Paint mShortLinePaint;
    private Paint mLongLinePaint;
    private Paint mTextPaint;
    private float mAngle;
    private GestureDetector mGestureDetector;
    private Paint mPointerPaint;
    private Paint mCenterCirclePaint;

    private OnValueChangedListener mOnValueChangedListener;

    private float mUnitDegree;//一个刻度对应的角度大小
    private float mUnitNum;//一个刻度显示的数值的大小

    public DashboardView(Context context) {
        super(context);
    }

    public DashboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        parAttr(context, attrs, 0);
        init(context);
    }

    public DashboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parAttr(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        mGestureDetector = new GestureDetector(context);
        mGestureDetector.setListener(this);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(mOption.circleStrokeColor);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(mOption.circleStrokeWidth);

        mShortLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShortLinePaint.setColor(mOption.shortLineColor);
        mShortLinePaint.setStrokeWidth(mOption.shortLineWidth);

        mLongLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLongLinePaint.setColor(mOption.longLineColor);
        mLongLinePaint.setStrokeWidth(mOption.longLineWidth);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(mOption.textColor);
        mTextPaint.setTextSize(mOption.textSize);

        mPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerPaint.setColor(mOption.pointerColor);
        mPointerPaint.setStrokeWidth(mOption.pointerWidth);

        mCenterCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mUnitDegree = 360f / mOption.spaceNum;
        mUnitNum = (mOption.maxNum - mOption.valueOffset) / mOption.spaceNum;
    }

    private void parAttr(Context context, AttributeSet attrs, int defStyleAttr) {
        mOption = new Option();

        float density = getResources().getDisplayMetrics().density;

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.DashboardView, defStyleAttr, 0);

        mOption.circleStrokeWidth = attributes.getDimensionPixelSize(R.styleable.DashboardView_circleStrokeWidth, (int) (density * 1));
        mOption.circleStrokeColor = attributes.getColor(R.styleable.DashboardView_circleStrokeColor, Color.GREEN);

        mOption.shortLineColor = attributes.getColor(R.styleable.DashboardView_shortLineColor, Color.BLUE);
        mOption.shortLineWidth = attributes.getDimensionPixelSize(R.styleable.DashboardView_shortLineWidth, (int) (density * 1));
        mOption.shortLineLength = attributes.getDimensionPixelSize(R.styleable.DashboardView_shortLineLength, (int) (density * 5));

        mOption.longLineColor = attributes.getColor(R.styleable.DashboardView_longLineColor, Color.GRAY);
        mOption.longLineWidth = attributes.getDimensionPixelSize(R.styleable.DashboardView_longLineWidth, (int) (density * 2));
        mOption.longLineLength = attributes.getDimensionPixelSize(R.styleable.DashboardView_longLineLength, (int) (density * 10));

        mOption.spaceNum = attributes.getInt(R.styleable.DashboardView_spaceNum, 120);
        mOption.maxNum = attributes.getFloat(R.styleable.DashboardView_maxNum, 360);
        mOption.longLineIndex = attributes.getInteger(R.styleable.DashboardView_longLineIndex, 4);

        // TODO: 16/10/31 获取系统默认字体颜色？
        mOption.textColor = attributes.getColor(R.styleable.DashboardView_android_textColor, Color.BLUE);
        mOption.textSize = attributes.getDimensionPixelSize(R.styleable.DashboardView_android_textSize, (int) (density * 10));
        mOption.textMargin = attributes.getDimensionPixelSize(R.styleable.DashboardView_textMargin, (int) (density * 4));

        mOption.pointerWidth = attributes.getDimensionPixelSize(R.styleable.DashboardView_pointerWidth, (int) (density * 4));
        mOption.pointerColor = attributes.getColor(R.styleable.DashboardView_pointerColor, 0xFFFF9933);
        mOption.pointerInset = attributes.getDimensionPixelSize(R.styleable.DashboardView_pointerInset, (int) (density * 4));

        mOption.centerCircleRadius = attributes.getDimensionPixelSize(R.styleable.DashboardView_centerCircleRadius, (int) (density * 4));
        if (attributes.hasValue(R.styleable.DashboardView_centerCircleColors)) {
            String centerCircleColors = attributes.getString(R.styleable.DashboardView_centerCircleColors);
            if (null != centerCircleColors && !centerCircleColors.contains(","))
                throw new RuntimeException("中心圆需两种颜色");
            String[] colors = centerCircleColors.split(",");
            mOption.colors = new int[colors.length];
            try {
                for (int i = 0; i < colors.length; i++) {
                    mOption.colors[i] = Color.parseColor(colors[i]);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mOption.sensitivity = attributes.getFloat(R.styleable.DashboardView_sensitivity, 0.1f);
        mOption.sensitivity = Math.min(mOption.sensitivity, 1f);

        mOption.asInteger = attributes.getBoolean(R.styleable.DashboardView_asInteger, true);

        mOption.valueOffset = attributes.getFloat(R.styleable.DashboardView_valueOffset, 0);

        attributes.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //1、外圆
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mCirclePaint);
        //2、指针
        canvas.drawLine(mCenterX, mCenterY, mCenterX, mOption.pointerInset, mPointerPaint);
        //3、中心圆
        canvas.drawCircle(mCenterX, mCenterY, mOption.centerCircleRadius, mCenterCirclePaint);
        canvas.rotate(mAngle, mCenterX, mCenterY);

        for (int i = 0; i < mOption.spaceNum; i++) {
            canvas.save();
            //画线条
            canvas.rotate(mUnitDegree * i, mCenterX, mCenterY);

            boolean isLongLine = i % mOption.longLineIndex == 0;

            float startY = mCenterY - mRadius + mOption.insetWidth + mOption.circleStrokeWidth / 2;
            float stopY = startY + (isLongLine ? mOption.longLineLength : mOption.shortLineLength);

            canvas.drawLine(mCenterX, startY, mCenterX, stopY, isLongLine ? mLongLinePaint : mShortLinePaint);

            //长线条需要标刻度
            if (isLongLine) {
                String text;

                if (mOption.asInteger)
                    text = String.valueOf((int) (mUnitNum * i + mOption.valueOffset));
                else
                    text = String.valueOf(mUnitNum * i + mOption.valueOffset);

                Rect bounds = getTextBounds(mTextPaint, text);
                canvas.drawText(text, mCenterX - bounds.width() / 2, stopY + bounds.height() + mOption.textMargin, mTextPaint);
            }
            canvas.restore();
        }
    }

    private Rect getTextBounds(Paint textPaint, String text) {
        Rect rect = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), rect);
        return rect;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        mRadius = Math.min(mWidth, mHeight) / 2 - mOption.circleStrokeWidth / 2;

        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;

        //设置阴影
        RadialGradient centerCircleGradient = new RadialGradient(mCenterX, mCenterY, mOption.centerCircleRadius,
                mOption.colors, mOption.stops, Shader.TileMode.REPEAT);
        mCenterCirclePaint.setShader(centerCircleGradient);

        mGestureDetector.setCenterPoint(new PointF(mCenterX, mCenterY));
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(float deltaAngle) {
        mAngle += deltaAngle * 0.1f;
        invalidate();

        //转动的角度
        float angle = (mAngle < 0 ? 0 : 360) - mAngle % 360;

        if (null != mOnValueChangedListener)
            mOnValueChangedListener.valueChange(angle / mUnitDegree * mUnitNum + mOption.valueOffset, this);
        return true;
    }

    public void setOnValueChangedListener(OnValueChangedListener onValueChangedListener) {
        mOnValueChangedListener = onValueChangedListener;
    }

    class Option {
        float longLineLength;
        float longLineWidth;
        int longLineColor;

        float shortLineLength;
        float shortLineWidth;
        int shortLineColor;

        float circleStrokeWidth;
        int circleStrokeColor;

        float insetWidth;

        int textColor;
        float textSize;
        float textMargin;

        int spaceNum;
        float maxNum;
        int longLineIndex;

        boolean asInteger;

        int pointerColor;
        float pointerWidth;
        float pointerInset;

        float centerCircleRadius;
        int[] colors = {Color.BLACK, Color.GRAY, Color.TRANSPARENT};
        float[] stops;

        float sensitivity;

        float valueOffset;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mGestureDetector.onTouchEvent(ev);
    }

    public interface OnValueChangedListener {
        void valueChange(float value, DashboardView dashboard);
    }

}
