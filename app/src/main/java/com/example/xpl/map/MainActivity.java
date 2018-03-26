package com.example.xpl.map;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Typeface;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.autonavi.ae.gmap.listener.MapListener;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationSource, AMapLocationListener,
        View.OnClickListener, AMap.OnMapClickListener, GeocodeSearch.OnGeocodeSearchListener{

    private MapView mapView;
    private AMap aMap;
    private LinearLayout mapLayout, userLayout;

    //定位需要的声明
    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private OnLocationChangedListener mListener = null;//定位监听器

    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;

    private TextView textViewTop;
    private ImageButton imageButtonLogout;

    //地图点击marker
    private Marker marker;

    //点击地图获取的经纬度
    double latitude, longitude;

    //经纬度转地理编码
    private GeocodeSearch geocodeSearch;
    private String geocodeString;

    //获取的Json
    private String SearchResults;

    //floatingaction
    private FloatingActionsMenu menuChangeMaptype;//改变地图类型菜单按钮
    private com.getbase.floatingactionbutton.FloatingActionButton menuNormalMapBtn;//改变地图类型为普通图按钮
    private com.getbase.floatingactionbutton.FloatingActionButton menuSatelliteMapBtn;//改变地图类型为遥感图按钮
    private com.getbase.floatingactionbutton.FloatingActionButton menuDarkoMapBtn;//改变地图类型为黑夜图按钮
    private com.getbase.floatingactionbutton.FloatingActionButton menuForcast;//进入天气预报


    //bottomnavigation监听：控制可见性切换layout
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener(){
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.navigation_map:
                    mapLayout.setVisibility(View.VISIBLE);
                    userLayout.setVisibility(View.GONE);
                    menuChangeMaptype.setVisibility(View.VISIBLE);
                    return true;
                case R.id.navigation_user:
                    mapLayout.setVisibility(View.GONE);
                    userLayout.setVisibility(View.VISIBLE);
                    menuChangeMaptype.setVisibility(View.GONE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewTop = (TextView)findViewById(R.id.textView_top);
        textViewTop.setBackgroundColor(Color.YELLOW);

        //实例化三个底部导航按钮所对应的layout
        mapLayout = (LinearLayout)findViewById(R.id.map_layout);
//        weatherLayout = (LinearLayout)findViewById(R.id.weather_layout);
        userLayout = (LinearLayout)findViewById(R.id.user_layout);

        //实例化mapView
        mapView = (MapView)findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        //实例化登出按钮，添加逻辑
        imageButtonLogout = (ImageButton)findViewById(R.id.imageButton_logout);
        imageButtonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder isLogout = new AlertDialog.Builder(MainActivity.this);
                isLogout.setTitle("提示");
                isLogout.setMessage("确认退出当前帐号？");
                isLogout.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                    }
                });
                isLogout.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                isLogout.show();
            }
        });

        //实例化Geocodesearch,设置监听
        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);

        //初始显示地图,aMap为地图控制类
        aMap = mapView.getMap();
        //设置定位监听
        aMap.setLocationSource(this);
        //是否可触发定位并显示定位层
        aMap.setMyLocationEnabled(true);
        //定位的小图标，默认蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.strokeWidth(0);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);

        //地图点击监听
        aMap.setOnMapClickListener(this);

        //开始定位
        initLoc();

        //底部导航监听
        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        //获得悬浮菜单按钮
        menuChangeMaptype = (FloatingActionsMenu)findViewById(R.id.changeMaptypeBtn);
        menuDarkoMapBtn = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.darkMapBtn);
        menuDarkoMapBtn.setOnClickListener(this);
        menuSatelliteMapBtn = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.satelliteMapBtn);
        menuSatelliteMapBtn.setOnClickListener(this);
        menuNormalMapBtn = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.normalMapBtn);
        menuNormalMapBtn.setOnClickListener(this);
        menuForcast = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.forcastBtn);
        menuForcast.setOnClickListener(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

        //获取成功登录的用户信息，将用户页更新
        Intent intent = getIntent();
        String user_firstname = intent.getStringExtra("firstname");
        String user_lastname = intent.getStringExtra("lastname");
        String user_phonenum = intent.getStringExtra("phonenum");
        String user_email = intent.getStringExtra("email");
        TextView textView_username = (TextView)findViewById(R.id.user_name);
        TextView textView_userphonenum = (TextView)findViewById(R.id.user_phonenum);
        TextView textView_useremail = (TextView)findViewById(R.id.user_email);
        textView_username.setText(textView_username.getText().toString()+user_firstname+user_lastname);
        textView_userphonenum.setText(textView_userphonenum.getText().toString()+user_phonenum);
        textView_useremail.setText(textView_useremail.getText().toString()+user_email);
    }

    //悬浮菜单
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.darkMapBtn:
                aMap.setMapType(AMap.MAP_TYPE_NIGHT);
                //收起悬浮展开
                menuChangeMaptype.collapse();
                break;
            case R.id.normalMapBtn:
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
                menuChangeMaptype.collapse();
                break;
            case R.id.satelliteMapBtn:
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
                menuChangeMaptype.collapse();
                break;
            case R.id.forcastBtn:
                Intent intent = new Intent(MainActivity.this, ForcastActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("Json", SearchResults);
                intent.putExtras(bundle);
                startActivity(intent);
                menuChangeMaptype.collapse();
                break;
        }
    }

    //定位
    public void initLoc(){
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    //定义回调函数
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                amapLocation.getLatitude();//获取纬度
                amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                df.format(date);//定位时间
                amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                amapLocation.getCountry();//国家信息
                amapLocation.getProvince();//省信息
                amapLocation.getCity();//城市信息
                amapLocation.getDistrict();//城区信息
                amapLocation.getStreet();//街道信息
                amapLocation.getStreetNum();//街道门牌号信息
                amapLocation.getCityCode();//城市编码
                amapLocation.getAdCode();//地区编码

                // 如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                if (isFirstLoc) {
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(17));
                    //将地图移动到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude())));
                    //点击定位按钮 能够将地图的中心移动到定位点
                    mListener.onLocationChanged(amapLocation);
                    //添加图钉
