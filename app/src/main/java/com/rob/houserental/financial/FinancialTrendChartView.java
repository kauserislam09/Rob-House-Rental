package com.rob.houserental.financial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FinancialTrendChartView extends View {

    private final List<MonthlyFinancialTrend> trendData = new ArrayList<>();

    private Paint barPaint;
    private Paint textPaint;
    private Paint gridPaint;

    private int incomeColor = Color.parseColor("#2E7D32"); // Green
    private int expenseColor = Color.parseColor("#C62828"); // Red
    private int netColor = Color.parseColor("#1565C0"); // Blue

    public FinancialTrendChartView(Context context) {
        super(context);
        init();
    }

    public FinancialTrendChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FinancialTrendChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(28f);
        textPaint.setColor(Color.GRAY);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(Color.parseColor("#E0E0E0"));
        gridPaint.setStrokeWidth(2f);
    }

    public void setTrendData(List<MonthlyFinancialTrend> data) {
        trendData.clear();
        if (data != null) {
            trendData.addAll(data);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (trendData.isEmpty()) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int paddingBottom = 60;
        int paddingTop = 40;
        int paddingLeft = 40;
        int paddingRight = 40;

        int chartHeight = height - paddingTop - paddingBottom;
        int chartWidth = width - paddingLeft - paddingRight;

        double maxVal = 0.0;
        for (MonthlyFinancialTrend t : trendData) {
            maxVal = Math.max(maxVal, t.getCollectedRent());
            maxVal = Math.max(maxVal, t.getExpenses());
            maxVal = Math.max(maxVal, Math.abs(t.getNetIncome()));
        }
        if (maxVal <= 0) maxVal = 1000.0;

        int groupCount = trendData.size();
        float groupWidth = (float) chartWidth / groupCount;
        float barWidth = groupWidth / 4f;

        for (int i = 0; i < groupCount; i++) {
            MonthlyFinancialTrend item = trendData.get(i);
            float groupLeft = paddingLeft + i * groupWidth;

            // Income bar
            float incomeHeight = (float) ((item.getCollectedRent() / maxVal) * chartHeight);
            RectF incomeRect = new RectF(groupLeft + 4, height - paddingBottom - incomeHeight, groupLeft + barWidth, height - paddingBottom);
            barPaint.setColor(incomeColor);
            canvas.drawRoundRect(incomeRect, 6, 6, barPaint);

            // Expense bar
            float expenseHeight = (float) ((item.getExpenses() / maxVal) * chartHeight);
            RectF expenseRect = new RectF(groupLeft + barWidth + 8, height - paddingBottom - expenseHeight, groupLeft + barWidth * 2 + 4, height - paddingBottom);
            barPaint.setColor(expenseColor);
            canvas.drawRoundRect(expenseRect, 6, 6, barPaint);

            // Net Income bar
            float netHeight = (float) ((Math.max(0, item.getNetIncome()) / maxVal) * chartHeight);
            RectF netRect = new RectF(groupLeft + barWidth * 2 + 12, height - paddingBottom - netHeight, groupLeft + barWidth * 3 + 8, height - paddingBottom);
            barPaint.setColor(netColor);
            canvas.drawRoundRect(netRect, 6, 6, barPaint);

            // Label (Month)
            String label = item.getMonth() != null && item.getMonth().length() >= 7 ? item.getMonth().substring(5) : "";
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(label, groupLeft + groupWidth / 2f, height - 15, textPaint);
        }

        // Draw baseline
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, gridPaint);
    }
}
