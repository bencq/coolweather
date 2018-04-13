package com.example.bencq.coolweather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bencq.coolweather.db.City;
import com.example.bencq.coolweather.db.County;
import com.example.bencq.coolweather.db.Province;
import com.example.bencq.coolweather.util.HttpUtil;
import com.example.bencq.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Bencq on 2018/04/12.
 */

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView textView_title;
    private Button button_back;
    private ListView listView;
    private ArrayAdapter<String> stringArrayAdapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectedProvince;
    private City selectedCity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        textView_title = (TextView)(view.findViewById(R.id.textView_title));
        button_back = (Button)(view.findViewById(R.id.button_back));
        listView = (ListView)(view.findViewById(R.id.listView));
        stringArrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(stringArrayAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(currentLevel == LEVEL_PROVINCE)
                {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }
                else if(currentLevel == LEVEL_CITY)
                {
                    selectedCity = cityList.get(position);
                    queryCounties();
                }
                else if(currentLevel == LEVEL_COUNTY)
                {
                    String weatherID = countyList.get(position).getWeatherId();
                    Intent intent = new Intent(getActivity(), WeatherActivity.class);
                    intent.putExtra("weather_id",weatherID);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        button_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLevel == LEVEL_COUNTY)
                {
                    queryCities();
                }
                else if(currentLevel == LEVEL_CITY)
                {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        textView_title.setText("中国");
        button_back.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if(provinceList.size() > 0)
        {
            dataList.clear();
            for (Province province : provinceList)
            {
                dataList.add(province.getProvinceName());
            }
            stringArrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }
        else
        {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }

    }

    private void queryCities() {
        textView_title.setText(selectedProvince.getProvinceName());
        button_back.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectedProvince.getId())).find(City.class);
        if(cityList.size() > 0)
        {
            dataList.clear();
            for(City city : cityList)
            {
                dataList.add(city.getCityName());
            }
            stringArrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
        else
        {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    private void queryCounties() {
        textView_title.setText(selectedCity.getCityName());
        button_back.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectedCity.getId())).find(County.class);
        if(countyList.size() > 0)
        {
            dataList.clear();
            for(County county : countyList)
            {
                dataList.add(county.getCountyName());
            }
            stringArrayAdapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }
        else
        {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type))
                {
                    result = Utility.handleProvinceResponse(responseText);
                }
                else if("city".equals(type))
                {
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }
                else if("county".equals(type))
                {
                    result = Utility.handleCountyResponse(responseText, selectedCity.getId());
                }
                if(result)
                {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type))
                            {
                                queryProvinces();
                            }
                            else if("city".equals(type))
                            {
                                queryCities();
                            }
                            else if("county".equals(type))
                            {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showProgressDialog() {
        if(progressDialog == null)
        {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
    }

    private void closeProgressDialog() {
        if(progressDialog != null)
        {
            progressDialog.dismiss();
        }
    }


}
