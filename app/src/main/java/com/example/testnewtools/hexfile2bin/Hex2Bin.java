package com.example.testnewtools.hexfile2bin;

import android.util.Log;


import com.example.testnewtools.MainActivity;
import com.example.testnewtools.utils.ToastUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;


public class Hex2Bin {

    private static final int ByteSize = 600 * 1024; //读取的字节数
    private static final String firstLinePrefix = ":02000008"; //地址循环行的标识
    private static final Integer firstAddressPrefix_10 = 2048; //0800，第一次循环的第一个地址位的16进制字符串，转换成十进制是2048
    private static final int dataMaxCharNum = 32; //每行数据最多32字符，16个字节（16进制2个字符占一个字节）
    private static final String compareString = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"; //数据部分全F
    private String mUrl=null;
    private int mlen=0;
    private OnConvertStatusListerner mListerner;
    private Semaphore semaphore = new Semaphore(1);
    private byte[] bytes;

    public Hex2Bin(String url) {
        mUrl=url;
    }
    public void converhex ()
    {
        convernow cv= new convernow();
        Thread thread1 = new Thread(cv);
        thread1.start();
    }
    class convernow implements Runnable
    {
        int code=0;
        @Override
        public void run() {
            if(semaphore.tryAcquire()==false)
            {
                mListerner.OnBusing();
                Log.d("zl","正在数据转换");

            }
            else
            {
                code=Hex2Binconvert();
                if(mListerner!=null)
                {
                    if(code==0)
                    {
                        mListerner.OnConvertSuccess(bytes,mlen);
                    }
                    else
                    {
                        mListerner.OnConvertFailed(code);
                    }
                }
                semaphore.release();
            }
        }
    }
    private   int Hex2Binconvert() {
        int type = 2;
        File mfile= new File(mUrl);
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        FileOutputStream fileOutputStream = null;
        try {
            if (mfile == null) {
//                System.out.println("文件为空");
                Log.d("zl","文件为空");
            }
            if(mfile!=null)
            {
                inputStream = new FileInputStream(mfile); //转成 reader 以 行 为单位读取文件
            }
            else
            {
                ToastUtils.showToast(MainActivity.getInstance(),"参数错误");
                return 5;
            }
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream)); //当前行字符串
        String hexLineStr = null; //当前行数
        int hexLineNum = 0; //当前行的地址位
        Integer hexLineAddress = null; //连续三行数据全F终止
        int offset1=0;
        int hexLineNumForF = 0; //目前数据保存的下标位置
        int offset = 0; //地址归零从新开始时，保存前面地址的偏移量
        int offsetAdd = 0; //每次循环的首地址（第一次是0800，后面循环都是从0000开始了）代码中以十进制来匹配计算
        int firstAddress = firstAddressPrefix_10; //200k固定长度的数组
            //选择了3F行结束时赋值的数组
            bytes = new byte[ByteSize];
        byte[] bytes2 = null; //初始化数组为全FFF
             int lenth=0;
        fillFToByteArray(bytes, compareString);
        Log.d("zl","fillFToByteArray");
        while ((hexLineStr = bufferedReader.readLine()) != null) {
            hexLineNum++;
//            if (hexLineStr.startsWith(firstLinePrefix)) {
//                if (hexLineNum != 1) {
//                    firstAddress = 0;
//                    offsetAdd = offset + dataMaxCharNum / 2;
//                }
//                Log.d("zl","循环，位置偏移：" + offset + " 开始地址：" + firstAddress);
////                System.out.println("循环，位置偏移：" + offset + " 开始地址：" + firstAddress);
//                continue;
//            } //地址为转换成十进制
            //获取地址的偏移量
            hexLineAddress = Integer.parseInt(hexLineStr.substring(3, 7), 16); //获取数据部分
            //首先获取数据类型
            String datatype= hexLineStr.substring(7,9);
            int typecmd= Integer.valueOf(datatype);
            switch (typecmd)
            {
                case 0:  //数据
                    int pagelen= Integer.parseInt(hexLineStr.substring(1, 3), 16); //获取数据部分
                    lenth=pagelen+hexLineAddress+offset1;
                    if(lenth>mlen)
                    {
                        mlen=lenth;
                    }
                    break;
                case 1:  //文件结束
                    Log.d("zl","文件结束了 1");
                    break;
                case 2:  //表示扩展地址的记录
                    Log.d("zl","表示扩展地址的记录 2");
                    break;
                case 3:  // 开始段地址记录
                    Log.d("zl","开始段地址记录 3");
                    break;
                case 4: //线性扩展地址段记录

                    String preData = hexLineStr.substring(9,hexLineStr.length()-2);

                    offset1=0x10000*( Integer.valueOf(preData,16) -firstAddressPrefix_10);
                    Log.d("zl","线性扩展地址段记录4: "+ offset);
                        continue;
                case 5:
                    Log.d("zl","开始线性地址记录 5");
                    break;
                default:
                        break;
            }
            if(typecmd==1)
            {
                break;
            }
            else if(typecmd==2||typecmd==3||typecmd==5)
            {
                continue;
            }

            String preData = hexLineStr.substring(9);
            String data = preData.substring(0, preData.length() - 2); //数据部分小于32个字符串的话，要用F填充满
            if (data.length() < dataMaxCharNum) {
                data = fillHexString(data);
            } //遇到全F就累计+1
//            if (data.equals(compareString)) {
//                hexLineNumForF++;
//            } else { //一旦全F断了就归0
//                hexLineNumForF = 0;
//            } //由于地址存在归0的情况，所以要注意每次循环首地址的变化，以及上次循环结束时，地址的下标
           // offset = hexLineAddress - firstAddress + offsetAdd; //进入该if就表示数组满了，需求是最大200k
            offset=hexLineAddress+offset1;
            if(offset<0)
            {
                int temp=offset;
                Log.d("zl","offset: "+temp+"  转换异常");
                return 1;
            }
            if(offset> bytes.length)
            {
                Log.d("zl","String: "+hexLineStr);
                Log.d("zl","hexLineAddress: "+hexLineAddress);
                Log.d("zl","offset: "+offset);
                Log.e("zl","ERROR: 转换异常");
                return 2;
            }
            if (hexStringToByteArray(data, bytes, offset) == 1) {
                break;
            } //当碰到3行数据全是F的时候，把数据复制到另一个数组，注意数组大小，是根据地址下标来计算的
//            if (hexLineNumForF >= 3 && type == 1) {
//                bytes2 = new byte[offset + dataMaxCharNum / 2];
//                for (int i = 0; i < bytes.length; i++) {
//                    bytes2[i] = bytes[i];
//                    if (i == bytes2.length - 1) {
//                        break;
//                    }
//                }
//                break;
//            }
        }
        Log.d("zl","While end");
        fileOutputStream = new FileOutputStream(mUrl.substring(0,mUrl.length()-4)+".bin");
        if (type == 1) {
            fileOutputStream.write(bytes2);
        } else {
            fileOutputStream.write(bytes,0,mlen);
        }
    } catch (Exception e) {
        e.printStackTrace();
            return 3;
    } finally {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return 4;
        }
    }
    return 0;
    } //初始化数组成全F

    public static void fillFToByteArray(byte[] b, String s) {
        int len = s.length();
        for (int i = 0; i < b.length; i++) {
            for (int j = 0; j < len; j += 2) {
                b[i] = (byte) ((Character.digit(s.charAt(j), 16) << 4) + Character.digit(s.charAt(j + 1), 16));
            }
        }
    } //数据要填充满32个字符

    private static String fillHexString(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(s);
        while (stringBuilder.length() < dataMaxCharNum) {
            stringBuilder.append("F");
        }
        return stringBuilder.toString();
    } //16进制字符串转字节数组,如果数组满了，就返回1，否则返回0

    public static int hexStringToByteArray(String s, byte[] b, int offset) {
        int len = s.length();
        for (int i = 0; i < len; i += 2) { // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[offset] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
            if (offset == b.length - 1) {
                return 1;
            }
            offset++;
        }
        return 0;
    }

    public interface  OnConvertStatusListerner
    {
        public void OnConvertSuccess(byte[] buf, int len);
        public void OnConvertFailed(int code);
        public void OnBusing();
    }
    public void SetOnConverterListerner(OnConvertStatusListerner onConvertStatusListerner)
    {
        mListerner=onConvertStatusListerner;
    }
}
