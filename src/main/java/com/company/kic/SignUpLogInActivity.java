package com.company.kic;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import okhttp3.OkHttpClient;


import org.json.JSONException;
import org.json.JSONObject;


public class SignUpLogInActivity extends AppCompatActivity {

    private EditText mUserName;
    private EditText mPassword;
    private Button mSignUpButton;
    private Button mLoInButton;
    private LoginButton fbLoginButton;
    CallbackManager callbackManager;
    public String username;
    public String email;


    private MobileServiceClient mClient;
    private MobileServiceTable<SignUpItem> mUsersTable;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup_login);

        mUserName = (EditText) findViewById(R.id.editTextUserName);
        mPassword = (EditText) findViewById(R.id.editTextPassword);
        mSignUpButton = (Button) findViewById(R.id.signUpButton);
        mLoInButton = (Button) findViewById(R.id.logInbutton);
        fbLoginButton = findViewById(R.id.login_button);
        fbLoginButton.setPermissions(Arrays.asList("public_profile", "email"));

        //FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(this);

        callbackManager = CallbackManager.Factory.create();
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                String accessToken = loginResult.getAccessToken().getToken();


                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        final SignUpItem item=getFacebookData(object);



                        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    final SignUpItem entity = addItemInTable(item);

                                } catch (final Exception e) {
                                    createAndShowDialogFromTask(e, "Error");
                                }
                                return null;
                            }
                        };

                        runAsyncTask(task);
                    }
                });

                Bundle parameters=request.getParameters();
                parameters.putString("fields","id,first_name,email");
                request.executeAsync();



                Intent intent = new Intent(SignUpLogInActivity.this, MainActivity.class);
                intent.putExtra("userId",AccessToken.getCurrentAccessToken().getUserId());
                startActivity(intent);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                String toastMessage = error.getMessage();
                Toast.makeText(SignUpLogInActivity.this, toastMessage, Toast.LENGTH_LONG).show();
            }
        });

        if(AccessToken.getCurrentAccessToken() !=null){
            Intent intent = new Intent(SignUpLogInActivity.this, MainActivity.class);
            String s=AccessToken.getCurrentAccessToken().getUserId();
            intent.putExtra("userId",AccessToken.getCurrentAccessToken().getUserId());
            startActivity(intent);
        }

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


            mUsersTable = mClient.getTable(SignUpItem.class);

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


        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    signUpFunction();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        mLoInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logInFunction();
            }
        });

    }



    private SignUpItem getFacebookData(JSONObject object){
        try {
                username=object.getString("first_name");
                email=object.getString("email");
                final SignUpItem myItem = new SignUpItem();
                myItem.setUserName(username);
                myItem.setPassword(email);
                myItem.setId(AccessToken.getCurrentAccessToken().getUserId());
                return  myItem;
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void signUpFunction() throws ExecutionException, InterruptedException {

        if (mClient == null) {
            return;
        }

        // Create a new item
        final SignUpItem myItem = new SignUpItem();
        myItem.setUserName(mUserName.getText().toString());
        myItem.setPassword(mPassword.getText().toString());


        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final SignUpItem entity = addItemInTable(myItem);

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);

        mUserName.setText("");
        mPassword.setText("");

        Intent intent=new Intent(this, MainActivity.class);
        startActivity(intent);

    }

    public SignUpItem addItemInTable(SignUpItem item) throws ExecutionException, InterruptedException {
        SignUpItem entity = mUsersTable.insert(item).get();
        return entity;
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
                    tableDefinition.put("username", ColumnDataType.String);
                    tableDefinition.put("password", ColumnDataType.String);

                    localStore.defineTable("SignUpItem", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }



    public void logInFunction(){


        if (mClient == null) {
            return;
        }

        // Create a new item
        final SignUpItem myItem = new SignUpItem();
        myItem.setUserName(mUserName.getText().toString());
        myItem.setPassword(mPassword.getText().toString());



        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final MobileServiceList<SignUpItem> entity = checkItemInTable(myItem);
                    f(entity);



                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);



    }

    public void f(MobileServiceList<SignUpItem> entity){

        if(entity.size()==0){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.create().show();
        }
        else{
            Intent intent=new Intent(this, MainActivity.class);
            intent.putExtra("userId",entity.get(0).getId());
            startActivity(intent);
        }

    }

    public MobileServiceList<SignUpItem> checkItemInTable(SignUpItem item) throws ExecutionException, InterruptedException {
        MobileServiceList<SignUpItem> result = mUsersTable
                .where().field("username").eq(item.getUserName()).and().field("password").eq(item.getPassword()).execute()
                .get();

        return result;
    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }

    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }


    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if(exception.getCause() != null){
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    private class ProgressFilter implements ServiceFilter {

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);
    }
}
