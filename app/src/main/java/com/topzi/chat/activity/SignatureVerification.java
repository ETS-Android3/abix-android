package com.topzi.chat.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.topzi.chat.R;
import com.topzi.chat.sLock.DrawingView;
import com.topzi.chat.sLock.GestureChecker;
import com.topzi.chat.sLock.Point;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SignatureVerification extends AppCompatActivity {

    private int step;
    private ArrayList<Point> gesture1, gesture2;
    private DrawingView gview;
    private TextView tip;
    private Button btnOk;
    private boolean wait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature_verification);

        step = 1;
        gview = (DrawingView)findViewById(R.id.sgview);
        tip = (TextView)findViewById(R.id.tip);
        btnOk = (Button) findViewById(R.id.btnOk);

        wait = false;

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SignatureVerification.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (gview.isTouch_up() && !wait) {
                            wait = true;
                            if (step == 1) {
                                gesture1 = gview.getGesture();
                                tip.setText("Draw your gesture again");
                                step++;
                                gview.clearDraw();
                                gview.reset();
                                btnOk.setText("Confirm");
                            } else if (step == 2) {
                                gesture2 = gview.getGesture();
                                if (GestureChecker.check(gesture1, gesture2)) {
                                    tip.setText("");
                                    step = 1;
                                    if (save()) {
                                        Toast.makeText(getApplicationContext(), "Gesture changed!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                } else {
                                    tip.setText("Draw your gesture");
                                    step = 1;
                                    Toast.makeText(getApplicationContext(), "Gestures don't match", Toast.LENGTH_SHORT).show();
                                }
                                gview.reset();
                            }
                            wait = false;
                        }
                    }
                });
            }
        }, 0, 300);

    }

    private boolean save(){
        try {
            FileOutputStream fos = getApplicationContext().openFileOutput("SLCKNDFLP", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(gesture1);
            os.close();
            return true;
        }catch (IOException e){
            Toast.makeText(getApplicationContext(),"An error occurred!",Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}