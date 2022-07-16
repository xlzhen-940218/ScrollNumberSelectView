package com.xlzhen.scrollnumberselectview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class ScrollNumberSelectView extends View implements View.OnTouchListener {

    private float height;

    private int dp30, dp40, dp50, dp100;
    private float[] numbers;
    private float[] y;
    private Paint[] paints;
    private float centerX, centerY;
    private int centerIndex;

    private GestureDetector detector;

    private OnSelectChangeListener listener;

    private Vibrator vibrator;

    public ScrollNumberSelectView(Context context) {
        super(context);
        init(context);
    }

    public ScrollNumberSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScrollNumberSelectView);
        float end = array.getFloat(R.styleable.ScrollNumberSelectView_scroll_end, 10f);
        float interval = array.getFloat(R.styleable.ScrollNumberSelectView_scroll_interval, 0.25f);
        setInterval(end, interval);
    }

    public void setListener(OnSelectChangeListener listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        dp30 = dp2px(30);
        dp40 = dp2px(40);
        dp50 = dp2px(50);
        dp100 = dp2px(100);

        detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                for (int i = 0; i < y.length; i++) {
                    y[i] -= distanceY * 1.5f;
                    if (rangeInDefined(y[i], centerY - dp50, centerY + dp50)) {
                        centerIndex = i;
                        if (listener != null) {
                            listener.onSelectChange(numbers[i]);
                        }
                    }
                    if (y[i] < height && paints[i] == null) {//滑动到底部，添加新的
                        addFooterPaint(i);
                    }
                }
                for (int i = 0; i < paints.length; i++) {

                    if (paints[i] == null)
                        break;

                    if (rangeInDefined(y[i], centerY - dp50, centerY + dp50)) {
                        paints[i].setColor(getContext().getResources().getColor(R.color.teal_200));
                        paints[i].setTextSize(dp50);
                    } else if (y[i] > centerY) {
                        paints[i].setColor(getContext().getResources().getColor(R.color.color_ba));
                        if (i == centerIndex + 1) {
                            paints[i].setTextSize(paints[i].getTextSize() + (distanceY / 5f));
                        } else {
                            paints[i].setTextSize(dp50 * (4 - (i - centerIndex)) * 0.25f);
                        }
                    } else if (y[i] < centerY) {
                        paints[i].setColor(getContext().getResources().getColor(R.color.color_ba));
                        if (i == centerIndex - 1) {
                            paints[i].setTextSize(paints[i].getTextSize() - (distanceY / 5f));
                        } else {
                            paints[i].setTextSize(dp50 * (4 - (centerIndex - i)) * 0.25f);
                        }
                    }
                }
                vibrator.vibrate(20);
                invalidate();
                return true;
            }
        });

        setOnTouchListener(this);
    }

    /**
     * dp转px
     */
    public int dp2px(float dpVal) {
        try {
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, dpVal, Resources.getSystem()
                            .getDisplayMetrics());
        } catch (Exception ex) {
            return 0;
        }

    }

    private void addFooterPaint(int i) {
        y[i] = y[i - 1] + dp100;
        Paint paint = new Paint();
        paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        paint.setColor(getContext().getResources().getColor(R.color.color_ba));
        paint.setTextSize(dp50 * (4 - (i - centerIndex)) * 0.25f);
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paints[i] = paint;
    }

    public void setInterval(float end, float interval) {
        float[] numbers = new float[(int) (end / interval)];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = interval * i + interval;
        }
        setNumbers(numbers);
    }

    public void setNumbers(float[] numbers) {
        this.numbers = numbers;
        resize();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        for (int i = 0; i < numbers.length; i++) {
            if (paints[i] != null) {
                canvas.drawText(String.valueOf(numbers[i]), centerX, y[i], paints[i]);
            }

        }
    }

    private boolean rangeInDefined(float current, float min, float max) {
        return Math.max(min, current) == Math.min(current, max);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
        centerY = height / 2f;
        centerX = (float) w / 2f;
        resize();
    }

    private void resize() {
        paints = new Paint[numbers.length];
        y = new float[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            y[i] = i == 0 ? dp100 : y[i - 1] + dp100;

            if (rangeInDefined(y[i], centerY - dp50, centerY + dp50)) {
                centerIndex = i;
            }
            if (y[i] > height * 2f) {
                break;
            }
        }
        resizePaint();
    }

    private void resizePaint() {
        for (int i = 0; i < numbers.length; i++) {

            if (y[i] > height * 2)
                break;

            Paint paint = new Paint();

            if (rangeInDefined(y[i], centerY - dp50, centerY + dp50)) {
                paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
                paint.setColor(getContext().getResources().getColor(R.color.teal_200));
                paint.setTextSize(dp50);
            } else if (y[i] > centerY) {
                paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
                paint.setColor(getContext().getResources().getColor(R.color.color_ba));
                paint.setTextSize(dp50 * (4 - (i - centerIndex)) * 0.25f);
            } else if (y[i] < centerY) {
                paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
                paint.setColor(getContext().getResources().getColor(R.color.color_ba));
                paint.setTextSize(dp50 * (4 - (centerIndex - i)) * 0.25f);
            }
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.CENTER);

            paints[i] = paint;
        }
        invalidate();
    }

    boolean animAddOrCut;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            if (y[0] > centerY) {
                animAddOrCut = true;
                post(animRunnable);
            } else if (y[y.length - 1] < centerY) {
                animAddOrCut = false;
                post(animRunnable);
            }
            return true;
        }
        return detector.onTouchEvent(event);
    }

    private final Runnable animRunnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < y.length; i++) {
                if (y[i] != 0) {
                    if (animAddOrCut) {
                        y[i] -= 20;
                    } else {
                        y[i] += 20;
                    }
                }
            }
            resizePaint();
            if (y[0] > centerY || y[y.length - 1] < centerY)
                postDelayed(animRunnable, 10);
        }
    };

    public float getSelectData() {
        return numbers[centerIndex];
    }

    public interface OnSelectChangeListener {
        void onSelectChange(float data);
    }
}