//                    aMap.addMarker(getMarkerOptions(amapLocation));
                    //获取定位信息
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(amapLocation.getCountry() + "" + amapLocation.getProvince() + "" + amapLocation.getCity() + "" + amapLocation.getProvince() + "" + amapLocation.getDistrict() + "" + amapLocation.getStreet() + "" + amapLocation.getStreetNum());
                    textViewTop.setText("当前："+buffer.toString()+"\n"+"经度："+amapLocation.getLongitude()+"      "+"纬度："+amapLocation.getLatitude());
                    Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();
                    isFirstLoc = false;
                }

            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());

                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    //激活定位
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }
    //停止定位
    @Override
    public void deactivate() {
        mListener = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //地图点击监听，获取经纬度
    @Override
    public void onMapClick(LatLng latLng) {
        aMap.clear();
        latitude = latLng.latitude;//纬度
        longitude = latLng.longitude;//经度
        //设置marker
        marker = aMap.addMarker(new MarkerOptions());
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_click_place));
        marker.setPosition(latLng);
        marker.setTitle(">>>地理信息<<<");
        //逆地理编码
        LatLonPoint latLonPoint = new LatLonPoint(latitude, longitude);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
        //小窗口显示经纬度
        marker.setSnippet("经度："+longitude+"\n"+"纬度："+latitude);
    }

    //逆地理编码监听回调
    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        //获取地理在marker中显示
        geocodeString = regeocodeResult.getRegeocodeAddress().getProvince()
                +regeocodeResult.getRegeocodeAddress().getCity()
                +regeocodeResult.getRegeocodeAddress().getDistrict();
        marker.setSnippet(geocodeString+"\n"+marker.getSnippet());
        makeSearchQuery(regeocodeResult.getRegeocodeAddress().getCity());
        marker.showInfoWindow();
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

    private void makeSearchQuery(String city){
        URL SearchUrl = NetworkUtils.buildUrl(city);
        new SearchTask().execute(SearchUrl);
    }

    public class SearchTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... params) {
            URL searchUrl = params[0];
            SearchResults = null;
            try{
                SearchResults = NetworkUtils.getResponseFromHttpUrl(searchUrl);
            }catch (IOException e){
                e.printStackTrace();
            }
            return SearchResults;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s != null && !s.equals("")){
                String ss = null;
                try {
                    ss = JsonAnalysis(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                marker.setSnippet(marker.getSnippet()+"\n"+ss);
            }
        }
    }

    //解析Json方法
    private String JsonAnalysis(String Json) throws JSONException {
        JSONObject object = null;
        try{
            object = new JSONObject(Json);
        }catch (JSONException e){
            e.printStackTrace();
        }
        //数据来自www.sojson.com
        JSONObject objectInfo = object.getJSONObject("data").getJSONArray("forecast").getJSONObject(0);
        String high = objectInfo.optString("high");
        String low = objectInfo.optString("low");
        String type = objectInfo.optString("type");
        String fx = objectInfo.optString("fx");
        String fl = objectInfo.optString("fl");
        String notice = objectInfo.optString("notice");

        String weatherResult = "\n>>>天气信息<<<"+"\n天气类型："+type+"\n"+high+"\n"+low+"\n风向："+fx
                +"\n风速："+fl+"\n\""+notice+"\"";
        return weatherResult;
    }
    //在主页面下按下返回键进入手机后台运行

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
