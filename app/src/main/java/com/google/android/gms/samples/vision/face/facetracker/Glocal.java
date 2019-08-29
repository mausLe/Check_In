package com.google.android.gms.samples.vision.face.facetracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.face.Face;

public final class Glocal {
    public static Bitmap CurrentFrame;
    public static Face CurrentFace;

    public static Context ApplicationContext;
    public static Boolean inProgress=true;
    public static ImageView viewresult;
    public static ImageView status;
    public static Bitmap ProgressFrame;
    public static Face ProgressFace;
    public static String ClassID="0";
    public static int[] coordEllip = {150,40,450,440}; //left,top,right,bottom
    @SuppressLint("StaticFieldLeak")
    public static TextView Msg;

}
