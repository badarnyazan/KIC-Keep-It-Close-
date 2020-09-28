package com.company.kic;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static com.company.kic.App.CHANNEL_1_ID;
import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

public class LostActivity  extends AppCompatActivity {

    private String userId;
    public String[] separated;
    private MobileServiceClient mClient;
    private MobileServiceTable<devicesTable> mDevicesTable;
    private NotificationManagerCompat notificationManager;


    private TextView lostInfo;
    private TextView deviceName;
    private TextView deviceAddress;
    private TextView deviceLocation;
    private TextView timeDate;
    private Button mapButton;
    private Button foundItButton;
    //private Button refreshButton;





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost);

        lostInfo=(TextView) findViewById(R.id.msg_text);
        deviceName=(TextView) findViewById(R.id.device_name);
        deviceAddress=(TextView)findViewById(R.id.mac_address);
        deviceLocation=(TextView)findViewById(R.id.location_text);
        timeDate=(TextView)findViewById(R.id.timeDate_text);
        foundItButton=(Button) findViewById(R.id.found_button);
        mapButton=(Button)findViewById(R.id.map_button);
        //refreshButton=(Button)findViewById(R.id.refresh_button);;

        notificationManager =  NotificationManagerCompat.from(this);

        /*refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            final MobileServiceList<devicesTable> entity = checkItemInTable();


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (entity.size() != 0) {
                                        for (devicesTable item : entity) {
                                            deviceName.setText(item.getDeviceName());
                                            deviceAddress.setText(item.getDeviceAddress());
                                            String mAddress=item.getLocation();
                                            separated = mAddress.split(",");
                                            Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                                            List<Address> addresses = null;
                                            try {
                                                addresses = geo.getFromLocation(Double.parseDouble(separated[0]), Double.parseDouble(separated[1]), 1);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            if (addresses.isEmpty()) {
                                                deviceLocation.setText("Waiting for Location");
                                            }
                                            else {
                                                if (addresses.size() > 0) {
                                                    String addressName=addresses.get(0).getFeatureName() + "," + addresses.get(0).getLocality() +"," + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();

                                                    deviceLocation.setText(addressName);

                                                }
                                            }
                                            timeDate.setText(item.getUpdatdAt());
                                            lostInfo.setText("Lost item Info");
                                            mapButton.setVisibility(View.VISIBLE);
                                            foundItButton.setVisibility(View.VISIBLE);
                                            if(item.getIsFound()==true){
                                                notifyUser(deviceLocation.getText().toString());
                                            }
                                        }
                                    }
                                    else{
                                        f();
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
            }
        });*/


        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Uri gmmIntentUri = Uri.parse("google.streetview:"+separated[0]+separated[1]);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });

        foundItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            final MobileServiceList<devicesTable> entity = getItemInTable();
                            for (devicesTable item : entity) {
                                item.setIsLost(false);
                                item.setIsFound(false);
                                mDevicesTable.update(item).get();
                            }

                        } catch (final Exception e) {
                            //createAndShowDialogFromTask(e, "Error");
                        }
                        return null;
                    }
                };

                runAsyncTask(task);

                lostInfo.setText("NO lost devices found for you");
                deviceName.setText(" ");
                deviceAddress.setText(" ");
                deviceLocation.setText(" ");
                timeDate.setText(" ");
                mapButton.setVisibility(View.GONE);
                foundItButton.setVisibility(View.GONE);


            }
        });

        try {
            mClient = new MobileServiceClient(
                    "https://kic.azurewebsites.net",
                    this).withFilter(new ProgressFilter());


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


        Intent intent=getIntent();
        String flag=intent.getStringExtra("notificationReciever");
        userId=intent.getStringExtra("userId");
        if(flag.equals("yes")){

                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            final MobileServiceList<devicesTable> entity = getItemInTable();
                            for (devicesTable item : entity) {
                                item.setIsLost(true);
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

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            final MobileServiceList<devicesTable> entity = checkItemInTable();


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (entity.size() != 0) {
                                        for (devicesTable item : entity) {
                                            deviceName.setText(item.getDeviceName());
                                            deviceAddress.setText(item.getDeviceAddress());
                                            String mAddress = item.getLocation();
                                            separated = mAddress.split(",");
                                            Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
                                            List<Address> addresses = null;
                                            try {
                                                addresses = geo.getFromLocation(Double.parseDouble(separated[0]), Double.parseDouble(separated[1]), 1);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            if (addresses.isEmpty()) {
                                                deviceLocation.setText("Waiting for Location");
                                            } else {
                                                if (addresses.size() > 0) {
                                                    String addressName = addresses.get(0).getFeatureName() + "," + addresses.get(0).getLocality() + "," + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();

                                                    deviceLocation.setText(addressName);

                                                }
                                            }
                                            timeDate.setText(item.getUpdatdAt());
                                            lostInfo.setText("Lost item Info");
                                            mapButton.setVisibility(View.VISIBLE);
                                            foundItButton.setVisibility(View.VISIBLE);
                                            if (item.getIsFound() == true) {
                                                notifyUser(deviceLocation.getText().toString());
                                                item.setIsFound(false);
                                                mDevicesTable.update(item);

                                            }
                                        }
                                    } else {
                                        f();
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

            }
        }, 0, 2000);


    }




    public void f(){
        lostInfo.setText("NO lost devices found for you");
        deviceName.setText(" ");
        deviceAddress.setText(" ");
        deviceLocation.setText(" ");
        timeDate.setText(" ");
        mapButton.setVisibility(View.GONE);
        foundItButton.setVisibility(View.GONE);
    }

    public MobileServiceList<devicesTable> checkItemInTable() throws ExecutionException, InterruptedException {
        MobileServiceList<devicesTable> result = mDevicesTable
                .where().field("userID").eq(userId).and().field("isLost").eq(val(true)).execute()
                .get();

        return result;
    }

    public MobileServiceList<devicesTable> getItemInTable() throws ExecutionException, InterruptedException {
        MobileServiceList<devicesTable> result = mDevicesTable
                .where().field("userID").eq(userId).execute()
                .get();
        return result;
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
                    tableDefinition.put("updatedAt", ColumnDataType.String);
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

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
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

    private void notifyUser(String location){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_1_ID)
                .setSmallIcon(R.drawable.alert_icon)
                .setContentTitle("KIC")
                .setContentText("your Device at " + location)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                //.addAction(R.mipmap.ic_launcher, "Lost", actionIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        notificationManager.notify(1, builder.build());

    }

}
