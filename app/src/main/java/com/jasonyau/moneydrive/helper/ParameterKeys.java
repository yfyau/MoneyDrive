package com.jasonyau.moneydrive.helper;

/**
 *      # COMP 4521    #  YAU Yat Fung        20371106          yfyau@connect.ust.hk
 */

public class ParameterKeys
{

    // Save Logged in User id
    public static final String USER_ID        = "user_id";
    // First Open Date
    public static final String INIT_DATE       = "init_date";
    // User defined Fields
    public static final String FIELDS       = "fields";

    // Alarm Data
    public static final String ALARM_STATUS = "alarm_status";
    public static final String ALARM_HOUR = "alarm_hour";
    public static final String ALARM_MIN = "alarm_min";

    // The chosen ISO code of the currency (string)
    public static final String CURRENCY_ISO    = "currency_iso";
    // Default amount use for low money warning (can be changed in settings)
    public static final int DEFAULT_LOW_MONEY_WARNING_AMOUNT = 100;
    // Warning limit for low money on account (int)
    public static final String LOW_MONEY_WARNING_AMOUNT = "low_money_warning_amount";
    // Are animations enabled (boolean)
    public static final String ANIMATIONS_ENABLED = "animation_enabled";
}
