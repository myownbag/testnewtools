package com.example.testnewtools.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.testnewtools.MainActivity;
import com.example.testnewtools.R;
import com.example.testnewtools.bluetooth.BluetoothState;
import com.example.testnewtools.hexfile2bin.FileBrowserActivity;
import com.example.testnewtools.hexfile2bin.Hex2Bin;
import com.example.testnewtools.myview.CustomDialog;
import com.example.testnewtools.myview.Procseedlg;
import com.example.testnewtools.utils.CodeFormat;
import com.example.testnewtools.utils.Constants;
import com.example.testnewtools.utils.DigitalTrans;
import com.example.testnewtools.utils.ToastUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.Semaphore;

import pub.devrel.easypermissions.EasyPermissions;

public class Hex2BinConvertFragment extends BaseFragment implements  EasyPermissions.PermissionCallbacks {
    private View mView=null;
    private static final int ByteSize = 200 * 1024; //读取的字节数
    private static final String TAG = "zl";
    public static final int FILE_RESULT_CODE = 1;
    private ImageView btn_open;
    private TextView changePath;
    private TextView textshow;
    private CardView textcontainerView;
//    private ViewFlipper viewFlipper;

    private Button btn_Convert;
    private String url;
    public CustomDialog mDialog1;
    private Handler mHander;
    private byte[] byte_firmware;
    private Semaphore semaphore = new Semaphore(1);
    private Semaphore semaphore2 = new Semaphore(1);

    private String buftextshow;

    private int updatestep=0;

    private  long checksum=0;

    private int mpackageIndex=0;
//    private int checksum=0;
    private int databytelen=0;

    private int mpackagelen=0;

    private Procseedlg mprodlg;
    private Thread cv=null;
    private Thread ErrorTimesTh=null;

    private int ErrorTimesCounter=0;

    //Http 请求
//    Callback.Cancelable httpget;
    String mfileName ;
    String mHttpupdatefile;
    //    CountDownTimer mcountDownTimer;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        mView=inflater.inflate(R.layout.hexfile2binfile_fragment,null,false);
        initView();
        initListener();
        return mView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        Intent intent = new Intent();  intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        Uri uri = Uri.fromParts("package", getPackageName(), null);
//        intent.setData(uri);
//        startActivity(intent);
        //把申请权限的回调交由EasyPermissions处理
        Log.d("zl","onRequestPermissionsResult");
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void initView() {
        btn_open =  mView.findViewById(R.id.btn_openfile);
        btn_Convert= mView.findViewById(R.id.btn_firmupdate);
        changePath =  mView.findViewById(R.id.hex2binfilepath);
        textcontainerView = mView.findViewById(R.id.textcontainer);
//        textcontainerView.setScrollContainer(true);
//        textcontainerView.setVerticalScrollBarEnabled(true);
        int currentapiVersion=android.os.Build.VERSION.SDK_INT;
        if(currentapiVersion>=26)
        {
            ViewGroup.LayoutParams layoutParams =new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);
            textshow = new TextView(MainActivity.getInstance());
            textshow.setLayoutParams(new WindowManager
                    .LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT));

            textcontainerView.addView(textshow);
            textshow.setMovementMethod(ScrollingMovementMethod.getInstance());
//

//            WebView scrollView  = new WebView(MainActivity.getInstance());
//            ViewGroup.LayoutParams sclayoutParams =new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
//                    , ViewGroup.LayoutParams.MATCH_PARENT);
//            scrollView.setLayoutParams(new ViewGroup.LayoutParams(sclayoutParams));
//            scrollView.addView(textshow);
//            textcontainerView.addView(scrollView);
//            scrollView.setScrollContainer(true);
//            scrollView.setVerticalScrollBarEnabled(true);
//            viewFlipper.addView(textshow);
//            textshow.setMovementMethod(ScrollingMovementMethod.getInstance());
        }
        else
        {
            ScrollView scrollView  = new ScrollView(MainActivity.getInstance());
            ViewGroup.LayoutParams layoutParams =new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT);
            scrollView.setLayoutParams(new ViewGroup.LayoutParams(layoutParams));
            textshow = new TextView(MainActivity.getInstance());
            textshow.setLayoutParams(new WindowManager
                    .LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT));
            scrollView.addView(textshow);
            textcontainerView.addView(scrollView);
        }
