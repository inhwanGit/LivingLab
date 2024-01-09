package com.example.myapplication_naver;

import static com.naver.maps.map.NaverMap.LAYER_GROUP_TRANSIT;

import static java.lang.Thread.sleep;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;


import android.Manifest;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonObject;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.LocationSource;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.overlay.PathOverlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;
import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ActivityResultLauncher<Intent> startActivityResult;
    private ODsayService odsayService; //SDK
    //-------NaverMap----------
    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private FusedLocationSource mLocationSource;
    private NaverMap mNaverMap;
    private MapFragment mapFragment;
    private PathOverlay path = new PathOverlay();
    private Marker search_marker = new Marker();
    private Marker start_marker = new Marker();
    private Marker and_marker = new Marker();
    private Marker mylocation_marker = new Marker();
    private LocationButtonView locationButtonView;  //현재위치 버튼
    //--------------------------
    private Context cThis;//context 설정
    private STT stt; //음성인식용
    private TTS tts; //음성 출력용
    private SharedPrefManager_RecentRoute mSharedPrefs;
    private SharedPrefManager_StarSave mSharedPrefs2;
    private SharedPreferences recentlist_sf;
    private SharedPreferences setting_sf;
    private final ArrayList<String> items = new ArrayList<>();
    private ArrayAdapter<String> default_adapter;
    private BusStop_ListViewAdapter busStop_listViewAdapter;
    private FusedLocationProviderClient fusedLocationClient; //마지막 위치 가져오기
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Double nowLatitude;
    private Double nowLongitude;
    private boolean SearchList;
    private boolean textRaise;
    private boolean colorEmphasis;
    private boolean Vibrator;
    private int star_save_count;
    private ArrayList<Integer> busNumList = new ArrayList<>(); //승차할 버스 리스트
    private boolean busOut_Check = false;
    private boolean station_0_Check = false;
    private boolean station_and_Check = false;
    private String busOut_splitID = null;
    private String stationID_0 = "";  //처음에 승차할 정류장 id
    private String stationID_and = ""; //하차할 정류장 id
    private int numStaionLeft_count = 0;
    //승차할 수 있는 다른 버스 리스트
    private ArrayList<String> routeNoList_1 = new ArrayList<>();
    private ArrayList<String> statusPosList_1 = new ArrayList<>();
    private ArrayList<String> extimeMinList_1 = new ArrayList<>();
    private ArrayList<String> msgList_1 = new ArrayList<>();
    private long lastTimeBackPressed; //뒤로가기 버튼이 클릭된 시간
    //승차할 버스 정보
    private String busIn_Latitude;
    private String busIn_Longitude;
    private String busIn_Number;
    private String busIn_carRegNo = "";
    private String busIn_routeCd = "";
    private String busIn_statusPos;
    private int busIn_type;
    private String busIn_BusStopId7;
    private String busIn_BusStopNM;
    private boolean iambusin;   //승차 감지
    private boolean iambusout;  //하차 감지
    //승차할 정류장 정보
    private String StartbusStop_Latitude;
    private String StartbusStop_Longitude;
    private String StartbusStop_ID;
    private String StartbusStop_Name;
    //하차할 정류장 정보
    private String EndbusStop_Latitude;
    private String EndbusStop_Longitude;
    private String EndbusStop_ID;
    private String EndbusStop_Name;
    //서버에 보낼 데이터 json
    private JsonObject userobj = new JsonObject();
    private JsonObject locaionobj = new JsonObject();
    private SocketConnect sc = new SocketConnect();
    private String beforeStatusPos; //남은 정류장 저장 변수
    private boolean mainbusout = false;     //도착까지 한정거장 남았을때
    private boolean OnODSayRequestSearchPubTransPath = false; //길찾기 api 호출
    private boolean bus_arrival_at_busstop = false;


    //------ Thread ----------
    private MainHandler handler;

    //--------------ui--------------
    private Button sttStart;
    private TextView textView;
    private LinearLayout menu_layout;
    private Button busstop_btn;
    private Button star_save_btn;
    private Button setting_btn;
    private Button bus_route_btn;
    private SearchView mSearchView;     // 검색어를 입력할 Input 창
    private ListView jibunAddr_listView;
    private ListView recent_listView;
    private RelativeLayout search_layout_back;
    private LinearLayout busin_layout6;
    private LinearLayout search_result_layout;
    private LinearLayout search_result_layout1;
    private RelativeLayout search_result_layout2;
    private LinearLayout search_result_layout3;
    private LinearLayout search_result_layout4;
    private ListView search_result_layout4_list;
    private Button search_result_layout5_difbusBtn;
    private TextView search_keyword;
    private TextView search_jibusAddr;
    private TextView search_keyword2;
    private TextView search_jibusAddr2;
    private TextView nowAddr;
    private CheckBox star_save_btn2;
    private Button bus_route_btn2;
    private Button inbusName;
    private TextView status_pos;
    private TextView busin_depAddr;
    private TextView busin_nowStation;
    private TextView busin_arrKeyword;
    private TextView busin_arrAddr;
    private Button busout_btn;
    private TextView busin_numStaionLeft;
    private CheckBox star_save_btn3;
    private LinearLayout odsayBi_tv;

    //------------------public------------------------------
    //GeoPointer_Thread
    public static String GeoLatitude;
    public static String GeoLongitude;
    //BusStopArrival_Thread
    public static ArrayList<String> routeNoList = new ArrayList<>();
    public static ArrayList<String> carRegNoList = new ArrayList<>();
    public static ArrayList<String> routeCdList = new ArrayList<>();
    public static ArrayList<String> statusPosList = new ArrayList<>();
    public static ArrayList<String> extimeMinList = new ArrayList<>();
    public static ArrayList<String> msgTpList = new ArrayList<>();
    //GetBusPosByRtid_Thread
    public static ArrayList<String> plateNoList = new ArrayList<>();
    public static ArrayList<String> bus_gpsLatiList = new ArrayList<>();
    public static ArrayList<String> bus_gpsLongList = new ArrayList<>();
    public static ArrayList<String> busStopId7List = new ArrayList<>();
    public static ArrayList<String> busStopId5List = new ArrayList<>();
    //BusStopByRoute_Thread
    public static ArrayList<BusStop> busStops = new ArrayList<>();
    //GetBusRegInfoByRouteId_Thread
    public static ArrayList<Bus> buslist = new ArrayList<>();
    //GetStationByStopID_Thread
    public static String aroBusstopId;
    public static String busStopNM;


    private class MainHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();

            if (bundle.getBoolean("AddAPIThread")) {
                //------ 건물이름 ------------
                ArrayList<String> bdNmArraylist = new ArrayList<String>(bundle.getStringArrayList("BDNM_ADDR_LIST"));

                for (int i = 0; i < bdNmArraylist.size(); i++) {
                    Log.d("ReceiveMessage", bdNmArraylist.get(i));
                }

                //---- 지번 주소 ----
                ArrayList<String> jibun_arrayList = new ArrayList<String>(bundle.getStringArrayList("JIBUN_ADDR_LIST"));

                for (int i = 0; i < jibun_arrayList.size(); i++) {
                    Log.d("ReceiveMessage", jibun_arrayList.get(i));
                }

                final ArrayAdapter<String> jibun_arrayAdapter = new ArrayAdapter<String>(
                        getApplicationContext(),
                        R.layout.default_listview_textview,
                        jibun_arrayList){
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView tv = (TextView)view.findViewById(R.id.defaultList_TextView);

                        //글자 크기 설정
                        if(textRaise){
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                        } else {
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                        }

                        return view;
                    }
                };
                //ArrayAdapter<String> jibun_arrayAdapter = new ArrayAdapter<String>(cThis, R.layout.default_listview_textview, jibun_arrayList);

                jibunAddr_listView.setAdapter(jibun_arrayAdapter);

                jibunAddr_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        //진동 설정
                        if(Vibrator){
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                        }

                        //Toast.makeText(MainActivity.this, position + " 번째 선택! ", Toast.LENGTH_LONG).show(); //확인 완료
                        Log.d("jibunAddr_listView_CLICK", jibun_arrayAdapter.getItem(position));    //확인 완료

                        // 검색 버튼이 눌러졌을 때 이벤트 처리
                        if (SearchList) {
                            String text = bdNmArraylist.get(position);
                            int count = recentlist_sf.getInt("count_his", 0);

                            //리스트에 저장되어있는 값이 아니라면
                            if(mSharedPrefs.findText(text) == false){
                                //저장
                                int reCount = mSharedPrefs.addText(count, text);
                                mSharedPrefs.addCount(reCount + 1);

                                items.add(0, text);
                            } else{
                                //리스트 순서 수정
                                //지우고
                                for(int i=0;i<items.size();i++){
                                    if(text.equals(items.get(i))){
                                        items.remove(i);
                                    }
                                }
                                //다시저장
                                items.add(0, text);
                            }

                            mSearchView.clearFocus();
                        }
                        //-------------------------------------

                        if (jibunAddr_listView.getVisibility() == View.VISIBLE || recent_listView.getVisibility() == View.VISIBLE) {
                            //jibunAddr_listView 숨기기
                            jibunAddr_listView.setVisibility(View.GONE);
                            //recent_listView 숨기기
                            recent_listView.setVisibility(View.GONE);
                        }

                        if (search_result_layout.getVisibility() == View.GONE) {
                            //글자크기설정
                            if(textRaise){
                                search_keyword.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
                                search_jibusAddr.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
                                bus_route_btn2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
                            }else{
                                search_keyword.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
                                search_jibusAddr.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                                bus_route_btn2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                            }

                            //search_result_layout 보이기
                            ViewGroup.LayoutParams layoutParams = search_result_layout.getLayoutParams();

                            //90dp
                            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics());
                            layoutParams.height = height;
                            search_result_layout.setLayoutParams(layoutParams);
                            //search_result_layout.setLayoutTransition(null);

                            search_result_layout.setVisibility(View.VISIBLE);
                            odsayBi_tv.setVisibility(View.VISIBLE);
                            search_result_layout1.setVisibility(View.VISIBLE);
                            search_result_layout2.setVisibility(View.GONE);
                            search_result_layout3.setVisibility(View.GONE);
                            search_result_layout4.setVisibility(View.GONE);
                            search_result_layout5_difbusBtn.setVisibility(View.GONE);
                            busin_layout6.setVisibility(View.GONE);
                        }

                        GeoPointer_Thread geoPointer = new GeoPointer_Thread(handler, jibun_arrayAdapter.getItem(position));
                        geoPointer.start();
                        try {
                            geoPointer.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (mNaverMap != null) {
                            //지도상에 마커 표시
                            start_marker.setMap(null);
                            and_marker.setMap(null);
                            //search_marker = new Marker();
                            search_marker.setPosition(new LatLng(Double.parseDouble(GeoLatitude), Double.parseDouble(GeoLongitude)));
                            search_marker.setMap(mNaverMap);

                            //카메라 이동
                            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(Double.parseDouble(GeoLatitude), Double.parseDouble(GeoLongitude)));
                            mNaverMap.moveCamera(cameraUpdate);
                        }

                        //주소에서 키워드 자르기
                        String split[] = jibun_arrayAdapter.getItem(position).split(" ");
                        String jibunAddr = split[0] + " " + split[1] + " " + split[2] + " " + split[3];

                        //검색결과 textview
                        search_jibusAddr.setText(jibunAddr);
                        search_keyword.setText(bdNmArraylist.get(position));

                        search_jibusAddr2.setText(jibunAddr);
                        search_keyword2.setText(bdNmArraylist.get(position));

                        //검색창 layout 배경색 투명로 설정
                        search_layout_back.setBackgroundColor(Color.TRANSPARENT);

                        //키보드 내리기
                        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        manager.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
                    }
                });
            }
            else if (bundle.getBoolean("GeoPointerThread")) {
                final Double Latitude = bundle.getDouble("Latitude");
                final Double Longitude = bundle.getDouble("Longitude");
                final Double Distance = bundle.getDouble("Distance");

                Log.d("GeoPointerThread", "Latitude " + Latitude + " / " + "Longitude " + Longitude + " / " + "Distance " + Distance);

                if (jibunAddr_listView.getVisibility() == View.VISIBLE) {
                    //jibunAddr_listView 숨기기
                    jibunAddr_listView.setVisibility(View.INVISIBLE);
                }

                if (search_result_layout.getVisibility() == View.INVISIBLE) {
                    //search_result_layout 보이기
                    search_result_layout.setVisibility(View.VISIBLE);
                    odsayBi_tv.setVisibility(View.VISIBLE);
                }

                Log.d("test", "GeoPointerThread/End");

            }
            else if (bundle.getBoolean("ReverseGeoPointerThread")) {

                ArrayList<String> areas = new ArrayList<>();
                areas.addAll(bundle.getStringArrayList("areas"));
                ArrayList<String> numbers = new ArrayList<>();
                numbers.addAll(bundle.getStringArrayList("numbers"));

                ArrayList<String> addr = new ArrayList<>();

                for (int i = 0; i < areas.size(); i++) {
                    Log.d("ReceiveMessage", areas.get(i));
                    addr.add(areas.get(i));
                    addr.add(" ");
                }

                for (int i = 0; i < numbers.size(); i++) {
                    Log.d("ReceiveMessage", numbers.get(i));
                    addr.add(numbers.get(i));
                    addr.add(" ");
                }

                addr.remove(addr.size() - 1);   //맨 마지막 공백 지우기

                String strAddr = String.join("", addr);
                nowAddr.setText(strAddr);

                String dep_addr = nowAddr.getText().toString();
                String arr_addr = search_jibusAddr2.getText().toString();
                String save_arrKeyword = search_keyword2.getText().toString();
                String route = dep_addr + "→" + arr_addr + " " + save_arrKeyword;
                Log.d("test", route);
                if (mSharedPrefs2.findStar(route)) {
                    //즐겨찾기 표시
                    star_save_btn2.setChecked(true);
                }

                Log.d("test", "ReverseGeoPointerThread/End");
            }
            else if (bundle.getBoolean("BusStopArrival_Thread")) {
                ArrayList<String> routeNoList = new ArrayList<>(bundle.getStringArrayList("ROUTE_NO"));     //버스 번호
                ArrayList<String> statusPosList = new ArrayList<>(bundle.getStringArrayList("STATUS_POS")); //남은 정류장 수
                ArrayList<String> extimeMinList = new ArrayList<>(bundle.getStringArrayList("EXTIME_MIN")); //도착 예상 시간(분)
                ArrayList<String> carRegNoList = new ArrayList<>(bundle.getStringArrayList("CAR_REG_NO"));  //차 번호 ID
                ArrayList<String> routeCdList = new ArrayList<>(bundle.getStringArrayList("ROUTE_CD"));     //노선 ID
                ArrayList<String> msgTpList = new ArrayList<>(bundle.getStringArrayList("MSG_TP"));         //메세지 유형
                //메시지유형 '01'도착, '02' 출발, '03' 몇분후 도착 '04' 교차로 통과 '06 진입중  '07' 차고지 운행대기중

                ArrayList<Integer> extimeMinList_int = new ArrayList<Integer>();    //형변환 배열

                for (int i = 0; i < routeNoList.size(); i++) {
                }


                for (int i = 0; i < statusPosList.size(); i++) {
                    //Log.d("ReceiveMessage",statusPosList.get(i));
                }

                for (int i = 0; i < extimeMinList.size(); i++) {
                    //Log.d("ReceiveMessage",extimeMinList.get(i));
                    extimeMinList_int.add(Integer.parseInt(extimeMinList.get(i)));
                }
            }
            else if (bundle.getBoolean("BusInSense")){
                boolean result = bundle.getBoolean("result");

                if(result){
                    Toast.makeText(MainActivity.this, busIn_Number + "번 버스에 탑승했습니다.", Toast.LENGTH_LONG).show();

                    ViewGroup.LayoutParams layoutParams = search_result_layout.getLayoutParams();

                    //450dp
                    int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 450, getResources().getDisplayMetrics());
                    layoutParams.height = height;
                    search_result_layout.setLayoutParams(layoutParams);
                    search_result_layout1.setVisibility(View.GONE);
                    search_result_layout2.setVisibility(View.GONE);
                    search_result_layout3.setVisibility(View.GONE);
                    search_result_layout4.setVisibility(View.GONE);
                    search_result_layout5_difbusBtn.setVisibility(View.GONE);
                    busin_layout6.setVisibility(View.VISIBLE);

                    busin_depAddr.setText(nowAddr.getText().toString());                //출발
                    busin_arrKeyword.setText(search_keyword2.getText().toString());     //도착주소 키워드
                    busin_arrAddr.setText(search_jibusAddr2.getText().toString());      //도착주소

                    if (mSharedPrefs2.findStar(busin_depAddr.getText().toString() + "→" + busin_arrAddr.getText().toString() + " " + busin_arrKeyword.getText().toString())) {
                        //즐겨찾기 표시
                        star_save_btn3.setChecked(true);
                    }
                } else {
                    Toast.makeText(MainActivity.this, busIn_Number + "번 버스에 탑승하지 않았습니다.\n안내를 종료합니다.", Toast.LENGTH_LONG).show();

                    //뒤로가기 버튼 호출
                    onBackPressed();
                }
            }
            else if (bundle.getBoolean("SetBusInData")){
                String BusStopName = bundle.getString("BusStopName");
                int numStaionLeft_count = bundle.getInt("numStaionLeft_count");
                boolean busout = bundle.getBoolean("busout_btn");
                boolean UserBusStopArrival = bundle.getBoolean("UserBusStopArrival");

                //갱신 전 정류장이름 및 정거장 수 가져오기
                String before_busin_nowStation = busin_nowStation.getText().toString();
                String before_busin_numStaionLeft = busin_numStaionLeft.getText().toString();

                //만약 데이터가 담겨있다면
                if(BusStopName.equals("") == false){
                    busin_nowStation.setText(BusStopName);                                  //이번정류장
                    busin_numStaionLeft.setText(numStaionLeft_count + " 정거장 남았습니다.");  //남은정류장 수

                    if(UserBusStopArrival == false){
                        tts.TextToSpeech("이번 정거장은 " + BusStopName + "입니다.");
                    }
                }
                else{
                    //데이터가 안담겨있다면 이전 데이터 출력
                    busin_nowStation.setText(before_busin_nowStation);
                    busin_numStaionLeft.setText(before_busin_numStaionLeft);

                    tts.TextToSpeech("이번 정거장은 " + before_busin_nowStation + "입니다.");
                }

                //도착 정류장까지 한정거장 남았을 시
                if(busout){
                    busout_btn.callOnClick();
                }

                //도착 정류장에 도착했을 경우
                if(UserBusStopArrival){
                    Toast.makeText(MainActivity.this, EndbusStop_Name + " 정거장에 도착했습니다. 하차하시길 바랍니다.", Toast.LENGTH_LONG).show();
                    busin_numStaionLeft.setText("하차하시길 바랍니다.");
                }
            }
        }

    }

    private void ActivityResultLauncher() {
        startActivityResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Log.d(TAG, "MainActivity로 돌아왔다. ");

                            String activity = result.getData().getStringExtra("Activity");

                            if (activity.equals("Directions")) {

                                String direct_Acti_rice_arr_addr = result.getData().getStringExtra("arr_addr");
                                String direct_Acti_rice_arr_keyw = result.getData().getStringExtra("arr_keyw");
                                String direct_Acti_rice_dep_addr = result.getData().getStringExtra("dep_addr");

                                //네이버 지도 레이아웃 크기 조절
                                FrameLayout frameLayout = (FrameLayout) mapFragment.getMapView();
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
                                params.bottomMargin = 0;
                                frameLayout.setLayoutParams(params);

                                //현재위치 주소
                                nowAddr.setText(direct_Acti_rice_dep_addr);

                                //주소에서 키워드 자르기
                                String split[] = direct_Acti_rice_arr_addr.split(" ");
                                String jibunAddr = split[0] + " " + split[1] + " " + split[2] + " " + split[3];

                                //도착위치 주소
                                search_jibusAddr2.setText(jibunAddr);
                                search_keyword2.setText(direct_Acti_rice_arr_keyw);

                                //레이아웃 조절
                                search_layout_back.setVisibility(View.INVISIBLE);
                                menu_layout.setVisibility(View.INVISIBLE);

                                ViewGroup.LayoutParams layoutParams = search_result_layout.getLayoutParams();

                                //450dp
                                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 450, getResources().getDisplayMetrics());
                                layoutParams.height = height;
                                search_result_layout.setLayoutParams(layoutParams);
                                search_result_layout.setVisibility(View.VISIBLE);
                                odsayBi_tv.setVisibility(View.VISIBLE);
                                search_result_layout1.setVisibility(View.GONE);
                                search_result_layout2.setVisibility(View.VISIBLE);
                                search_result_layout3.setVisibility(View.VISIBLE);
                                search_result_layout5_difbusBtn.setVisibility(View.VISIBLE);

                                String route = direct_Acti_rice_dep_addr + "→" + direct_Acti_rice_arr_addr + " " + direct_Acti_rice_arr_keyw;
                                if (mSharedPrefs2.findStar(route)) {
                                    //즐겨찾기 표시
                                    star_save_btn2.setChecked(true);
                                }

                                GeoPointer_Thread geoPointer1 = new GeoPointer_Thread(handler, direct_Acti_rice_dep_addr);
                                GeoPointer_Thread geoPointer2 = new GeoPointer_Thread(handler, direct_Acti_rice_arr_addr);

                                //출발 주소를 좌표로
                                geoPointer1.start();
                                try {
                                    geoPointer1.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Log.d("directions_activity", "latitude : " + GeoLatitude + " longtude : " + GeoLongitude);
                                String dep_lati;
                                String dep_longi;
                                dep_lati = GeoLatitude;
                                dep_longi = GeoLongitude;

                                //도착 주소를 좌표로
                                geoPointer2.start();
                                try {
                                    geoPointer2.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Log.d("directions_activity", "latitude : " + GeoLatitude + " longtude : " + GeoLongitude);
                                String arr_lati;
                                String arr_longi;
                                arr_lati = GeoLatitude;
                                arr_longi = GeoLongitude;

                                //길찾기 시 얻었던  데이터 리셋
                                bus_arrival_at_busstop = false;

                                routeNoList.clear();
                                carRegNoList.clear();
                                routeCdList.clear();
                                statusPosList.clear();
                                extimeMinList.clear();
                                msgTpList.clear();

                                plateNoList.clear();
                                bus_gpsLatiList.clear();
                                bus_gpsLongList.clear();
                                busStopId7List.clear();
                                busStopId5List.clear();

                                busStops.clear();
                                busNumList.clear();

                                routeNoList_1.clear();
                                statusPosList_1.clear();
                                extimeMinList_1.clear();
                                msgList_1.clear();

                                //길찾기 api시작
                                ODSayRequestSearchPubTransPath(dep_longi, dep_lati, arr_longi, arr_lati);

                                //목적지 좌표 서버로 데이터 전송
                                //..
                                userobj.remove("destination");
                                userobj.addProperty("destination", direct_Acti_rice_arr_addr);

                            }
                            else if (activity.equals("StarSave")) {

                                String start = result.getData().getStringExtra("start");
                                String end = result.getData().getStringExtra("end");

                                //네이버 지도 레이아웃 크기 조절
                                FrameLayout frameLayout = (FrameLayout) mapFragment.getMapView();
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
                                params.bottomMargin = 0;
                                frameLayout.setLayoutParams(params);

                                //즐겨찾기 표시
                                star_save_btn2.setChecked(true);

                                //현재위치 주소
                                nowAddr.setText(start);

                                //주소에서 키워드 자르기
                                String split[] = end.split(" ");
                                String jibunAddr = split[0] + " " + split[1] + " " + split[2] + " " + split[3];

                                //도착위치 주소
                                search_jibusAddr2.setText(jibunAddr);
                                //키워드
                                search_keyword2.setText(split[4]);

                                //레이아웃 조절
                                search_layout_back.setVisibility(View.INVISIBLE);
                                menu_layout.setVisibility(View.INVISIBLE);

                                ViewGroup.LayoutParams layoutParams = search_result_layout.getLayoutParams();

                                //450dp
                                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 450, getResources().getDisplayMetrics());
                                layoutParams.height = height;
                                search_result_layout.setLayoutParams(layoutParams);
                                search_result_layout.setVisibility(View.VISIBLE);
                                odsayBi_tv.setVisibility(View.VISIBLE);
                                search_result_layout1.setVisibility(View.GONE);
                                search_result_layout2.setVisibility(View.VISIBLE);
                                search_result_layout3.setVisibility(View.VISIBLE);
                                search_result_layout5_difbusBtn.setVisibility(View.VISIBLE);

                                GeoPointer_Thread geoPointer1 = new GeoPointer_Thread(handler, start);
                                GeoPointer_Thread geoPointer2 = new GeoPointer_Thread(handler, end);
                                String dep_lati;
                                String dep_longi;
                                String arr_lati;
                                String arr_longi;

                                //출발 주소를 좌표로
                                geoPointer1.start();
                                try {
                                    geoPointer1.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Log.d("StarSave_activity", "latitude : " + GeoLatitude + " longtude : " + GeoLongitude);
                                dep_lati = GeoLatitude;
                                dep_longi = GeoLongitude;

                                //도착 주소를 좌표로
                                geoPointer2.start();
                                try {
                                    geoPointer2.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Log.d("StarSave_activity", "latitude : " + GeoLatitude + " longtude : " + GeoLongitude);
                                arr_lati = GeoLatitude;
                                arr_longi = GeoLongitude;

                                //길찾기 시 얻었던 데이터 리셋
                                bus_arrival_at_busstop = false;

                                routeNoList.clear();
                                carRegNoList.clear();
                                routeCdList.clear();
                                statusPosList.clear();
                                extimeMinList.clear();
                                msgTpList.clear();

                                plateNoList.clear();
                                bus_gpsLatiList.clear();
                                bus_gpsLongList.clear();
                                busStopId7List.clear();
                                busStopId5List.clear();

                                busStops.clear();
                                busNumList.clear();

                                routeNoList_1.clear();
                                statusPosList_1.clear();
                                extimeMinList_1.clear();
                                msgList_1.clear();

                                //길찾기 api시작
                                ODSayRequestSearchPubTransPath(dep_longi, dep_lati, arr_longi, arr_lati);

                                //목적지 좌표 서버로 데이터 전송
                                //..
                                //userobj.remove("destination");
                                userobj.addProperty("destination", end);
                            }
                            else if (activity.equals("Setting")) {

                                boolean textRaise_Btn = result.getData().getBooleanExtra("textRaise_Btn", false);
                                boolean SearchList_Btn = result.getData().getBooleanExtra("SearchListOn_Btn", false);
                                boolean VibratorOn_Btn = result.getData().getBooleanExtra("VibratorOn_Btn", false);
                                int DisabledType = result.getData().getIntExtra("DisabledType", 0);

                                if (textRaise_Btn == true) {
                                    Log.d("Setting", "textRaise_Btn : true");

                                    /*
                                    search_keyword.setTextSize(Dimension.DP, PxToDp(search_keyword.getTextSize()) - 1);
                                    search_jibusAddr.setTextSize(Dimension.DP, PxToDp(search_jibusAddr.getTextSize()) - 1);
                                    search_keyword2.setTextSize(Dimension.DP, PxToDp(search_keyword2.getTextSize()) - 1);
                                    search_jibusAddr2.setTextSize(Dimension.DP, PxToDp(search_jibusAddr2.getTextSize()) - 1);
                                    nowAddr.setTextSize(Dimension.DP, PxToDp(nowAddr.getTextSize()) - 1);
                                    status_pos.setTextSize(Dimension.DP, PxToDp(status_pos.getTextSize()) - 1);
                                    busin_depAddr.setTextSize(Dimension.DP, PxToDp(busin_depAddr.getTextSize()) - 1);
                                    busin_nowStation.setTextSize(Dimension.DP, PxToDp(busin_nowStation.getTextSize()) - 1);
                                    busin_arrKeyword.setTextSize(Dimension.DP, PxToDp(busin_arrKeyword.getTextSize()) - 1);
                                    busin_arrAddr.setTextSize(Dimension.DP, PxToDp(busin_arrAddr.getTextSize()) - 1);
                                    busin_numStaionLeft.setTextSize(Dimension.DP, PxToDp(busin_numStaionLeft.getTextSize()) - 1);
                                     */

                                    textRaise = true;

                                    //현재 액티비티 종료
                                    finish();
                                    //TODO 액티비티 화면 재갱신 시키는 코드
                                    Intent intent = getIntent();
                                    startActivity(intent);
                                } else {
                                    Log.d("Setting", "textRaise_Btn : false");

                                    textRaise = false;

                                    //현재 액티비티 종료
                                    finish();
                                    //TODO 액티비티 화면 재갱신 시키는 코드
                                    Intent intent = getIntent();
                                    startActivity(intent);
                                }

                                if (SearchList_Btn == true) {
                                    Log.d("Setting", "SearchList_Btn : true");
                                    SearchList = true;
                                } else {
                                    Log.d("Setting", "SearchList_Btn : false");
                                    SearchList = false;
                                }

                                if (VibratorOn_Btn == true) {
                                    Log.d("Setting", "VibratorOn_Btn : true");
                                    Vibrator = true;
                                } else {
                                    Log.d("Setting", "VibratorOn_Btn : false");
                                    Vibrator = false;
                                }

                                if (DisabledType != 0) {
                                    Log.d("Setting", "DisabledType : " + DisabledType);

                                    //장애인 타입 변경
                                    SharedPreferences sharedPreferences = getSharedPreferences("DisabledType", MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt("DisabledType", DisabledType);
                                    editor.commit();
                                }
                            }
                            else if (activity.equals("OnBoardingActivity")) {
                                int DisabledType = result.getData().getIntExtra("DisabledType", 0);

                                SharedPreferences sharedPreferences = getSharedPreferences("DisabledType", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("DisabledType", DisabledType);
                                editor.commit();

                                //1 : 시각장애인, 2 : 지체장애인, 3 : 청각장애인
                                Log.d("OnBoardingActivity", "DisabledType : " + DisabledType);

                                //장애인 유형 서버에 데이터 전송
                                //..
                                //userobj.remove("type");
                                userobj.addProperty("type", DisabledType);
                            }
                        }
                    }
                });
    }

    private void Init() {
        cThis = this;
        handler = new MainHandler();
        sc.OpenSocket();

        odsayService = ODsayService.init(MainActivity.this, getString(R.string.odsay_key));
        odsayService.setReadTimeout(5000);
        odsayService.setConnectionTimeout(5000);

        mSharedPrefs = SharedPrefManager_RecentRoute.getInstance(this);
        mSharedPrefs2 = SharedPrefManager_StarSave.getInstance(this);

        sttStart = (Button) findViewById(R.id.btn_sttStart);
        jibunAddr_listView = (ListView) findViewById(R.id.jibunAddr_listView);
        recent_listView = (ListView) findViewById(R.id.recent_listView);
        mSearchView = (SearchView) findViewById(R.id.searchView);
        menu_layout = (LinearLayout) findViewById(R.id.menu_layout);
        search_layout_back = (RelativeLayout) findViewById(R.id.search_layout_back);
        bus_route_btn = (Button) findViewById(R.id.bus_route_btn);
        busstop_btn = (Button) findViewById(R.id.busstop_btn);
        star_save_btn = (Button) findViewById(R.id.star_save_btn);
        setting_btn = (Button) findViewById(R.id.setting_btn);
        search_jibusAddr = (TextView) findViewById(R.id.search_jibusAddr);
        search_keyword = (TextView) findViewById(R.id.search_keyword);
        search_result_layout = (LinearLayout) findViewById(R.id.search_result_layout);
        search_result_layout1 = (LinearLayout) findViewById(R.id.search_result_layout1);
        search_result_layout2 = (RelativeLayout) findViewById(R.id.search_result_layout2);
        search_result_layout3 = (LinearLayout) findViewById(R.id.search_result_layout3);
        search_result_layout4 = (LinearLayout) findViewById(R.id.search_result_layout4);
        search_result_layout4_list = (ListView) findViewById(R.id.search_result_layout4_list);
        search_result_layout5_difbusBtn = (Button) findViewById(R.id.search_result_layout5_difbusBtn);
        search_keyword2 = (TextView) findViewById(R.id.search_keyword2);
        search_jibusAddr2 = (TextView) findViewById(R.id.search_jibusAddr2);
        star_save_btn2 = (CheckBox) findViewById(R.id.star_save_btn2);
        bus_route_btn2 = (Button) findViewById(R.id.bus_route_btn2);
        nowAddr = (TextView) findViewById(R.id.nowAddr);
        inbusName = (Button) findViewById(R.id.inbusName);
        status_pos = (TextView) findViewById(R.id.status_pos);
        busin_layout6 = (LinearLayout) findViewById(R.id.busin_layout6);
        busin_depAddr = (TextView) findViewById(R.id.busin_depAddr);
        busin_nowStation = (TextView) findViewById(R.id.busin_nowStation);
        busin_arrKeyword = (TextView) findViewById(R.id.busin_arrKeyword);
        busin_arrAddr = (TextView) findViewById(R.id.busin_arrAddr);
        busout_btn = (Button) findViewById(R.id.busout_btn);
        busin_numStaionLeft = (TextView) findViewById(R.id.busin_numStaionLeft);
        star_save_btn3 = (CheckBox) findViewById(R.id.star_save_btn3);
        odsayBi_tv = (LinearLayout) findViewById(R.id.odsayBi_tv);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Init();

        ActivityResultLauncher(); //Activity_Receive

        FirstUserCheak(); //처음 사용자 체크하는 메소드


        //-------------------MAP-----------------------------
        //지도 객체 생성
        FragmentManager fm = getSupportFragmentManager();
        mapFragment = (MapFragment) fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            //NaverMapOptions options = new NaverMapOptions().locationButtonEnabled(true);
            //mapFragment = MapFragment.newInstance(options);
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }
        //getMepAsync를 호출하여 비동기로 onMapReady 콜백 메서드 호출
        //onMapReady에서 NaverMap 객체를 받음
        mapFragment.getMapAsync(this);


        //위치를 반환하는 구현체인 FusedLocationSource생성
        mLocationSource = new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

        /*
        locationButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FusedLocationSource fusedLocationSource = new FusedLocationSource((Activity) cThis, PERMISSION_REQUEST_CODE);
                mNaverMap.setLocationSource(fusedLocationSource);

                LatLng latLng = new LatLng(127.395472, 36.409171);
                CameraPosition cameraPosition = new CameraPosition(latLng,0.5);
                mNaverMap.setCameraPosition(cameraPosition);
            }
        });
        */

        //-----------------------------------------------------------
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    //현재 위치
                    Log.d("fusedLocationClient", "update/Latitude " + location.getLatitude() + " Longitude " + location.getLongitude());
                    nowLatitude = location.getLatitude();
                    nowLongitude = location.getLongitude();

                    //현재 위치 서버에 전송할 데이터 저장
                    JsonObject object = new JsonObject();
                    object.addProperty("lat", nowLatitude);
                    object.addProperty("lng", nowLongitude);
                    userobj.remove("location");
                    locaionobj.remove("location");
                    userobj.add("location", object);
                    locaionobj.add("location", object);

                    //전송
                    Log.d("Location JsonObject", locaionobj.toString());
                    sc.sendLocation(locaionobj);

                    //길찾기를 했다면
                    if(OnODSayRequestSearchPubTransPath == true){
                        //사용자 위치와 승차 버스 정류장 위치 거리 미터 단위로 계산
                        LocationDistance locationdistance = new LocationDistance();
                        Double distance = locationdistance.distance(nowLatitude, nowLongitude, Double.parseDouble(StartbusStop_Latitude),
                                Double.parseDouble(StartbusStop_Longitude), "meter");

                        //Log.d("fusedLocationClient", "StartBusStop/Latitude " + StartbusStop_Latitude + " Longitude " + StartbusStop_Longitude);
                        Log.d("fusedLocationClient", "distance/meter : " + distance.toString());

                        //탑승하지 않았다면
                        if(iambusin == false){

                            //toast 위치 바꾸려고 했지만 android 30 이상 부터는 위치를 못바꾼다.
                            //실시간 알림
                            tts.FuncVoiceOut("승차할 버스정거장 위치가 " + distance.intValue() + "m 반경 내에 있습니다.");
                            Toast.makeText(MainActivity.this, "승차할 버스정거장 위치가 " + distance.intValue() + "m 반경 내에 있습니다.", Toast.LENGTH_LONG).show();

                            //거리가 100m 이하라면
                            if(distance.intValue() <= 100){
                                //tts.FuncVoiceOut("승차할 버스정거장 위치가 100m 반경 내에 있습니다.");
                                //Toast.makeText(MainActivity.this, "승차할 버스정거장 위치가 100m 반경 내에 있습니다.", Toast.LENGTH_LONG).show();

                            } //거리가 10m 이하라면
                            else if(distance.intValue() <= 10){
                                //tts.FuncVoiceOut("승차할 버스정거장 위치에 도착하였습니다.");
                                //Toast.makeText(MainActivity.this, "승차할 버스정거장 위치에 도착하였습니다.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
            }
        };

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000);

        //설정 정보
        setting_sf = getSharedPreferences("Setting_History", MODE_PRIVATE);
        textRaise = setting_sf.getBoolean("textRaise", false);
        SearchList = setting_sf.getBoolean("SearchListOn", true);
        Vibrator = setting_sf.getBoolean("VibratorOn", true);

        //글자 크기 설정
        if(textRaise){
            //menu
            busstop_btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            star_save_btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            setting_btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            bus_route_btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            //길찾기 후 450dp
            nowAddr.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
            search_keyword2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 33);
            search_jibusAddr2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            inbusName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 43);
            status_pos.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            search_result_layout5_difbusBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
        }else{
            //menu
            busstop_btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            star_save_btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            setting_btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            bus_route_btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            //길찾기 후 450dp
            nowAddr.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            search_keyword2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30);
            search_jibusAddr2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            inbusName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
            status_pos.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
            search_result_layout5_difbusBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        }

        //---------------SearchView------------------------

        //listview 숨기기
        recent_listView.setVisibility(View.INVISIBLE);
        jibunAddr_listView.setVisibility(View.INVISIBLE);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.default_listview_textview,
                items){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView)view.findViewById(R.id.defaultList_TextView);

                //글자 크기 설정
                if(textRaise){
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                } else {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                }

                return view;
            }
        };
        //final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.default_listview_textview, items);
        //adapter = new ArrayAdapter<String>(this, R.layout.default_listview_textview, items);

        //ListView 객체에 adapter 객체 연결
        recent_listView.setAdapter(adapter);

        recent_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                //클릭한 아이템의 문자열 가져오기
                String selected_item = (String) adapterView.getItemAtPosition(position);

                AddrAPI_Thread addrAPI = new AddrAPI_Thread(handler, selected_item);
                addrAPI.start();

                recent_listView.setVisibility(View.INVISIBLE); //죄근 검색 리스트 숨기기
                jibunAddr_listView.setVisibility(View.VISIBLE);//주소 검색 리스트 보이기

                //--------------------------------------------------------------------
                //해당 아이템을 searchView 에 띄우기
                mSearchView.setQuery(selected_item, false);
                //mSearchView.clearFocus();
                //어댑터 객체에 변경을 반영시켜줘야 에러가 없음.
                adapter.notifyDataSetChanged();
            }
        });

        //------------------------- 저장값 불러오기 Start ------------------------------
        //최근 검색기록 20개
        recentlist_sf = getSharedPreferences("Search_History", MODE_PRIVATE);
        final int[] count = {recentlist_sf.getInt("count_his", 0)};
        String[] his = new String[20];
        for (int i = 0; i < 20; i++) {
            his[i] = recentlist_sf.getString("history" + i, "");
        }
        for (int i = 0; i < 20; i++) {
            if (his[i] != "") {
                items.add(0, his[i]);
                if (i == 19 && count[0] != 20) {
                    items.clear();
                    for (int j = 0; j < 20; j++) {
                        if (count[0] == j) {
                            items.add(0, his[j]);
                            for (int k = 1; k < 20; k++) {
                                if (k + j <= 19) {
                                    items.add(0, his[k + j]);
                                } else if (k + j > 19) {
                                    int zero = 0;
                                    while (19 - k >= 0) {
                                        items.add(0, his[zero]);
                                        zero++;
                                        k++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //------------------------- 저장값 불러오기 End --------------------------------

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                AddrAPI_Thread addrAPI = new AddrAPI_Thread(handler, query);
                addrAPI.start();

                recent_listView.setVisibility(View.INVISIBLE); //죄근 검색 리스트 숨기기
                jibunAddr_listView.setVisibility(View.VISIBLE);//주소 검색 리스트 보이기

                //배경색 화이트로 설정
                search_layout_back.setBackgroundColor(Color.rgb(255, 255, 255));

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                recent_listView.setVisibility(View.VISIBLE);   //죄근 검색 리스트 보이기
                menu_layout.setVisibility(View.INVISIBLE);  //메뉴 그룹(레이아웃) 숨기기

                //배경색 화이트로 설정
                search_layout_back.setBackgroundColor(Color.rgb(255, 255, 255));
            }
        });

        //----------------------STT&TTS--------------------------
        //음성인식 Button
        tts = new TTS(cThis);
        stt = new STT(cThis);

        sttStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                stt.setTTS(tts);
                stt.DialogShow();
                stt.setSearchView(mSearchView);

                System.out.println("음성인식 시작!");
                if (ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                    //권한을 허용하지 않는 경우
                } else {
                    //권한을 허용한 경우
                    try {
                        stt.sttStert();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //----------------------menu -------------------------------

        bus_route_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                Intent intent = new Intent(getApplicationContext(), Directions_Activity.class);
                intent.putExtra("BUS_ROUTE_CLICK_LAT", nowLatitude);
                intent.putExtra("BUS_ROUTE_CLICK_LONG", nowLongitude);
                intent.putExtra("TEXT_RAISE", textRaise);
                intent.putExtra("VIBRATOR", Vibrator);
                startActivityResult.launch(intent);
            }
        });

        busstop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                Intent intent = new Intent(getApplicationContext(), BusStop_Activity.class);
                intent.putExtra("BUSSTOP_CLICK_LAT", nowLatitude);
                intent.putExtra("BUSSTOP_CLICK_LONG", nowLongitude);
                intent.putExtra("TEXT_RAISE", textRaise);
                intent.putExtra("VIBRATOR", Vibrator);
                startActivity(intent);
            }
        });

        star_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                Intent intent = new Intent(getApplicationContext(), StarSave_Activity.class);
                intent.putExtra("TEXT_RAISE", textRaise);
                intent.putExtra("VIBRATOR", Vibrator);
                startActivityResult.launch(intent);
            }
        });

        setting_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                Intent intent = new Intent(getApplicationContext(), Setting_Activity.class);
                intent.putExtra("TEXT_RAISE", textRaise);
                intent.putExtra("VIBRATOR", Vibrator);
                startActivityResult.launch(intent);
                //startActivity(intent);
            }
        });

        //----------------------- 길찾기 layout 애니메이션 설정----------------------
        LayoutTransition lt = new LayoutTransition();
        lt.disableTransitionType(LayoutTransition.DISAPPEARING);
        search_result_layout.setLayoutTransition(lt);

        //즐겨찾기 아이콘 클릭시
        star_save_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                String save_depAddr = nowAddr.getText().toString();
                String save_arrAddr = search_jibusAddr2.getText().toString();
                String save_arrKeyword = search_keyword2.getText().toString();
                String route = save_depAddr + "→" + save_arrAddr + " " + save_arrKeyword;

                if (star_save_btn2.isChecked() == true) {
                    SharedPreferences rh = getSharedPreferences("Search_History", MODE_PRIVATE);
                    star_save_count = rh.getInt("count_star", 0);

                    //즐겨찾기 추가
                    int reCount = mSharedPrefs2.addStar(star_save_count, route);
                    mSharedPrefs2.addCount_star(reCount + 1);
                    
                    tts.FuncVoiceOut("즐겨찾기 추가");
                    Toast.makeText(MainActivity.this, "즐겨찾기 추가", Toast.LENGTH_SHORT).show();

                } else {
                    //즐겨찾기 삭제
                    mSharedPrefs2.DelStar(star_save_count, route);

                    tts.FuncVoiceOut("즐겨찾기 삭제");
                    Toast.makeText(MainActivity.this, "즐겨찾기 삭제", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //승차후 즐겨찾기 아이콘 클릭시
        star_save_btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                String save_depAddr = nowAddr.getText().toString();
                String save_arrAddr = search_jibusAddr2.getText().toString();
                String save_arrKeyword = search_keyword2.getText().toString();
                String route = save_depAddr + "→" + save_arrAddr + " " + save_arrKeyword;

                if (star_save_btn3.isChecked() == true) {
                    SharedPreferences rh = getSharedPreferences("Search_History", MODE_PRIVATE);
                    star_save_count = rh.getInt("count_star", 0);

                    //즐겨찾기 추가
                    int reCount = mSharedPrefs2.addStar(star_save_count, route);
                    mSharedPrefs2.addCount_star(reCount + 1);

                    tts.FuncVoiceOut("즐겨찾기 추가");
                    Toast.makeText(MainActivity.this, "즐겨찾기 추가", Toast.LENGTH_SHORT).show();

                } else {
                    //즐겨찾기 삭제
                    mSharedPrefs2.DelStar(star_save_count, route);

                    tts.FuncVoiceOut("즐겨찾기 삭제");
                    Toast.makeText(MainActivity.this, "즐겨찾기 삭제", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //검색 후 길찾기 클릭시
        bus_route_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                ViewGroup.LayoutParams layoutParams = search_result_layout.getLayoutParams();

                //450dp
                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 450, getResources().getDisplayMetrics());
                layoutParams.height = height;
                search_result_layout.setLayoutParams(layoutParams);
                search_result_layout1.setVisibility(View.GONE);
                search_result_layout2.setVisibility(View.VISIBLE);
                search_result_layout3.setVisibility(View.VISIBLE);
                search_result_layout4.setVisibility(View.GONE);
                search_result_layout5_difbusBtn.setVisibility(View.VISIBLE);
                menu_layout.setVisibility(View.GONE);

                //검색창 및 리스트 숨기기
                search_layout_back.setVisibility(View.GONE);
                jibunAddr_listView.setVisibility(View.GONE);
                recent_listView.setVisibility(View.GONE);

                //네이버 지도 레이아웃 크기 조절
                FrameLayout frameLayout = (FrameLayout) mapFragment.getMapView();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
                params.bottomMargin = 0;
                frameLayout.setLayoutParams(params);

                //현재위치좌표를 주소로
                ReverseGeoPointer_Thread thread = new ReverseGeoPointer_Thread(handler, nowLongitude, nowLatitude);     //반대로 넣어야 됨 안그러면 못찾음
                thread.start();
                try {
                    thread.join();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //도착 주소를 좌표로
                GeoPointer_Thread geoPointer = new GeoPointer_Thread(handler, search_jibusAddr2.getText().toString());
                geoPointer.start();
                try {
                    geoPointer.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //길찾기 시 얻었던 데이터 리셋
                bus_arrival_at_busstop = false;

                routeNoList.clear();
                carRegNoList.clear();
                routeCdList.clear();
                statusPosList.clear();
                extimeMinList.clear();
                msgTpList.clear();

                plateNoList.clear();
                bus_gpsLatiList.clear();
                bus_gpsLongList.clear();
                busStopId7List.clear();
                busStopId5List.clear();

                busStops.clear();
                busNumList.clear();

                routeNoList_1.clear();
                statusPosList_1.clear();
                extimeMinList_1.clear();
                msgList_1.clear();

                //길찾기 api 실행
                ODSayRequestSearchPubTransPath(Double.toString(nowLongitude), Double.toString(nowLatitude), GeoLongitude, GeoLatitude);

                //목적지 좌표 서버로 데이터 전송
                //..
                userobj.remove("destination");
                userobj.addProperty("destination", search_jibusAddr2.getText().toString());

            }
        });

        //다른버스 선택하기 클릭시
        search_result_layout5_difbusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                if (search_result_layout5_difbusBtn.getText().toString().equals("다른버스 선택하기")) {
                    ViewGroup.LayoutParams layoutParams = search_result_layout.getLayoutParams();

                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    search_result_layout.setLayoutParams(layoutParams);
                    search_result_layout1.setVisibility(View.GONE);
                    search_result_layout2.setVisibility(View.VISIBLE);
                    search_result_layout3.setVisibility(View.GONE);
                    search_result_layout4.setVisibility(View.VISIBLE);
                    search_result_layout5_difbusBtn.setVisibility(View.VISIBLE);
                    menu_layout.setVisibility(View.GONE);

                    search_result_layout5_difbusBtn.setText("기존버스 탑승하기");

                } else if (search_result_layout5_difbusBtn.getText().toString().equals("기존버스 탑승하기")) {
                    ViewGroup.LayoutParams layoutParams = search_result_layout.getLayoutParams();

                    //450dp
                    int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 450, getResources().getDisplayMetrics());
                    layoutParams.height = height;
                    search_result_layout.setLayoutParams(layoutParams);
                    search_result_layout1.setVisibility(View.GONE);
                    search_result_layout2.setVisibility(View.VISIBLE);
                    search_result_layout3.setVisibility(View.VISIBLE);
                    search_result_layout4.setVisibility(View.GONE);
                    search_result_layout5_difbusBtn.setVisibility(View.VISIBLE);
                    menu_layout.setVisibility(View.GONE);

                    //검색창 및 리스트 숨기기
                    search_layout_back.setVisibility(View.GONE);
                    jibunAddr_listView.setVisibility(View.GONE);
                    recent_listView.setVisibility(View.GONE);

                    //네이버 지도 레이아웃 크기 조절
                    FrameLayout frameLayout = (FrameLayout) mapFragment.getMapView();
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
                    params.bottomMargin = 0;
                    frameLayout.setLayoutParams(params);

                    search_result_layout5_difbusBtn.setText("다른버스 선택하기");
                }

            }
        });

        //승차 버스 변경 시
        search_result_layout4_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                //사용 종료 이벤트 서버에 전송 (단말기로 전송)
                userobj.remove("event");
                userobj.addProperty("event", 400);  //400-사용종료
                sc.sendUser(userobj);

                //변경전 버스 번호
                String busNoSp[] = inbusName.getText().toString().split("번 ");
                String busNo = busNoSp[0];
                String msg = "";
                String stapos = "";
                String busmin = "";

                ViewGroup.LayoutParams layoutParams = search_result_layout.getLayoutParams();

                //450dp
                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 450, getResources().getDisplayMetrics());
                layoutParams.height = height;
                search_result_layout.setLayoutParams(layoutParams);
                search_result_layout1.setVisibility(View.GONE);
                search_result_layout2.setVisibility(View.VISIBLE);
                search_result_layout3.setVisibility(View.VISIBLE);
                search_result_layout4.setVisibility(View.GONE);
                search_result_layout5_difbusBtn.setVisibility(View.VISIBLE);
                menu_layout.setVisibility(View.GONE);

                //검색창 및 리스트 숨기기
                search_layout_back.setVisibility(View.GONE);
                jibunAddr_listView.setVisibility(View.GONE);
                recent_listView.setVisibility(View.GONE);

                //네이버 지도 레이아웃 크기 조절
                FrameLayout frameLayout = (FrameLayout) mapFragment.getMapView();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) frameLayout.getLayoutParams();
                params.bottomMargin = 0;
                frameLayout.setLayoutParams(params);

                //내가 리스트에서 선택한 버스
                String dataBusNum = adapterView.getItemAtPosition(position).toString();
                inbusName.setText(dataBusNum);
                busIn_Number = dataBusNum;

                for (int i = 0; i < routeNoList.size(); i++) {
                    if(dataBusNum.equals(routeNoList.get(i))){
                        busIn_carRegNo = carRegNoList.get(i);
                        busIn_routeCd = routeCdList.get(i);
                        busIn_statusPos = statusPosList.get(i);

                        Log.d("BusInInfo", "busIn_routeCd : " + busIn_routeCd + " busIn_carRegNo : " + busIn_carRegNo);

                        //타입 정보 얻기
                        GetBusInInfo(busIn_routeCd, busIn_carRegNo);

                        //승차 버스 id 서버에 데이터 전송
                        //...
                        userobj.remove("bus");
                        userobj.addProperty("bus", busIn_carRegNo);
                        Log.d(TAG, userobj.toString());
                    }

                    if (dataBusNum.equals(routeNoList.get(i))) {
                        switch (msgTpList.get(i)) {
                            case "01":
                                //도착
                                status_pos.setText("도착");
                                tts.FuncVoiceOut(inbusName.getText() + "가 도착했습니다.\n승차 하시길 바랍니다.");
                                Toast.makeText(MainActivity.this,inbusName.getText() + "가 도착했습니다.\n승차 하시길 바랍니다.",Toast.LENGTH_LONG).show();

                                //이벤트 설정
                                userobj.remove("event");
                                userobj.addProperty("event", 104);   //104-버스도착
                                sc.sendUser(userobj);

                                //내위치와 버스위치 비교
                                //여기서 사용자가 승차했는지 감지
                                BusInSense();
                                break;
                            case "06":
                                status_pos.setText("진입중");
                                tts.FuncVoiceOut(inbusName.getText() + "가 진입중입니다.");

                                //이벤트 설정
                                userobj.remove("event");
                                userobj.addProperty("event", 103);   //103-버스근접
                                sc.sendUser(userobj);

                                //내위치와 버스위치 비교
                                //여기서 사용자가 승차했는지 감지
                                //BusInSense();
                                break;
                            case "07":
                                status_pos.setText("차고지 운행 대기중");
                                break;
                            default:
                                if(bus_arrival_at_busstop){
                                    status_pos.setText("도착");
                                    tts.FuncVoiceOut(inbusName.getText() + "가 도착했습니다.\n승차 하시길 바랍니다.");
                                    Toast.makeText(MainActivity.this,inbusName.getText() + "가 도착했습니다.\n승차 하시길 바랍니다.",Toast.LENGTH_LONG).show();
                                }else{
                                    tts.FuncVoiceOut(inbusName.getText().toString() + "가 " + statusPosList.get(i) + " 정거장 남았습니다.");
                                    Toast.makeText(MainActivity.this, inbusName.getText().toString() + "가 " + statusPosList.get(i) + " 정거장 남았습니다.", Toast.LENGTH_LONG).show();
                                    status_pos.setText(statusPosList.get(i) + " 정거장 남았습니다 " + "(" + extimeMinList.get(i) + "분)");
                                }
                                break;
                        }

                        beforeStatusPos = statusPosList.get(i);

                        //01(도착) msg 얻기 어려워서
                        //승차할 버스 정거장에 승차할 버스가 도착했는지 화인
                        BusArrivalStartBusStop();

                    }

                    if (busNo.equals(routeNoList.get(i))) {
                        stapos = statusPosList.get(i);
                        busmin = extimeMinList.get(i);
                        msg = msgTpList.get(i);
                    }
                }

                routeNoList_1.remove(position);
                statusPosList_1.remove(position);
                extimeMinList_1.remove(position);
                msgList_1.remove(position);

                routeNoList_1.add(busNo);
                statusPosList_1.add(stapos);
                extimeMinList_1.add(busmin);
                msgList_1.add(msg);

                //busStop_listViewAdapter.notifyDataSetChanged(); //<==안먹음
                BusStop_ListViewAdapter adapter = new BusStop_ListViewAdapter(cThis, routeNoList_1, statusPosList_1, extimeMinList_1, msgList_1, textRaise);
                search_result_layout4_list.setAdapter(adapter);

                search_result_layout5_difbusBtn.setText("다른버스 선택하기");

                try {
                    //단말기 테이블 중복 추가 되지 않게 1초 delay
                    Thread.sleep(1000);

                    //안내 재시작 이벤트 서버에 전송 (단말기로 전송)
                    userobj.remove("event");
                    userobj.addProperty("event", 101);  //101-안내시작
                    sc.sendUser(userobj);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        //승차할 버스 번호 클릭시
        inbusName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                tts.FuncVoiceOut(inbusName.getText() + " 도착정보 새로고침");
                Toast.makeText(MainActivity.this, inbusName.getText() + " 도착정보 새로고침", Toast.LENGTH_LONG).show();

                //01(도착) msg 얻기 어려워서
                //승차할 버스 정거장에 승차할 버스가 도착했는지 화인
                BusArrivalStartBusStop();

                //기존값 초기화
                routeNoList.clear();
                carRegNoList.clear();
                routeCdList.clear();
                statusPosList.clear();
                extimeMinList.clear();
                msgTpList.clear();
                
                //승차버스의 정류장 도착정보 조회 및 갱신
                BusStopArrival_Thread7 thread = new BusStopArrival_Thread7(handler, StartbusStop_ID);
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String Text = inbusName.getText().toString();
                String[] TextSp = Text.split("번 ");
                String inbusNameText = TextSp[0];

                beforeStatusPos = busIn_statusPos;

                for(int i=0; i<routeNoList.size(); i++){
                    //처음에 승차할 버스의 정보 얻기
                    if(busNumList.get(0).toString().equals(routeNoList.get(i))){
                        busIn_carRegNo = carRegNoList.get(i);
                        busIn_routeCd = routeCdList.get(i);
                        busIn_statusPos = statusPosList.get(i);
                    }

                    //---------------------------------------------------------------------
                    //승차할 버스 상태 ui 설정
                    if (inbusNameText.equals(routeNoList.get(i))) {
                        switch (msgTpList.get(i)) {
                            case "01":
                                //도착
                                status_pos.setText("도착");
                                tts.FuncVoiceOut(inbusName.getText() + "가 도착했습니다.\n승차 하시길 바랍니다.");
                                Toast.makeText(MainActivity.this,inbusName.getText() + "가 도착했습니다.\n승차 하시길 바랍니다.",Toast.LENGTH_LONG).show();

                                //이벤트 설정
                                userobj.remove("event");
                                userobj.addProperty("event", 104);   //104-버스도착
                                sc.sendUser(userobj);

                                //내위치와 버스위치 비교
                                //여기서 사용자가 승차했는지 감지
                                BusInSense();
                                break;
                            case "06":
                                status_pos.setText("진입중");
                                tts.FuncVoiceOut(inbusName.getText() + "가 진입중입니다.");

                                //이벤트 설정
                                userobj.remove("event");
                                userobj.addProperty("event", 103);   //103-버스근접
                                sc.sendUser(userobj);

                                //내위치와 버스위치 비교
                                //여기서 사용자가 승차했는지 감지
                                //BusInSense();
                                break;
                            case "07":
                                status_pos.setText("차고지 운행 대기중");
                                break;
                            default:
                                if(bus_arrival_at_busstop){
                                status_pos.setText("도착");
                                tts.FuncVoiceOut(inbusName.getText() + "가 도착했습니다.\n승차 하시길 바랍니다.");
                                Toast.makeText(MainActivity.this,inbusName.getText() + "가 도착했습니다.\n승차 하시길 바랍니다.",Toast.LENGTH_LONG).show();
                                }else{
                                    tts.FuncVoiceOut(inbusName.getText().toString() + "가 " + statusPosList.get(i) + " 정거장 남았습니다.");
                                    Toast.makeText(MainActivity.this, inbusName.getText().toString() + "가 " + statusPosList.get(i) + " 정거장 남았습니다.", Toast.LENGTH_LONG).show();
                                    status_pos.setText(statusPosList.get(i) + " 정거장 남았습니다 " + "(" + extimeMinList.get(i) + "분)");
                                }

                                break;
                        }

                    }

                    //-------------------------------------------------------------------------
                }

                //승차 가능한 다른 버스 리스트 ui 설정
                if (busNumList.size() == 1) {
                    Log.d("ReceiveMessage", "대체가능한 버스가 없습니다.");

                    ArrayList<String> items = new ArrayList<>();
                    items.add("대체가능한 버스가 없습니다.");

                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            R.layout.default_listview_textview,
                            items){
                        @NonNull
                        @Override
                        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                            View view = super.getView(position, convertView, parent);
                            TextView tv = (TextView)view.findViewById(R.id.defaultList_TextView);

                            //글자 크기 설정
                            if(textRaise){
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                            } else {
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                            }

                            return view;
                        }
                    };
                    //ArrayAdapter<String> adapter = new ArrayAdapter<String>(cThis, R.layout.default_listview_textview, items);
                    search_result_layout4_list.setAdapter(adapter);

                } else {
                    routeNoList_1.clear();
                    statusPosList_1.clear();
                    extimeMinList_1.clear();
                    msgList_1.clear();

                    for(int i=0; i<routeNoList.size(); i++){
                        for (int j = 1; j < busNumList.size(); j++) {
                            if (routeNoList.get(i).equals(busNumList.get(j).toString())) {
                                routeNoList_1.add(routeNoList.get(i));
                                statusPosList_1.add(statusPosList.get(i));
                                extimeMinList_1.add(extimeMinList.get(i));
                                msgList_1.add(msgTpList.get(i));

                                busStop_listViewAdapter = new BusStop_ListViewAdapter(cThis, routeNoList_1, statusPosList_1, extimeMinList_1, msgList_1, textRaise);
                                search_result_layout4_list.setAdapter(busStop_listViewAdapter);

                                Log.d("ReceiveMessage", "busNumList : " + busNumList.get(j).toString());
                            }
                        }
                    }
                }

                //이전 정거장과 현재 정거장 수가 다르고 한정거장 남지 않았다면
                if(beforeStatusPos.equals(busIn_statusPos) == false && Integer.parseInt(busIn_statusPos) != 1){
                    //이벤트 설정
                    userobj.remove("event");
                    userobj.addProperty("event", 102);   //102-버스 한정류장 가까워짐
                    //서버에 전송
                    sc.sendUser(userobj);
                }

                beforeStatusPos = busIn_statusPos;
            }
        });

        //하차벨 클릭시
        busout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                if (busOut_Check == false) {
                    busout_btn.setBackground(getDrawable(R.drawable.btn_busout_on));
                    busout_btn.setText("하차벨 취소");

                    busin_numStaionLeft.setText("다음 정거장에서 버스가 정차합니다.");

                    //203 이벤트랑 겹쳐서
                    if(mainbusout == false){
                        //하차 요청 보내기
                        userobj.remove("event");
                        userobj.addProperty("event", 301);  //301-중간에내리기요청
                        sc.sendUser(userobj);
                    }

                    busOut_Check = true;
                } else {
                    busout_btn.setBackground(getDrawable(R.drawable.btn_busout_off));
                    busout_btn.setText("하차벨");

                    //하차 요청 보내기
                    userobj.remove("event");
                    userobj.addProperty("event", 399);  //399-중간에내리기취소
                    sc.sendUser(userobj);

                    busOut_Check = false;

                    //데이터 얻기
                    SetBusInData2();
                }

            }
        });

        //현재정류장 클릭시 데이터 갱신
        busin_nowStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                tts.FuncVoiceOut("현재 정거장 새로고침");
                Toast.makeText(MainActivity.this, "현재 정거장 새로고침", Toast.LENGTH_LONG).show();

                int before = numStaionLeft_count;

                //데이터 얻기
                //SetBusInData();
                SetBusInData2();

                if(before > numStaionLeft_count && numStaionLeft_count > 1){
                    //이벤트 설정
                    userobj.remove("event");
                    userobj.addProperty("event", 202);  //202-한정거장지나감
                    sc.sendUser(userobj);
                }
            }
        });

    }

    public void FirstUserCheak(){
        SharedPreferences sharedPreferences = getSharedPreferences("isFirst", MODE_PRIVATE);
        boolean first = sharedPreferences.getBoolean("isFirst", false);

        if(first == false) {
            Log.d(TAG, "Is first Time? first");
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isFirst", true);
            editor.commit();

            //인스턴스ID 앱 설치시 고유 식별자 부여 (수명 : 앱 삭제 후 재설치 시 변경됨)
            String uniqueID = UUID.randomUUID().toString();

            //저장
            SharedPreferences sharedPreferences2 = getSharedPreferences("DisabledType", MODE_PRIVATE);
            SharedPreferences.Editor editor2 = sharedPreferences2.edit();
            editor2.putString("uniqueID", uniqueID);
            editor2.commit();

            Log.d(TAG, "uniqueID : " + uniqueID);

            //고유 식별자 서버에 데이터 전송
            //..
            //userobj.remove("id");
            //locaionobj.remove("id");

            userobj.addProperty("id", uniqueID);
            locaionobj.addProperty("id", uniqueID);

            //앱 최초 실행 시 하고싶은 작업
            Intent intent = new Intent(getApplicationContext(), OnBoardingActivity.class);
            startActivityResult.launch(intent); //다음화면으로 넘어감

        } else { //최초 실행이 아닌 경우
            Log.d(TAG, "Is first Time? not first");

            SharedPreferences sharedPreferences2 = getSharedPreferences("DisabledType", MODE_PRIVATE);
            int DisabledType = sharedPreferences2.getInt("DisabledType", 0);
            String uniqueID = sharedPreferences2.getString("uniqueID", null);

            //장애인 타입 설정을 안했다면
            if(DisabledType == 0){
                Intent intent = new Intent(getApplicationContext(), OnBoardingActivity.class);
                startActivityResult.launch(intent); //다음화면으로 넘어감
            }

            //1 : 시각장애인, 2 : 청각장애인, 3 : 지체장애인
            Log.d(TAG, "DisabledType : " + DisabledType);

            //고유 식별자 가져오기
            Log.d(TAG, "uniqueID : " + uniqueID);

            //장애인 유형 및 고유 식별자 서버에 데이터 전송
            //..
            //userobj.remove("id");
            //userobj.remove("type");
            //locaionobj.remove("id");

            userobj.addProperty("id", uniqueID);
            userobj.addProperty("type", DisabledType);
            locaionobj.addProperty("id", uniqueID);
        }
    }

    public int PxToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return Integer.parseInt(String.format("%.0f", dp));
    }

    public void SetBusInData2(){
        Double busInLat = 0.0;
        Double busInLong = 0.0;
        String busStopId7 = "";
        String busStopId5 = "";
        int busIn_busstopSeq = 0;
        int busOut_busstopSeq = 0;
        String busIn_Name="";

        Log.d("BusIn", "carRegNo:" + busIn_carRegNo + " routeCd:" + busIn_routeCd + " type:" + busIn_type);

        plateNoList.clear();
        bus_gpsLatiList.clear();
        bus_gpsLongList.clear();
        busStopId7List.clear();
        busStopId5List.clear();

        GetBusPosByRtid_Thread thread1 = new GetBusPosByRtid_Thread(handler, busIn_routeCd);
        thread1.start();
        try {
            thread1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int j = 0; j < plateNoList.size(); j++) {

            if (busIn_carRegNo.equals(plateNoList.get(j))) {
                //내가 승차한 버스의 위치값과 현재 위치한 버스정류장 id 가져오기
                busInLat = Double.parseDouble(bus_gpsLatiList.get(j));
                busInLong = Double.parseDouble(bus_gpsLongList.get(j));
                busStopId7 = busStopId7List.get(j);
                busStopId5 = busStopId5List.get(j);

                Log.d("BusIn", "busInLat-" + busInLat + " busInLong-" + busInLong);
                Log.d("BusIn", "plateNoList-" + plateNoList.get(j));
            }
        }

        BusStopByRoute_Thread thread2 = new BusStopByRoute_Thread(handler, busIn_routeCd);
        thread2.start();
        try {
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int j = 0; j < busStops.size(); j++) {
            //하차하는 정류장의 순번 얻기
            if(busOut_splitID != null){
                if (busOut_splitID.equals(busStops.get(j).getBusStopID())) {
                    busOut_busstopSeq = busStops.get(j).getBusstopSeq();
                    Log.d("BusIn", "busOut_splitID : " + busOut_splitID);
                    Log.d("BusIn", "busOut_BusStopID7 : " + busStops.get(j).getBusStopID());
                    Log.d("BusIn", "busOut_busstopSeq : " + busOut_busstopSeq);
                }
                //현재 정류장의 순번 및 이름 얻기
                if(busStopId7.equals(busStops.get(j).getBusStopID())){
                    busIn_busstopSeq = busStops.get(j).getBusstopSeq();
                    busIn_Name = busStops.get(j).getBusStopName();
                    Log.d("BusIn", "busIn_BusStopID5 : " + busStopId5);
                    Log.d("BusIn", "busIn_BusStopID7 : " + busStopId7);
                    Log.d("BusIn", "busIn_busstopSeq : " + busIn_busstopSeq);
                }
            }
        }

        //남은 정거장 수
        numStaionLeft_count = busOut_busstopSeq - busIn_busstopSeq;

        Log.d("BusIn", busIn_Number + "번 버스의 " + "(carRegNo:" + busIn_carRegNo + ") 이번 정거장은 " + busIn_Name);
        Log.d("BusIn", "도착까지 남은 정거장 수 : " + numStaionLeft_count);

        //현재 정류장 서버에 전송할 데이터 저장
        JsonObject currentobj = new JsonObject();
        currentobj.addProperty("id", busStopId5);
        currentobj.addProperty("name", busIn_Name);
        userobj.remove("current");  //기존값(null) 담긴거 지움
        userobj.add("current", currentobj);

        //-------------------------------------------------------------------------------------------------------------------

        //ui 설정
        Bundle bundle = new Bundle();
        Message message = handler.obtainMessage();
        bundle.putBoolean("SetBusInData", true);
        bundle.putString("BusStopName", busIn_Name);
        bundle.putInt("numStaionLeft_count", numStaionLeft_count);

        //남은 정거장이 1개 남았다면 하차 알림
        if(numStaionLeft_count == 1){
            tts.FuncVoiceOut("도착까지 한 정거장 남았습니다.");

            //이벤트 설정
            userobj.remove("event");
            userobj.addProperty("event", 203);  //203-내릴곳거의도착
            sc.sendUser(userobj);

            //이벤트 중복 방지 301과
            mainbusout = true;

            bundle.putBoolean("busout_btn", true);

        } else{
            bundle.putBoolean("busout_btn", false);
        }

        if(numStaionLeft_count == 0){
            tts.FuncVoiceOut(EndbusStop_Name + " 정거장에 도착했습니다. 하차하시길 바랍니다.");

            //진동 설정
            if(Vibrator){
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
            }

            //이벤트 설정
            userobj.remove("event");
            userobj.addProperty("event", 204);  //204-내릴곳도착
            sc.sendUser(userobj);

            //하차 감지
            BusOutSense();

            bundle.putBoolean("UserBusStopArrival", true);

        } else{
            bundle.putBoolean("UserBusStopArrival", false);
        }

        //메시지 전송
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public void SetBusInDataFiveSecond(){
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int count = 0;

            @Override
            public void run() {
                Log.d("SetBusInDataFiveSecond", "count : " + count);

                int before = numStaionLeft_count;

                //데이터 얻기
                //SetBusInData();

                //값이 바뀌었다면
                if(before != numStaionLeft_count){
                    userobj.remove("event");
                    userobj.addProperty("event", 102);  //102-버스 한정류장 가까워짐
                    //sc.sendUser(userobj);
                }
            }
        };
        timer.schedule(timerTask, 0, 5000);
    }

    //아래 사용 후 데이터 안담김
    public void BusInLocationUpdate(){
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int i = 0;

            @Override
            public void run() {
                if(i < 500){
                    plateNoList.clear();
                    bus_gpsLatiList.clear();
                    bus_gpsLongList.clear();
                    busStopId7List.clear();
                    busStopId5List.clear();

                    GetBusPosByRtid_Thread thread1 = new GetBusPosByRtid_Thread(handler, busIn_routeCd);
                    thread1.start();
                    try {
                        thread1.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (int j = 0; j < plateNoList.size(); j++) {
                        //Log.d("BusIn", "plateNoList-" + plateNoList.get(j).toString());

                        if (plateNoList.get(j).equals(busIn_carRegNo)) {
                            //내가 승차한 버스의 위치값 가져오기

                            busIn_Latitude = bus_gpsLatiList.get(j);
                            busIn_Longitude = bus_gpsLongList.get(j);
                        }
                    }

                    i++;

                    Log.d("BusInLocationUpdate", "버스 갱신 횟수 : " + i);
                    Log.d("BusInLocationUpdate", "busIn_Latitude : " + busIn_Latitude + " busIn_Longitude : " + busIn_Longitude);

                } else {

                    Log.d("BusInLocationUpdate", "버스 갱신 횟수 초과");
                    return;
                }
            }
        };
        timer.schedule(timerTask, 0, 5000);
    }

    public void OnceSecondSand_MyLocationAndBusInLocation(){
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("OnceSecondSand", "MyLocation/" + "Latitude : " + nowLatitude.toString() + " Longitude" + nowLongitude.toString());
                Log.d("OnceSecondSand", "BusInLocation/" + "Latitude : " + busIn_Latitude + " Longitude" + busIn_Longitude);
            }
        };
        timer.schedule(timerTask, 0, 5000);
    }

    public void BusInSense(){
        //버스의 위치와 내위치가 10초이상 같다면 승차 했다고 감지
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int count = 0;

            @Override
            public void run() {
                Log.d("BusInSense", "count : " + count);

                //10초 동안 실행되면 타이머 종료
                if(count == 10){
                    Log.d("BusInSense", "iambusin : " + iambusin);
                    cancel(); //timerTask 종료
                    timer.cancel();

                    //내가 버스에 승차했다면
                    if(iambusin == true){
                        Log.d("BusInSense", busIn_Number + "번 버스에 탑승했습니다.");
                        tts.FuncVoiceOut(busIn_Number + "번 버스에 탑승했습니다.");

                        //ui설정
                        Bundle bundle = new Bundle();
                        Message message = handler.obtainMessage();
                        bundle.putBoolean("BusInSense", true);
                        bundle.putBoolean("result", true);
                        message.setData(bundle);
                        handler.sendMessage(message);

                        //진동 설정
                        if(Vibrator){
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));
                        }

                        //승차시 데이터 얻기
                        //SetBusInData();
                        SetBusInData2();
                        //승차시 데이터 얻기(5초에 한번씩)
                        //SetBusInDataFiveSecond();

                        //이벤트 설정
                        userobj.remove("event");
                        userobj.addProperty("event", 201);  //201-탑승
                        sc.sendUser(userobj);


                    } else if(iambusin == false) {
                        Log.d("BusInSense", busIn_Number + "번 버스에 탑승하지 않았습니다.");
                        tts.FuncVoiceOut(busIn_Number + "번 버스에 탑승하지 않았습니다. 안내를 종료합니다.");

                        //ui설정
                        Bundle bundle = new Bundle();
                        Message message = handler.obtainMessage();
                        bundle.putBoolean("BusInSense", true);
                        bundle.putBoolean("result", false);
                        message.setData(bundle);
                        handler.sendMessage(message);
                        
                        //승차 못했을 시
                        //이벤트 설정
                        userobj.remove("event");
                        userobj.addProperty("event", 199);  //199-버스도착했는데 탑승안함
                        sc.sendUser(userobj);
                    }
                }
                else{
                    //사용자 위치와 승차할 버스 위치 거리 미터 단위로 계산
                    //수정 해야됨
                    if(StartbusStop_Latitude != null && StartbusStop_Longitude != null){

                        plateNoList.clear();
                        bus_gpsLatiList.clear();
                        bus_gpsLongList.clear();
                        busStopId7List.clear();
                        busStopId5List.clear();

                        GetBusPosByRtid_Thread thread1 = new GetBusPosByRtid_Thread(handler, busIn_routeCd);
                        thread1.start();
                        try {
                            thread1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        for (int j = 0; j < plateNoList.size(); j++) {
                            //Log.d("BusIn", "plateNoList-" + plateNoList.get(j).toString());

                            if (plateNoList.get(j).equals(busIn_carRegNo)) {
                                //내가 승차한 버스의 위치값 가져오기
                                busIn_Latitude = bus_gpsLatiList.get(j);
                                busIn_Longitude = bus_gpsLongList.get(j);
                            }
                        }

                        //값이 안담긴다면 다시 가져오기
                        if(busIn_Latitude == null && busIn_Longitude == null){
                            GetBusPosByRtid_Thread thread2 = new GetBusPosByRtid_Thread(handler, busIn_routeCd);
                            thread2.start();
                            try {
                                thread1.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            for (int j = 0; j < plateNoList.size(); j++) {
                                //Log.d("BusIn", "plateNoList-" + plateNoList.get(j).toString());

                                if (plateNoList.get(j).equals(busIn_carRegNo)) {
                                    //내가 승차한 버스의 위치값 가져오기
                                    busIn_Latitude = bus_gpsLatiList.get(j);
                                    busIn_Longitude = bus_gpsLongList.get(j);
                                }
                            }
                        }
                        else{
                            Log.d("BusInSense", "busIn_Latitude : " + busIn_Latitude + " busIn_Longitude : " + busIn_Longitude);
                            Log.d("BusInSense", "nowLatitude : " + nowLatitude + " nowLongitude : " + nowLongitude);

                            LocationDistance locationdistance = new LocationDistance();
                            Double distance = locationdistance.distance(Double.parseDouble(busIn_Latitude), Double.parseDouble(busIn_Longitude), nowLatitude, nowLongitude, "meter");

                            Log.d("BusInSense", "distance(UserToStartBusstop) : " + distance.intValue());

                            //수정해야됨
                            //거리가 100m 이내라면
                            if(distance.intValue() <= 100){
                                iambusin = true;
                            }
                            else{
                                iambusin = false;
                            }
                        }
                    }

                    count++;
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);

        //이벤트 수정 후 전송
        //sc.sendUser(userobj);
    }

    public void BusOutSense(){
        //버스의 위치와 내위치가 10초이상 다르다면 하차 했다고 감지
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            int count = 0;

            @Override
            public void run() {
                Log.d("BusOutSense", "count : " + count);

                //10초 동안 실행되면 타이머 종료
                if(count == 10){
                    Log.d("BusOutSense", "iambusout : " + iambusout);
                    cancel(); //timerTask 종료
                    timer.cancel();

                    //내가 버스에 하차했다면
                    if(iambusout == true){
                        //이벤트 설정
                        userobj.remove("event");
                        userobj.addProperty("event", 400);  //400- 사용종료
                        sc.sendUser(userobj);
                        
                    } else {
                        //하차 못했을 시
                        //이벤트 설정
                        userobj.remove("event");
                        userobj.addProperty("event", 299);  //299-내릴곳도착했는데안내림
                        sc.sendUser(userobj);
                    }
                }
                else{
                    //사용자 위치와 승차한 버스 위치 거리 미터 단위로 계산
                    //수정 해야됨
                    if(EndbusStop_Latitude != null && EndbusStop_Latitude != null){

                        plateNoList.clear();
                        bus_gpsLatiList.clear();
                        bus_gpsLongList.clear();
                        busStopId7List.clear();
                        busStopId5List.clear();

                        GetBusPosByRtid_Thread thread1 = new GetBusPosByRtid_Thread(handler, busIn_routeCd);
                        thread1.start();
                        try {
                            thread1.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        for (int j = 0; j < plateNoList.size(); j++) {
                            //Log.d("BusIn", "plateNoList-" + plateNoList.get(j).toString());

                            if (plateNoList.get(j).equals(busIn_carRegNo)) {
                                //내가 승차한 버스의 위치값 가져오기
                                busIn_Latitude = bus_gpsLatiList.get(j);
                                busIn_Longitude = bus_gpsLongList.get(j);
                            }
                        }

                        //값이 안담긴다면 다시 가져오기
                        if(busIn_Latitude == null && busIn_Longitude == null){
                            GetBusPosByRtid_Thread thread2 = new GetBusPosByRtid_Thread(handler, busIn_routeCd);
                            thread2.start();
                            try {
                                thread1.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            for (int j = 0; j < plateNoList.size(); j++) {
                                //Log.d("BusIn", "plateNoList-" + plateNoList.get(j).toString());

                                if (plateNoList.get(j).equals(busIn_carRegNo)) {
                                    //내가 승차한 버스의 위치값 가져오기
                                    busIn_Latitude = bus_gpsLatiList.get(j);
                                    busIn_Longitude = bus_gpsLongList.get(j);
                                }
                            }
                        }
                        else{
                            Log.d("BusOutSense", "busIn_Latitude : " + busIn_Latitude + " busIn_Longitude : " + busIn_Longitude);

                            //수정해야됨
                            LocationDistance locationdistance = new LocationDistance();
                            Double distance = locationdistance.distance(Double.parseDouble(busIn_Latitude), Double.parseDouble(busIn_Longitude), nowLatitude, nowLongitude, "meter");

                            Log.d("BusOutSense", "distance : " + distance.intValue());

                            //수정해야됨
                            //거리가 100m 이상이라면
                            if(distance.intValue() >= 100){
                                iambusout = true;
                            }
                            else{
                                iambusout = false;
                            }
                        }
                    }

                    count++;
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);

        //sc.sendUser(userobj);
    }

    public void GetBusInInfo(String busIn_routeCd, String busIn_carRegNo){
        buslist.clear();

        //노선별 차량정보 가져오기
        GetBusRegInfoByRouteId_Thread thread = new GetBusRegInfoByRouteId_Thread(handler, busIn_routeCd);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //내가 승차할 차량 타입 가져오기 (0:정보없음 1:일반 2:저상)
        for(int i=0; i<buslist.size(); i++){
            if(busIn_carRegNo.equals(buslist.get(i).GetBusCarRegNo())){
                busIn_type = buslist.get(i).GetBusType();
                Log.d("GetBusInInfo", "busIn_routeCd : " + busIn_routeCd + " busIn_carRegNo : " + busIn_carRegNo + " type : " + busIn_type);

                //저상 버스일 경우
                if(busIn_type == 2){
                    inbusName.setText(inbusName.getText() + "번 저상버스");
                } else {
                    inbusName.setText(inbusName.getText() + "번 버스");
                }
            }
        }
    }

    public void BusArrivalStartBusStop(){
        if(StartbusStop_Latitude != null && StartbusStop_Longitude != null){

            plateNoList.clear();
            bus_gpsLatiList.clear();
            bus_gpsLongList.clear();
            busStopId7List.clear();
            busStopId5List.clear();

            GetBusPosByRtid_Thread thread1 = new GetBusPosByRtid_Thread(handler, busIn_routeCd);
            thread1.start();
            try {
                thread1.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (int j = 0; j < plateNoList.size(); j++) {
                //Log.d("BusIn", "plateNoList-" + plateNoList.get(j).toString());

                if (plateNoList.get(j).equals(busIn_carRegNo)) {
                    //내가 승차한 버스의 위치값 가져오기
                    //busIn_Latitude = bus_gpsLatiList.get(j);
                    //busIn_Longitude = bus_gpsLongList.get(j);
                    //내가 승차한 버스의 현재정류장 가져오기
                    busIn_BusStopId7 = busStopId7List.get(j);

                    GetStationByStopID_Thread thread = new GetStationByStopID_Thread(handler, busIn_BusStopId7, "MainActivity");
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(busStopNM != null){
                busIn_BusStopNM = busStopNM;

                Log.d("DistanceBusAndStartBusStop", "busIn_BusStopId7 : " + busIn_BusStopId7 + " busStopNM : " + busIn_BusStopNM);

                //승차할 버스정거장 이름과 버스의 현재정거장 이름이 같다면
                //즉, 버스가 승차정거장에 도착했다면
                if(StartbusStop_Name.equals(busIn_BusStopNM)){
                    bus_arrival_at_busstop = true;

                    //이벤트 설정
                    userobj.remove("event");
                    userobj.addProperty("event", 104);   //104-버스도착
                    sc.sendUser(userobj);

                    //내위치와 버스위치 비교
                    //여기서 사용자가 승차했는지 감지
                    BusInSense();
                } else{
                    bus_arrival_at_busstop = false;
                }
            }
        }
    }

    public void ODSayRequestSearchPubTransPath(String sx, String sy, String ex, String ey){
        //길찾기 api 사용
        odsayService.requestSearchPubTransPath(sx, sy, ex, ey, "1", "0", "2", onResultCallbackListener1);
    }

    public void ODSayRequestBusStationInfo(String stationId) {
        //버스 정류장 세부 정보 조회
        odsayService.requestBusStationInfo(stationId, onResultCallbackListener2);
    }

    private OnResultCallbackListener onResultCallbackListener1 = new OnResultCallbackListener() {
        @Override
        public void onSuccess(ODsayData oDsayData, API api) {
            JSONObject jsonObject = oDsayData.getJson();
            Log.d("onResultCallbackListener1", jsonObject.toString());

            try {
                if(jsonObject.has("result")){
                    ArrayList<LatLng> LatLngList = new ArrayList<>();

                    //infoList
                    ArrayList<PubTransPath_Info> pubTransPathInfos = new ArrayList<>();
                    //infoList2
                    ArrayList<PubTransPath_Info> pubTransPathInfos_2 = new ArrayList<>();
                    //내가 사용할 경로 위치
                    int position = 0;
                    
                    //-----------------------------------------------------------------
                    JSONObject result = jsonObject.getJSONObject("result");
                    JSONArray addrArray = result.getJSONArray("path");
                    for(int i=0; i<addrArray.length(); i++){
                        JSONObject obj = addrArray.getJSONObject(i);
                        JSONObject info = obj.getJSONObject("info");

                        int totalTime = info.getInt("totalTime");                       //총 소요시간
                        int payment = info.getInt("payment");                           //총 요금
                        int busTransitCount = info.getInt("busTransitCount");           //총 환승 카운트
                        String firstStartStation = info.getString("firstStartStation"); //최초 출발 정류장
                        String lastEndStation = info.getString("lastEndStation");       //최초 도착 정류장
                        int busStationCount = info.getInt("busStationCount");           //총 버스정류장 합
                        double totalDistance = info.getDouble("totalDistance");         //총 거리

                        Log.d("onResultCallbackListener1/info","totalTime:" + totalTime + "payment:" + payment + "busTransitCount:" + busTransitCount + "firstStartStation:" +
                                firstStartStation + "lastEndStation:" + lastEndStation + "busStationCount:" + busStationCount + "totalDistance:" + totalDistance);

                        //객체 생성 및 저장
                        PubTransPath_Info pubTransPathInfo = new PubTransPath_Info(i, totalTime, payment, busTransitCount, firstStartStation, lastEndStation, busStationCount, totalDistance);
                        pubTransPathInfos.add(pubTransPathInfo);

                    }

                    for(int a=0; a<pubTransPathInfos.size(); a++){
                        //총 환승 카운트가 1개인 경우에만 리스트 저장
                        if(pubTransPathInfos.get(a).GetBusTransitCount() == 1){
                            pubTransPathInfos_2.add(pubTransPathInfos.get(a));
                        }
                    }


                    //총 환승 카운트가 1개인 경우가 없다면
                    if(pubTransPathInfos_2.size() == 0){
                        tts.FuncVoiceOut("경로를 찾을 수 없습니다.");
                        Toast.makeText(MainActivity.this, "경로를 찾을 수 없습니다.", Toast.LENGTH_LONG).show();

                        //뒤로가기
                        onBackPressed();
                        
                        return;
                    }
                    else {
                        int min = pubTransPathInfos_2.get(0).GetTotalTime(); //최소값

                        //총 소요시간이 제일 적은 것을 구하기
                        for(int b=0; b<pubTransPathInfos_2.size(); b++){
                            if(min > pubTransPathInfos_2.get(b).GetTotalTime()) {
                                min = pubTransPathInfos_2.get(b).GetTotalTime();
                                position = pubTransPathInfos_2.get(b).GetNum();
                            }
                        }
                    }

                    Log.d("onResultCallbackListener1/minTime","totalTime:" + pubTransPathInfos.get(position).GetTotalTime());

                    //총 소요시간이 제일 적은 것을 출력
                    JSONObject minTimeobj = addrArray.getJSONObject(position);
                    JSONArray subPath = minTimeobj.getJSONArray("subPath");
                    for(int h=0; h<subPath.length(); h++){
                        JSONObject subPathobj = subPath.getJSONObject(h);
                        int trafficType = subPathobj.getInt("trafficType");

                        if(trafficType == 2){//이동 수단 버스
                            String startName = subPathobj.getString("startName");  //숭차 정류장 이름
                            double startX = subPathobj.getDouble("startX");        //승차 정류장 좌표 x
                            double startY = subPathobj.getDouble("startY");        //승차 정류장 좌표 Y
                            String endName = subPathobj.getString("endName");      //하차 정류장 이름
                            double endX = subPathobj.getDouble("endX");            //하차 정류장 좌표 x
                            double endY = subPathobj.getDouble("endY");            //하차 정류장 좌표 Y
                            String startID = subPathobj.getString("startID");            //승차 정류장 아이디
                            String endID = subPathobj.getString("endID");                //하차 정류장 아이디

                            Log.d("onResultCallbackListener1/2/subPath", "startName:" + startName + "startX:" + startX + "startY:" + startY + "endName:" + endName + "endX:" +
                                    endX + "endY:" + endY + "startID:" + startID + "endID:" + endID);

                            //카메라 이동
                            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(startY, startX));
                            mNaverMap.moveCamera(cameraUpdate);

                            //마커에 승하차 정류장 이름 넣기
                            start_marker.setCaptionText(startName);
                            start_marker.setCaptionRequestedWidth(100);
                            start_marker.setCaptionTextSize(16);
                            and_marker.setCaptionText(endName);
                            and_marker.setCaptionRequestedWidth(100);
                            and_marker.setCaptionTextSize(16);

                            JSONArray lane = subPathobj.getJSONArray("lane");

                            for(int k=0; k<lane.length(); k++){
                                JSONObject laneobj = lane.getJSONObject(k);
                                int busNo = laneobj.getInt("busNo");                //버스 번호
                                int bustype = laneobj.getInt("type");               //버스 타입
                                int busID = laneobj.getInt("busID");                //버스 아이디

                                Log.d("onResultCallbackListener1/2/lane", "busNo:" + busNo + "bustype:" + bustype + "busID:" + busID);

                                //승차할 버스 리스트 저장
                                busNumList.add(busNo);
                            }

                            JSONObject passStopList = subPathobj.getJSONObject("passStopList");
                            JSONArray stations = passStopList.getJSONArray("stations");
                            for(int j=0; j<stations.length(); j++){
                                JSONObject stationobj = stations.getJSONObject(j);
                                int stationindex = stationobj.getInt("index");              //정류장 순번
                                int stationID = stationobj.getInt("stationID");             //정류장 ID
                                String stationName = stationobj.getString("stationName");   //정류장 이름
                                double stationX = stationobj.getDouble("x");                //정류장 X좌표
                                double stationY = stationobj.getDouble("y");                //정류장 Y좌표
                                String isNonStop = stationobj.getString("isNonStop");       //미정차 정류장 여부

                                Log.d("onResultCallbackListener1/2/stations", "stationindex:" + stationindex + "stationID:" + stationID + "stationName:" + stationName +
                                        "stationX:" + stationX + "stationY:" + stationY + "isNonStop:" + isNonStop);

                                LatLng latLng = new LatLng(stationY, stationX); //반대로 넣어야됨
                                LatLngList.add(latLng);
                            }

                            //승차할 정류장의 정보 저장
                            JSONObject stationobj_0 = stations.getJSONObject(0);
                            //stationID_0 = stationobj_0.getInt("stationID");             //정류장 ID
                            stationID_0 = startID;

                            //하차할 정류장의 정보 저장
                            JSONObject stationobj_and = stations.getJSONObject(stations.length()-1);
                            //stationID_and = stationobj_and.getInt("stationID");             //정류장 ID
                            stationID_and = endID;

                        }
                        else if(trafficType == 3){//이동 수단 도보
                            double distance = subPathobj.getDouble("distance"); //이동거리
                            int sectionTime = subPathobj.getInt("sectionTime");  //이동 소요 시간
                            Log.d("onResultCallbackListener1/3","distance" + distance + "sectionTime" + sectionTime);
                        }
                    }
                    //-----------------------------------------------------------------
                    //MAP에 경로선 추가
                    //전척률
                    //path.setProgress(0.5);
                    //두께
                    path.setWidth(20);
                    //패턴이미지
                    path.setPatternImage(OverlayImage.fromResource(R.drawable.ic_baseline_navigation_24));
                    path.setPatternInterval(30);
                    //지나간 경로선 색 및 테두리 색 설정
                    path.setPassedColor(Color.GRAY);
                    path.setPassedOutlineColor(Color.GRAY);
                    //지나기 전 경로선 색 및 테두리 색 설정
                    path.setColor(Color.BLUE);
                    path.setOutlineColor(Color.BLUE);
                    //path.setColor(Color.rgb(255,69,0));
                    //path.setOutlineColor(Color.rgb(255,69,0));
                    //경로선과 곂치는 지도 심벌 숨김
                    path.setHideCollidedSymbols(true);
                    path.setCoords(LatLngList);

                    path.setMap(mNaverMap);

                    //승하차 정류장 좌표 지도상에 마커 표시
                    search_marker.setMap(null);

                    start_marker.setPosition(LatLngList.get(0));
                    //start_marker.setIcon(MarkerIcons.GREEN);
                    start_marker.setWidth(140);
                    start_marker.setHeight(140);
                    start_marker.setIcon(OverlayImage.fromResource(R.drawable.ic_bus_marker_green));
                    start_marker.setMap(mNaverMap);

                    and_marker.setPosition(LatLngList.get(LatLngList.size()-1));
                    //and_marker.setIcon(MarkerIcons.RED);
                    and_marker.setWidth(140);
                    and_marker.setHeight(140);
                    and_marker.setIcon(OverlayImage.fromResource(R.drawable.ic_bus_marker_red));
                    and_marker.setMap(mNaverMap);

                    //powered by www.ODsay.com 꼭 표시

                    //승차할 정류장 정보 얻기
                    station_0_Check = true;
                    ODSayRequestBusStationInfo(stationID_0);

                }
                else if(jsonObject.has("error")){
                    Log.d("onResultCallbackListener1", "error");

                    //{"error":{"msg":"출, 도착지가 700m이내입니다.","code":"-98"}}
                    JSONObject error = jsonObject.getJSONObject("error");
                    String msg = error.getString("msg");
                    String code = error.getString("code");
                    Log.d("onResultCallbackListener1", "code : " + code + " msg : " + msg);
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                    tts.FuncVoiceOut(msg);

                    onBackPressed(); //뒤로가기 버튼
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(int i, String errorMessage, API api) {
            Log.d("onResultCallbackListener1", "API : " + api.name() + errorMessage);
        }
    };

    private OnResultCallbackListener onResultCallbackListener2 = new OnResultCallbackListener(){
        @Override
        public void onSuccess(ODsayData oDsayData, API api) {

            try{
               if(station_0_Check){
                   JSONObject jsonObject = oDsayData.getJson();
                   Log.d("onResultCallbackListener2", jsonObject.toString());

                   JSONObject result = jsonObject.getJSONObject("result");
                   String Latitude = result.getString("y");     //경도
                   String Longitude = result.getString("x");    //위도
                   String stationName = result.getString("stationName");
                   String localStationID = result.getString("localStationID");

                   String[] localid = localStationID.split("DJB");
                   String splitID = localid[1];

                   Log.d("onResultCallbackListener2", "stationName:" + stationName + " localStationID:" + localStationID
                           + " splitID:" + splitID + " Latitude:" + Latitude + " Longitude:" + Longitude);

                   //승차 정류장 정보 저장
                   StartbusStop_Latitude = Latitude;
                   StartbusStop_Longitude = Longitude;
                   StartbusStop_ID = splitID;
                   StartbusStop_Name = stationName;

                   //정류장 5자리 id 가져오기
                   GetStationByStopID_Thread thread1 = new GetStationByStopID_Thread(handler, StartbusStop_ID, TAG);
                   thread1.start();
                   try {
                       thread1.join();
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }

                   //승차 정류장 id, 이름, 좌표 서버에 데이터 전송
                   //..
                   JsonObject startObject = new JsonObject();
                   startObject.addProperty("id",aroBusstopId);
                   startObject.addProperty("name",StartbusStop_Name);
                   //userobj.remove("start");
                   userobj.add("start",startObject);

                   BusStopArrival_Thread7 thread2 = new BusStopArrival_Thread7(handler, StartbusStop_ID);
                   thread2.start();
                   try {
                       thread2.join();
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }

                   //처음에 승차할 버스 이름
                   String inbusNameText = busNumList.get(0).toString();
                   inbusName.setText(inbusNameText);
                   busIn_Number = inbusNameText;

                   boolean busCheck = false;    //버스 운행하는지 체크

                   for(int i=0; i<routeNoList.size(); i++){
                       //처음에 승차할 버스의 정보 얻기
                       if(busNumList.get(0).toString().equals(routeNoList.get(i))){
                           busIn_carRegNo = carRegNoList.get(i);
                           busIn_routeCd = routeCdList.get(i);
                           busIn_statusPos = statusPosList.get(i);

                           Log.d("BusInInfo", "busIn_routeCd : " + busIn_routeCd + " busIn_carRegNo : " + busIn_carRegNo + "busIn_routeCd : " + busIn_routeCd);

                           //타입 정보 얻기
                           GetBusInInfo(busIn_routeCd, busIn_carRegNo);

                           //승차 버스 id 서버에 데이터 전송
                           //...
                           userobj.remove("bus");
                           userobj.addProperty("bus", busIn_carRegNo);

                           busCheck = true;
                       }

                       //---------------------------------------------------------------------
                       //승차할 버스 상태 ui 설정
                       if (busNumList.get(0).toString().equals(routeNoList.get(i))) {
                           switch (msgTpList.get(i)) {
                               case "01":
                                   //도착
                                   status_pos.setText("도착");
                                   tts.FuncVoiceOut(inbusName.getText() + "가 도착했습니다.\n승차 하시길 바랍니다.");
                                   Toast.makeText(MainActivity.this,inbusName.getText() + "가 도착했습니다.\n승차 하시길 바랍니다.",Toast.LENGTH_LONG).show();

                                   //이벤트 설정
                                   userobj.remove("event");
                                   userobj.addProperty("event", 104);   //104-버스도착
                                   //sc.sendUser(userobj); //current 값 안담기고 보내짐

                                   //내위치와 버스위치 비교
                                   //여기서 사용자가 승차했는지 감지
                                   BusInSense();
                                   break;
                               case "06":
                                   status_pos.setText("진입중");

                                   //승차할 버스 정보, 승차할 정류장 정보 음성 및 화면 안내
                                   tts.FuncVoiceOut("승차할 버스는 " + inbusName.getText() + " 이고 " + "진입중 입니다."
                                           + " 승차할 정거장 이름은 " + stationName + " 입니다.");
                                   Toast.makeText(MainActivity.this, "승차할 버스는 " + inbusName.getText() + " 이고 " + "진입중 입니다."
                                           + " 승차할 정거장 이름은 " + stationName + " 입니다.", Toast.LENGTH_LONG).show();

                                   //이벤트 설정
                                   userobj.remove("event");
                                   userobj.addProperty("event", 103);   //103-버스근접
                                   //sc.sendUser(userobj);  //current 값 안담기고 보내짐

                                   //내위치와 버스위치 비교
                                   //여기서 사용자가 승차했는지 감지
                                   //BusInSense();
                                   break;
                               case "07":
                                   status_pos.setText("차고지 운행 대기중");

                                   //승차할 버스 정보, 승차할 정류장 정보 음성 및 화면 안내
                                   tts.FuncVoiceOut("승차할 버스는 " + inbusName.getText() + " 이고 " + "차고지 운행 대기중 입니다."
                                           + " 승차할 정거장 이름은 " + stationName + " 입니다.");
                                   Toast.makeText(MainActivity.this, "승차할 버스는 " + inbusName.getText() + " 이고 " + "차고지 운행 대기중 입니다."
                                           + " 승차할 정거장 이름은 " + stationName + " 입니다.", Toast.LENGTH_LONG).show();

                                   break;
                               default:
                                   status_pos.setText(statusPosList.get(i) + " 정거장 남았습니다 (" + extimeMinList.get(i) + "분)");
                                   
                                   //승차할 버스 정보, 승차할 정류장 정보 음성 및 화면 안내
                                   tts.FuncVoiceOut("승차할 버스는 " + inbusName.getText() + " 이고 " + extimeMinList.get(i) + "분 후 도착예정입니다."
                                           + " 승차할 정거장 이름은 " + stationName + " 입니다.");
                                   Toast.makeText(MainActivity.this, "승차할 버스는 " + inbusName.getText() + " 이고 " + extimeMinList.get(i) + "분 후 도착예정입니다."
                                           + " 승차할 정거장 이름은 " + stationName + " 입니다.", Toast.LENGTH_LONG).show();
                                   break;
                           }

                           //01(도착) msg 얻기 어려워서
                           //승차할 버스 정거장에 승차할 버스가 도착했는지 화인
                           BusArrivalStartBusStop();
                       }
                       //-------------------------------------------------------------------------
                   }

                   //승차 가능한 다른 버스 리스트 ui 설정
                   if (busNumList.size() == 1) {
                       Log.d("ReceiveMessage", "대체가능한 버스가 없습니다.");

                       ArrayList<String> items = new ArrayList<>();
                       items.add("대체가능한 버스가 없습니다.");

                       final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                               getApplicationContext(),
                               R.layout.default_listview_textview,
                               items){
                           @NonNull
                           @Override
                           public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                               View view = super.getView(position, convertView, parent);
                               TextView tv = (TextView)view.findViewById(R.id.defaultList_TextView);

                               //글자 크기 설정
                               if(textRaise){
                                   tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                               } else {
                                   tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                               }

                               return view;
                           }
                       };
                       //ArrayAdapter<String> adapter = new ArrayAdapter<String>(cThis, R.layout.default_listview_textview, items);
                       search_result_layout4_list.setAdapter(adapter);

                   } else {
                       for(int i=0; i<routeNoList.size(); i++){
                           for (int j = 1; j < busNumList.size(); j++) {
                               if (routeNoList.get(i).equals(busNumList.get(j).toString())) {
                                   routeNoList_1.add(routeNoList.get(i));
                                   statusPosList_1.add(statusPosList.get(i));
                                   extimeMinList_1.add(extimeMinList.get(i));
                                   msgList_1.add(msgTpList.get(i));

                                   busStop_listViewAdapter = new BusStop_ListViewAdapter(cThis, routeNoList_1, statusPosList_1, extimeMinList_1, msgList_1, textRaise);
                                   search_result_layout4_list.setAdapter(busStop_listViewAdapter);

                                   Log.d("ReceiveMessage", "busNumList : " + busNumList.get(j).toString());
                               }
                           }
                       }
                   }

                   //버스가 없을경우
                   if(busCheck == false){
                       tts.FuncVoiceOut(busIn_Number + "번 버스가 운행하지 않습니다.\n안내를 종료합니다.");
                       Toast.makeText(MainActivity.this, busIn_Number + "번 버스가 운행하지 않습니다.\n안내를 종료합니다.", Toast.LENGTH_LONG).show();

                       //뒤로가기 호출
                       onBackPressed();



                       return;
                   }

                   //하차할 정류장 정보 정보 얻기
                   station_and_Check = true;
                   ODSayRequestBusStationInfo(stationID_and);

                   station_0_Check = false;

               }
               else if(station_and_Check){
                   JSONObject jsonObject = oDsayData.getJson();
                   Log.d("onResultCallbackListener2", jsonObject.toString());

                   JSONObject result = jsonObject.getJSONObject("result");
                   String Latitude = result.getString("y");     //경도
                   String Longitude = result.getString("x");    //위도
                   String stationName = result.getString("stationName");
                   String localStationID = result.getString("localStationID");

                   String[] localid = localStationID.split("DJB");
                   String splitID = localid[1];

                   Log.d("onResultCallbackListener2", "stationName:" + stationName + " localStationID:" + localStationID
                           + " splitID:" + splitID + " Latitude:" + Latitude + " Longitude:" + Longitude);

                   //하차 정류장 정보 저장
                   EndbusStop_Latitude = Latitude;
                   EndbusStop_Longitude = Longitude;
                   EndbusStop_ID = splitID;
                   EndbusStop_Name = stationName;

                   //정류장 5자리 id 가져오기
                   GetStationByStopID_Thread thread1 = new GetStationByStopID_Thread(handler, EndbusStop_ID, TAG);
                   thread1.start();
                   try {
                       thread1.join();
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }

                   //하차 정류장 id, 이름, 좌표 서버에 데이터 전송
                   //..
                   JsonObject endObject = new JsonObject();
                   endObject.addProperty("id",aroBusstopId);
                   endObject.addProperty("name",EndbusStop_Name);
                   //userobj.remove("end");
                   userobj.add("end",endObject);

                   //현재 정류장 서버에 데이터 전송(대기중)
                   JsonObject currentobj = new JsonObject();
                   currentobj.addProperty("id", "null");
                   currentobj.addProperty("name", "null");
                   userobj.remove("current");
                   userobj.add("current", currentobj);

                   //이벤트 설정
                   userobj.addProperty("event", 101);   //101-안내시작

                   busOut_splitID = splitID;

                   //소켓으로 서버에 user 전송
                   sc.sendUser(userobj);

                   //길찾기 api 사용 시
                   OnODSayRequestSearchPubTransPath = true;

                   station_and_Check = false;
               }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(int i, String errorMessage, API api) {
            Log.d("onResultCallbackListener2", "API : " + api.name() + "\n" + errorMessage);
        }
    };

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d(TAG, "onMapReady");
        //NaverMap 객체를 받기
        mNaverMap = naverMap;

        //NaverMap 객체에 현재 위치 소스 지정
        mNaverMap.setLocationSource(mLocationSource);

        //현재 위치 버튼 활성화
        mNaverMap.getUiSettings().setLocationButtonEnabled(true);

        //권한 요청, 결과는 onRequestPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        //mNaverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true);    //기본적으로 활성화
        mNaverMap.setLayerGroupEnabled(LAYER_GROUP_TRANSIT, true);

        //locationButtonView = findViewById(R.id.navermap_location_button);
        //locationButtonView.setMap(mNaverMap);
    }

    //누락된 권한을 요청하고 재정의하려면
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //request code와 권한 획득 여부 확인, PackageManager.PERMISSION_GRANTED : 권한이 있는 경우
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //권한을 허용했을 때
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

                Log.d(TAG, "ParmissionTrue");

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Log.d("fusedLocationClient", "Latitude " + location.getLatitude() + " Longitude " + location.getLongitude());
                            nowLatitude = location.getLatitude();  //36.3031689
                            nowLongitude = location.getLongitude();//127.447116
                        }
                    }
                });

                //사용자 위치 정보 갱신 시작
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
            else {
                //권한을 거부했을 때
                Log.d(TAG,"ParmissionFalse");
                finish();   //앱종료
            }
        }
    }


    //뒤로가기 버튼 클릭시
    @Override
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        //super.onBackPressed();

        //2초 이내에 뒤로가기 버튼을 재 클릭 시 앱 종료
        if (System.currentTimeMillis() - lastTimeBackPressed < 2000)
        {
            finish();
            return;
        }

        onMapReady(mNaverMap);

        //길찾기를 했다면 사용종료 메시지 서버에 전송
        if(OnODSayRequestSearchPubTransPath){
            userobj.remove("event");
            userobj.addProperty("event", 400);  //400- 사용종료
            sc.sendUser(userobj);
        }

        jibunAddr_listView.setVisibility(View.GONE); //리스트 숨기기
        recent_listView.setVisibility(View.GONE); //리스트 숨기기
        search_result_layout.setVisibility(View.GONE);
        odsayBi_tv.setVisibility(View.GONE);
        busin_layout6.setVisibility(View.GONE);
        search_result_layout4.setVisibility(View.GONE);

        menu_layout.setVisibility(View.VISIBLE);
        search_layout_back.setVisibility(View.VISIBLE);
        search_layout_back.setBackgroundColor(Color.argb(0,0,0,0)); //투명색 설정

        path.setMap(null);
        search_marker.setMap(null);
        start_marker.setMap(null);
        and_marker.setMap(null);
        mSearchView.setQuery("", false);

        //'뒤로' 버튼 한번 클릭 시 메시지
        Toast.makeText(this, "'뒤로' 버튼을 한번 더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
        //lastTimeBackPressed에 '뒤로'버튼이 눌린 시간을 기록
        lastTimeBackPressed = System.currentTimeMillis();

        OnODSayRequestSearchPubTransPath = false;

        Log.d(TAG, "onBackPressed()");

    }
    
    //어플 종료 함수
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts != null){
            tts.EndTTS();
        }
        if(stt != null){
            stt.EndSTT();
        }

        //길찾기 했을 시
        if(OnODSayRequestSearchPubTransPath){
            userobj.remove("event");
            userobj.addProperty("event", 400);  //400- 사용종료
            sc.sendUser(userobj);

            sc.CloseSocket();
        }

        Log.d(TAG, "onDestroy()");

        finish();
    }

    //화면에 안보이면 호출 다른 엑티비티로 이동해도 호출 됨
    @Override
    protected void onStop() {
        super.onStop();
        /*
        if(tts != null){
            tts.EndTTS();
        }
        if(stt != null){
            stt.EndSTT();
        }
        
        //길찾기 했을 시
        if(OnODSayRequestSearchPubTransPath){
            userobj.remove("event");
            userobj.addProperty("event", 400);  //400- 사용종료
            sc.sendUser(userobj);

            sc.CloseSocket();
        }
        
        finish();
         */
        Log.d(TAG, "onStop()");
    }

}