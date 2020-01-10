package com.example.testnewtools.myview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.testnewtools.MainActivity;
import com.example.testnewtools.R;
import com.example.testnewtools.utils.ToastUtils;


public class Procseedlg extends Dialog {

    private ProgressBar mProgressBar;
    private TextView infotext;
    private ImageView imageView;
    private Button mbtn_know;
    long exitTime = 0;

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

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
            boolean resultvalue = false;

        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

            if ((System.currentTimeMillis() - exitTime) > 2000) {
                ToastUtils.showToast(MainActivity.getInstance(), "再按一次退出升级");
                exitTime = System.currentTimeMillis();
                resultvalue = true;
            } else {
//                Intent intent = new Intent(Intent.ACTION_MAIN);
//                intent.addCategory(Intent.CATEGORY_HOME);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
                dismiss();
                resultvalue =false;
            }
            return resultvalue;
        }
        return super.dispatchKeyEvent(event);
    }
}
