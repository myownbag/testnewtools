package com.example.testnewtools.activitys;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.testnewtools.R;
import com.example.testnewtools.utils.Constants;


public class NbServiceAddrInputActivity extends Activity {
    Button but;
    EditText edtext;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nb_service_addr_input_layout);
        but=findViewById(R.id.butnbaddrcommite);
        edtext=findViewById(R.id.edtextnbaddripput);
        String addrurl;
        SharedPreferences sp=null;
        sp=getSharedPreferences("User", Context.MODE_PRIVATE);
        addrurl=sp.getString(Constants.NB_SERVICE_KEY,"");
        Log.d("zl","onActivityResult: "+addrurl);
        edtext.setText(addrurl);

        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp;
                sp=NbServiceAddrInputActivity.this.getSharedPreferences("User", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString(Constants.NB_SERVICE_KEY,edtext.getText().toString());
                //edit.putInt("info",1);
                edit.commit();
                Log.d("zl",sp.getString(Constants.NB_SERVICE_KEY,"未获取"));
                NbServiceAddrInputActivity.this.finish();
            }
        });
    }
}
