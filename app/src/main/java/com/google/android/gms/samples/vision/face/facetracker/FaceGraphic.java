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
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;

import android.media.ToneGenerator;

import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;

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
    private static final float ID_TEXT_SIZE = 40.0f;
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
    private static final int cx = (Global.coordEllip[0] + Global.coordEllip[2])/2; // (left + right)/2
    private static final int cy = (Global.coordEllip[1] + Global.coordEllip[3])/2; //(top + bot)/2
    private static final int radius = 40;
//    private Intent intent = new Intent(Global.ApplicationContext, FaceTrackerActivity.class);

    private double dis(float x, float y){
        return Math.sqrt((x-cx)*(x-cx)+(y-cy)*(y-cy));
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            Global.Msg.setText("Wait for face");
            return;
        }

        Global.CurrentFace=face;
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

        Global.ProgressFace= Global.CurrentFace;
        Global.ProgressFrame= Global.CurrentFrame;

        if (dis(x,y)>radius){
            Global.Msg.setText("Fit your face in box");
            return;
        } else if (!(face.getIsLeftEyeOpenProbability()>0.7 && face.getIsRightEyeOpenProbability()>0.7)){
            Global.Msg.setText("Open your eyes");
            return;
        } else if (xOffset < (cx - Global.coordEllip[0]-30)) {
            Global.Msg.setText("Too small face");
            return;
        } else if (xOffset > (cx - Global.coordEllip[0]+70)) {
            Global.Msg.setText("Too large face");
            return;
        } else if (Global.inProgress && dis(x,y)<radius) {
            Global.inProgress=false;
            Bitmap bmp = GetFaceFromProgressFrame();

            try {

                savebitmap(bmp);
                Global.NumOfSV +=1;

                Global.numofsv.setText("Count: " + Global.NumOfSV.toString());
                Global.viewresult.setImageBitmap(bmp);
                Global.status.setImageResource(R.drawable.confirm);
//
//                Intent intent = new Intent(Global.ApplicationContext, FaceTrackerActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                Global.ApplicationContext.startActivity(intent);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        Intent intent = new Intent(Global.ApplicationContext, FaceTrackerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        Global.ApplicationContext.startActivity(intent);
                    }
                }, 1500);




            } catch (IOException e) {
                e.printStackTrace();
            }

            return;

        }

    }

    public static void savebitmap(@NonNull Bitmap bmp) throws IOException {
        Global.inProgress=false;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        //Create root folder
        File folder_data = new File(Environment.getExternalStorageDirectory() + File.separator + "CHECK_IN_DATA");

        //Create class folder
        File folder_class = new File(folder_data, Global.ClassID.toUpperCase());
        if (!folder_class.exists())
        {
            folder_class.mkdir();
            File logfile = new File(folder_class,"SYNC.LOG");
            logfile.createNewFile();
        }

        //Create date folder
        Date date = new Date();
        SimpleDateFormat date_format = new SimpleDateFormat("dd_MM_yyyy");
        File folder_date = new File(folder_class,date_format.format(date));
        if (!folder_date.exists())
        {
            folder_date.mkdir();
        }


        SimpleDateFormat time_format = new SimpleDateFormat("HH_mm_ss");

        File outputFile = new File(folder_date,time_format.format(date) + ".jpg");

        FileOutputStream fo = new FileOutputStream(outputFile);
        fo.write(bytes.toByteArray());
        fo.close();

        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        toneGen1.startTone(ToneGenerator.TONE_SUP_PIP,150);



    }

}


