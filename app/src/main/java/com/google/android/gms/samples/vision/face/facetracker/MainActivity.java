package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.google.android.gms.samples.vision.face.facetracker.Ultis.BitmapToBase64;

public class MainActivity extends AppCompatActivity {

    private Button btnRes;
    private Button btnSync;
    private Spinner mySpin;
    private  boolean isSyncing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Glocal.NumOfSV=0;

        btnSync = (Button)findViewById(R.id.sync);
        btnRes = (Button) findViewById(R.id.regis);
        mySpin = (Spinner) findViewById(R.id.list_dir);

        ArrayList<String> list_class = getAllFiles("/CHECK_IN_DATA",true);
        list_class.add(0,"Select class to sync");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,list_class);
        mySpin.setAdapter(adapter);

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
                if (mySpin.getSelectedItemId() == 0){
                    TextView errorText = (TextView)mySpin.getSelectedView();
                    errorText.setError("");
                    errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    errorText.setText("Select class to sync");//changes the selected item text to this

                } else
                {
                    isSyncing = true;
                    String class_dir = mySpin.getSelectedItem().toString();
                    syncImage(class_dir);
                }


            }
        });

    }


    public void syncImage(String class_dir)
    {
        ArrayList<String> list_date = getAllFiles("/CHECK_IN_DATA/"+class_dir,true);

        for (int i = list_date.size() - 1; i >= 0; i--)
        {
            if (isSyncing)
            {
                String path_date = "/CHECK_IN_DATA/"+class_dir+ "/" + list_date.get(i);
                JSONObject json_data_imgs = createJSON_IMG(path_date);

                new SendDeviceDetails().execute("http://192.168.20.170:5000/syncImage", json_data_imgs.toString());
            }

        }

    }

    public JSONObject createJSON_IMG(String path_date)
    {
        JSONArray json_list_img = new JSONArray();
        ArrayList<String> list_imgs = getAllFiles(path_date,false);
        for (String img : list_imgs)
        {

            JSONObject img_json = new JSONObject();
            String path_img = Environment.getExternalStorageDirectory().toString() + path_date + "/" + img;
            Bitmap bm = BitmapFactory.decodeFile(path_img);
            String string_image = BitmapToBase64(bm);
            try {
                img_json.put("image_name",img);
                img_json.put("base64_image",string_image);
            } catch (JSONException e){
                e.printStackTrace();
            }
            json_list_img.put(img_json);

        }

        JSONObject data_json_imgs = new JSONObject();
        try {
            data_json_imgs.put("data_imgs",json_list_img);
            data_json_imgs.put("path_date",path_date);
        } catch (JSONException e){
            e.printStackTrace();
        }

        return data_json_imgs;
    }
    private class SendDeviceDetails extends AsyncTask<String, Void, String> {
        private boolean isSyncing = true;

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
//                JSONObject postJson = new JSONObject();
//                postJson.put("name", params[1]);
                wr.write(params[1].getBytes());
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
            if (result=="F")
            {
                isSyncing=false;
            }
            Log.e("RESULT_SYNC", result); // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }

    public static ArrayList<String> getAllFiles(String _path, boolean isDir)
    {
        //isDir = true: Find Folder; false: find Files
        String path = Environment.getExternalStorageDirectory().toString() + _path;
        //Log.d("Files", "Path: " + path);
        File directory = new File(path);
        FileFilter filterDirectoriesOnly = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        File[] dirs;
        ArrayList<String> list_class = new ArrayList<String>();
        if (isDir) {
            dirs = directory.listFiles(filterDirectoriesOnly);
        }
        else
        {
            dirs = directory.listFiles();
        }
        //Log.d("MNMNMN", dirs[1].getName());
        for (File dir : dirs)
        {
            list_class.add(dir.getName());
        }
        return list_class;
    }


}
