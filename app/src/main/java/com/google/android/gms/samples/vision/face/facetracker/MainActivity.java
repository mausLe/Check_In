package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.google.android.gms.samples.vision.face.facetracker.Ultis.BitmapToBase64;

public class MainActivity extends AppCompatActivity {

    private Button btnRes;
    private Button btnSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSync = (Button)findViewById(R.id.sync);
        btnRes = (Button)findViewById(R.id.regis);

        btnRes.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        btnSync.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                syncImage();

            }
        });
    }


    public void syncImage()
    {
        String path = Environment.getExternalStorageDirectory().toString() + "/IMAGE_DATA";
        //Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        //Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            //Log.d("Files", "FileName:" + files[i].getPath());
            Bitmap bm = BitmapFactory.decodeFile(files[i].getPath());
            String base64_image = BitmapToBase64(bm);

            JSONObject postData = new JSONObject();
            try {
                postData.put("image_name", files[i].getName());
                postData.put("base64_image", base64_image);

                Log.d("AAAA", postData.toString());
                new SendDeviceDetails().execute("http://192.168.20.170:5000/syncImage", postData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }


    private class SendDeviceDetails extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String data = "";

            HttpURLConnection httpURLConnection = null;
            try {

                httpURLConnection = (HttpURLConnection) new URL(params[0]).openConnection();
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setDoOutput(true);

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                JSONObject postJson = new JSONObject();
                postJson.put("name", params[1]);
                wr.write(postJson.toString().getBytes());
                wr.flush();
                wr.close();

                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);

                int inputStreamData = inputStreamReader.read();
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.e("TAG", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }



}
