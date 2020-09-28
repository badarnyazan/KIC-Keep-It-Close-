package com.company.kic;

import java.io.IOException;
import java.lang.Runnable;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.OnSuccessListener;
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
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static com.company.kic.App.CHANNEL_1_ID;
import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionFragment extends Fragment implements DevicesAdapter.ItemClickListener,LocationListener {

    private static final String TAG = ConnectionFragment.class.getSimpleName();
    private static final int SCAN_PERIOD = 3000;
    private boolean mScanning = false;
    private RecyclerView mRecycleView;
    private DevicesAdapter mAdapter;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mDevices;
    private MainActivity mHome;
    private Button lostItem;
    public String[] separated;
    //private Button scanLostItems;


    public static String tvLongi;
    public static String tvLati;
    LocationManager locationManager;


    private MobileServiceClient mClient;
    private MobileServiceTable<devicesTable> mDevicesTable;
    private NotificationManagerCompat notificationManager;

    private BluetoothAdapter.LeScanCallback mLEScanCallBack = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (!mDevices.contains(device)) {
                mAdapter.add(device.getName(), device.getAddress());
                mAdapter.notifyDataSetChanged();
                mDevices.add(device);
                Log.d(TAG, "Device found");
            }
        }
    };

    public ConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mHome = (MainActivity) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_connection, container, false);
        mRecycleView = rootView.findViewById(R.id.recycle_view);
        lostItem = rootView.findViewById(R.id.lost_button);
        //scanLostItems=rootView.findViewById(R.id.scan_lost_button);

        notificationManager =  NotificationManagerCompat.from(getActivity());

        lostItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getActivity(), LostActivity.class);
                in.putExtra("notificationReciever", "no");
                in.putExtra("userId", mHome.userId);
                startActivity(in);
            }
        });

        /*scanLostItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> addresslst=mAdapter.getAdressesList();
                final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            final MobileServiceList<devicesTable> entity = checkItemInTableIfLost();
                            if (entity.size() != 0) {
                                for (devicesTable item : entity) {
                                    for (String macAddress: addresslst){
                                        if(macAddress.equals(item.getDeviceAddress())){
                                            item.setLocation(tvLati.concat(",").concat(tvLongi));
                                            item.setIsFound(true);
                                            updateitemLocation(item);
                                        }
                                    }
                                }
                            }


                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };

                runAsyncTask(task);
                Toast.makeText(getActivity(), "Scan Succesfully ended", Toast.LENGTH_SHORT).show();
            }

    });*/



        mHandler = new Handler();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDevices = new ArrayList<>();
        Log.d(TAG, "onCreateView called");

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



        Timer timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {

                final List<String> addresslst=mAdapter.getAdressesList();
                final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            final MobileServiceList<devicesTable> entity = checkItemInTableIfLost();
                            if (entity.size() != 0) {
                                for (devicesTable item : entity) {
                                    for (String macAddress: addresslst){
                                        if(macAddress.equals(item.getDeviceAddress())){
                                            item.setLocation(tvLati.concat(",").concat(tvLongi));
                                            item.setIsFound(true);
                                            updateitemLocation(item);
                                        }
                                    }
                                }
                            }


                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };

                runAsyncTask(task);


            }
        }, 2000, 5000);

        /*timer.schedule(new TimerTask() {
            @Override
            public void run() {
                final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            final MobileServiceList<devicesTable> entity = checkItemInTable();
                            if (entity.size() != 0) {
                                for(devicesTable item: entity) {
                                    String mAddress = item.getLocation();
                                    separated = mAddress.split(",");
                                    Geocoder geo = new Geocoder(mHome.getApplicationContext(), Locale.getDefault());
                                    List<Address> addresses = null;
                                    try {
                                        addresses = geo.getFromLocation(Double.parseDouble(separated[0]), Double.parseDouble(separated[1]), 1);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    if (addresses.isEmpty()) {
                                    } else {
                                        if (addresses.size() > 0) {
                                            String addressName = addresses.get(0).getFeatureName() + "," + addresses.get(0).getLocality() + "," + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryName();

                                            notifyUser(addressName);

                                        }
                                    }

                                }
                            }


                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
            }

        }, 1000, 4000);*/


        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAdapter = new DevicesAdapter(this);
        mRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecycleView.setHasFixedSize(true);
        mRecycleView.setAdapter(mAdapter);
        if (!mBluetoothAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 100);
            Log.d(TAG, "requesting bluetooth");
        } else {
            // checkLocationPermission();
        }


    }

    public void updateitemLocation(devicesTable item) throws ExecutionException, InterruptedException {
        mDevicesTable.update(item).get();
    }

    public MobileServiceList<devicesTable> checkItemInTableIfLost() throws ExecutionException, InterruptedException {
        return mDevicesTable.where().field("isLost").eq(true).execute().get();
    }

    public MobileServiceList<devicesTable> checkItemInTable() throws ExecutionException, InterruptedException {
        return mDevicesTable.where().field("userID").eq(mHome.userId).and().field("isFound").eq(true).execute().get();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActvityResult found");
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                scanLeDevice(true);
                Log.d(TAG, "Scan started");
            } else {
                if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(getActivity(), "You should turn on bluetooth to use this application,try again later", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                    Log.d(TAG, "cancel clicked");
                }
            }
        }


    }



    @Override
    public void onStart() {
        super.onStart();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(true);
            Log.d(TAG, "Scan started");
        }
        Log.d(TAG, "onStart called");

    }

    @Override
    public void onStop() {
        if (mScanning) {
            scanLeDevice(false);
            Log.d(TAG, "Scan stopped");
        }
        super.onStop();
        Log.d(TAG, "onStop called");



    }

    /* Request updates at startup */
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

    @Override
    public void onItemClick(View view, int position) {
        if (mScanning) scanLeDevice(false);

        if (!mHome.isConnected) {
            mHome.getmUartService().connect(mAdapter.getAddress(position));
            devicesTable item = new devicesTable();
            item.setDeviceAddress(mAdapter.getAddress(position));
            item.setDeviceName(mAdapter.getName(position));
            item.setIsLost(false);
            item.setLocation(tvLati.concat(",").concat(tvLongi));
            item.setUserId(mHome.userId);
            item.setIsFound(false);
            addItemToTable(item);
        }


    }

    private void addItemToTable(final devicesTable item) {
        if (mClient == null) {
            return;
        }
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceList<devicesTable> entity=checkItemInTable(item.getDeviceAddress());
                    if(entity.size()==0)
                         mDevicesTable.insert(item);

                } catch (final Exception e) {
                    //createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);
    }

    public MobileServiceList<devicesTable> checkItemInTable(String deviceAddress) throws ExecutionException, InterruptedException {
        MobileServiceList<devicesTable> result = mDevicesTable
                .where().field("deviceAddress").eq(deviceAddress).execute()
                .get();

        return result;
    }

    private void scanLeDevice(boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLEScanCallBack);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLEScanCallBack);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLEScanCallBack);

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
                    tableDefinition.put("deviceAddress", ColumnDataType.String);
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


    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    @Override
    public void onLocationChanged(Location location) {
        tvLongi = String.valueOf(location.getLongitude());
        tvLati = String.valueOf(location.getLatitude());
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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity(),CHANNEL_1_ID)
                .setSmallIcon(R.drawable.alert_icon)
                .setContentTitle("KIC")
                .setContentText("your Device at " + location)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                //.addAction(R.mipmap.ic_launcher, "Lost", actionIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);
        //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getActivity());
        notificationManager.notify(1, builder.build());

    }



}
