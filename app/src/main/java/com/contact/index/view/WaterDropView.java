package com.contact.index.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * author:feng.G
 * time:  2019-05-29 15:44
 * desc:
 */
public class WaterDropView extends View {
    private Paint mPaint;

    public WaterDropView(Context context) {
        super(context);
    }

    public WaterDropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Path path = new Path();
        path.moveTo(200, 700);
        path.quadTo(50, 900, 200, 910);
        path.quadTo(350, 900, 200, 700);
        canvas.drawPath(path, mPaint);
    }
}
