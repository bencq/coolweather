package com.example.bencq.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.bencq.coolweather.MainActivity;
import com.example.bencq.coolweather.WeatherActivity;
import com.example.bencq.coolweather.gson.Weather;
import com.example.bencq.coolweather.util.HttpUtil;
import com.example.bencq.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager alarmManager = (AlarmManager)(getSystemService(ALARM_SERVICE));
        int eightHours = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + eightHours;
        Intent i = new Intent(AutoUpdateService.this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(AutoUpdateService.this,0,i,0);
        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
        return super.onStartCommand(intent,flags,startId);
    }

    private void updateBingPic() {
        HttpUtil.sendOkHttpRequest(WeatherActivity.requestBingPicUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bingPic",bingPic);
                editor.apply();
            }
        });
    }


    /**
     * 更新天气消息
     */
    private void updateWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
        String weatherString = sharedPreferences.getString("weather",null);
        if(weatherString != null)
        {
            //有缓存时直接解析天气数据

            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherUrl = WeatherActivity.apiUrl + "?cityid=" + weatherId + "&key=" + WeatherActivity.apiKey;

            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather = Utility.handleWeatherResponse(responseText);
                    if(weather != null && "ok".equalsIgnoreCase(weather.status))
                    {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });
        }
    }
}
