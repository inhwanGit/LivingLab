package com.example.myapplication_naver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonObject;
import com.naver.maps.map.LocationTrackingMode;
import com.odsay.odsayandroidsdk.API;
import com.odsay.odsayandroidsdk.ODsayData;
import com.odsay.odsayandroidsdk.ODsayService;
import com.odsay.odsayandroidsdk.OnResultCallbackListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class BusStop_Activity extends AppCompatActivity {

    private ODsayService odsayService; //SDK
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String TAG = "BusStopActivity";
    private BusStopHandler handler;
    private BusStopArrival_Thread5 busStopArrival_thread;
    private String BusStopLatitude;
    private String BusStopLongitude;
    private Double Latitude;
    private Double Longitude;
    private Context cThis;
    private TTS tts;
    private String busStopNm;
    private String arsID;       //버스정류장 고유 번호
    private boolean textRaise;
    private boolean Vibrator;
    private FusedLocationProviderClient fusedLocationClient; //마지막 위치 가져오기
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private Double nowLatitude;
    private Double nowLongitude;

    private ListView busnum_list;
    private TextView busstopNm;
    private TextView nowbosstop;
    private ImageButton busStop_refresh_btn;

    public static Double busStop_gpsLati;
    public static Double busStop_gpsLong;

    private class BusStopHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();

            if(bundle.getBoolean("BusStopArrival_Thread")){
                ArrayList<String> routeNoList = new ArrayList<>(bundle.getStringArrayList("ROUTE_NO"));     //버스 번호
                ArrayList<String> statusPosList = new ArrayList<>(bundle.getStringArrayList("STATUS_POS")); //남은 정류장 수
                ArrayList<String> extimeMinList = new ArrayList<>(bundle.getStringArrayList("EXTIME_MIN")); //도착 예상 시간(분)
                ArrayList<String> msgList = new ArrayList<>(bundle.getStringArrayList("MSG_TP"));
                ArrayList<String> busNodeIdList = new ArrayList<>(bundle.getStringArrayList("BUS_NODE_ID"));

                ArrayList<Integer> extimeMinList_int_calc = new ArrayList<Integer>();    //형변환 배열
                ArrayList<String> routeNoList_calc = new ArrayList<>();

                for(int i=0; i<routeNoList.size(); i++){
                    Log.d("ReceiveMessage",routeNoList.get(i));
                    routeNoList_calc.add(routeNoList.get(i));
                }

                for(int i=0; i<statusPosList.size(); i++){
                    Log.d("ReceiveMessage",statusPosList.get(i));
                }

                for(int i=0; i<extimeMinList.size(); i++){
                    Log.d("ReceiveMessage",extimeMinList.get(i));
                    extimeMinList_int_calc.add(Integer.parseInt(extimeMinList.get(i)));
                }

                for(int i=0; i<msgList.size(); i++){
                    //만약 버스가 운행대기중이라면 제외
                    if(msgList.get(i).equals("07")){
                        extimeMinList_int_calc.remove(i);
                        routeNoList_calc.remove(i);
                    }
                }

                int max = extimeMinList_int_calc.get(0); //최대값
                int min = extimeMinList_int_calc.get(0); //최소값
                int position = 0;

                for(int i=0; i<extimeMinList_int_calc.size(); i++) {
                    if(max < extimeMinList_int_calc.get(i)) {
                        max = extimeMinList_int_calc.get(i);
                    }
                    if(min > extimeMinList_int_calc.get(i)) {
                        min = extimeMinList_int_calc.get(i);
                        position = i;
                    }
                }

                //제일 빨리오는 버스 읽어주기
                tts.TextToSpeech("가장 빨리오는 버스는 "+ routeNoList_calc.get(position) + "번 버스 이고" + min + "분 후 도착예정입니다.");

                BusStop_ListViewAdapter adapter = new BusStop_ListViewAdapter(cThis, routeNoList, statusPosList, extimeMinList, msgList, textRaise);
                busnum_list.setAdapter(adapter);

                busnum_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        //진동 설정
                        if(Vibrator){
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                        }

                        //String selected_item = (String) adapterView.getItemAtPosition(position);
                        String busname = routeNoList.get(position);
                        String busstopnum = statusPosList.get(position);
                        String min = extimeMinList.get(position);
                        tts.TextToSpeech(busname + "번 버스가" + busstopnum + "정거장 남았고" + min + "분 후 도착예정입니다.");
                    }
                });


                //정거장의 위치값 가져오기
                GetStationByStopID_Thread thread = new GetStationByStopID_Thread(handler, busNodeIdList.get(0), TAG);
                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void Init(){
        cThis = this;
        handler = new BusStopHandler();
        tts = new TTS(cThis);

        odsayService = ODsayService.init(BusStop_Activity.this, getString(R.string.odsay_key));
        odsayService.setReadTimeout(5000);
        odsayService.setConnectionTimeout(5000);

        busnum_list = (ListView) findViewById(R.id.busnum_list);
        busstopNm = (TextView) findViewById(R.id.busstopNm);
        nowbosstop = (TextView) findViewById(R.id.nowbosstop);
        busStop_refresh_btn = (ImageButton) findViewById(R.id.busStop_refresh_btn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop);

        Init();

        //우송대학교서캠퍼스 정류장 좌표
        //36.337547;
        //127.44774;

        //main activity에서 마지막 위치 좌표 데이터를 받음
        Intent receive_intent = getIntent();
        Latitude = receive_intent.getDoubleExtra("BUSSTOP_CLICK_LAT", 0.0);
        Longitude = receive_intent.getDoubleExtra("BUSSTOP_CLICK_LONG", 0.0);
        textRaise = receive_intent.getBooleanExtra("TEXT_RAISE", false);
        Vibrator = receive_intent.getBooleanExtra("VIBRATOR", false);

        Log.d("ReceiveMainActivity","Latitude " + Latitude + " Longitude " + Longitude + " textRaise " + textRaise + " Vibrator " + Vibrator);

        //글자 크기
        if(textRaise){
            nowbosstop.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
            busstopNm.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 31);

        } else{
            nowbosstop.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            busstopNm.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28);
        }

        //내위치에서 100미터 반경내의 버스정류장 찾기
        //odsayService.requestPointSearch("127.44774", "36.337547", "100", "1", onResultCallbackListener);
        odsayService.requestPointSearch(Double.toString(Longitude), Double.toString(Latitude), "100", "1", onResultCallbackListener);

        //버스 도착정보 갱신
        busStop_refresh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                if(arsID != null){
                    tts.FuncVoiceOut("도착정보 새로고침");

                    //정류장의 버스 도착정보 가져오기
                    busStopArrival_thread = new BusStopArrival_Thread5(handler, arsID);
                    busStopArrival_thread.start();
                }
            }
        });



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
                    Log.d(TAG, "update/Latitude " + location.getLatitude() + " Longitude " + location.getLongitude());
                    nowLatitude = location.getLatitude();
                    nowLongitude = location.getLongitude();

                    //정거장 위치 값이 있다면
                    if(busStop_gpsLati != null && busStop_gpsLong != null){

                        //사용자 위치와 승차 버스 정류장 위치 거리 미터 단위로 계산
                        LocationDistance locationdistance = new LocationDistance();
                        Double distance = locationdistance.distance(nowLatitude, nowLongitude, busStop_gpsLati, busStop_gpsLong, "meter");

                        Log.d(TAG, "distance/meter : " + distance.toString());

                        //실시간 알림
                        tts.FuncVoiceOut("정거장 위치가 " + distance.intValue() + "m 반경 내에 있습니다.");
                        Toast.makeText(BusStop_Activity.this, "정거장 위치가 " + distance.intValue() + "m 반경 내에 있습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000);

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
                    Log.d(TAG, "Latitude " + location.getLatitude() + " Longitude " + location.getLongitude());
                    nowLatitude = location.getLatitude();  //36.3031689
                    nowLongitude = location.getLongitude();//127.447116
                }
            }
        });

        //사용자 위치 정보 갱신 시작
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private OnResultCallbackListener onResultCallbackListener = new OnResultCallbackListener(){
        @Override
        public void onSuccess(ODsayData oDsayData, API api) {
            JSONObject jsonObject = oDsayData.getJson();
            Log.d("onResultCallbackListener", jsonObject.toString());

            try{
                JSONObject result = jsonObject.getJSONObject("result");
                JSONArray stationArray = result.getJSONArray("station");

                //값이 없으면
                if(stationArray.length() == 0){
                    Log.d("onResultCallbackListener", "100m 반경내에 정류장이 없습니다.");
                    busstopNm.setText("정류장 없음");
                    tts.TextToSpeech("100m 반경내에 정류장이 없습니다.");
                    Toast.makeText(BusStop_Activity.this, "100m 반경내에 정류장이 없습니다.", Toast.LENGTH_LONG).show();
                }
                else {
                    for(int i=0;i<stationArray.length();i++){
                        JSONObject obj = stationArray.getJSONObject(i);
                        busStopNm = obj.getString("stationName");
                        arsID = obj.getString("arsID");
                        BusStopLatitude = obj.getString("x");
                        BusStopLongitude = obj.getString("y");


                        String arsIDSplit[] = arsID.split("-");
                        arsID = arsIDSplit[0] + arsIDSplit[1];

                        Log.d("onResultCallbackListener","busStopNm-" + busStopNm + " arsID-" + arsID);

                        tts.TextToSpeech("100m 반경내에 " + busStopNm + "정류장이 있습니다.");
                        Toast.makeText(BusStop_Activity.this, "100m 반경내에 " + busStopNm + "정류장이 있습니다.", Toast.LENGTH_LONG).show();

                        busstopNm.setText(busStopNm);

                        //key 오류 때문에 아래로 못함;;;
                        /*
                        //정거장의 위치값 가져오기
                        GetStationByUid_Thread thread = new GetStationByUid_Thread(handler, arsID);
                        thread.start();
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        */

                        //정류장의 버스 도착정보 가져오기
                        busStopArrival_thread = new BusStopArrival_Thread5(handler, arsID);
                        busStopArrival_thread.start();
                    }
                }

            } catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onError(int i, String errorMessage, API api) {
            Log.d("onResultCallbackListener", "API : " + api.name() + "\n" + errorMessage);

            if(errorMessage.equals("Json object undefined")){

            }
        }
    };


    @Override
    public void onBackPressed(){
        busStop_gpsLati = null;
        busStop_gpsLong = null;
        finish();
    }
}