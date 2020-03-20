package com.example.testnewtools.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.testnewtools.MainActivity;
import com.example.testnewtools.utils.Constants;


public abstract class BaseFragment extends Fragment {
    public Boolean mIsatart=false;
    public boolean m_dlgcancled=false;
    Bundle bundle;
    public int position=0;
    String str;
    abstract public void OndataCometoParse(String readOutMsg1, byte[] readOutBuf1) ;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void Oncurrentpageselect(int index)
    {
        bundle = getArguments();
        if (bundle != null) {
            position=bundle.getInt("position");
            str=bundle.getString("extra");
        }
        else
        {
            Log.d("zl","position:"+"ERROR");
        }
        if(position!=index)
        {
            mIsatart=false;
        }
        Log.d("zl","fragment:"+str);
        if(str.equals(Constants.FunFregmemt1))
        {
            MainActivity.getInstance().getcurblueservice().ChangetimeoutofPackage(50);
        }
        else
        {
            MainActivity.getInstance().getcurblueservice().ChangetimeoutofPackage(200);
        }
    }
    public void Ondlgcancled()
    {
        mIsatart=false;
    }
}
