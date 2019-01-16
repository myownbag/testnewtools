package com.example.testnewtools.myview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.testnewtools.R;


public class Procseedlg extends Dialog {

    private ProgressBar mProgressBar;
    private TextView infotext;
    private ImageView imageView;
    private Button mbtn_know;

    public Procseedlg(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_step_dlg_layout);
        mProgressBar=findViewById(R.id.dlg_progressBar);
        infotext =findViewById(R.id.update_info);
        imageView =findViewById(R.id.image_update_result);
        mbtn_know = findViewById(R.id.but_updateresult);
        initclickliterner();
//        Log.d("zl","mProgressBar / infotext / imageView: "+mProgressBar+" / "+infotext+" / "+imageView);
    }
    public void setCurProcess(int step)
    {
        if(mProgressBar!=null)
        {
            mProgressBar.setProgress(step);
        }
    }

    private void initclickliterner()
    {
        mbtn_know.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
    public void  dismiss(String info)
    {
        infotext.setText(info);
        CountDownTimer countDownTimer =new CountDownTimer(3000,1500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                dismiss();
            }
        };
        countDownTimer.start();
    }

    public void show(String s) {
        show();
        infotext.setText(s);
        imageView.setImageResource(R.drawable.chuckle);
        mbtn_know.setVisibility(View.GONE);
    }
    public void showresult(String s,int res,boolean isshow)
    {

        infotext.setText(s);
        imageView.setImageResource(res);
        if(isshow)
            mbtn_know.setVisibility(View.VISIBLE);
        else
            mbtn_know.setVisibility(View.GONE);
        show();
    }
    public void  dismiss(String info,int res)
    {
        infotext.setText(info);
        imageView.setImageResource(res);
        CountDownTimer countDownTimer =new CountDownTimer(3000,1500) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                dismiss();
            }
        };
        countDownTimer.start();
    }

}
