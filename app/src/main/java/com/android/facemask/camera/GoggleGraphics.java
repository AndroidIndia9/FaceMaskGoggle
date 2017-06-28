package com.android.facemask.camera;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Log;

import com.android.facemask.R;

import java.util.logging.Logger;

/**
 * Created by shashank on 26/6/17.
 */
public class GoggleGraphics extends GraphicOverlay.Graphic {
    private static final String TAG = "GoggleGraphics";
    private volatile PointF mLeftPosition;
    private volatile boolean mLeftOpen;

    private volatile PointF mRightPosition;
    private volatile boolean mRightOpen;

    private Bitmap mGoggleBitmap = null;

    public GoggleGraphics(Bitmap bitmap, GraphicOverlay overlay) {
        super(overlay);
        mGoggleBitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        PointF detectLeftPosition = mLeftPosition;
        PointF detectRightPosition = mRightPosition;
        if ((detectLeftPosition == null) || (detectRightPosition == null) || (mGoggleBitmap == null)) {
            return;
        }

        PointF leftPosition =
                new PointF(translateX(detectLeftPosition.x), translateY(detectLeftPosition.y));
        PointF rightPosition =
                new PointF(translateX(detectRightPosition.x), translateY(detectRightPosition.y));

        // Use the inter-eye distance to set the size of the eyes.
        float distance = (float) Math.sqrt(
                Math.pow(rightPosition.x - leftPosition.x, 2) +
                        Math.pow(rightPosition.y - leftPosition.y, 2));
        Log.d(TAG, "distance " + distance);
        int newWidth = (int)(distance * 2.5);
        Bitmap bitmap = null;
        int newHeight = (int)((mGoggleBitmap.getHeight() * (newWidth)) / (float) mGoggleBitmap.getWidth());
        bitmap = Bitmap.createScaledBitmap(mGoggleBitmap, newWidth, newHeight, true);
        Log.d(TAG, "Rendered Image Bitmap Size " + newWidth + " , " + newHeight);

        int left = bitmap.getWidth() / 3;

//
//        int remainingDistance = (int) (distance - mGoggleBitmap.getWidth());
//        Bitmap bitmap = null;
//        if(remainingDistance > 0){
//            int width = mGoggleBitmap.getWidth() + 2 * remainingDistance;
//            int height = (int)((mGoggleBitmap.getHeight() * (width)) / (float) mGoggleBitmap.getWidth());
//            bitmap = Bitmap.createScaledBitmap(mGoggleBitmap, width, height, true);
//        }else{
//            int width = mGoggleBitmap.getWidth() - 2 * remainingDistance;
//            int height = (int)((mGoggleBitmap.getHeight() * (width)) / (float) mGoggleBitmap.getWidth());
//            bitmap = Bitmap.createScaledBitmap(mGoggleBitmap, width, height, true);
//        }
        canvas.drawBitmap(bitmap, leftPosition.x - left, leftPosition.y - bitmap.getHeight() / 2, null);
    }

    void updateEyes(PointF leftPosition, boolean leftOpen,
                    PointF rightPosition, boolean rightOpen) {
        mLeftPosition = leftPosition;
        mLeftOpen = leftOpen;

        mRightPosition = rightPosition;
        mRightOpen = rightOpen;

        postInvalidate();
    }

}
