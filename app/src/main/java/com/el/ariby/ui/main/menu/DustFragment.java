package com.el.ariby.ui.main.menu;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.el.ariby.R;
import com.el.ariby.databinding.FragmentDustBinding;
import com.el.ariby.ui.api.CoordApi;
import com.el.ariby.ui.api.DustApi;
import com.el.ariby.ui.api.GeoApi;
import com.el.ariby.ui.api.MeasureApi;
import com.el.ariby.ui.api.SelfCall;
import com.el.ariby.ui.api.WeatherApi;
import com.el.ariby.ui.api.response.CoordRepoResponse;
import com.el.ariby.ui.api.response.DustRepoResponse;
import com.el.ariby.ui.api.response.GeoRepoResponse;
import com.el.ariby.ui.api.response.MeasureRepoResponse;
import com.el.ariby.ui.api.response.WeatherRepoResponse;
import com.el.ariby.ui.main.menu.dust.DustHourData;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DustFragment extends Fragment {
    public final static int SMILE = 16;
    public final static int NORMAL = 36;
    public final static int BAD = 76;
    public final static int TO_GRID = 0;
    public final static int TO_GPS = 1;

    private FragmentDustBinding mBinding;
    String openKey = "vMgzVOM7K3D3t89QY%2F%2FtYxGc7fTDhMi3AkGC" +
            "qakZut7sDmQCzfeWtcT9NDRbrR4dK8OBJsR5d4QwhZkn%2FeTZ3w%3D%3D";
    String kakaoKey = "KakaoAK e880d656790ed7e10098f0742679154e";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dust, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBinding = FragmentDustBinding.bind(getView());

        startLocationService();
        getWeatherData("20190528","1930","60","127");

    }

    private void getCoord(String x, String y) {
        Retrofit retrofit = SelfCall.createRetrofit(CoordApi.BASEURL);

        CoordApi apiService = retrofit.create(CoordApi.class);
        Call<CoordRepoResponse> call =
                apiService.getCoord(kakaoKey, x, y, "WGS84", "TM");

        call.enqueue(new Callback<CoordRepoResponse>() {
            @Override
            public void onResponse(Call<CoordRepoResponse> call,
                                   Response<CoordRepoResponse> response) {
                if (response.isSuccessful()) {
                    CoordRepoResponse repo = response.body();
                    String x = Double.toString(repo.getDocuments().get(0).getX());
                    String y = Double.toString(repo.getDocuments().get(0).getY());
                    getMeasure(x, y, "json");
                }
            }

            @Override
            public void onFailure(Call<CoordRepoResponse> call, Throwable t) {

            }
        });
    }

    private void getGeo(String x, String y, String cord) {
        Retrofit retrofit = SelfCall.createRetrofit(GeoApi.BASEURL);

        GeoApi apiService = retrofit.create(GeoApi.class);
        Call<GeoRepoResponse> call = apiService.getGeo(kakaoKey, x, y, cord);
        Log.d("call : ", call.toString());

        call.enqueue(new Callback<GeoRepoResponse>() {
            @Override
            public void onResponse(Call<GeoRepoResponse> call,
                                   Response<GeoRepoResponse> response) {
                if (response.isSuccessful()) {
                    GeoRepoResponse repo = response.body();
                    ArrayList<String> Region = new ArrayList<>();
                    mBinding.weather.setText("");
                    Region.add(repo.getDocuments().get(0).getAddress().getRegion_1depth_name() + " ");
                    Region.add(repo.getDocuments().get(0).getAddress().getRegion_2depth_name() + " ");
                    Region.add(repo.getDocuments().get(0).getAddress().getRegion_3depth_name());

                    for (String name : Region) {
                        mBinding.weather.append(name);
                    }
                }
            }

            @Override
            public void onFailure(Call<GeoRepoResponse> call, Throwable t) {
                Log.d("getGeo Error:", t.toString());
            }
        });
    }

    private void getDustData(int numOfRows, int pageNo, String stationName,
                             String dataTerm, String ver) { // 대기정보 받아옴
        final ArrayList<DustHourData> dustHourData = new ArrayList<>();
        Retrofit retrofit = SelfCall.createRetrofit(DustApi.BASEURL);
        DustApi apiService = retrofit.create(DustApi.class);
        Call<DustRepoResponse> call = null;
        try {
            call = apiService.getWeather(
                    URLDecoder.decode(openKey, "utf-8"),
                    numOfRows,
                    pageNo,
                    stationName,
                    dataTerm,
                    ver,
                    "json");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("call : ", call.toString());
        call.enqueue(new Callback<DustRepoResponse>() {
            @Override
            public void onResponse(Call<DustRepoResponse> call,
                                   Response<DustRepoResponse> response) {
                if (response.isSuccessful()) {
                    DustRepoResponse repo = response.body();
                    if (repo != null) {
                        for (int i = 0; i < repo.getList().size(); i++) { // 시간 별 대기정보를 저장함.
                            DustHourData dustData = new DustHourData();
                            dustData.setKhaiValue(repo.getList().get(i).getKhaiValue());
                            dustData.setPm10Value(repo.getList().get(i).getPm10Value());
                            dustData.setPm25Value(repo.getList().get(i).getPm25Value());
                            dustData.setNo2Value(repo.getList().get(i).getNo2Value());
                            dustData.setCoValue(repo.getList().get(i).getCoValue());
                            dustHourData.add(dustData);
                        }

                        for (int i = 0; i < dustHourData.size(); i++) {
                            if (!isNullDustHourData(dustHourData.get(i))) {
                                DustHourData list = dustHourData.get(i);
                                mBinding.txtKhai.setText(list.getKhaiValue());
                                mBinding.txtDust1.setText(list.getPm10Value().concat("㎍/㎥"));
                                mBinding.txtDust2.setText(list.getPm25Value().concat("㎍/㎥"));
                                mBinding.txtDust3.setText(list.getNo2Value().concat("ppm"));
                                mBinding.txtDust4.setText(list.getCoValue().concat("ppm"));
                                setDustView(Integer.parseInt(list.getKhaiValue()),
                                        Integer.parseInt(list.getPm10Value()),
                                        Integer.parseInt(list.getPm25Value()),
                                        Double.parseDouble(list.getNo2Value()),
                                        Double.parseDouble(list.getCoValue()));
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<DustRepoResponse> call, Throwable t) {
                Log.d("getDustData Error:", t.toString());
            }
        });
    }

    private void getMeasure(String x, String y, String returnType) { // 가까운 측정소 구해옴.
        Retrofit retrofit = SelfCall.createRetrofit(MeasureApi.BASEURL);

        MeasureApi apiService = retrofit.create(MeasureApi.class);
        Call<MeasureRepoResponse> call = null;
        try {
            call = apiService.getMeasure(
                    URLDecoder.decode(openKey, "utf-8"),
                    x,
                    y,
                    returnType);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("call : ", call.toString());
        call.enqueue(new Callback<MeasureRepoResponse>() {
            @Override
            public void onResponse(Call<MeasureRepoResponse> call,
                                   Response<MeasureRepoResponse> response) {
                if (response.isSuccessful()) {
                    MeasureRepoResponse repo = response.body();
                    getDustData(10, 1, repo.getList().get(0).getStationName(), "DAILY", "1.3");
                }
            }

            @Override
            public void onFailure(Call<MeasureRepoResponse> call, Throwable t) {
                Log.d("getMeasure Error:", t.toString());
            }
        });
    }

    private void getWeatherData(String baseDate,String baseTime,String nx,String ny) {
        Retrofit retrofit = SelfCall.createRetrofit(WeatherApi.BASEURL);
        WeatherApi apiService = retrofit.create(WeatherApi.class);
        Call<WeatherRepoResponse> call = null;
        try {
            call = apiService.getWeather(
                    URLDecoder.decode(openKey, "utf-8"),
                    baseDate,
                    baseTime,
                    nx,
                    ny,
                    "10",
                    "1",
                    "json");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d("call : ", call.toString());
        call.enqueue(new Callback<WeatherRepoResponse>() {
            @Override
            public void onResponse(Call<WeatherRepoResponse> call,
                                   Response<WeatherRepoResponse> response) {
                if (response.isSuccessful()) {
                    WeatherRepoResponse repo = response.body();
                    if (repo != null) {
                        Log.e("test",repo.getResponse().getBody().getItems().getItem().get(0).getBaseTime());
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherRepoResponse> call, Throwable t) {
                Log.d("getWeatherData Error:", t.toString());
            }
        });
    }

    private void startLocationService() {
        DustFragment.GPSListener gpsListener = new DustFragment.GPSListener();
        long minTime = 10000;
        float minDistance = 0;

        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }
        Location location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();

        String msg = "Last Known Location -> Latitude : " +
                location.getLatitude() +
                "\nLongitude : " + location.getLongitude();
        Log.i("SampleLocation ", msg);

        manager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime, minDistance, gpsListener);
        getGeo(longitude.toString(), latitude.toString(), "WGS84");
        getCoord(longitude.toString(), latitude.toString());

    }

    private class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String msg = "Latitude : " + latitude + "\nLongitude : " + longitude;
            Log.i("GPSListener", msg);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    public boolean isNullDustHourData(DustHourData data) { // 필드 중 null 이거나 비어있으면 false 반환.
        if (TextUtils.isEmpty(data.getKhaiValue())
                || TextUtils.isEmpty(data.getPm10Value())
                || TextUtils.isEmpty(data.getPm25Value())
                || TextUtils.isEmpty(data.getCoValue())
                || TextUtils.isEmpty(data.getNo2Value())
                || data.getKhaiValue().equals("-")
                || data.getPm10Value().equals("-")
                || data.getPm25Value().equals("-")) // 최소 통합,미세,초미세가 "-" 이면 안된다.
            return true;
        else
            return false;
    }

    public void setDustView(int khaiValue, int pm10Value,
                            int pm25Value, Double no2Value, Double coValue) {

        if (khaiValue < SMILE) {
            mBinding.layout.setBackgroundColor(Color.BLUE);
            mBinding.txtKhaiText.setText("통합지수 : 매우좋음");
        } else if (khaiValue < NORMAL) {
            mBinding.layout.setBackgroundColor(Color.GREEN);
            mBinding.txtKhaiText.setText("통합지수 : 보통");
        } else if (khaiValue < BAD) {
            mBinding.layout.setBackgroundColor(Color.parseColor("#cc6600"));
            mBinding.txtKhaiText.setText("통합지수 : 나쁨");
        } else {
            mBinding.layout.setBackgroundColor(Color.RED);
            mBinding.txtKhaiText.setText("통합지수 : 매우나쁨");
        }

        if (pm10Value < SMILE)
            mBinding.imgDust1.setImageResource(R.drawable.smile);
        else if (pm10Value < NORMAL)
            mBinding.imgDust1.setImageResource(R.drawable.normal);
        else if (pm10Value < BAD)
            mBinding.imgDust1.setImageResource(R.drawable.bad);
        else
            mBinding.imgDust1.setImageResource(R.drawable.worst);

        if (pm25Value < SMILE)
            mBinding.imgDust2.setImageResource(R.drawable.smile);
        else if (pm25Value < NORMAL)
            mBinding.imgDust2.setImageResource(R.drawable.normal);
        else if (pm25Value < BAD)
            mBinding.imgDust2.setImageResource(R.drawable.bad);
        else
            mBinding.imgDust2.setImageResource(R.drawable.worst);

        if (no2Value < 0.03)
            mBinding.imgDust3.setImageResource(R.drawable.smile);
        else if (no2Value < 0.06)
            mBinding.imgDust3.setImageResource(R.drawable.normal);
        else if (no2Value < 0.20)
            mBinding.imgDust3.setImageResource(R.drawable.bad);
        else
            mBinding.imgDust3.setImageResource(R.drawable.worst);

        if (coValue < 5.5)
            mBinding.imgDust4.setImageResource(R.drawable.smile);
        else if (coValue < 9.0)
            mBinding.imgDust4.setImageResource(R.drawable.normal);
        else if (coValue < 12.0)
            mBinding.imgDust4.setImageResource(R.drawable.bad);
        else
            mBinding.imgDust4.setImageResource(R.drawable.worst);
    }

    private LatXLngY convertGRID_GPS(int mode, double lat_X, double lng_Y )
    {
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기1준점 Y좌표(GRID)

        //
        // LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도,  lng_Y:경도), "TO_GPS"(좌표->위경도,  lat_X:x, lng_Y:y) )
        //


        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        LatXLngY rs = new LatXLngY();

        if (mode == TO_GRID) {
            rs.lat = lat_X;
            rs.lng = lng_Y;
            double ra = Math.tan(Math.PI * 0.25 + (lat_X) * DEGRAD * 0.5);
            ra = re * sf / Math.pow(ra, sn);
            double theta = lng_Y * DEGRAD - olon;
            if (theta > Math.PI) theta -= 2.0 * Math.PI;
            if (theta < -Math.PI) theta += 2.0 * Math.PI;
            theta *= sn;
            rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
            rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
        }
        else {
            rs.x = lat_X;
            rs.y = lng_Y;
            double xn = lat_X - XO;
            double yn = ro - lng_Y + YO;
            double ra = Math.sqrt(xn * xn + yn * yn);
            if (sn < 0.0) {
                ra = -ra;
            }
            double alat = Math.pow((re * sf / ra), (1.0 / sn));
            alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

            double theta = 0.0;
            if (Math.abs(xn) <= 0.0) {
                theta = 0.0;
            }
            else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI * 0.5;
                    if (xn < 0.0) {
                        theta = -theta;
                    }
                }
                else theta = Math.atan2(xn, yn);
            }
            double alon = theta / sn + olon;
            rs.lat = alat * RADDEG;
            rs.lng = alon * RADDEG;
        }
        return rs;
    }



    class LatXLngY
    {
        public double lat;
        public double lng;

        public double x;
        public double y;

    }
}