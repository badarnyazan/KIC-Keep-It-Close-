package com.company.kic;

public class SignUpItem {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("username")
    private String mUserName;

    @com.google.gson.annotations.SerializedName("password")
    private String mPassword;


    public SignUpItem() {

    }


        @Override
    public String toString() {
        return getUserName();
    }

    public SignUpItem(String id, String userName, String password) {
        this.setId(id);
        this.setUserName(userName);
        this.setPassword(password);
    }


    public String getUserName(){
        return mUserName;
    }

    public String getPassword(){
        return mPassword;
    }

    public String getId() {
        return mId;
    }

    public final void setUserName(String UserName) {
        mUserName = UserName;
    }

    public final void setId(String id){
        mId = id;
    }

    public final void setPassword(String password){
        mPassword=password;
    }


    @Override
    public boolean equals(Object o) {
        return o instanceof SignUpItem && ((SignUpItem) o).mId == mId;
    }




}
