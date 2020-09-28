package com.company.kic;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.MyViewHolder> {
    interface ItemClickListener{
        void onItemClick(View view, int position);
    }
    private static ItemClickListener mCallBack;
    private List<String> nameList;
    private List<String> addressList;

    //private MobileServiceClient mClient;
    //private MobileServiceTable<devicesTable> mDevicesTable;

    public DevicesAdapter(ConnectionFragment fragment){
        nameList=new ArrayList<>();
        addressList=new ArrayList<>();
        mCallBack=fragment;
        /*try {
            mClient = new MobileServiceClient("https://kic.azurewebsites.net",
                    fragment.getActivity());


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

            //initLocalStore().get();


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (MobileServiceLocalStoreException e) {
            e.printStackTrace();
        }*/
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_item,viewGroup,false);
        return new MyViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.nameText.setText(nameList.get(i));
        myViewHolder.addresstext.setText(addressList.get(i));
        if(nameList.get(i)!=null) {
            myViewHolder.imageView.setImageDrawable(getDrawable(nameList.get(i).substring(0, 1)));
        }else {
            myViewHolder.imageView.setImageDrawable(getDrawable("u"));
            /*myViewHolder.imageView.setVisibility(View.GONE);
            myViewHolder.nameText.setVisibility(View.GONE);
            myViewHolder.addresstext.setVisibility(View.GONE);
            myViewHolder.connectButton.setVisibility(View.GONE);
            myViewHolder.cardView.setVisibility(View.GONE);
            myViewHolder.itemView.setVisibility(View.GONE);*/
        }
    }

    /*private void checkItem(final String name, final String address) {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final MobileServiceList<devicesTable> entity = checkItemInTable(name,address);
                    if(entity.size()!=0){
                        for (devicesTable item : entity) {
                            item.setLocation(ConnectionFragment.tvLati+","+ConnectionFragment.tvLongi);
                            mDevicesTable.update(item).get();
                        }
                    }



                } catch (final Exception e) {
                    //createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);
    }*/



    /*public MobileServiceList<devicesTable> checkItemInTable(String name, String address) throws ExecutionException, InterruptedException {
        MobileServiceList<devicesTable> result =  mDevicesTable
                .where().field("deviceName").eq(name).and().field("deviceAddress").eq(address).and().field("isLost").eq(true).execute()
                .get();

        return result;
    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }*/

   /* private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

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
    }*/

    /*private class ProgressFilter implements ServiceFilter {

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    //if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
                        }
                    });

                    resultFuture.set(response);
                }
            });

            return resultFuture;
        }
    }*/

    public void add(String name,String address){
        nameList.add(name);
        addressList.add(address);
    }
    public void clear(){
        nameList.clear();
        addressList.clear();
    }
    public String getAddress(int position){
        return this.addressList.get(position);
    }

    public String getName(int position){
        return this.nameList.get(position);
    }


    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public List<String> getAdressesList() {
        return addressList;
    }
    public List<String> getNamesList() {
        return nameList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CardView cardView;
        TextView nameText,addresstext;
        ImageView imageView;
        Button connectButton;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView=itemView.findViewById(R.id.device_list_card_view);
            nameText=itemView.findViewById(R.id.device_list_name_text_view);
            addresstext=itemView.findViewById(R.id.device_list_address_text_view);
            connectButton=itemView.findViewById(R.id.device_list_connect_button);
            imageView=itemView.findViewById(R.id.text_drawable_image_view);
            cardView.setOnClickListener(this);
            connectButton.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            if(getAdapterPosition()!=-1){
                mCallBack.onItemClick(v,getAdapterPosition());
            }
        }
    }
    public TextDrawable getDrawable(String text){
        ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
        int color = generator.getColor(text);
        TextDrawable.IBuilder builder = TextDrawable.builder()
                .beginConfig()
                .endConfig()
                .round();
        TextDrawable ic = builder.build(text,color);
        return ic;
    }


}