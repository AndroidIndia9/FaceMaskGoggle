package com.android.facemask.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by shashank on 26/6/17.
 */
public class GooglyFaceTracker extends Tracker<Face> {
    private static final float EYE_CLOSED_THRESHOLD = 0.4f;

    private GraphicOverlay mOverlay;
    //private GooglyEyesGraphic mEyesGraphic;
    private GoggleGraphics mGoggleGraphic;

    // Record the previously seen proportions of the landmark locations relative to the bounding box
    // of the face.  These proportions can be used to approximate where the landmarks are within the
    // face bounding box if the eye landmark is missing in a future update.
    private Map<Integer, PointF> mPreviousProportions = new HashMap<>();

    // Similarly, keep track of the previous eye open state so that it can be reused for
    // intermediate frames which lack eye landmarks and corresponding eye state.
    private boolean mPreviousIsLeftOpen = true;
    private boolean mPreviousIsRightOpen = true;
    private Bitmap mBitmap = null;


    //==============================================================================================
    // Methods
    //==============================================================================================

    public GooglyFaceTracker(Bitmap bitmap, GraphicOverlay overlay) {
        mOverlay = overlay;
        mBitmap = bitmap;
    }

    /**
     * Resets the underlying googly eyes graphic and associated physics state.
     */
    @Override
    public void onNewItem(int id, Face face) {
        //mEyesGraphic = new GooglyEyesGraphic(mOverlay);
        mGoggleGraphic = new GoggleGraphics(mBitmap, mOverlay);
    }

    /**
     * Updates the positions and state of eyes to the underlying graphic, according to the most
     * recent face detection results.  The graphic will render the eyes and simulate the motion of
     * the iris based upon these changes over time.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
//        mOverlay.add(mEyesGraphic);
        mOverlay.add(mGoggleGraphic);

        updatePreviousProportions(face);

        PointF leftPosition = getLandmarkPosition(face, Landmark.LEFT_EYE);
        PointF rightPosition = getLandmarkPosition(face, Landmark.RIGHT_EYE);
//
//        float leftOpenScore = face.getIsLeftEyeOpenProbability();
//        boolean isLeftOpen;
//        if (leftOpenScore == Face.UNCOMPUTED_PROBABILITY) {
//            isLeftOpen = mPreviousIsLeftOpen;
//        } else {
//            isLeftOpen = (leftOpenScore > EYE_CLOSED_THRESHOLD);
//            mPreviousIsLeftOpen = isLeftOpen;
//        }
//
//        float rightOpenScore = face.getIsRightEyeOpenProbability();
//        boolean isRightOpen;
//        if (rightOpenScore == Face.UNCOMPUTED_PROBABILITY) {
//            isRightOpen = mPreviousIsRightOpen;
//        } else {
//            isRightOpen = (rightOpenScore > EYE_CLOSED_THRESHOLD);
//            mPreviousIsRightOpen = isRightOpen;
//        }

//        mEyesGraphic.updateEyes(leftPosition, isLeftOpen, rightPosition, isRightOpen);
//        mEyesGraphic.updateEyes(leftPosition, true, rightPosition, true);
        mGoggleGraphic.updateEyes(leftPosition, true, rightPosition, true);
    }

    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
//        mOverlay.remove(mEyesGraphic);
        mOverlay.remove(mGoggleGraphic);
    }

    /**
     * Called when the face is assumed to be gone for good. Remove the googly eyes graphic from
     * the overlay.
     */
    @Override
    public void onDone() {
 //       mOverlay.remove(mEyesGraphic);
        mOverlay.remove(mGoggleGraphic);
    }

    //==============================================================================================
    // Private
    //==============================================================================================

    private void updatePreviousProportions(Face face) {
        for (Landmark landmark : face.getLandmarks()) {
            PointF position = landmark.getPosition();
            float xProp = (position.x - face.getPosition().x) / face.getWidth();
            float yProp = (position.y - face.getPosition().y) / face.getHeight();
            mPreviousProportions.put(landmark.getType(), new PointF(xProp, yProp));
        }
    }

    /**
     * Finds a specific landmark position, or approximates the position based on past observations
     * if it is not present.
     */
    private PointF getLandmarkPosition(Face face, int landmarkId) {
        for (Landmark landmark : face.getLandmarks()) {
            if (landmark.getType() == landmarkId) {
                return landmark.getPosition();
            }
        }

        PointF prop = mPreviousProportions.get(landmarkId);
        if (prop == null) {
            return null;
        }

        float x = face.getPosition().x + (prop.x * face.getWidth());
        float y = face.getPosition().y + (prop.y * face.getHeight());
        return new PointF(x, y);
    }
}
