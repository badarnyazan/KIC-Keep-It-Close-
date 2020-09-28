package com.company.kic;

import java.sql.Date;
import java.sql.Time;

public class devicesTable {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("userID")
    private String mUserId;

    @com.google.gson.annotations.SerializedName("deviceName")
    private String mDeviceName;

    @com.google.gson.annotations.SerializedName("deviceAddress")
    private String mDeviceAddress;

    @com.google.gson.annotations.SerializedName("location")
    private String mLocation;

    @com.google.gson.annotations.SerializedName("isLost")
    private boolean mIsLost;

    @com.google.gson.annotations.SerializedName("isFound")
    private boolean mIsFound;

    @com.google.gson.annotations.SerializedName("updatedAt")
    private String mUpdatedAt;



    public devicesTable() {

    }

    public String getId(){
        return mId;
    }
    public String getUserId(){
        return mUserId;
    }
    public String getDeviceName(){
        return mDeviceName;
    }
    public String getDeviceAddress(){
        return mDeviceAddress;
    }
    public String getLocation(){
        return mLocation;
    }
    public boolean getIsLost(){
        return mIsLost;
    }
    public boolean getIsFound(){
        return mIsFound;
    }
    public String getUpdatdAt(){
        return mUpdatedAt;
    }

    public void setId(String id){
        mUserId=id;
    }
    public void setUserId(String userId){
        mUserId=userId;
    }
    public void setDeviceName(String deviceName){
         mDeviceName=deviceName;
    }
    public void setDeviceAddress(String deviceAddress){
        mDeviceAddress=deviceAddress;
    }
    public void setLocation(String location){
         mLocation=location;
    }
    public void setIsLost(boolean isLost){
         mIsLost=isLost;
    }
    public void setIsFound(boolean isFound){
        mIsFound=isFound;
    }




    @Override
    public boolean equals(Object o) {
        return o instanceof devicesTable && ((devicesTable) o).mId == mId;
    }


}
