package com.example.testnewtools.hexfile2bin;

import android.Manifest;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.testnewtools.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by blue on 2016/10/23.
 */

public class FileBrowserActivity extends ListActivity implements EasyPermissions.PermissionCallbacks {
    private static final String TAG = "zl";
    private String rootPath;
    private int pathFlag;
    private List<String> pathList;
    private List<String> itemsList;
    private TextView curPathTextView;
    Intent intent;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser_acitivity);
        intent=getIntent();
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE};
        if (EasyPermissions.hasPermissions(this, perms)) {//检查是否获取该权限
            Log.i(TAG, "已获取权限");
        } else {
            //第二个参数是被拒绝后再次申请该权限的解释
            //第三个参数是请求码
            //第四个参数是要申请的权限
            EasyPermissions.requestPermissions(this,"必要的权限", 0, perms);
        }

        initView();
//        initInfo();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //把申请权限的回调交由EasyPermissions处理
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    private void initInfo() {
        pathFlag = getIntent().getIntExtra("area", 0);
        rootPath = getRootPath();
        if (rootPath == null) {
            Toast.makeText(this, "所选SD卡为空！", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            getFileDir(rootPath);
        }
    }

    private void initView() {
        curPathTextView = (TextView) findViewById(R.id.curPath);
    }

    private void getFileDir(String filePath) {
        curPathTextView.setText(filePath);
        itemsList = new ArrayList<>();
        pathList = new ArrayList<>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        if (!filePath.equals(rootPath)) {
            itemsList.add("b1");
            pathList.add(rootPath);
            itemsList.add("b2");
            pathList.add(file.getParent());
        }
        if (files == null) {
            Toast.makeText(this, "所选SD卡为空！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
           // if (checkSpecificFile(f)) {
                itemsList.add(f.getName());
                pathList.add(f.getPath());
         //   }
        }
        setListAdapter(new MyAdapter(this, itemsList, pathList));
    }

    public boolean checkSpecificFile(File file) {
        String fileNameString = file.getName();
        String endNameString = fileNameString.substring(
                fileNameString.lastIndexOf(".") + 1, fileNameString.length())
                .toLowerCase();
        Log.d(TAG, "checkShapeFile: " + endNameString);
        if (file.isDirectory()) {
            return true;
        }
        if (endNameString.equals("txt")) {
            return true;
        } else {
            return false;
        }
    }

    private String getRootPath() {

        try {
            String rootPath;
//            Log.d("zl","pathFlag: "+pathFlag);
                if (pathFlag==0) {
                    Log.d("zl", "getRootPath: 正在获取内置SD卡根目录");
                    rootPath = Environment.getExternalStorageDirectory()
                            .toString();
                    Log.d("zl", "getRootPath: 内置SD卡目录为:" + rootPath);
                    return rootPath;
                } else if(pathFlag==1) {
                    rootPath = System.getenv("SECONDARY_STORAGE");
                    if ((rootPath.equals(Environment.getExternalStorageDirectory().toString())))
                        rootPath = null;
                    Log.d("zl", "getRootPath:  外置SD卡路径为：" + rootPath);
                    return rootPath;
            }
            else
                {
//                    rootPath = Environment.getDownloadCacheDirectory()
//                            .toString();
//                    rootPath= Environment.getExternalStorageDirectory().getAbsolutePath();
                    rootPath = "/";
//                    Toast.makeText(this,rootPath,Toast.LENGTH_LONG).show();
                    Log.d("zl", "getRootPath: 根目录:" + rootPath);
                    return rootPath;
                }

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        File file = new File(pathList.get(position));
        if (file.isDirectory()) {
            getFileDir(file.getPath());
        } else {
          //  Intent data = new Intent(FileBrowserActivity.this, MainActivity.getInstance());
            Bundle bundle = new Bundle();
            bundle.putString("file", file.getPath());
            intent.putExtras(bundle);
            setResult(2, intent);
            finish();
        }
    }

    public boolean checkSDcard() {
        String sdStutusString = Environment.getExternalStorageState();
        if (sdStutusString.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        initInfo();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
}
