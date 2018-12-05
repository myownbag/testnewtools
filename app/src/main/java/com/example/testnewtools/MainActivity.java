package com.example.testnewtools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.testnewtools.bluetooth.BluetoothService;
import com.example.testnewtools.bluetooth.BluetoothState;
import com.example.testnewtools.bluetooth.DeviceListActivity;
import com.example.testnewtools.fragments.Hex2BinConvertFragment;
import com.example.testnewtools.myview.CustomDialog;
import com.example.testnewtools.utils.Constants;
import com.example.testnewtools.utils.DigitalTrans;
import com.example.testnewtools.utils.ToastUtils;

import static com.example.testnewtools.bluetooth.BluetoothState.REQUEST_CONNECT_DEVICE;
import static com.example.testnewtools.bluetooth.BluetoothState.REQUEST_ENABLE_BT;

public class MainActivity extends AppCompatActivity {

    long exitTime = 0;
    public CustomDialog mDialog;
    public static final String METERFRAGMENT="meterfragment";
    private MenuItem menuitem=null;
    static MainActivity instanceMainActivity = null;
    ActionBar mActionBar;

    ///*******************蓝牙相关***************//
    //超时
    private BlueToothTimeOutMornitor mThreedTimeout;
    //蓝牙状态保存
    public Boolean mIsconnect = false;
    // Name of the connected device
    public String mConnectedDeviceName = null;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the services
    private BluetoothService mBTService = null;
    //接口
    Ondataparse mydataparse=null;
    Hex2BinConvertFragment mchildfragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActionBar = getSupportActionBar();
        mActionBar.setTitle(R.string.secure_connect);
        InitView();
        InitBlueTooth();
    }

    private void InitView() {
        instanceMainActivity = this;
        mDialog = CustomDialog.createProgressDialog(this, Constants.TimeOutSecond, new CustomDialog.OnTimeOutListener() {
            @Override
            public void onTimeOut(CustomDialog dialog) {
                dialog.dismiss();
                ToastUtils.showToast(getBaseContext(), "超时啦!");
            }
        });
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d("zl","dialog has been cancelde");
            }
        });
        Initfragment();
    }

    private void Initfragment() {
        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction(); //开启事务
        mchildfragment=new Hex2BinConvertFragment();
        transaction.replace(R.id.mainframe,mchildfragment ,
                METERFRAGMENT);
        transaction.commit();// 提交事务
    }

    private void InitBlueTooth()
    {
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null)
        {
            ToastUtils.showToast(this, "该设备不支持蓝牙，强制退出");
            finish();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled())
        {
            //打开蓝牙
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            if (mBTService == null) {
                // Initialize the BluetoothService to perform bluetooth
                // connections
                mBTService = new BluetoothService(this, mHandler);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity
        // returns.
        if (mBTService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't
            // started already
            if (mBTService.getState() == BluetoothState.STATE_NONE) {
                // Start the Bluetooth services
                mBTService.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth services
        if (mBTService != null)
            mBTService.stop();
    }
    @SuppressLint("HandlerLeak")
    public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothState.MESSAGE_STATE_CHANGE:
                    // if (D)
                    //    Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothState.STATE_CONNECTED:
                            //setStatus(getString(R.string.title_connected_to,
                            //		mConnectedDeviceName));
//                            mTxtStatus.setText("已连接到:" + mConnectedDeviceName);
                            mActionBar.setTitle("已连接到:" + mConnectedDeviceName);
                            // mConversationArrayAdapter.clear();
                            mIsconnect = true;
                            break;
                        case BluetoothState.STATE_CONNECTING:
                            //setStatus(R.string.title_connecting);
//                            mTxtStatus.setText(R.string.title_connecting);
                            mActionBar.setTitle(R.string.title_connecting);
                            mIsconnect = false;
                            break;
                        case BluetoothState.STATE_LISTEN:
                        case BluetoothState.STATE_NONE:
                            //   Log.d("zl","BluetoothState_state:"+"STATE_NONE/STATE_LISTEN");
                            //setStatus(R.string.title_not_connected);
//                            mTxtStatus.setText(R.string.title_not_connected);
                            mActionBar.setTitle(R.string.title_not_connected);
                            mIsconnect = false;
                            break;
                    }
                    break;
                case BluetoothState.MESSAGE_WRITE:
                    // byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    // String writeMessage = new String(writeBuf);
                    // mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case BluetoothState.MESSAGE_READ:
                    if (mThreedTimeout != null)
                        mThreedTimeout.interrupt();
                    mThreedTimeout = null;
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = "";
                    for (int i = 0; i < msg.arg1; i++) {
                        //readMessage += readBuf[i];

                        String hex = Integer.toHexString(readBuf[i] & 0xFF);
                        if (hex.length() == 1) {
                            hex = '0' + hex;
                        }

                        readMessage += hex;
                    }

                    byte[] readOutBuf = DigitalTrans.hex2byte(readMessage);
                    String readOutMsg = DigitalTrans.byte2hex(readOutBuf);

                    //获取接收的返回数据
                    Log.v("ttt", "recv:" + readOutMsg);

                    if (mydataparse != null) {
                        mydataparse.datacometoparse(readOutMsg, readOutBuf);
                    }
                    else {
                        mchildfragment.OndataCometoParse(readOutMsg, readOutBuf);
                    }
                    //mDialog.dismiss();

                    break;
                case BluetoothState.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(BluetoothState.DEVICE_NAME);
                    ToastUtils.showToast(getApplicationContext(), "连接到" + mConnectedDeviceName);

                    break;
                case BluetoothState.MESSAGE_TOAST:

                    ToastUtils.showToast(getApplicationContext(),
                            msg.getData().getString(BluetoothState.TOAST));
                    break;
                case BluetoothState.MESSAGE_STATE_TIMEOUT:
                    if (mIsconnect) {
                        // 关闭连接socket
                        try {
                            // 关闭蓝牙
                            // mTxtStatus.setText(R.string.title_not_connected);
                            mActionBar.setTitle(R.string.title_not_connected);
                            mBTService.stop();
                        } catch (Exception e) {
                        }
                    }
                    mThreedTimeout = null;
                    mDialog.dismiss();
                    // ToastUtils.showToast(getActivity(), "数据长度异常");
                    Toast.makeText(MainActivity.this, "蓝牙无回应请重连", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothState.MESSAGE_BLOCK_TIMEOUT:
//                    if(mCurrentpage==fregment3) {
////                        Log.d("zl","BluetoothState.MESSAGE_BLOCK_TIMEOUT:"+msg.arg1);
//                        if(msg.arg1==Constants.NB_FRESONDATA_KEY_BLOCK_FINISHED)
//                        {
////                            Log.d("zl","Main OnBlockdataFinished");
//                            fregment3.OnBlockdataFinished();
//                        }
//                        else if(msg.arg1==Constants.NB_FRESONDATA_KEY_TASKFINISHED_FINISHED)
//                        {
////                            Log.d("zl","Main updatelistview");
//                            fregment3.updatelistview();
//                        }
//                    }
                    break;
                case BluetoothState.MESSAGE_CONVERT_INFO:
//                    if(mCurrentpage==fragment10)
//                    {
//                        fragment10.OnFileConvertResult(msg.arg1);
//                    }
                    mchildfragment.OnFileConvertResult(msg.arg1);
                    break;
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        MainActivity.this.menuitem=menu.findItem(R.id.secure_connect_scan);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(R.id.secure_connect_scan==id)
        {
//            ToastUtils.showToast(this,"menu pressed");
            if(!mIsconnect)
            {
                Intent serverIntent = new Intent(MainActivity.this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            }
            else
            {
                // 关闭连接socket
                try {
                    // 关闭蓝牙
//                    mTxtStatus.setText(R.string.title_not_connected);
                    mActionBar.setTitle(R.string.title_not_connected);
                    mBTService.stop();
                } catch (Exception e) {
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {

            if ((System.currentTimeMillis() - exitTime) > 2000) {
                ToastUtils.showToast(this, "再按一次退出程序");
                exitTime = System.currentTimeMillis();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

            return false;
        }

        return super.dispatchKeyEvent(event);
    }

    public class BlueToothTimeOutMornitor extends Thread
    {
        public int mtimeout;
        BlueToothTimeOutMornitor()
        {
            mtimeout=2000;
        }
        BlueToothTimeOutMornitor(int timeout)
        {
            mtimeout=timeout;
        }
        @Override
        public void run() {
            try {
                sleep(mtimeout);
                MainActivity.this.mHandler.obtainMessage(BluetoothState.MESSAGE_STATE_TIMEOUT)
                        .sendToTarget(); //       mHandler.obtainMessage(BluetoothState.MESSAGE_READ, bytes, -1, buffer)
                //  .sendToTarget();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    // Attempt to connect to the device
                    mBTService.connect(device);
                }
                break;
            case BluetoothState.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled
                    // Initialize the BluetoothService to perform bluetooth
                    // connections

                    mBTService = new BluetoothService(this, mHandler);

                    Intent serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);


                } else {
                    // User did not enable Bluetooth or an error occured
                    // Log.d(TAG, "BT not enabled");
                    ToastUtils.showToast(this, "蓝牙没有打开，程序退出");

                    finish();
                }

                break;

        }
    }
    public static MainActivity getInstance() {
        return instanceMainActivity;
    }
    public interface Ondataparse
    {
        void datacometoparse(String readOutMsg1,byte[] readOutBuf1);
    }
    public BluetoothService getcurblueservice()
    {
        return   mBTService;
    }

    public String GetStateConnect()
    {
        // return mTxtStatus.getText().toString();
        return  mActionBar.getTitle().toString();
    }

    public void sendData(String data, String strOwner) {


        // Check that we're actually connected before trying anything
        if (mBTService.getState() != BluetoothState.STATE_CONNECTED) {

            ToastUtils.showToast(this,  R.string.not_connected);
            return;
        }

        // Check that there's actually something to send
        if (data.length() > 0) {
            // gOwner = strOwner;
            Log.v("ttt", "Send: " + data);
            String hexString = data;
            byte[] buff = DigitalTrans.hex2byte(hexString);

            mBTService.write(buff);
            if(mThreedTimeout==null)
            {
                mThreedTimeout=new BlueToothTimeOutMornitor();
                mThreedTimeout.start();
            }
        }
    }

    public void sendData(byte[] databuf,int timeout)
    {
        mBTService.write(databuf);
        if(timeout>0)
        {
            if(mThreedTimeout==null)
            {
                mThreedTimeout=new BlueToothTimeOutMornitor(timeout);
                mThreedTimeout.start();
            }
        }
    }
    //当timeout设置为0时 ，不会做超时计算
    public void sendData(String data, String strOwner,int timeout) {

        Log.d("zl","MainActivity:"+data);
        // Check that we're actually connected before trying anything
        if (mBTService.getState() != BluetoothState.STATE_CONNECTED) {

            ToastUtils.showToast(this,  R.string.not_connected);
            return;
        }

        // Check that there's actually something to send
        if (data.length() > 0) {
//            gOwner = strOwner;
            Log.v("ttt", "Send: " + data);
            String hexString = data;
            byte[] buff = DigitalTrans.hex2byte(hexString);

            mBTService.write(buff);
            if(timeout>0)
            {
                if(mThreedTimeout==null)
                {
                    mThreedTimeout=new BlueToothTimeOutMornitor(timeout);
                    mThreedTimeout.start();
                }
            }
        }
    }
}
