package com.example.testnewtools.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testnewtools.MainActivity;
import com.example.testnewtools.R;
import com.example.testnewtools.activitys.NbServiceAddrInputActivity;
import com.example.testnewtools.utils.CodeFormat;
import com.example.testnewtools.utils.Constants;
import com.example.testnewtools.utils.DigitalTrans;
import com.example.testnewtools.utils.ToastUtils;
import com.google.gson.Gson;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nbregisterdetaildata.NbOnenetRegRequest;
import nbregisterdetaildata.RegisterName;

public class NBRegisiterfragment extends BaseFragment {
    public  View mView;
    public ImageView mImageView;
    public TextView maddrview;
    public TextView mimeiview;
    public TextView mresultview;
    private SharedPreferences sp ;
    private String addrurl;
    private Button mbutsend;

    private String mSDevcieSn;
    private String mSdeviceId;
    private String mImei;
    private String mDeviceType;
    private String mImsi = "1234567890";

    private int mAtmpindex=0;

    private int mIndexCMD;
    byte sendbufread[]={(byte) 0xFD, 0x00 ,0x00 ,0x0D ,        0x00 ,0x19 ,0x00 ,        0x00 ,0x00 ,0x00
            ,0x00 ,0x00 ,0x00 ,0x00 , (byte) 0xD9 ,0x00 ,0x0C , (byte) 0xA0};

    byte[][] mComd=new byte[4][sendbufread.length];

    //DMU IMEI 读取指令
    byte[] mDmuCmd = {0x00 ,(byte)0xE0 ,0x00 ,0x00 ,0x00 ,0x01 ,0x40 ,0x0D};


    //倒计时

