package com.cylan.jiafeigou.widget.wheel;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import com.cylan.jiafeigou.R;
import com.cylan.jiafeigou.cache.db.module.HistoryFile;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanzhendong on 2017/12/22.
 */

public class HistoryWheelView extends View implements GestureDetector.OnGestureListener {
    private static final String TAG = HistoryWheelView.class.getSimpleName();
    private OverScroller mScroller;
    private GestureDetectorCompat mDetector;

    private Paint markerPaint = new Paint();
    private Paint naturalDateLinePaint = new Paint();
    private Paint naturalDateTextPaint = new Paint();
    private Paint dataMaskPaint = new Paint();
    private int mTouchSlop;
    private volatile boolean mLocked = false;
    private volatile boolean mHasPendingUpdateAction = false;
    private volatile boolean mHasPendingSnapAction = false;
    private int markerColor;
    private int maskColor;
    private int lineColor;
    private int textColor;
    private int lineInterval;
    private int lineWidth;
    private int textSize;
    private int shortLineHeight;
    private int longLineHeight;
    private int scrollerLockTime;
    private int updateDelay;
    private boolean markerCenterInScreen;
    private int mCenterPosition;
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    @IntDef({SnapDirection.NONE, SnapDirection.LEFT, SnapDirection.RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SnapDirection {
        int NONE = -1;
        int LEFT = 0;
        int RIGHT = 1;
    }

    private int mSnapDirection = SnapDirection.RIGHT;


    private Calendar mCalendar = Calendar.getInstance();
    private TimeZone mTimeZone;
    private long mZeroTime;
    private TreeSet<HistoryFile> mHistoryFiles = new TreeSet<>();
    private Runnable mUnlockRunnable = new Runnable() {
        @Override
        public void run() {
            mLocked = false;
        }
    };

    private Runnable mNotifyRunnable = new Runnable() {
        @Override
        public void run() {
            if (mHistoryFiles.size() == 0) {
                return;
            }
            if (mHistoryListener != null) {
                long currentTime = getCurrentTime();
                Log.d(TAG, "CurrentTime is" + new Date(currentTime).toLocaleString());
                mHistoryListener.onHistoryTimeChanged(currentTime);
            }
        }
    };

    private Runnable mMarkerPositionRunnable = new Runnable() {
        @Override
        public void run() {
            long currentTime = getCurrentTime();
            if (markerCenterInScreen) {
                int[] location = new int[2];
                getLocationOnScreen(location);
                mCenterPosition = getResources().getDisplayMetrics().widthPixels / 2 - location[0];
            } else {
                mCenterPosition = getMeasuredWidth() / 2;
            }
            scrollToPositionInternal(currentTime);
        }
    };

    private HistoryListener mHistoryListener;


    public HistoryWheelView(Context context) {
        this(context, null);
    }

    public HistoryWheelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mScroller = new OverScroller(getContext());
        mDetector = new GestureDetectorCompat(getContext(), this);
        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mZeroTime = mCalendar.getTimeInMillis();

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.HistoryWheelView);
        markerColor = attributes.getColor(R.styleable.HistoryWheelView_hw_marker_color, Color.parseColor("#36BDFF"));
        markerCenterInScreen = attributes.getBoolean(R.styleable.HistoryWheelView_hw_marker_on_screen_center, true);
        maskColor = attributes.getColor(R.styleable.HistoryWheelView_hw_mask_color, Color.parseColor("#4036BDFF"));
        textColor = attributes.getColor(R.styleable.HistoryWheelView_hw_text_color, Color.parseColor("#FFAAAAAA"));
        textSize = attributes.getDimensionPixelSize(R.styleable.HistoryWheelView_hw_text_size, dp2px(10));
        lineColor = attributes.getColor(R.styleable.HistoryWheelView_hw_line_color, Color.parseColor("#FFDEDEDE"));
        lineInterval = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_line_interval, (int) (0.04 * 10 * 60));
        lineWidth = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_line_width, dp2px(2.5f));
        shortLineHeight = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_short_line_height, dp2px(10));
        longLineHeight = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_long_line_height, dp2px(25));
        scrollerLockTime = attributes.getInteger(R.styleable.HistoryWheelView_hw_scroller_lock_time, 5000);
        updateDelay = attributes.getInteger(R.styleable.HistoryWheelView_hw_history_update_delay, 700);

        markerPaint.setAntiAlias(true);
        markerPaint.setColor(markerColor);
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setStrokeWidth(lineWidth);

        naturalDateLinePaint.setAntiAlias(true);
        naturalDateLinePaint.setColor(lineColor);
        naturalDateLinePaint.setStrokeWidth(lineWidth);

        dataMaskPaint.setAntiAlias(true);
        dataMaskPaint.setColor(maskColor);

        naturalDateTextPaint.setAntiAlias(true);
        naturalDateTextPaint.setColor(textColor);
        naturalDateTextPaint.setTextSize(textSize);
        naturalDateTextPaint.setTextAlign(Paint.Align.CENTER);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mTimeZone = TimeZone.getDefault();
        attributes.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        removeCallbacks(mMarkerPositionRunnable);
        post(mMarkerPositionRunnable);
    }

    private int dp2px(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    private volatile float mDistanceX = 0;

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mDistanceX += distanceX;
        if (Math.abs(mDistanceX) >= mTouchSlop) {
            setHistoryLock(true);
            mHasPendingUpdateAction = true;
            if (mSnapDirection != SnapDirection.NONE) {
                mHasPendingSnapAction = true;
            }
            distanceX = mDistanceX;
            mDistanceX = 0;
            mScroller.startScroll(mScroller.getCurrX(), 0, (int) distanceX, 0);
            invalidate();
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        setHistoryLock(true);
        mHasPendingUpdateAction = true;
        if (mSnapDirection != SnapDirection.NONE) {
            mHasPendingSnapAction = true;
        }
        mScroller.fling(mScroller.getCurrX(), 0, (int) -velocityX, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        invalidate();
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            invalidate();
        } else {
            notifyScrollCompleted();
        }
    }

    private void setHistoryLock(boolean locked) {
        if (locked) {
            this.mLocked = true;
            removeCallbacks(mUnlockRunnable);
            removeCallbacks(mNotifyRunnable);
        } else {
            postDelayed(mUnlockRunnable, scrollerLockTime);
        }
    }

    public long getCurrentTime() {
        return (long) (getPixelTime() * (mScroller.getCurrX() + mCenterPosition) + mZeroTime);
    }

    public void scrollToPosition(long time) {
        if (!mLocked) {
            scrollToPositionInternal(time);
        }
    }

    private void scrollToPositionInternal(long time) {
        setHistoryLock(true);
        if (time == 0) {
            time = mZeroTime;
        }
        long distance = getDistanceByTime(time, getCurrentTime());
        mScroller.startScroll(mScroller.getCurrX(), 0, (int) distance, 0);
        invalidate();
    }

    private HistoryFile mSnapHistoryBlock = new HistoryFile();

    private void notifySnapAction() {
        switch (mSnapDirection) {
            case SnapDirection.LEFT: {

            }
            break;
            case SnapDirection.RIGHT: {
                mHasPendingSnapAction = false;
                if (mHistoryFiles.size() == 0) {
                    return;
                }
                long currentTime = getCurrentTime();
                long unitTime = getUnitTime();
                mSnapHistoryBlock.time = currentTime / unitTime;
                HistoryFile floor = mHistoryFiles.floor(mSnapHistoryBlock);
                HistoryFile ceiling = mHistoryFiles.ceiling(mSnapHistoryBlock);
                if (floor == null) {
                    floor = mHistoryFiles.first();
                }
                if (ceiling == null) {
                    ceiling = mHistoryFiles.last();
                }
                long target = currentTime;
                if (target < floor.time * unitTime) {
                    target = floor.time * unitTime;
                } else if (target > (ceiling.time + ceiling.duration) * unitTime) {
                    target = (ceiling.time + ceiling.duration) * unitTime;
                } else if (target > (floor.time + floor.duration) * unitTime && target < ceiling.time * unitTime) {
                    target = ceiling.time * unitTime;
                }
                if (target != currentTime) {
                    scrollToPositionInternal(target);
                } else {
                    notifyUpdate();
                }
            }
            break;
            case SnapDirection.NONE: {

            }
            break;
        }
    }

    private void notifyUpdate() {
        setHistoryLock(false);
        if (mHasPendingUpdateAction) {
            mHasPendingUpdateAction = false;
            postDelayed(mNotifyRunnable, updateDelay);
        }
    }

    private void notifyScrollCompleted() {
        if (mHasPendingSnapAction) {
            notifySnapAction();
        } else {
            notifyUpdate();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawHistoryBlock(canvas);
        drawBackground(canvas);
        drawTimeText(canvas);
        drawDivider(canvas);
    }

    private HistoryFile mStartHistoryBlock = new HistoryFile();
    private HistoryFile mStopHistoryBlock = new HistoryFile();

    private float getPixelTime() {
        return 10 * 60 * 1000F / lineInterval;
    }

    private void drawHistoryBlock(Canvas canvas) {
        if (mHistoryFiles.size() == 0) {
            return;
        }
        int startX = mScroller.getCurrX() - getMeasuredWidth() / 2;
        int stopX = startX + getMeasuredWidth() * 2;
        long unitTime = getUnitTime();
        mStartHistoryBlock.time = (long) (startX * getPixelTime() + mZeroTime) / unitTime;
        mStopHistoryBlock.time = (long) (stopX * getPixelTime() + mZeroTime) / unitTime;
        HistoryFile start = mHistoryFiles.floor(mStartHistoryBlock);
        if (start == null) {
            start = mHistoryFiles.first();
        }
        HistoryFile stop = mHistoryFiles.ceiling(mStopHistoryBlock);
        if (stop == null) {
            stop = mHistoryFiles.last();
        }
        SortedSet<HistoryFile> historyFiles = mHistoryFiles.subSet(start, true, stop, true);
        for (HistoryFile file : historyFiles) {
            long distanceX1 = getDistanceByTime(file.time * unitTime, mZeroTime);
            long distanceX2 = getDistanceByTime((file.time + file.duration) * unitTime, mZeroTime);
            canvas.drawRect(distanceX1, 0, distanceX2, getMeasuredHeight(), dataMaskPaint);
        }
    }

    private long getDistanceByTime(long start, long end) {
        float unit = 10 * 60 * 1000F / lineInterval;
        return (long) ((start - end) / unit);
    }

    private void drawDivider(Canvas canvas) {
        int divider = mScroller.getCurrX() + mCenterPosition;
        canvas.drawLine(divider, 0, divider, getMeasuredHeight(), markerPaint);

    }

    private void drawBackground(Canvas canvas) {
        int startX = (mScroller.getCurrX() - getMeasuredWidth() / 2) / lineInterval * lineInterval;
        int stopX = startX + getMeasuredWidth() * 2;
        while (startX <= stopX) {
            canvas.drawLine(startX, 0, startX, startX % (lineInterval * 6) == 0 ? longLineHeight : shortLineHeight, naturalDateLinePaint);
            startX += lineInterval;
        }
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private long getUnitTime() {
        long unitTime = 1;
        if (timeUnit == TimeUnit.SECONDS) {
            unitTime = 1000;
        }
        return unitTime;
    }

    private void drawTimeText(Canvas canvas) {
        if (mTimeZone != null) {
            int startX = (mScroller.getCurrX() - getMeasuredWidth() / 2) / lineInterval * lineInterval;
            int stopX = startX + getMeasuredWidth() * 2;
            long unitTime = getUnitTime();
            while (startX <= stopX) {
                if (startX % (lineInterval * 6) == 0) {
                    long dis = startX / lineInterval * 10 * 60 * unitTime;

                    canvas.drawText(dateFormat.format(mZeroTime + dis), startX, getMeasuredHeight() - 40, naturalDateTextPaint);
                }
                startX += lineInterval;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return mDetector.onTouchEvent(event);
    }

    public void setTimeZone(TimeZone timeZone) {
        this.mTimeZone = timeZone;
        dateFormat.setTimeZone(timeZone);
        postInvalidate();
    }

    public void setSnapDirection(@SnapDirection int snapDirection) {
        this.mSnapDirection = snapDirection;
    }

    public void setHistoryFiles(List<HistoryFile> historyFiles) {
        mHistoryFiles.clear();
        mHistoryFiles.addAll(historyFiles);
        postInvalidate();
    }

    public void addHistoryFiles(List<HistoryFile> historyFiles) {
        mHistoryFiles.addAll(historyFiles);
        postInvalidate();
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public int getHistoryCount() {
        return mHistoryFiles.size();
    }

    public void setHistoryListener(HistoryListener listener) {
        this.mHistoryListener = listener;
    }

    public interface HistoryListener {
        void onHistoryTimeChanged(long time);
    }

}
