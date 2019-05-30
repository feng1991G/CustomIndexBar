package com.contact.index.sidebar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.contact.index.R;
import com.contact.index.sidebar.helper.IIndexBarDataHelper;
import com.contact.index.sidebar.helper.IndexBarDataHelperImpl;
import com.contact.index.sidebar.model.BaseIndexPinyinBean;
import com.contact.index.sidebar.utils.ConvertUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 介绍：索引右侧边栏
 */

public class IndexBar extends View {
    private static final String TAG = "IndexBar";

    //#在最后面（默认的数据源）
    public static String[] INDEX_STRING = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};
    //是否需要根据实际的数据来生成索引数据源（例如 只有 A B C 三种tag，那么索引栏就 A B C 三项）
    private boolean isNeedRealIndex;
    //索引数据源
    private List<String> mIndexDatas;

    private Paint wordsPaint = new Paint(); // 画笔
    /*字母背景画笔*/
    private Paint bgPaint = new Paint();
    //View的宽高
    private int mWidth, mHeight;
    //每个index区域的高度
    private int mGapHeight;

    /**固定背景半径*/
    private float radius;
    //手指按下时的背景色
    private int mPressedBackground;
    public boolean isPressDown = false;
    public int chooseItem = -1;

    //以下是帮助类
    //汉语->拼音，拼音->tag
    private IIndexBarDataHelper mDataHelper;

    //以下边变量是外部set进来的
    private TextView mPressedShowTextView;//用于特写显示正在被触摸的index值
    private boolean isSourceDatasAlreadySorted;//源数据 已经有序？
    private List<? extends BaseIndexPinyinBean> mSourceDatas;//Adapter的数据源
    private LinearLayoutManager mLayoutManager;
    private int mHeaderViewCount = 0;

    public IndexBar(Context context) {
        this(context, null);
    }

    public IndexBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndexBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public int getHeaderViewCount() {
        return mHeaderViewCount;
    }

    /**
     * 设置Headerview的Count
     *
     * @param headerViewCount
     * @return
     */
    public IndexBar setHeaderViewCount(int headerViewCount) {
        mHeaderViewCount = headerViewCount;
        return this;
    }

    public boolean isSourceDatasAlreadySorted() {
        return isSourceDatasAlreadySorted;
    }

    /**
     * 源数据 是否已经有序
     *
     * @param sourceDatasAlreadySorted
     * @return
     */
    public IndexBar setSourceDatasAlreadySorted(boolean sourceDatasAlreadySorted) {
        isSourceDatasAlreadySorted = sourceDatasAlreadySorted;
        return this;
    }

    public IIndexBarDataHelper getDataHelper() {
        return mDataHelper;
    }

    /**
     * 设置数据源帮助类
     *
     * @param dataHelper
     * @return
     */
    public IndexBar setDataHelper(IIndexBarDataHelper dataHelper) {
        mDataHelper = dataHelper;
        return this;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        int textSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 11, getResources().getDisplayMetrics());//默认的TextSize
        int textColor = Color.BLACK;//默认是纯黑色
        mPressedBackground = Color.parseColor("#FDDA43");//默认按下是纯黑色
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IndexBar, defStyleAttr, 0);
        int n = typedArray.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = typedArray.getIndex(i);
            //如果引用成AndroidLib 资源都不是常量，无法使用switch case
            if (attr == R.styleable.IndexBar_indexBarTextSize) {
                textSize = typedArray.getDimensionPixelSize(attr, textSize);
            } else if(attr == R.styleable.IndexBar_indexBarTextColor){
                textColor = typedArray.getColor(attr, textColor);
            } else if (attr == R.styleable.IndexBar_indexBarPressBackground) {
                mPressedBackground = typedArray.getColor(attr, mPressedBackground);
            }
        }
        typedArray.recycle();
        initIndexDatas();
        //设置index触摸监听器
        setmOnIndexPressedListener(new onIndexPressedListener() {
            @Override
            public void onIndexPressed(int index, String text) {
                if (mPressedShowTextView != null) { //显示hintTexView
                    mPressedShowTextView.setVisibility(View.VISIBLE);
                    mPressedShowTextView.setText(text);
                }
                //滑动Rv
                if (mLayoutManager != null) {
                    int position = getPosByTag(text);
                    if (position != -1) {
                        mLayoutManager.scrollToPositionWithOffset(position, 0);
                    }
                }
            }

            @Override
            public void onMotionEventEnd() {
                //隐藏hintTextView
                if (mPressedShowTextView != null) {
                    mPressedShowTextView.setVisibility(View.GONE);
                }
            }
        });

        mDataHelper = new IndexBarDataHelperImpl();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mGapHeight  = getMeasuredHeight() / 27;//26，#个字母
        //动态设置宽高
        int height = mGapHeight*INDEX_STRING.length;
        Log.e(TAG, height +"*");
        setMeasuredDimension(mWidth,mGapHeight*mIndexDatas.size());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //获取固定字母半径
        Paint raPaint = new Paint();
        raPaint.setTextSize(ConvertUtils.sp2px(getContext(), 14));
        raPaint.setAntiAlias(true);

        Rect rect = new Rect();
        raPaint.getTextBounds("H", 0, 1, rect);
        radius = rect.width();
        int t = getPaddingTop();//top的基准点(支持padding)
        String index;//每个要绘制的index内容
        for (int i = 0; i < mIndexDatas.size(); i++) {
            index = mIndexDatas.get(i);
//            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();//获得画笔的FontMetrics，用来计算baseLine。因为drawText的y坐标，代表的是绘制的文字的baseLine的位置
//            int baseline = (int) ((mGapHeight - fontMetrics.bottom - fontMetrics.top) / 2);//计算出在每格index区域，竖直居中的baseLine值
            wordsPaint.setTextSize(ConvertUtils.sp2px(getContext(), 14));
            wordsPaint.setAntiAlias(true);
            if(chooseItem == i && isPressDown){
                //绘制文字圆形背景
                bgPaint.setColor(mPressedBackground);
                canvas.drawCircle(mWidth / 2,  mGapHeight * i +radius, radius , bgPaint);
                wordsPaint.setColor(Color.WHITE);
            } else {
                wordsPaint.setColor(Color.GRAY);
            }

//            canvas.drawText(index, mWidth / 2 - mPaint.measureText(index) / 2, t + mGapHeight * i + baseline, mPaint);//调用drawText，居中显示绘制index

            //获取文字的宽高,部分字母过宽
            wordsPaint.getTextBounds(index, 0, 1, rect);
            int wordWidth = rect.width();
            float xPos;
            if (wordWidth > radius){
                xPos = mWidth / 2 - wordWidth / 2;
            }else {
                xPos = mWidth / 2 -  radius / 2;
            }

            // 文字绘制的起始Y
            float yPos = mGapHeight * i + radius*3/2;
            Log.e(TAG, "onDraw: "+yPos +":"+xPos);
            canvas.drawText(index, xPos, yPos, wordsPaint);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //注意这里没有break，因为down时，也要计算落点 回调监听器
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                //通过计算判断落点在哪个区域：
                int pressI = (int) ((y - getPaddingTop()) / mGapHeight);
                //边界处理（在手指move时，有可能已经移出边界，防止越界）
                if (pressI < 0) {
                    pressI = 0;
                } else if (pressI >= mIndexDatas.size()) {
                    pressI = mIndexDatas.size() - 1;
                }
                Log.e(TAG,pressI +"");
                isPressDown = true;
                chooseItem = pressI;
                invalidate();
                //回调监听器
                if (null != mOnIndexPressedListener && pressI > -1 && pressI < mIndexDatas.size()) {
                    mOnIndexPressedListener.onIndexPressed(pressI, mIndexDatas.get(pressI));
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            default:
                isPressDown = false;
                chooseItem = -1;
                invalidate();
                setBackgroundResource(android.R.color.transparent);//手指抬起时背景恢复透明
                //回调监听器
                if (null != mOnIndexPressedListener) {
                    mOnIndexPressedListener.onMotionEventEnd();
                }
                break;
        }
        return true;
    }

    /**
     * 当前被按下的index的监听器
     */
    public interface onIndexPressedListener {
        void onIndexPressed(int index, String text);//当某个Index被按下

        void onMotionEventEnd();//当触摸事件结束（UP CANCEL）
    }

    private onIndexPressedListener mOnIndexPressedListener;

    public onIndexPressedListener getmOnIndexPressedListener() {
        return mOnIndexPressedListener;
    }

    public void setmOnIndexPressedListener(onIndexPressedListener mOnIndexPressedListener) {
        this.mOnIndexPressedListener = mOnIndexPressedListener;
    }

    /**
     * 显示当前被按下的index的TextView
     *
     * @return
     */

    public IndexBar setmPressedShowTextView(TextView mPressedShowTextView) {
        this.mPressedShowTextView = mPressedShowTextView;
        return this;
    }

    public IndexBar setmLayoutManager(LinearLayoutManager mLayoutManager) {
        this.mLayoutManager = mLayoutManager;
        return this;
    }

    /**
     * 一定要在设置数据源{@link #setmSourceDatas(List)}之前调用
     *
     * @param needRealIndex
     * @return
     */
    public IndexBar setNeedRealIndex(boolean needRealIndex) {
        isNeedRealIndex = needRealIndex;
        initIndexDatas();
        return this;
    }

    private void initIndexDatas() {
        if (isNeedRealIndex) {
            mIndexDatas = new ArrayList<>();
        } else {
            mIndexDatas = Arrays.asList(INDEX_STRING);
        }
    }

    public IndexBar setmSourceDatas(List<? extends BaseIndexPinyinBean> mSourceDatas) {
        this.mSourceDatas = mSourceDatas;
        initSourceDatas();//对数据源进行初始化
        return this;
    }


    /**
     * 初始化原始数据源，并取出索引数据源
     *
     * @return
     */
    private void initSourceDatas() {
        //解决源数据为空 或者size为0的情况,
        if (null == mSourceDatas || mSourceDatas.isEmpty()) {
            return;
        }
        if (!isSourceDatasAlreadySorted) {
            //排序sourceDatas
            mDataHelper.sortSourceDatas(mSourceDatas);
        } else {
            //汉语->拼音
            mDataHelper.convert(mSourceDatas);
            //拼音->tag
            mDataHelper.fillInexTag(mSourceDatas);
        }
        if (isNeedRealIndex) {
            mDataHelper.getSortedIndexDatas(mSourceDatas, mIndexDatas);
            computeGapHeight();
        }
        //sortData();
    }

    /**
     * 以下情况调用：
     * 1 在数据源改变
     * 2 控件size改变时
     * 计算gapHeight
     */
    private void computeGapHeight() {
//        mGapHeight = (mHeight - getPaddingTop() - getPaddingBottom()) / mIndexDatas.size();
    }

    /**
     * 对数据源排序
     */
    private void sortData() {

    }


    /**
     * 根据传入的pos返回tag
     *
     * @param tag
     * @return
     */
    private int getPosByTag(String tag) {
        //解决源数据为空 或者size为0的情况,
        if (null == mSourceDatas || mSourceDatas.isEmpty()) {
            return -1;
        }
        if (TextUtils.isEmpty(tag)) {
            return -1;
        }
        for (int i = 0; i < mSourceDatas.size(); i++) {
            if (tag.equals(mSourceDatas.get(i).getBaseIndexTag())) {
                return i + getHeaderViewCount();
            }
        }
        return -1;
    }

}
