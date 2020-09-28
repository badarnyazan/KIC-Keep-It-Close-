package com.company.kic;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;




public class NotificationReceiver extends BroadcastReceiver {

    private LostActivity mLostActivity;
    private DisplayFragment mDisplayFragment;

    @Override
    public void onReceive(Context context, Intent intent) {

        String userId=intent.getStringExtra("userId");
        Intent in=new Intent(mDisplayFragment.getActivity(),LostActivity.class);
        in.putExtra("notificationReciever","yes");
        in.putExtra("userId",userId);

        context.sendBroadcast(in);
    }

}
