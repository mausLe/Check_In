package com.google.android.gms.samples.vision.face.facetracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;
import android.util.Log;
import android.widget.ScrollView;

import com.google.android.gms.vision.face.Face;

import java.io.ByteArrayOutputStream;

public final class Ultis {
    public static String BitmapToBase64(Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }
    public static Bitmap Base64ToBitmap(String encodedImage){
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
    public static Bitmap GetFaceFromProgressFrame(){
        return GetFaceFromFrameAndFace(Glocal.ProgressFrame,Glocal.ProgressFace);
    }
    public static Bitmap GetFaceFromCurrentFrame(){
        return GetFaceFromFrameAndFace(Glocal.CurrentFrame,Glocal.CurrentFace);
    }
    public static Bitmap GetFaceFromFrameAndFace(Bitmap bitmap,Face face){
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        Bitmap Src = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        //end rotate

        int X,Y,Width,Height;

        X=(int) face.getPosition().x;
        if (X<0) X=0;
        if (X>Src.getWidth()) X=Src.getWidth();
        Width = (int) face.getWidth();
        if (X+Width>Src.getWidth()) Width=Src.getWidth()-X;

        Y=(int) face.getPosition().y;
        if (Y<0) Y=0;
        if (Y>Src.getHeight()) Y=Src.getHeight();
        Height = (int) face.getHeight();
        if (Y+Height>Src.getHeight()) Height=Src.getHeight()-Y;

        return Bitmap.createBitmap(Src,X,Y, Width, Height);
    }

}
