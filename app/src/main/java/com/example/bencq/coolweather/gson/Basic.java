package com.example.bencq.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Bencq on 2018/04/13.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update
    {
        @SerializedName("loc")
        public String updateTime;
    }
}
