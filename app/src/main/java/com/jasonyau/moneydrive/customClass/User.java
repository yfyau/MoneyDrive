package com.jasonyau.moneydrive.customClass;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class User {

    String userId;
    String password;

    List<String> accountIds;

    String lastUpdated;

    public User(){

    }

    public User(String userId, String password) {
        this.userId = userId;
        this.password = password;

        this.accountIds = null;

        this.lastUpdated = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss Z ", Locale.getDefault()).format(new Date());
    }

    public User(String userId, String password, List<String> accountIds) {
        this.userId = userId;
        this.password = password;

        this.accountIds = accountIds;

        this.lastUpdated = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss Z ", Locale.getDefault()).format(new Date());
    }

    public String getUserId(){
        return userId;
    }
    public String getPassword(){
        return password;
    }
    public String getLastUpdated(){
        return lastUpdated;
    }
}