    CountDownTimer mytimer= new CountDownTimer(3000, 1000) {
        @SuppressLint("NewApi")
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @SuppressLint("NewApi")
        @Override
        public void onFinish() {
            mImsi = "1234567890";
            if(mDeviceType.equals("DMU"))
            {
                String readOutMsg = DigitalTrans.byte2hex(mDmuCmd);
                mIndexCMD = 4;
                verycutstatus(readOutMsg,3000);
            }
//            register2onenet();
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mIsatart=false;
        if (mView != null) {
            // 防止多次new出片段对象，造成图片错乱问题
            return mView;
        }
        mView=inflater.inflate(R.layout.nb_registger_fragment,null);
        initview();

        return mView;
    }

    private void initview() {

        mImageView=mView.findViewById(R.id.nb_img_set_addr);
        maddrview=mView.findViewById(R.id.nb_add_info);
        mbutsend=mView.findViewById(R.id.nb_but1);
        mimeiview=mView.findViewById(R.id.nb_imei_info);
        mresultview=mView.findViewById(R.id.nb_result_info);
        sp= MainActivity.getInstance().getSharedPreferences("User", Context.MODE_PRIVATE);

        initdata();

    }

    private void initdata() {
        //String addr;
        addrurl=sp.getString(Constants.NB_SERVICE_KEY,"");
        maddrview.setText(addrurl);
        mImageView.setOnClickListener(new OnClicklisternerIMPL());
        mbutsend.setOnClickListener(new OnClicklisternerIMPL());
        //url=addr+Constants.NB_Service_END;
        initcmd();
        mIndexCMD=0;

    }

    private void initcmd() {
        ByteBuffer buf1;
        for(int i=0;i<4;i++)
        {
            buf1=ByteBuffer.allocateDirect(sendbufread.length);
            buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
            buf1.put(sendbufread);
            buf1.rewind();
            buf1.get(mComd[i]);
        }

        buf1=ByteBuffer.allocateDirect(4);
        buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
        buf1.putInt(5);
        buf1.rewind();
        buf1.get(mComd[0],14,2);
        CodeFormat.crcencode(mComd[0]);


        buf1=ByteBuffer.allocateDirect(4);
        buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
        buf1.putInt(103);
        buf1.rewind();
        buf1.get(mComd[1],14,2);
        CodeFormat.crcencode(mComd[1]);

        buf1=ByteBuffer.allocateDirect(4);
        buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
        buf1.putInt(2);
        buf1.rewind();
        buf1.get(mComd[2],14,2);
        CodeFormat.crcencode(mComd[2]);

        buf1=ByteBuffer.allocateDirect(4);
        buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
        buf1.putInt(6);
        buf1.rewind();
        buf1.get(mComd[3],14,2);
        CodeFormat.crcencode(mComd[3]);
    }

    @Override
    public void OndataCometoParse(String readOutMsg1, byte[] readOutBuf1) {
//        MainActivity.getInstance().mDialog.dismiss();
        Log.d("zl", "OndataCometoParse: "+CodeFormat.byteToHex(readOutBuf1,readOutBuf1.length));
        int i=0;
        if(!mIsatart)
        {
            return;
        }
        if(readOutBuf1.length<5)
        {
            ToastUtils.showToast(getActivity(), "数据长度短");
            return;
        }
//        else
//        {
//            if(readOutBuf1[3]!=(readOutBuf1.length-5))
//            {
//                ToastUtils.showToast(getActivity(), "数据长度异常");
//                return;
//            }
//        }
        String str="";
        if(mIndexCMD==0)
        {
            byte[] buf=new byte[15];
            ByteBuffer buf1;
            buf1=ByteBuffer.allocateDirect(15);
            buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
            buf1.put(readOutBuf1,30,15);
            buf1.rewind();
            buf1.get( buf);

            for(i=0;i<15;i++)
            {
                if(buf[i]>=0x30&&buf[i]<=0x39)
                {
                    str+=(char)buf[i];
                }
                else
                {
                    mAtmpindex++;
                    if(mAtmpindex<3)
                    {
                        String readOutMsg = DigitalTrans.byte2hex(mComd[mIndexCMD]);
                        verycutstatus(readOutMsg);
                     //   Log.d("zl", "onClick: "+CodeFormat.byteToHex(mComd[mIndexCMD],mComd[mIndexCMD].length));
                    }
                    else
                    {
                       MainActivity.getInstance().mDialog.dismiss();
                       Toast.makeText(getActivity(),"获取IMEI失败",Toast.LENGTH_SHORT).show();
                        mresultview.setText("获取IMEI失败");
                        mresultview.setTextColor(getResources().getColor(R.color.color_warning));
                    }
                    return;
                }
                mImei=str;
                mimeiview.setText(mImei);
            }
          //  Log.d("zl","mImei:"+mImei);
        }
        else  if(mIndexCMD==1)
        {
            str="";
            byte[] bufsn=new byte[8];
            ByteBuffer buf1;
            buf1=ByteBuffer.allocateDirect(8);
            buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
            buf1.put(readOutBuf1,16,8);
            buf1.rewind();
            buf1.get( bufsn);
            for(i=0;i<8;i++)
            {
                str+= (char) bufsn[i];
            }
            mSDevcieSn=str;
         //   Log.d("zl","mSDevcieSn:"+mSDevcieSn);
        }
        else if(mIndexCMD==2)
        {
            str="";
            byte[] bufsn=new byte[40];
            ByteBuffer buf1;
            buf1=ByteBuffer.allocateDirect(40);
            buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
            buf1.put(readOutBuf1,16,40);
            buf1.rewind();
            buf1.get( bufsn);
            for(i=0;i<40;i++)
            {
                if(bufsn[i]==0)
                {
                    break;
                }
                str+= (char) bufsn[i];
            }
            mDeviceType=str;
          //  Log.d("zl","mDeviceType:"+mDeviceType);
        }
        else if(mIndexCMD == 3)
        {
            mytimer.cancel();
            str="";
            byte[] bufsn=new byte[15];
            ByteBuffer buf1;
            buf1=ByteBuffer.allocateDirect(15);
            buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
            buf1.put(readOutBuf1,36,15);
            buf1.rewind();
            buf1.get( bufsn);
            for(i=0;i<15;i++)
            {
                if(bufsn[i]==0)
                {
                    break;
                }
                str+= (char) bufsn[i];
            }
            mImsi=str;
            Log.d("zl","imei is :"+mImsi);
        }
        else
        {
            if(mDeviceType.equals("DMU"))
            {
                str="";
                byte[] bufsn=new byte[10];
                ByteBuffer buf1;
                buf1=ByteBuffer.allocateDirect(10);
                buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
                buf1.put(readOutBuf1,28,10);
                buf1.rewind();
                buf1.get( bufsn);
                for(i=0;i<10;i++)
                {
                    str+= (char) (bufsn[i]+0x30);
                }
                mImsi=str;
                Log.d("zl","DMU imei is :"+mImsi);
            }
        }
        mIndexCMD++;
        if(mComd.length>mIndexCMD)
        {
            String readOutMsg = DigitalTrans.byte2hex(mComd[mIndexCMD]);
            if(mIndexCMD == mComd.length - 1)
            {
                verycutstatus(readOutMsg,0);
                mytimer.start();
            }
            else
            {
                verycutstatus(readOutMsg);
            }

         //   Log.d("zl", "onClick: "+CodeFormat.byteToHex(mComd[mIndexCMD],mComd[mIndexCMD].length));
            Log.d("zl",""+mIndexCMD);
        }
        else if(mIndexCMD>=mComd.length)
        {
            //电信注册
           // regiditer();

            //onenet注册
            register2onenet();
        }
    }
    private void register2onenet() {

        if(addrurl.equals(""))
        {
            Toast.makeText(getActivity(),"请完善URL",Toast.LENGTH_SHORT).show();
            return;
        }
        String urlrequest=addrurl+Constants.NB_Service_TO_ONENET;
        String url=String.format(urlrequest,mImei,mSDevcieSn,mDeviceType,mImsi);
        Log.d("zl",url);
        RequestParams params = new RequestParams(url);
        x.http().post(params, new httppostOnenetregisterimpl());
    }
    private void regiditer() {
        if(addrurl.equals(""))
        {
            Toast.makeText(getActivity(),"请完善URL",Toast.LENGTH_SHORT).show();
            return;
        }
        String urlrequest=addrurl+Constants.NB_Service_END;
        String url=String.format(urlrequest,mImei,mSDevcieSn);
        Log.d("zl",url);
        RequestParams params = new RequestParams(url);
        x.http().post(params, new httppost2register1impl());
    }

    public class OnClicklisternerIMPL implements View.OnClickListener
    {

        @Override
        public void onClick(View v) {
            int id=v.getId();
            switch(id)
            {
                case R.id.nb_img_set_addr:
                    Intent intent;
                    // intent=new Intent(mainActivity, InstrumemtItemseetingActivity.class);
                    intent=new Intent(MainActivity.getInstance(), NbServiceAddrInputActivity.class);
                    startActivityForResult(intent, Constants.NBINPUTSETTINGFLAG);
                    break;
                case R.id.nb_but1:
//                    String  url = "http://192.168.1.120:8090/NBWeegServer/weeg/createDevice?OperatorInfo=111&imei=869976036228865&serial=92800026&deviceType=DMU&clientId=010&phone=123&imsi=3231902762";
//
//                    RequestParams params = new RequestParams(url);
//                    x.http().get(params, new httppostOnenetregisterimpl());

                    mIsatart=true;
                    mIndexCMD=0;
                    mAtmpindex=0;
                    mresultview.setText("");
                    String readOutMsg = DigitalTrans.byte2hex(mComd[mIndexCMD]);
                    verycutstatus(readOutMsg);
                    Log.d("zl", "onClick: "+CodeFormat.byteToHex(mComd[mIndexCMD],mComd[mIndexCMD].length));
                    break;
                    default:
                        break;
            }

        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //String addr;

        addrurl=sp.getString(Constants.NB_SERVICE_KEY,"");
        Log.d("zl","onActivityResult: "+addrurl);
        maddrview.setText(addrurl);
        //url=addr+Constants.NB_Service_END;
    }

    private void verycutstatus(String readOutMsg) {
        MainActivity parentActivity1 = (MainActivity) getActivity();
        String strState1 = parentActivity1.GetStateConnect();
        if(!strState1.equalsIgnoreCase("无连接"))
        {
            parentActivity1.mDialog.show();
            parentActivity1.mDialog.setDlgMsg("读取中...");
            //String input1 = Constants.Cmd_Read_Alarm_Pressure;
            parentActivity1.sendData(readOutMsg, "FFFF");
        }
        else
        {
            ToastUtils.showToast(getActivity(), "请先建立蓝牙连接!");
        }
    }

    private void verycutstatus(String readOutMsg,int timeout) {
        MainActivity parentActivity1 = (MainActivity) getActivity();
        String strState1 = parentActivity1.GetStateConnect();
        if(!strState1.equalsIgnoreCase("无连接"))
        {
            parentActivity1.mDialog.show();
            parentActivity1.mDialog.setDlgMsg("读取中...");
            //String input1 = Constants.Cmd_Read_Alarm_Pressure;
            parentActivity1.sendData(readOutMsg, "FFFF",timeout);
        }
        else
        {
            ToastUtils.showToast(getActivity(), "请先建立蓝牙连接!");
        }
    }

//    @Override
//    public void Oncurrentpageselect(int index) {
//        if(index!=position)
//        {
//            mIsatart=false;
//        }
//    }
    public class httppostOnenetregisterimpl implements Callback.CommonCallback<String>{

        @Override
        public void onSuccess(String result) {

            Log.d("zl","urlRegisiter onSuccess:"+result);
            //  Gson gson = new Gson();
            Gson gson =new Gson();
            NbOnenetRegRequest registerName= gson.fromJson(result, NbOnenetRegRequest.class);
            mSdeviceId=registerName.code;
//            Log.d("zl","deviceID:"+registerName.code);
//            Log.d("zl","result1:"+registerName.result);
//            Log.d("zl","result1:"+registerName.msg);

            if(registerName.result)
            {
                mresultview.setText(registerName.msg);
                mresultview.setTextColor(getResources().getColor(R.color.color_grey));
            }
            else
            {
                MainActivity.getInstance().mDialog.dismiss();
                String str="";
                if(registerName!=null)
                {
                    str =registerName.code;
                }
                mresultview.setText("注册任务失败 STEP1:"+registerName.msg);
                mresultview.setTextColor(getResources().getColor(R.color.color_warning));
            }
            MainActivity.getInstance().mDialog.dismiss();
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            mresultview.setText("异常退出");
            Log.d("zl","onError1:"+ex.toString());
            mresultview.setTextColor(getResources().getColor(R.color.color_warning));
            MainActivity.getInstance().mDialog.dismiss();
        }

        @Override
        public void onCancelled(CancelledException cex) {
            Log.d("zl","onCancelled1:"+cex.toString());
            mresultview.setText("异常退出");
            mresultview.setTextColor(getResources().getColor(R.color.color_warning));
            MainActivity.getInstance().mDialog.dismiss();
        }

        @Override
        public void onFinished() {

        }
    }
    public class httppost2register1impl implements Callback.CommonCallback<String>
    {

        @Override
        public void onSuccess(String result) {
            Log.d("zl","urlRegisiter onSuccess:"+result);
            //  Gson gson = new Gson();
            Gson gson =new Gson();
            RegisterName registerName= gson.fromJson(result, RegisterName.class);
            mSdeviceId=registerName.deviceId;
            Log.d("zl","deviceID:"+registerName.deviceId);
            Log.d("zl","result1:"+registerName.result);

            if(registerName.result.equals("true"))
            {

               String urlrequest=addrurl+Constants.NB_Service_END1;
               String url=String.format(urlrequest,mSdeviceId,mDeviceType);
                Log.d("zl",url);
               RequestParams params = new RequestParams(url);
               x.http().post(params, new httppost2register2impl());
            }
            else
            {
                MainActivity.getInstance().mDialog.dismiss();
                String str="";
                if(registerName!=null)
                {
                    str =registerName.code;
                }
                mresultview.setText("注册任务失败 STEP1:"+str);
                mresultview.setTextColor(getResources().getColor(R.color.color_warning));
            }
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            mresultview.setText("异常退出");
            Log.d("zl","onError1:"+ex.toString());
            mresultview.setTextColor(getResources().getColor(R.color.color_warning));
            MainActivity.getInstance().mDialog.dismiss();
        }

        @Override
        public void onCancelled(CancelledException cex) {
            Log.d("zl","onCancelled1:"+cex.toString());
            mresultview.setText("异常退出");
            mresultview.setTextColor(getResources().getColor(R.color.color_warning));
            MainActivity.getInstance().mDialog.dismiss();
        }

        @Override
        public void onFinished() {

        }
    }

    public class httppost2register2impl implements Callback.CommonCallback<String>
    {

        @Override
        public void onSuccess(String result) {
            Log.d("zl","urladdinfo onSuccess:"+result);
            Gson gson =new Gson();
            RegisterName registerName= gson.fromJson(result, RegisterName.class);
            MainActivity.getInstance().mDialog.dismiss();
            if(registerName.result.equals("true"))
            {
                Toast.makeText(getActivity(),"注册成功",Toast.LENGTH_SHORT).show();
                mresultview.setText("注册成功");
                mresultview.setTextColor(getResources().getColor(R.color.color_unselected));
            }
            else
            {
                Toast.makeText(getActivity(),"添加描述失败",Toast.LENGTH_SHORT).show();
                String str="";
                if(registerName!=null)
                {
                    str=registerName.code;
                }

                mresultview.setText("添加描述失败 STEP2:"+str);
                mresultview.setTextColor(getResources().getColor(R.color.color_warning));

            }
        }

        @Override
        public void onError(Throwable ex, boolean isOnCallback) {
            mresultview.setText("异常退出");
            Log.d("zl","onError2:"+ex.toString());
            mresultview.setTextColor(getResources().getColor(R.color.color_warning));
            MainActivity.getInstance().mDialog.dismiss();

        }

        @Override
        public void onCancelled(CancelledException cex) {
            mresultview.setText("异常退出");
            Log.d("zl","onCancelled2:"+cex.toString());
            mresultview.setTextColor(getResources().getColor(R.color.color_warning));
            MainActivity.getInstance().mDialog.dismiss();
        }

        @Override
        public void onFinished() {
            MainActivity.getInstance().mDialog.dismiss();
        }
    }
}
