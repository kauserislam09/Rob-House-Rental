package com.rob.houserental.financial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class IncomeExpenseComparisonView extends View {

    private double expectedRent = 0;
    private double collectedRent = 0;
    private double expenses = 0;
    private double netIncome = 0;

    private Paint paint;
    private Paint textPaint;

    private int expectedColor = Color.parseColor("#9E9E9E");
    private int incomeColor = Color.parseColor("#2E7D32");
    private int expenseColor = Color.parseColor("#C62828");
    private int netColor = Color.parseColor("#1565C0");

    public IncomeExpenseComparisonView(Context context) {
        super(context);
        init();
    }

    public IncomeExpenseComparisonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IncomeExpenseComparisonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(32f);
        textPaint.setColor(Color.DKGRAY);
    }

    public void setComparisonData(double expected, double collected, double expenses, double net) {
        this.expectedRent = expected;
        this.collectedRent = collected;
        this.expenses = expenses;
        this.netIncome = net;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        double maxVal = Math.max(expectedRent, Math.max(collectedRent, Math.max(expenses, Math.abs(netIncome))));
        if (maxVal <= 0) maxVal = 100.0;

        float rowHeight = height / 4f;
        float barMaxW = width - 200f;

        // Row 1: Expected
        drawRow(canvas, 0, (float) (expectedRent / maxVal), expectedColor, "Expected");
        // Row 2: Collected
        drawRow(canvas, 1, (float) (collectedRent / maxVal), incomeColor, "Collected");
        // Row 3: Expenses
        drawRow(canvas, 2, (float) (expenses / maxVal), expenseColor, "Expenses");
        // Row 4: Net
        drawRow(canvas, 3, (float) (Math.max(0, netIncome) / maxVal), netColor, "Net");
    }

    private void drawRow(Canvas canvas, int rowIndex, float ratio, int color, String label) {
        float h = getHeight() / 4f;
        float top = rowIndex * h + 10f;
        float bottom = (rowIndex + 1) * h - 10f;
        float barWidth = (getWidth() - 100f) * Math.min(1.0f, Math.max(0.05f, ratio));

        paint.setColor(color);
        RectF rect = new RectF(10f, top, barWidth, bottom);
        canvas.drawRoundRect(rect, 8f, 8f, paint);
    }
}
