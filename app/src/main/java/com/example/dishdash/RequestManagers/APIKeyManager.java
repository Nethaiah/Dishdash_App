package com.example.dishdash.RequestManagers;

/*
maestrojomar143@gmail.com = b43efc8c5839435e9e50b7bed84d5c56
hyroinb213@gmail.com = e405618a4ad247ea9db3f2aaf08af3f3
kenkidaiki69@gmail.com = 037a892edbd847db8838176c40e9b871
Jomarmaestro132@gmail.com = 2d13f78af3d24b2b97754d47afd1f11a
Maestrojomar123@gmail.com = 3aab1291d39d45b4b16d936df69acc9f
Jomarmaestro467@gmail.com = ecab25f1cef44c8c9d955b844202d859
Maestrojomar0602@gmail.com = 82b63bc2240540a3b02509689785632f
hyroinplays@gmail.com = 76526f245a084acdaf76b617a16245f3
migspogi053@gmail.com = 6f134e698b7f48688ce7cb2aae51429e
hairowanbariri@gmail.com = e0e1c14c8e8f44299a5edc0b5bf31e15
balilihyroin6@gmail.com = a32550e03de74c1abf7916f5f1dcc523
*/

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class APIKeyManager {
    List<String> apiKeys = Arrays.asList(
            "b43efc8c5839435e9e50b7bed84d5c56", "e405618a4ad247ea9db3f2aaf08af3f3",
            "037a892edbd847db8838176c40e9b871", "2d13f78af3d24b2b97754d47afd1f11a",
            "3aab1291d39d45b4b16d936df69acc9f", "ecab25f1cef44c8c9d955b844202d859",
            "82b63bc2240540a3b02509689785632f", "76526f245a084acdaf76b617a16245f3",
            "6f134e698b7f48688ce7cb2aae51429e", "e0e1c14c8e8f44299a5edc0b5bf31e15",
            "a32550e03de74c1abf7916f5f1dcc523");
    int currentIndex = 0;
    SharedPreferences sharedPreferences;

    public APIKeyManager(Context context){
        sharedPreferences = context.getSharedPreferences("API_key_preferences", Context.MODE_PRIVATE);
        checkAndResetDaily();
    }

    private void checkAndResetDaily(){
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastUsedDate = sharedPreferences.getString("last_used_date", "");

        if (!today.equals(lastUsedDate)) {
            currentIndex = 0;
            sharedPreferences.edit().putString("last_used_date", today).apply();
        }
    }

    public String getCurrentApiKey() {
        return apiKeys.get(currentIndex);
    }

    public boolean switchToNextKey() {
        if (currentIndex < apiKeys.size() - 1) {
            currentIndex++;
            return true;
        } else {
            return false;
        }
    }
}
