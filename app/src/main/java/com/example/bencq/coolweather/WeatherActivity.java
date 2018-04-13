package com.example.bencq.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bencq.coolweather.gson.Forecast;
import com.example.bencq.coolweather.gson.Weather;
import com.example.bencq.coolweather.util.HttpUtil;
import com.example.bencq.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout swipeRefreshLayout;

    private Button button_navigation;
    private ScrollView scrollView_weatherLayout;
    private TextView textView_cityTitle;
    private TextView textView_updateTimeTitle;
    private TextView textView_degree;
    private TextView textView_weatherInfo;
    private LinearLayout linearLayout_forecast;
    private TextView textView_aqi;
    private TextView textView_pm25;
    private TextView textView_comfort;
    private TextView textView_carWash;
    private TextView textView_sport;
    private ImageView imageView_bingPic;

    public static final String apiUrl = "http://guolin.tech/api/weather";
    public static final String apiKey = "d5d70a1396da4f048d5688ba869f12ce";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21)
        {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        //init

        swipeRefreshLayout = (SwipeRefreshLayout)(findViewById(R.id.swipeRefreshLayout));
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);

        drawerLayout = (DrawerLayout)(findViewById(R.id.drawerLayout));
        button_navigation = (Button)(findViewById(R.id.button_navigation));
        button_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        scrollView_weatherLayout = (ScrollView)(findViewById(R.id.scrollView_weatherLayout));
        textView_cityTitle = (TextView)(findViewById(R.id.textView_cityTitle));
        textView_updateTimeTitle = (TextView)(findViewById(R.id.textView_updateTimeTitle));
        textView_degree = (TextView)(findViewById(R.id.textView_degree));
        textView_weatherInfo = (TextView)(findViewById(R.id.textView_weatherInfo));
        linearLayout_forecast = (LinearLayout)(findViewById(R.id.linearLayout_forecast));
        textView_aqi = (TextView)(findViewById(R.id.textView_aqi));
        textView_pm25 = (TextView)(findViewById(R.id.textView_pm25));
        textView_comfort = (TextView)(findViewById(R.id.textView_comfort));
        textView_carWash = (TextView)(findViewById(R.id.textView_carWash));
        textView_sport = (TextView)(findViewById(R.id.textView_sport));
        imageView_bingPic = (ImageView)(findViewById(R.id.imageView_bingPic));



        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
        String weatherString = sharedPreferences.getString("weather", null);
        final String weatherId;
        if(weatherString != null)
        {
            //有缓存直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }
        else
        {
            //没有缓存时到服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            scrollView_weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        String bingPicUrl = sharedPreferences.getString("bingPic", null);
        if(bingPicUrl != null)
        {
            Glide.with(WeatherActivity.this).load(bingPicUrl).into(imageView_bingPic);
        }
        else
        {
            loadBingPic();
        }
    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherID)
    {
        String weatherUrl = apiUrl + "?cityid=" + weatherID + "&key=" + apiKey;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather != null && "ok".equalsIgnoreCase(weather.status))
                        {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }
                        else
                        {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });

        loadBingPic();
    }

    /**
     * 处理并展示Weather实体类中的数据
     */
    private void showWeatherInfo(Weather weather)
    {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime;
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        textView_cityTitle.setText(cityName);
        textView_updateTimeTitle.setText(updateTime);
        textView_degree.setText(degree);
        textView_weatherInfo.setText(weatherInfo);
        linearLayout_forecast.removeAllViews();
        for(Forecast forecast : weather.forecastList)
        {
            View view = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.forecast_item, linearLayout_forecast, false);
            TextView textView_date = (TextView)(view.findViewById(R.id.textView_date));
            TextView textView_info = (TextView)(view.findViewById(R.id.textView_info));
            TextView textView_max = (TextView)(view.findViewById(R.id.textView_max));
            TextView textView_min = (TextView)(view.findViewById(R.id.textView_min));

            textView_date.setText(forecast.date);
            textView_info.setText(forecast.more.info);
            textView_max.setText(forecast.temperature.max);
            textView_min.setText(forecast.temperature.min);
            linearLayout_forecast.addView(view);
        }
        if(weather.aqi != null)
        {
            textView_aqi.setText(weather.aqi.city.aqi);
            textView_pm25.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度:" + weather.suggestion.comfort.info;
        String carWash = "洗车指数:" + weather.suggestion.carWash;
        String sport = "运动建议:" + weather.suggestion.sport;

        textView_comfort.setText(comfort);
        textView_carWash.setText(carWash);
        textView_sport.setText(sport);

        scrollView_weatherLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic()
    {
        String requestBingPicUrl = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPicUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPicUrl = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bingPic", bingPicUrl);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPicUrl).into(imageView_bingPic);
                    }
                });
            }
        });
    }


}
