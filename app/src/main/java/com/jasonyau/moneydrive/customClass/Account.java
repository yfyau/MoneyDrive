package com.jasonyau.moneydrive.customClass;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class Account {

    String acId;
    String userId;
    String acName;
    String acType;
    String acDollar;
    double acAsset;

    public Account(){

    }

    public Account(String acId, String userId, String acName, String acType,  String acDollar) {
        this.acId = acId;
        this.userId = userId;
        this.acName = acName;
        this.acType = acType;
        this.acDollar = acDollar;
        this.acAsset = 0;
    }

    public Account(String acId, String userId, String acName, String acType,  String acDollar, Double acAsset) {
        this.acId = acId;
        this.userId = userId;
        this.acName = acName;
        this.acType = acType;
        this.acDollar = acDollar;
        this.acAsset = acAsset;
    }

    public String getAcId() {return  acId;}
    public String getUserId(){
        return userId;
    }
    public String getAcName(){
        return acName;
    }
    public String getAcType(){
        return acType;
    }
    public String getAcDollar(){
        return acDollar;
    }
    public double getAcAsset(){
        return acAsset;
    }
}
