package com.finrobotics.neyyasdk.core.preference;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages the preference data
 * Created by zac on 15/12/15.
 */
public class PreferenceManager {
    private String mFileName = "CallAssistantPreference";
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor sEditor;

    public PreferenceManager(Context context) {
        mSharedPreferences = context.getSharedPreferences(mFileName, Context.MODE_PRIVATE);
    }

    public void setNeyyaName(String name) {
        sEditor = mSharedPreferences.edit();
        sEditor.putString("neyyaname", name);
        sEditor.commit();
    }

    public String getNeyyaName() {
        return mSharedPreferences.getString("neyyaname", "");
    }

    public void setNeyyaAddress(String address) {
        sEditor = mSharedPreferences.edit();
        sEditor.putString("neyyaaddress", address);
        sEditor.commit();
    }

    public String getNeyyaAddress() {
        return mSharedPreferences.getString("neyyaaddress", "");
    }


}
