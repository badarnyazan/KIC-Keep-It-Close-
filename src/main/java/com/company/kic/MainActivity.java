package com.company.kic;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.facebook.login.LoginManager;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;

public class MainActivity extends AppCompatActivity implements ConnectionService.ConnectionServiceCallBack{

    public String userId;
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean bound = false;
    private ConnectionService mUartService;
    public boolean isConnected = false;
    private Handler mHandler;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ConnectionService.MyBinder myBinder=(ConnectionService.MyBinder)service;
            mUartService=myBinder.getUartService();
            bound=true;
            mUartService.registerActivity(MainActivity.this);
            Log.d(TAG,"onServiceConnected called,Activity registered");
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound=false;
            mUartService=null;
            Log.d(TAG,"onServiceDisconnected called,Activity unregistered");
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent in=getIntent();
        userId=in.getStringExtra("userId");
        mHandler = new Handler();
        if(!bound){
            bindService(new Intent(this, ConnectionService.class), mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onDestroy() {
        if(bound){
            if(mUartService!=null){
                mUartService.stopSelf();
                unbindService(mConnection);
                mUartService=null;
                Log.d(TAG,"Service unregistered");
                bound = false;
            }
        }
        super.onDestroy();

    }

    @Override
    public void notifyActivity(String msg) {

    }

    @Override
    public void onConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"Connected",Toast.LENGTH_SHORT).show();
            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Navigation.findNavController(MainActivity.this,R.id.nav_host_fragment)
                        .navigate(R.id.displayFragment);
            }
        },500);
                isConnected = true;
     Log.d(TAG,"onConnected called");
    }

    @Override
    public void onDisConnected() {
              //  Toast.makeText(MainActivity.this,"Disonnected",Toast.LENGTH_SHORT).show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,"Disconnected",Toast.LENGTH_SHORT).show();
            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Navigation.findNavController(MainActivity.this,R.id.nav_host_fragment)
                        .navigate(R.id.connectionFragment);
            }
        },500);
                isConnected = false;

        Log.d(TAG,"onDisconnected called");
    }
    public ConnectionService getmUartService(){
        if(mUartService != null){
            return mUartService;
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


}
