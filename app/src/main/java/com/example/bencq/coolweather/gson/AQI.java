package com.example.bencq.coolweather.gson;

/**
 * Created by Bencq on 2018/04/13.
 */

public class AQI {
    public AQICity city;

    public class AQICity
    {
        public String aqi;
        public String pm25;
    }
}
