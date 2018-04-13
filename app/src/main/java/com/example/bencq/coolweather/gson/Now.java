package com.example.bencq.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Bencq on 2018/04/13.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More
    {
        @SerializedName("txt")
        public String info;
    }
}
