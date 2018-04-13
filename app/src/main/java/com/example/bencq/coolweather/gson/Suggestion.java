package com.example.bencq.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Bencq on 2018/04/13.
 */

public class Suggestion {
    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    public Sport sport;

    public class Comfort
    {
        @SerializedName("txt")
        public String info;
    }

    public class CarWash
    {
        @SerializedName("txt")
        public String info;
    }

    public class Sport
    {
        @SerializedName("txt")
        public String info;
    }
}
