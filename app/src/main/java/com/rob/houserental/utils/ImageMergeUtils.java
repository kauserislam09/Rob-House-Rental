package com.rob.houserental.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.io.File;
import java.io.FileOutputStream;

public class ImageMergeUtils {

    /**
     * Merges two images (Front & Back of NID) vertically with clean headers and borders into a single composite JPEG file.
     */
    public static File mergeNidImages(Context context, String frontImagePath, String backImagePath) throws Exception {
        Bitmap frontBitmap = BitmapFactory.decodeFile(frontImagePath);
        Bitmap backBitmap = BitmapFactory.decodeFile(backImagePath);

        if (frontBitmap == null || backBitmap == null) {
            throw new IllegalArgumentException("Failed to decode NID front or back image");
        }

        int targetWidth = 1200;
        float scaleFront = (float) targetWidth / frontBitmap.getWidth();
        int heightFront = Math.round(frontBitmap.getHeight() * scaleFront);

        float scaleBack = (float) targetWidth / backBitmap.getWidth();
        int heightBack = Math.round(backBitmap.getHeight() * scaleBack);

        int headerHeight = 60;
        int margin = 30;
        int totalWidth = targetWidth + (margin * 2);
        int totalHeight = margin + headerHeight + heightFront + margin + headerHeight + heightBack + margin;

        Bitmap compositeBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(compositeBitmap);

        // White background
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setFilterBitmap(true);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#1E293B"));
        textPaint.setTextSize(32);
        textPaint.setFakeBoldText(true);

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.parseColor("#CBD5E1"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);

        // Draw Front Side Header & Image
        int currentY = margin;
        canvas.drawText("NID FRONT SIDE", margin, currentY + 38, textPaint);
        currentY += headerHeight;

        Rect srcFront = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
        Rect dstFront = new Rect(margin, currentY, margin + targetWidth, currentY + heightFront);
        canvas.drawBitmap(frontBitmap, srcFront, dstFront, paint);
        canvas.drawRect(dstFront, borderPaint);
        currentY += heightFront + margin;

        // Draw Back Side Header & Image
        canvas.drawText("NID BACK SIDE", margin, currentY + 38, textPaint);
        currentY += headerHeight;

        Rect srcBack = new Rect(0, 0, backBitmap.getWidth(), backBitmap.getHeight());
        Rect dstBack = new Rect(margin, currentY, margin + targetWidth, currentY + heightBack);
        canvas.drawBitmap(backBitmap, srcBack, dstBack, paint);
        canvas.drawRect(dstBack, borderPaint);

        // Recycle source bitmaps
        frontBitmap.recycle();
        backBitmap.recycle();

        // Save merged composite image to internal documents directory
        File docsDir = new File(context.getFilesDir(), "documents");
        if (!docsDir.exists()) {
            docsDir.mkdirs();
        }

        File outFile = new File(docsDir, "nid_merged_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(outFile);
        compositeBitmap.compress(Bitmap.CompressFormat.JPEG, 92, fos);
        fos.flush();
        fos.close();
        compositeBitmap.recycle();

        return outFile;
    }
}
