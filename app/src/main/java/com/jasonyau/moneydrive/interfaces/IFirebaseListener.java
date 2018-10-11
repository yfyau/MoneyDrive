package com.jasonyau.moneydrive.interfaces;

import com.jasonyau.moneydrive.customClass.Account;
import com.jasonyau.moneydrive.customClass.Record;

import java.util.List;
import java.util.Set;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public interface IFirebaseListener {
    void onCallback(String value);
    void onCallback(Double value);
    void onCallback(List value);
//    void onCallback(List<Record> value);
//    void onCallback(List<Account> value);
}
