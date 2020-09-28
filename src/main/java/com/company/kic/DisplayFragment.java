package com.company.kic;


import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.app.Notification;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.company.kic.App.CHANNEL_1_ID;



/**
 * A simple {@link Fragment} subclass.
 */
public class DisplayFragment extends Fragment implements LocationListener {


    public static String tvLongi;
    public static String tvLati;
    LocationManager locationManager;

    private MobileServiceClient mClient;
    private MobileServiceTable<devicesTable> mDevicesTable;

    private NotificationManagerCompat notificationManager;
     private static final String TAG = DisplayFragment.class.getSimpleName();
     private final int RSSI_LIMIT = -95;
     private MainActivity mHome;
     private TextView rssiText,msgText,deviceName,macAddress,keys,laptop,backpack;
     private Handler mHanlder;
     private boolean isWarnedUser = false;
     private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           String action = intent.getAction();
           if(action.equals(BroadsastActions.ACTION_READ_RSSI)){
               int value = intent.getIntExtra("value",0);
               rssiText.setText("Current RSSI value is "+value);
               if(value < RSSI_LIMIT){
                 msgText.setText("Hey you are loosing connection to your hardware dont go far away more please");
                 if(!isWarnedUser) {
                     notifyUser();
                     isWarnedUser = true;
                 }
               }else {
                   msgText.setText("");
                   isWarnedUser = false;
               }
           }
        }
    };
    public DisplayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mHome = (MainActivity) context;
        }catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        notificationManager =  NotificationManagerCompat.from(getActivity());
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_display, container, false);
        rssiText = rootView.findViewById(R.id.rssi_text);
        msgText = rootView.findViewById(R.id.msg_text);
        deviceName = rootView.findViewById(R.id.device_name);
        macAddress = rootView.findViewById(R.id.mac_address);
        keys=rootView.findViewById(R.id.keys_textView);
        laptop=rootView.findViewById(R.id.laptop_textView);
        backpack=rootView.findViewById(R.id.backpack_textView);
        mHanlder = new Handler();
        Button button = rootView.findViewById(R.id.disconnect);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mHome.isConnected){
                    mHome.getmUartService().disConnect();
                    Toast.makeText(getActivity(),"Disconnecting",Toast.LENGTH_SHORT).show();
                }
            }
        });

        keys.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deviceName.setText("Keys");
                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            final MobileServiceList<devicesTable> entity = getItemInTable();
                            for (devicesTable item : entity) {
                                item.setDeviceName("KEYS");
                                mDevicesTable.update(item).get();
                            }

                        } catch (final Exception e) {
                            //createAndShowDialogFromTask(e, "Error");
                        }
                        return null;
                    }
                };

                runAsyncTask(task);

            }
        });

        laptop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deviceName.setText("LAPTOP");
                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            final MobileServiceList<devicesTable> entity = getItemInTable();
                            for (devicesTable item : entity) {
                                item.setDeviceName("LAPTOP");
                                mDevicesTable.update(item).get();
                            }

                        } catch (final Exception e) {
                            //createAndShowDialogFromTask(e, "Error");
                        }
                        return null;
                    }
                };

                runAsyncTask(task);


            }
        });

        backpack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deviceName.setText("BACKPACK");
                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            final MobileServiceList<devicesTable> entity = getItemInTable();
                            for (devicesTable item : entity) {
                                item.setDeviceName("BACKPACK");
                                mDevicesTable.update(item).get();
                            }

                        } catch (final Exception e) {
                            //createAndShowDialogFromTask(e, "Error");
                        }
                        return null;
                    }
                };

                runAsyncTask(task);


            }
        });

        try {
            mClient = new MobileServiceClient("https://kic.azurewebsites.net",
                    getActivity()).withFilter(new ProgressFilter());


            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .build();

                    return client;
                }
            });


            mDevicesTable = mClient.getTable(devicesTable.class);

            initLocalStore().get();






        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (MobileServiceLocalStoreException e) {
            e.printStackTrace();
        }

        return rootView;
    }

    public MobileServiceList<devicesTable> getItemInTable() throws ExecutionException, InterruptedException {
        MobileServiceList<devicesTable> result = mDevicesTable
                .where().field("userID").eq(mHome.userId).execute()
                .get();
        return result;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BroadsastActions.ACTION_READ_RSSI);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver,intentFilter);
        mHanlder.postDelayed(mCheckRunnable,100);
        BluetoothDevice device = mHome.getmUartService().getConnectedDevice();
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final MobileServiceList<devicesTable> entity = getItemInTable();

                    mHome.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            for (devicesTable item : entity) {
                                deviceName.setText(item.getDeviceName());
                            }
                        }
                    });

                } catch (final Exception e) {
                    //createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);


        macAddress.setText(device.getAddress());
    }
    private Runnable mCheckRunnable = new Runnable() {
        @Override
        public void run() {
                  mHome.getmUartService().readRSSIValue();
                  mHanlder.postDelayed(mCheckRunnable,3000);
                  Log.d(TAG,"Reading RSSI value");
        }
    };
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        try {
            mHanlder.removeCallbacks(mCheckRunnable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void notifyUser(){

        Intent activityIntent = new Intent(this.getActivity(), LostActivity.class);
        activityIntent.putExtra("userId",mHome.userId);
        activityIntent.putExtra("notificationReciever","yes");
        PendingIntent contentIntent = PendingIntent.getActivity(this.getActivity(),
                0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*Intent broadcastIntent = new Intent(this.getActivity(), LostActivity.class);
        broadcastIntent.putExtra("userId",mHome.userId);
        broadcastIntent.putExtra("notificationReciever","yes");
        PendingIntent actionIntent = PendingIntent.getBroadcast(this.getActivity(),
                0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);*/

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(),CHANNEL_1_ID)
                .setSmallIcon(R.drawable.alert_icon)
                .setContentTitle("KIC")
                .setContentText("You are about to loose your connection")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                //.addAction(R.mipmap.ic_launcher, "Lost", actionIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);
        //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        notificationManager.notify(1, builder.build());

    }


    @Override
    public void onLocationChanged(Location location) {
        tvLongi = String.valueOf(location.getLongitude());
        tvLati = String.valueOf(location.getLatitude());
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final MobileServiceList<devicesTable> entity = getItemInTable();
                    for (devicesTable item : entity) {
                        item.setLocation(tvLati.concat(",").concat(tvLongi));
                        mDevicesTable.update(item).get();
                    }

                } catch (final Exception e) {
                    //createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getActivity(), "Enabled new provider!" + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    public void getLocation() {
        try {
            locationManager = (LocationManager) mHome.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }


    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("userID", ColumnDataType.String);
                    tableDefinition.put("DeviceName", ColumnDataType.String);
                    tableDefinition.put("deviceAdress", ColumnDataType.String);
                    tableDefinition.put("location", ColumnDataType.String);
                    tableDefinition.put("isLost", ColumnDataType.Boolean);
                    tableDefinition.put("isFound", ColumnDataType.Boolean);


                    localStore.defineTable("devicesTable", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    //createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }

    private class ProgressFilter implements ServiceFilter {

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();


            /*runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    //if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });*/

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    /*runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
                        }
                    });*/

                    resultFuture.set(response);
                }
            });

            return resultFuture;
        }
    }
}
