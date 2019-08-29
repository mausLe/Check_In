/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.facetracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.android.gms.samples.vision.face.facetracker.Ultis.GetFaceFromProgressFrame;

/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
        Color.BLUE,
        Color.CYAN,
        Color.GREEN,
        Color.MAGENTA,
        Color.RED,
        Color.WHITE,
        Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;


    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(Color.RED);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();

    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    private static final int cx = (Glocal.coordEllip[0] + Glocal.coordEllip[2])/2; // (left + right)/2
    private static final int cy = 30 + (Glocal.coordEllip[1] + Glocal.coordEllip[3])/2; //(top + bot)/2
    private static final int radius = 20;
//    private Intent intent = new Intent(Glocal.ApplicationContext, FaceTrackerActivity.class);

    private double dis(float x, float y){
        return Math.sqrt((x-cx)*(x-cx)+(y-cy)*(y-cy));
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            Glocal.Msg.setText("Wait for face");
            return;
        }
        Glocal.CurrentFace=face;
        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getPosition().x + face.getWidth() / 2);
        float y = translateY(face.getPosition().y + face.getHeight() / 2);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        mBoxPaint.setColor(Color.GREEN);
//        canvas.drawCircle(x,y,2,mBoxPaint);
//        canvas.drawCircle(cx,cy,2,mBoxPaint);
//        canvas.drawRect(left, top, right, bottom, mBoxPaint);
        Glocal.ProgressFace=Glocal.CurrentFace;
        Glocal.ProgressFrame=Glocal.CurrentFrame;

        if (dis(x,y)>radius){
            Glocal.Msg.setText("Fit your face in box");
            return;
        } else if (!(face.getIsLeftEyeOpenProbability()>0.8 && face.getIsRightEyeOpenProbability()>0.8)){
            Glocal.Msg.setText("Open your eyes");
            return;
        } else if (xOffset < (cx - Glocal.coordEllip[0])){
            Glocal.Msg.setText("Too small face");
            return;
        } else if (Glocal.inProgress && dis(x,y)<radius) {
            Glocal.inProgress=false;
            Bitmap bmp = GetFaceFromProgressFrame();
//            Glocal.CurrentImage = GetFaceFromProgressFrame();

            try {

                savebitmap(bmp);

                Glocal.viewresult.setImageBitmap(bmp);
                Glocal.status.setImageResource(R.drawable.confirm);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return;

        }

    }

    public static void savebitmap(@NonNull Bitmap bmp) throws IOException {
        Glocal.inProgress=false;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File folder_gui = new File(Environment.getExternalStorageDirectory() + File.separator + "IMAGE_DATA");
        if (!folder_gui.exists())
        {
            folder_gui.mkdir();
        }

        SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss_");

        Date date = new Date();

        File outputFile = new File(folder_gui,formatter.format(date) + Glocal.ClassID + ".png");

        FileOutputStream fo = new FileOutputStream(outputFile);
        fo.write(bytes.toByteArray());
        fo.close();

        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_SUP_PIP,150);

        Intent intent = new Intent(Glocal.ApplicationContext, FaceTrackerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Glocal.ApplicationContext.startActivity(intent);

    }

}


