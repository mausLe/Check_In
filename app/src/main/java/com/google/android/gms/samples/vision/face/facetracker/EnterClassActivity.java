package com.google.android.gms.samples.vision.face.facetracker;

import android.R.layout;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import java.util.ArrayList;

public class EnterClassActivity extends AppCompatActivity{
    private AutoCompleteTextView mEdit;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Global.ApplicationContext = this;

        //get all folder
        ArrayList<String> list_class = MainActivity.getAllFiles("/CHECK_IN_DATA",true);

        //edit enter class
        mEdit = findViewById(R.id.editTextID);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, layout.simple_expandable_list_item_1, list_class);
        mEdit.setAdapter(adapter);

        btn = findViewById(R.id.buttonOK);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(validateFields()){
                    Global.ClassID = mEdit.getText().toString();
                    Intent intent = new Intent(EnterClassActivity.this, FaceTrackerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public boolean validateFields() {
        int yourDesiredLength = 3;
        if (mEdit.getText().length() < yourDesiredLength) {
            mEdit.setError("Your Input is Invalid");
            return false;
        } else {
            return true;
        }
    }


    public void onDestroy() {

        super.onDestroy();

    }

}