//        textshow = mView.findViewById(R.id.firmware_show);
//        textshow.setMovementMethod(ScrollingMovementMethod.getInstance());
        mDialog1 = CustomDialog.createProgressDialog(MainActivity.getInstance(), Constants.TimeOutSecond, new CustomDialog.OnTimeOutListener() {
            @Override
            public void onTimeOut(CustomDialog dialog) {
                dialog.dismiss();
                ToastUtils.showToast(MainActivity.getInstance(), "超时啦!");
            }
        });
        mDialog1.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d("zl","Hex2BinConvertFragment dialog has been cancelde");
            }
        });
        mHander=MainActivity.getInstance().mHandler;
        mprodlg=new Procseedlg(MainActivity.getInstance());
        mprodlg.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d("zl","setOnDismissListener");
                updatestep=-1;
            }
        });
        mprodlg.setCanceledOnTouchOutside(false);
    }

    private void initListener() {
        btn_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                openBrowser();
//                HttpGetfile();
                openBrowser();
            }

//            private void HttpGetfile() {
//                MainActivity.getInstance().mDialog.show();
//                MainActivity.getInstance().mDialog.setDlgMsg("正在下载");
//                RequestParams params = new RequestParams(Constants.FIRM_BASEUPDATESERVICER+Constants.FIRM_UPDATESERVER_INFO);
//                httpget =  x.http().get(params, new Callback.CommonCallback<String>() {
//                    @Override
//                    public void onSuccess(String result) {
//                        Log.d("zl","Http GET:"+result);
//                        mfileName=result;
//                        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE };  //, Manifest.permission.CALL_PHONE
//                        if (EasyPermissions.hasPermissions(MainActivity.getInstance(), perms)) {//检查是否获取该权限
//                            Log.i(TAG, "已获取权限");
//                            httpgetfile2stored();
//                        } else {
//                            //第二个参数是被拒绝后再次申请该权限的解释
//                            //第三个参数是请求码
//                            //第四个参数是要申请的权限
//                            EasyPermissions.requestPermissions(Hex2BinConvertFragment.this,"必要的权限", 0, perms);
//                            Log.i(TAG, "申请权限");
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable ex, boolean isOnCallback) {
//
//                    }
//
//                    @Override
//                    public void onCancelled(CancelledException cex) {
//
//                    }
//
//                    @Override
//                    public void onFinished() {
//
//                    }
//                });
//            }
        });
        btn_Convert.setOnClickListener(new firmwareupdatbutlisterner());
    }

    private void openBrowser() {
        new AlertDialog.Builder(MainActivity.getInstance()).setTitle("选择存储区域").setIcon(
                R.drawable.icon_opnefile_browser).setSingleChoiceItems(
                new String[]{"内置sd卡", "外部sd卡","内部数据"}, 0,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.getInstance(), FileBrowserActivity.class);
                        if (which == 0)
                            intent.putExtra("area", 0);
                        else if(which==1)
                            intent.putExtra("area", 1);
                        else
                            intent.putExtra("area", 2);
                        startActivityForResult(intent, FILE_RESULT_CODE);
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", null).show();
    }

    @Override
    public void OndataCometoParse(String readOutMsg1, byte[] readOutBuf1) {
        Log.d("zl","OndataCometoParse: "+CodeFormat.byteToHex(readOutBuf1,readOutBuf1.length).toUpperCase());
        if(!mIsatart)
        {
            return;
        }
        if(cv!=null)
        {
            cv.interrupt();
            cv=null;
        }
            switch (updatestep)
            {
                case 0:
                    if(MainActivity.getInstance().mDialog.isShowing())
                    {
                        MainActivity.getInstance().mDialog.dismiss();
                    }
                    if(readOutBuf1[0]!=0x06)
                    {
                        Dialog dialog = new android.app.AlertDialog.Builder(MainActivity.getInstance()) // 实例化对象
                                .setIcon(R.drawable.warning_icon) 						// 设置显示图片
                                .setTitle("操作提示") 							// 设置显示标题
                                .setMessage("设备禁止写入!!\n请确认写入版本号是否高于设备版本号。") 				// 设置显示内容
                                .setPositiveButton("确定", 						// 增加一个确定按钮
                                        new DialogInterface.OnClickListener() {	// 设置操作监听
                                            public void onClick(DialogInterface dialog,
                                                                int whichButton) { 			// 单击事件
                                            }
                                        }).create(); 							// 创建Dialog
                        dialog.show();
                    }
                    else
                    {
                        updatestep=1;
                        byte [] sendbuf=new byte[10];
                        ByteBuffer buf;
                        buf=ByteBuffer.allocateDirect(4);
                        buf=buf.order(ByteOrder.LITTLE_ENDIAN);
                        buf.putInt(byte_firmware.length);
                        buf.rewind();
                        buf.get(sendbuf,0,4);

                        buf=ByteBuffer.allocateDirect(8);
                        buf=buf.order(ByteOrder.LITTLE_ENDIAN);
                        buf.putLong(checksum);
                        buf.rewind();
                        buf.get(sendbuf,4,4);
                        Log.d("zl","lenth/checksum: "+byte_firmware.length+" / "+checksum);


                        //根据新增协议，添加步骤序号
                        byte [] sendbuf1= new byte[11];
                        sendbuf1[0]=0x01;
                        //拷贝数据
//                        buf=ByteBuffer.allocateDirect(11);
//                        buf=buf.order(ByteOrder.LITTLE_ENDIAN);
//                        buf.put(sendbuf);
//                        buf.rewind();
//                        buf.get(sendbuf1,1,10);

                        memcry(sendbuf1,sendbuf,1,0,10);

                        CodeFormat.crcencode(sendbuf1);
                        Log.d("zl","checksum"+CodeFormat.byteToHex(sendbuf1,sendbuf1.length).toLowerCase());
                        databytelen=0;

                        //发送数据
                        verycutstatus(sendbuf1,2000);
                        Log.d("zl","OndataCometoParse: 开始");
                    }
                    break;
                case 1:

                    if(readOutBuf1[0]!=0x06)
                    {
                        if(mprodlg.isShowing())
                            mprodlg.showresult("文件属性失败",R.drawable.update_fail,true);
                    }
                    else
                    {
                        updatestep=2;
                        mpackageIndex=0;
                        byte sendbuf[]=new byte[Constants.FIRM_WRITE_FRAMELEN+4];
                        ByteBuffer buf;
                        buf = ByteBuffer.allocateDirect(4);
                        buf=buf.order(ByteOrder.LITTLE_ENDIAN);
                        buf.putInt(mpackageIndex);
                        buf.rewind();
                        buf.get(sendbuf,0,2);

                        memcry(sendbuf,byte_firmware,2,0,Constants.FIRM_WRITE_FRAMELEN);
                        //根据新增协议添加步骤序号
                        byte sendbuf1[]=new byte[Constants.FIRM_WRITE_FRAMELEN+4+1];
                        //拷贝数据
                        sendbuf1[0]=0x02;
                        memcry(sendbuf1,sendbuf,1,0,sendbuf.length);

                        CodeFormat.crcencode(sendbuf1);
                        verycutstatus(sendbuf1,2000);
//                        Log.d("zl","新增序号"+CodeFormat.byteToHex(sendbuf1,sendbuf1.length).toLowerCase());
//                        mprodlg.show();
                        mprodlg.show("正在写入...");
                        mprodlg.setCurProcess(0);
                        mpackagelen=Constants.FIRM_WRITE_FRAMELEN;
                    }
                    break;
                case 2:
                    if(ErrorTimesTh!=null)
                    {
                        ErrorTimesTh.interrupt();
                        ErrorTimesTh=null;

                    }
                    if(readOutBuf1[0]!=0x06)
                    {
                       // mprodlg.showresult("文件写入失败",R.drawable.update_fail,true);

                        ErrorTimesTh = new Thread(new ErrortiemsSupercisor() );
                        ErrorTimesTh.start();
                    }
                    else
                    {
                        ErrorTimesCounter=0;
                        mpackageIndex++;
                        byte[] sendbuf;
                        databytelen+=Constants.FIRM_WRITE_FRAMELEN;
                        ByteBuffer buf;
                        buf = ByteBuffer.allocateDirect(4);
                        buf=buf.order(ByteOrder.LITTLE_ENDIAN);
                        buf.putInt(mpackageIndex);
                        buf.rewind();
                        if((byte_firmware.length-databytelen)>Constants.FIRM_WRITE_FRAMELEN)
                        {
                            mpackagelen=Constants.FIRM_WRITE_FRAMELEN;
                            sendbuf=new byte[Constants.FIRM_WRITE_FRAMELEN+4];
                            buf.get(sendbuf,0,2);
                            memcry(sendbuf,byte_firmware,2,databytelen,Constants.FIRM_WRITE_FRAMELEN);
                        }
                        else
                        {
                            int lenleft= byte_firmware.length-databytelen;
                            mpackagelen=lenleft;
                            sendbuf=new byte[lenleft+4];
                            buf.get(sendbuf,0,2);
                            memcry(sendbuf,byte_firmware,2,databytelen,lenleft);
                            updatestep=3;
                        }
                        //根据新增协议添加步骤序号
                        byte sendbuf1[]=new byte[sendbuf.length+1];
                        //拷贝数据
                        sendbuf1[0]=0x02;
                        memcry(sendbuf1,sendbuf,1,0,sendbuf.length);

                        CodeFormat.crcencode(sendbuf1);
                        verycutstatus(sendbuf1,2000);
//                        Log.d("zl","新增序号 "+CodeFormat.byteToHex(sendbuf1,sendbuf1.length).toLowerCase());
                        int process=databytelen*100/byte_firmware.length;
                        if(mprodlg.isShowing())
                            mprodlg.setCurProcess(process);
                    }
                    break;
                case 3:
                    if(ErrorTimesTh!=null)
                    {
                        ErrorTimesTh.interrupt();
                        ErrorTimesTh=null;

                    }
                    if(readOutBuf1[0]!=0x06)
                    {
//                        mprodlg.dismiss("文件写入失败",R.drawable.update_fail);
                        ErrorTimesTh = new Thread(new ErrortiemsSupercisor() );
                        ErrorTimesTh.start();
//                        mprodlg.showresult("文件写入失败",R.drawable.update_fail,true);
                    }
                    else
                    {
//                        mprodlg.dismiss("文件写入成功",R.drawable.update_success);
                        if(mprodlg.isShowing())
                             mprodlg.show("文件写入完成");
                        ErrorTimesCounter=0;
                        updatestep=4;
                        if(mprodlg.isShowing())
                            mprodlg.setCurProcess(100);
                        ErrorTimesTh = new Thread(new ErrortiemsSupercisor(Constants.FIRMWARE_DATAFINISH_TIMEOUT) );
                        ErrorTimesTh.start();
                    }
                    break;
                case 4:
                    if(ErrorTimesTh!=null)
                    {
                        ErrorTimesTh.interrupt();
                        ErrorTimesTh=null;

                    }
                    //String readOutMsg = DigitalTrans.byte2hex(sendbuf);
                    Log.d("zl","OndataCometoParse: 结束");
                    Log.d("zl","OndataCometoParse step: "+4+"  "+readOutMsg1);
                    if(readOutMsg1.equals("0406"))
                    {
                        if(mprodlg.isShowing())
                            mprodlg.showresult("文件写入成功",R.drawable.update_success,true);
                    }
                    else
                    {
                        if(mprodlg.isShowing())
                            mprodlg.showresult("校验值出错",R.drawable.update_fail,true);
                    }
                    break;
                    default:
                        break;
            }
    }

    private void memcry(byte[] des, byte[] src,int offset_des, int offset, int len) {
        for(int i=0;i<len;i++)
        {
            des[i+offset_des]=src[i+offset];
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (FILE_RESULT_CODE == requestCode) {
            Bundle bundle = null;
            if (data != null && (bundle = data.getExtras()) != null) {
                String path = bundle.getString("file");
                url=path;
                Log.d(TAG, "onActivityResult: " + path);
                changePath.setText("选择路径为 : " + path);

                //判断文件类型，HXE文件需要转BIN
                String type=url.substring(url.length()-3);
                String type1 =  type.toUpperCase();
                Log.d("zl","TYPE: "+type1);
                mDialog1.show();
                if(type1.equals("HEX"))
                {
                    mDialog1.setDlgMsg("文件格式转换中...");
                    Hex2Bin hex2Bin;
                    hex2Bin=new Hex2Bin(url);
                    hex2Bin.SetOnConverterListerner(new ConvertStatusImpl());
                    hex2Bin.converhex();
                }
                else if(type1.equals("BIN"))
                {
                    mDialog1.setDlgMsg("文件加载中...");
                    readdatafromfile(url);
                }
                else
                {
                    mDialog1.dismiss();
                    ToastUtils.showToast(getActivity(), "文件类型无法识别");
                }
            }
        }
    }
    private void readdatafromfile(String arg)
    {

        Thread thread;
        thread=new Thread( new readfilesthread(arg));
        thread.start();
    }
    public void OnFileConvertResult(int code)
    {
//        Log.d("zl","OnFileConvertResult: "+code);
        if(code==Constants.FIRMWARE_CONVERT_SUCCESS)
        {
            if(mDialog1.isShowing())
            {
                if(buftextshow!=null)
                    textshow.setText(buftextshow);
                mDialog1.dismiss();
            }

        }
        else if(code==Constants.FIRMWARE_CONVERT_FAIL)
        {
            mDialog1.dismiss();
            ToastUtils.showToast(getActivity(),"文件转换失败");
        }
        else if(code==Constants.FIRMWARE_CONVERT_BUSING)
        {

        }
        else if(code==Constants.FIRMWARE_DATAWRITE_TIMEOUT)
        {
            data_write_timeout();
        }
        else if(code==Constants.FIRMWARE_DATAERROR_TIMEOUT)
        {
            int temp=0;
            ErrorTimesTh=null;
            try {
                semaphore2.acquire();
                temp=ErrorTimesCounter;
                semaphore2.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(temp<3)
            {
                byte sendbuf[]=new byte[mpackagelen+4];
                ByteBuffer buf;
                buf = ByteBuffer.allocateDirect(4);
                buf.order(ByteOrder.LITTLE_ENDIAN);
                buf.putInt(mpackageIndex);
                buf.rewind();
                buf.get(sendbuf,0,2);
                memcry(sendbuf,byte_firmware,2,databytelen,Constants.FIRM_WRITE_FRAMELEN);
                //根据新协议添加步骤 头部
                byte sendbuf1[]=new byte[mpackagelen+5];
                sendbuf1[0]=0x02;

                memcry(sendbuf1,sendbuf,1,0,sendbuf.length);
                CodeFormat.crcencode(sendbuf1);
                verycutstatus(sendbuf1,2000);
                CodeFormat.crcencode(sendbuf1);
                Log.d("zl","新增序号 超时"+CodeFormat.byteToHex(sendbuf1,sendbuf1.length).toLowerCase());
//                        mprodlg.show();
                mprodlg.show("正在写入...");
                int process=databytelen*100/byte_firmware.length;
                mprodlg.setCurProcess(process);
            }
            else
            {
                if(mprodlg.isShowing())
                    mprodlg.showresult("写入失败",R.drawable.update_fail,true);
                try {
                    semaphore2.acquire();
                    ErrorTimesCounter=0;
                    semaphore2.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(code==Constants.FIRMWARE_DATAFINISH_TIMEOUT)
        {
            if(mprodlg.isShowing())
                mprodlg.showresult("获取校验值超时",R.drawable.update_fail,true);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Boolean ishaveperms=false;
        for(int i=0;i<perms.size();i++)
        {
            if(perms.get(i).equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                ishaveperms=true;
                break;
            }
        }
        if(!ishaveperms)
        {
            ToastUtils.showToast(MainActivity.getInstance(),"未获取权限");
            return;
        }
        httpgetfile2stored();
    }

    private void httpgetfile2stored() {
        String rootPath  = Environment.getExternalStorageDirectory()
                .toString();
        Log.d("zl","onPermissionsGranted+URL: "+rootPath);
        if (rootPath == null) {
            Toast.makeText(MainActivity.getInstance(), "无法获取存储路径！", Toast.LENGTH_SHORT).show();
        } else {
            rootPath+="/GC2018";
            File file = new File(rootPath);
            if(!file.exists())
            {
                file.mkdirs();
            }
            else
            {
                if(mfileName!=null)
                {
                    String Httpurl= Constants.FIRM_BASEUPDATESERVICER+mfileName;
                    //设置请求参数
//                    RequestParams params = new RequestParams(Httpurl);
//                    params.setAutoResume(true);//设置是否在下载是自动断点续传
//                    params.setAutoRename(false);//设置是否根据头信息自动命名文件
//                    url=rootPath+"/"+mfileName;
//                    params.setSaveFilePath(url);
//                    Log.d("zl","缓存文件路径: "+url);
//
//                    params.setExecutor(new PriorityExecutor(2, true));//自定义线程池,有效的值范围[1, 3], 设置为3时, 可能阻塞图片加载.
//                    params.setCancelFast(true);//是否可以被立即停止
//
//                    Log.d("zl","recallHttpgetrequest: 开始下载");
//
//                    Log.d("zl","recallHttpgetrequest URL:"+Constants.FIRM_BASEUPDATESERVICER+mfileName);
//                    httpget = x.http().get(params,new recallHttpgetrequest(url) );
                }
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        for(int i=0;i<perms.size();i++)
        {
            Log.d("zl","onPermissionsDenied: "+perms.get(i));
            if(perms.get(i).equals(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            {
                if(MainActivity.getInstance().mDialog.isShowing())
                    MainActivity.getInstance().mDialog.dismiss();
                ToastUtils.showToast(MainActivity.getInstance(),"未获取存储权限");
            }
        }
    }

    class readfilesthread implements Runnable
    {
        String arg=null;
        byte [] filedata=new byte[ByteSize];
        int len=0;
        public readfilesthread(String url)
        {
            arg=url;
        }

        private void readfiledata()
        {
            File file = new File(arg);
            byte [] temp=new byte[512];
            InputStream in = null;
            ByteBuffer buf;
            int flag=1;
            try {
                in = new FileInputStream(file);
                while (flag!=-1)
                {
                    flag= in.read(temp);
                    if(flag>0)
                    {
                        buf=ByteBuffer.allocateDirect(flag);
                        buf=buf.order(ByteOrder.LITTLE_ENDIAN);
                        buf.put(temp,0,flag);
                        buf.rewind();
                        buf.get(filedata,len,flag);
                        len+=flag;
                    }
                }
                FirmwareDataProsess(filedata,len);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        @Override
        public void run() {
            if(semaphore.tryAcquire()==false)
            {
                Log.d("zl","获取信号量失败 readfiledata()");
                return;
            }
            readfiledata();
            semaphore.release();
        }
    }

    private void FirmwareDataProsess(byte [] buf,int lenth) {
        //线程中在执行可以处理耗时工作，但不可以操控界面
        byte_firmware=new byte[lenth];
        ByteBuffer buf1;
        buf1=ByteBuffer.allocateDirect(buf.length);
        buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
        buf1.put(buf);
        buf1.rewind();
        buf1.get(byte_firmware,0,lenth);
        buftextshow= CodeFormat.byteToHex(byte_firmware,byte_firmware.length).toUpperCase();
        //计算checksum
        checksum=0;
        for(int i=0;i<lenth;i++)
        {
            checksum+=byte_firmware[i]&0xff;
        }
        mHander.obtainMessage(BluetoothState.MESSAGE_CONVERT_INFO, Constants.FIRMWARE_CONVERT_SUCCESS,0) //FIRMWARE_CONVERT_SUCCESS
                .sendToTarget();
    }
    class ConvertStatusImpl implements Hex2Bin.OnConvertStatusListerner
    {
        // 因为在线程中调用的所以不能操控界面


        @Override
        public void OnConvertSuccess(byte[] buf, int len) {
//            byte_firmware=new byte[len];
//            ByteBuffer buf1;
//            buf1=ByteBuffer.allocateDirect(buf.length);
//            buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
//            buf1.put(buf);
//            buf1.rewind();
//            buf1.get(byte_firmware,0,len);
//            buftextshow= CodeFormat.byteToHex(byte_firmware,byte_firmware.length).toUpperCase();
//            mHander.obtainMessage(BluetoothState.MESSAGE_CONVERT_INFO,Constants.FIRMWARE_CONVERT_SUCCESS,0) //FIRMWARE_CONVERT_SUCCESS
//                    .sendToTarget();

            FirmwareDataProsess(buf,len);

        }

        @Override
        public void OnConvertFailed(int code) {
//            mDialog1.dismiss();
//            ToastUtils.showToast(getActivity(), "文件转换失败");
            mHander.obtainMessage(BluetoothState.MESSAGE_CONVERT_INFO,Constants.FIRMWARE_CONVERT_FAIL,code)
                    .sendToTarget();
        }

        @Override
        public void OnBusing() {
//            mDialog1.dismiss();
//            ToastUtils.showToast(getActivity(), "文件正在转换");
            mHander.obtainMessage(BluetoothState.MESSAGE_CONVERT_INFO,Constants.FIRMWARE_CONVERT_BUSING,0)
                    .sendToTarget();
        }
    }

    private  class firmwareupdatbutlisterner implements View.OnClickListener{

        @Override
        public void onClick(View v) {
//            Toast.makeText(getActivity(),"开始转换",Toast.LENGTH_SHORT)
//                    .show();
//            mprodlg.show();

            if(byte_firmware==null)
            {
                ToastUtils.showToast(getActivity(),"请先载入文件");
                return;
            }
            else if(byte_firmware.length==0)
            {
                ToastUtils.showToast(getActivity(),"文件数据长度异常");
                return;
            }
            else
            {
                //让设备处于等待状态
                updatestep=-1;
                verycutstatus("18",0);
                //延时2秒
                CountDownTimer countDownTimer = new CountDownTimer(2000,1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        updatestep=0;
                        byte [] sendbuf;
                        int datalen=4;
                        sendbuf=new byte[datalen+18];
                        sendbuf[0]= (byte) 0xFD;
                        sendbuf[3]= (byte) ((datalen+13)%0x100);
                        sendbuf[5]=0x15;
                        sendbuf[14]= (byte) (0xff&3);
                        sendbuf[16]=(byte) 'V';
                        String ver = url.substring(url.length()-7,url.length()-4);
                        Log.d("zl",ver);
                        ByteBuffer buf1;
                        buf1=ByteBuffer.allocateDirect(3);
                        buf1=buf1.order(ByteOrder.LITTLE_ENDIAN);
                        buf1.put(ver.getBytes());
                        buf1.rewind();
                        buf1.get(sendbuf,17,3);
                        CodeFormat.crcencode(sendbuf);
                        String readOutMsg = DigitalTrans.byte2hex(sendbuf);
                        Log.d("zl",CodeFormat.byteToHex(sendbuf,sendbuf.length).toLowerCase());
                        verycutstatus(readOutMsg,2000);
                    }
                };
                countDownTimer.start();
                mIsatart=true;
            }


        }


    }

    private void verycutstatus(String readOutMsg,int timeout) {
        MainActivity parentActivity1 = MainActivity.getInstance();
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
            ToastUtils.showToast(parentActivity1, "请先建立蓝牙连接!");
        }
    }

    private void verycutstatus(String readOutMsg) {
        MainActivity parentActivity1 = MainActivity.getInstance();
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
            ToastUtils.showToast(parentActivity1, "请先建立蓝牙连接!");
        }
    }
    private void verycutstatus(byte [] buf,int timeout)
    {
        if(buf!=null)
        {
           Log.d("zl","send:"+ CodeFormat.byteToHex(buf,buf.length).toUpperCase());
        }
        MainActivity parentActivity1 = MainActivity.getInstance();
        String strState1 = parentActivity1.GetStateConnect();
        if(!strState1.equalsIgnoreCase("无连接"))
        {
//            parentActivity1.mDialog.show();
//            parentActivity1.mDialog.setDlgMsg("读取中...");
            //String input1 = Constants.Cmd_Read_Alarm_Pressure;
            parentActivity1.sendData(buf,0);
        }
        else
        {
            ToastUtils.showToast(parentActivity1, "请先建立蓝牙连接!");
        }
        if(timeout>0)
        {

            cv = new Thread(new timeoutSupervisor(timeout));
            cv.start();
        }
    }

   private  class timeoutSupervisor implements Runnable
   {
       int mtimeout=0;
       public timeoutSupervisor(int timeout)
       {
           mtimeout=timeout;
       }

       @Override
       public void run() {
           try {
               Thread.sleep(mtimeout);
               mHander.obtainMessage(BluetoothState.MESSAGE_CONVERT_INFO,Constants.FIRMWARE_DATAWRITE_TIMEOUT,1)
                       .sendToTarget();
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
   }

   private class ErrortiemsSupercisor implements Runnable
   {
       int mtype=0;
       public ErrortiemsSupercisor(int requesttype)
       {
           mtype=requesttype;
       }
       public ErrortiemsSupercisor()
       {
           mtype=Constants.FIRMWARE_DATAERROR_TIMEOUT;
       }
       @Override
       public void run() {
           try {
               Thread.sleep(6000);
               if(mtype==Constants.FIRMWARE_DATAERROR_TIMEOUT)
               {
                   semaphore2.acquire();
                   ErrorTimesCounter++;
                   semaphore2.release();
               }
               mHander.obtainMessage(BluetoothState.MESSAGE_CONVERT_INFO,mtype,1)
                       .sendToTarget();
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
       }
   }
   private void data_write_timeout()
    {
        if(mprodlg.isShowing())
        {
            mprodlg.showresult("写入超时",R.drawable.update_fail,true);
            updatestep=-1;
        }
    }

    @Override
    public void Ondlgcancled() {
        super.Ondlgcancled();
//        if(httpget!=null)
//        {
//            httpget.cancel();
//            Log.d("zl","httpget cancle");
//        }
    }
//    class recallHttpgetrequest implements Callback.CommonCallback<File>
//    {
//        String murl;
//        Boolean Httpresult=false;
//        public  recallHttpgetrequest(String url)
//        {
//            murl = url;
//        }
//
//        @Override
//        public void onSuccess(File result) {
//            Log.d("zl","onSuccess");
//            Httpresult=true;
//            if(MainActivity.getInstance().mDialog.isShowing())
//                MainActivity.getInstance().mDialog.dismiss();
//            mDialog1.show();
//            mDialog1.setDlgMsg("文件格式转换...");
//            Hex2Bin hex2Bin;
//            hex2Bin=new Hex2Bin(murl);
//            hex2Bin.SetOnConverterListerner(new ConvertStatusImpl());
//            hex2Bin.converhex();
//        }
//
//        @Override
//        public void onError(Throwable ex, boolean isOnCallback) {
//            Log.d("zl","onError:\n"+ex.toString());
//            Httpresult=false;
//            if(MainActivity.getInstance().mDialog.isShowing())
//                MainActivity.getInstance().mDialog.dismiss();
//            ToastUtils.showToast(MainActivity.getInstance(),"文件下载失败");
//        }
//
//        @Override
//        public void onCancelled(CancelledException cex) {
//            Log.d("zl","onCancelled");
//            Httpresult=false;
//            if(MainActivity.getInstance().mDialog.isShowing())
//                MainActivity.getInstance().mDialog.dismiss();
//            ToastUtils.showToast(MainActivity.getInstance(),"文件下载任务被取消");
//        }
//
//        @Override
//        public void onFinished() {
//            Log.d("zl","onFinished");
//            if(Httpresult==false)
//            {
//                if(MainActivity.getInstance().mDialog.isShowing())
//                    MainActivity.getInstance().mDialog.dismiss();
//            }
//        }
//    }
}
