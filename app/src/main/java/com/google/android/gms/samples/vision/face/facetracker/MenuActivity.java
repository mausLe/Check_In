package com.google.android.gms.samples.vision.face.facetracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MenuActivity extends AppCompatActivity{
    private EditText mEdit;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Glocal.ApplicationContext = this;
        mEdit = (EditText)findViewById(R.id.editTextID);



        btn = (Button)findViewById(R.id.buttonOK);
        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(validateFields()){
                    Glocal.ClassID = mEdit.getText().toString();
                    Intent intent = new Intent(Glocal.ApplicationContext, FaceTrackerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Glocal.ApplicationContext.startActivity(intent);
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


    public void actionOK()
    {
        return;
    }

}

