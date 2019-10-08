package com.google.android.gms.samples.vision.face.facetracker;

import android.R.id;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.Spinner;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.google.android.gms.samples.vision.face.facetracker.Ultis.BitmapToBase64;

public class MainActivity extends AppCompatActivity {

    private Button btnRes;
    private Button btnSync;
    private Spinner mySpin;
    private ProgressDialog progressDialog;
    protected String class_path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create save folder
        File folder_data = new File(Environment.getExternalStorageDirectory() + File.separator + "CHECK_IN_DATA");
        if (!folder_data.exists())
        {
            folder_data.mkdir();
            Log.d("create","YES");
        }


        Global.NumOfSV=0;

        btnSync = findViewById(R.id.sync);
        btnRes = findViewById(R.id.regis);
        mySpin = findViewById(R.id.list_dir);

        final ArrayList<String> list_class = getAllFiles("/CHECK_IN_DATA",true);
        list_class.add(0,"Select class to sync");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,list_class);
        mySpin.setAdapter(adapter);

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

        btnRes.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this, EnterClassActivity.class);

                intent.setFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                startActivity(intent);

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
                    if (!isNetworkAvailable())
                    {
                        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

                        alertDialog.setMessage("Can not connect to the internet!");
                        alertDialog.setButton(Dialog.BUTTON_NEGATIVE,"OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        });

                        alertDialog.show();
                        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP,30.0f);
                        TextView textView = alertDialog.findViewById(android.R.id.message);
                        textView.setTextSize(30.0f);
                    }
                    else
                    {
                        String class_dir = mySpin.getSelectedItem().toString();
                        SyncingImages syncImg = new SyncingImages();
                        class_path = "/CHECK_IN_DATA/" + class_dir;
                        syncImg.execute(class_path);
                    }



                }


            }
        });

    }


    public ArrayList<String> getSyncLogFile(String class_path)
    {
        String path_log = Environment.getExternalStorageDirectory().toString() + class_path + "/SYNC.LOG";
        ArrayList<String> list_synced_date = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path_log));
            String line = reader.readLine();
            while (line != null)
            {
                list_synced_date.add(line);
                line=reader.readLine();
            }
            reader.close();
            return list_synced_date;


        }catch (IOException e)
        {
            e.printStackTrace();
            return list_synced_date;
        }
    }
    public ArrayList<String> getSyncDate(String class_path)
    {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> list_class_log = getSyncLogFile(class_path);
        //Log.d("MNMN_log", list_class_log.toString());
        ArrayList<String> list_all_date = getAllFiles(class_path, true);
        //Log.d("MNMN_all", list_all_date.toString());
        for (String date:list_all_date)
        {
            if (!list_class_log.contains(date))
            {
                result.add(date);
            }
        }
        return result;


    }
    public void writeSyncedDate(String path_save,ArrayList<String> list_save)
    {
        try {
            FileWriter fw = new FileWriter(path_save,true);
            BufferedWriter bw = new BufferedWriter(fw);
            //PrintWriter out = new PrintWriter(bw);

            for(String str: list_save) {
                fw.write(str+"\n");
            }
            fw.close();
            bw.close();
            //out.close();
        }catch (IOException e)
        {
            e.printStackTrace();
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
        ArrayList<String> list_class = new ArrayList<>();

        if (isDir) {
            dirs = directory.listFiles(filterDirectoriesOnly);
        }
        else
        {
            dirs = directory.listFiles();
        }
        try {
            //Log.d("MNMNMN", dirs[1].getName());
            for (File dir : dirs)
            {
                list_class.add(dir.getName());
            }
        }catch (NullPointerException e)
        {
            e.printStackTrace();
        }

        Collections.sort(list_class, new Comparator<String>() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");
            @Override
            public int compare(String d1, String d2) {
                try {
                    return dateFormat.parse(d1).compareTo(dateFormat.parse(d2));
                } catch (ParseException e)
                {
                    e.printStackTrace();
                    return 0;
                }

            }
        });
        return list_class;
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

    public class SyncingImages extends AsyncTask<String, Void, String> {
        ArrayList<String> list_sync_date;
        @Override
        protected String doInBackground(String... params) {
            list_sync_date = getSyncDate(params[0]);
            Log.d("ListSyncDate", list_sync_date.toString());
            String result= "F";
            for (String date: list_sync_date)
            {
                JSONObject json_data_imgs = createJSON_IMG(params[0]+"/"+date);
                result = sendJSON2("http://192.168.20.170:5000/syncImage",json_data_imgs.toString());
            }



            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Syncing...");
            progressDialog.show();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            writeSyncedDate(Environment.getExternalStorageDirectory().toString() + class_path+"/SYNC.LOG",list_sync_date);
            final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

            alertDialog.setMessage("Sync Done!");
            alertDialog.setButton(Dialog.BUTTON_NEGATIVE,"OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });

            alertDialog.show();
            alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setTextSize(TypedValue.COMPLEX_UNIT_SP,30.0f);
            TextView textView = alertDialog.findViewById(android.R.id.message);
            textView.setTextSize(30.0f);

            // this is expecting a response code to be sent from your server upon receiving the POST data
        }
    }

    public String sendJSON2(String api_server, String json_data)
    {
        String data = "";

        HttpURLConnection httpURLConnection = null;
        try {

            httpURLConnection = (HttpURLConnection) new URL(api_server).openConnection();
            httpURLConnection.setRequestMethod("POST");

            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());

            wr.write(json_data.getBytes());
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
