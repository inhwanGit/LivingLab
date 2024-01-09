package com.example.myapplicationtablet;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityResultLauncher<Intent> startActivityResult;
    private Context cThis;
    private MainHandler handler;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Double Longitude;
    private Double Latitude;
    private TTS tts;
    private Bus bus;
    //private ArrayList<Passenger> passengerList;
    private SocketConnect sc;

    private TextView routeTp_tv;
    private TextView routeNo_tv;
    private TextView routeCd_tv;
    private TextView busType_tv;
    private TextView busNum_tv;
    private ImageButton settings_btn;
    private LinearLayout main_layout;
    private ConstraintLayout settings_layout;
    private Button change_button;
    private Button cancel_button;
    private EditText routeCd_Edit;
    private EditText busNum_Edit;
    private ImageButton busNoSoundOn_btn;
    private TableLayout passenger_table_layout;
    private ImageView bus_imageView;

    //탑승객 리스트
    public static ArrayList<User> users;
    //버스정류장 좌표
    public static com.example.myapplicationtablet.Location busStoplocation;

    private class MainHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();

            if (bundle.getBoolean("GetRouteInfo_Thread")) {
                String error = bundle.getString("ERROR", null);
                if(error == null){
                    String routeNo = bundle.getString("ROUTE_NO");
                    String routeCd = bundle.getString("ROUTE_CD");
                    int routeTp = bundle.getInt("ROUTE_TP");

                    Log.d("GetRouteInfo_Thread", "ROUTE_NO : " + routeNo + " ROUTE_CD : " + routeCd + " ROUTE_TP : " + routeTp);

                    //객체에 데이터 저장
                    bus.SetBusRouteNo(routeNo);
                    bus.SetBusRouteCd(routeCd);
                    bus.SetBusRouteTp(routeTp);

                    GetBusRegInfoByRouteId_Thread thread = new GetBusRegInfoByRouteId_Thread(handler, routeCd);
                    thread.start();
                }
                else{
                    Log.d("GetRouteInfo_Thread", "ERROR : " + error);
                    Toast.makeText(MainActivity.this, error + " 다시 입력해주세요.", Toast.LENGTH_LONG).show();
                }
            }
            else if(bundle.getBoolean("GetBusRegInfoByRouteId_Thread")){
                ArrayList<Bus> buslist = (ArrayList<Bus>)bundle.getSerializable("BUS_LIST");

                //실제 운행되는 버스의 차 번호가 맞는지 확인
                for(int i=0; i<buslist.size(); i++){
                    //한글 키보드 안돼서 임시값 넣고 테스트
                    //String busNum = busNum_Edit.getText().toString();
                    String busNum = "대전75자9327";

                    if(busNum.equals(buslist.get(i).GetBusCarRegNo())){

                        Log.d("GetBusRegInfoByRouteId_Thread", "CAR_REG_NO : " + buslist.get(i).GetBusCarRegNo() + " BUS_TYPE : " + buslist.get(i).GetBusType());

                        //객체에 데이터 저장
                        bus.SetBusCarRegNo(buslist.get(i).GetBusCarRegNo());
                        bus.SetBusType(buslist.get(i).GetBusType());

                        //유지 데이터 변경
                        SharedPreferences sharedPreferences = getSharedPreferences("BusDriverInfo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("routeNo", bus.GetBusRouteNo());
                        editor.putString("routeCd", bus.GetBusRouteCd());
                        editor.putInt("routeTp", bus.GetBusRouteTp());
                        editor.putInt("busType", bus.GetBusType());
                        editor.putString("busNum", bus.GetBusCarRegNo());
                        editor.commit();

                        //ui 변경
                        SetBusInfoUI(bus.GetBusRouteNo(), bus.GetBusRouteCd(), bus.GetBusRouteTp(), bus.GetBusType(), bus.GetBusCarRegNo());

                        settings_btn.setImageResource(R.drawable.ic_settings_white);
                        settings_btn.setBackgroundResource(R.drawable.ic_circle_blue);

                        main_layout.setVisibility(View.VISIBLE);
                        settings_layout.setVisibility(View.GONE);
                    }
                }
            }
            else if(bundle.getBoolean("User")) {
                User user = (User) bundle.getSerializable("user");

                SharedPreferences sharedPreferences2 = getSharedPreferences("BusDriverInfo", MODE_PRIVATE);
                String busNum = sharedPreferences2.getString("busNum", null);

                //단말기에 설정된 차 번호와 전달받은 데이터의 차번호가 같다면
                if(user.GetBus().equals(busNum)){   //테스트 끝나고 적용
                }

                //이벤트 처리
                UserEvent(user);
            }
        }
    }

    public void Init(){
        cThis = this;
        handler = new MainHandler();
        sc = new SocketConnect(handler);
        users = new ArrayList<>();
        //passengerList = new ArrayList<Passenger>();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        tts = new TTS(cThis);

        routeTp_tv = (TextView) findViewById(R.id.routeTp_tv);
        routeNo_tv = (TextView) findViewById(R.id.routeNo_tv);
        routeCd_tv = (TextView) findViewById(R.id.routeCd_tv);
        busType_tv = (TextView) findViewById(R.id.busType_tv);
        busNum_tv = (TextView) findViewById(R.id.busNum_tv);
        settings_btn = (ImageButton) findViewById(R.id.settings_btn);
        main_layout = (LinearLayout) findViewById(R.id.main_layout);
        settings_layout = (ConstraintLayout) findViewById(R.id.settings_layout);
        change_button = (Button) findViewById(R.id.change_button);
        cancel_button = (Button) findViewById(R.id.cancel_button);
        routeCd_Edit = (EditText) findViewById(R.id.routeCd_Edit);
        busNum_Edit = (EditText) findViewById(R.id.busNum_Edit);
        busNoSoundOn_btn = (ImageButton) findViewById(R.id.busNoSoundOn_btn);
        passenger_table_layout = (TableLayout) findViewById(R.id.passenger_table_layout);
        bus_imageView = (ImageView) findViewById(R.id.bus_imageView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); //화면을 가로로 고정

        Init();

        startActivityResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Log.d(TAG, "MainActivity로 돌아왔다. ");

                            String activity = result.getData().getStringExtra("Activity");

                            if (activity.equals("OnBoardingActivity")) {
                                String routeCd = result.getData().getStringExtra("routeCd");
                                String routeNo = result.getData().getStringExtra("routeNo");
                                int routeTp = result.getData().getIntExtra("routeTp", 0);
                                String busNum = result.getData().getStringExtra("busNum");
                                int busType = result.getData().getIntExtra("busType", 0);

                                Log.d(TAG, "routeNo : " + routeNo + " routeCd : " + routeCd + " routeTp : " + routeTp + " busType : " + busType + " busNum : " + busNum);

                                //ui
                                SetBusInfoUI(routeNo, routeCd, routeTp, busType, busNum);

                                //버스 정보 저장
                                SharedPreferences sharedPreferences = getSharedPreferences("BusDriverInfo", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("routeNo", routeNo);
                                editor.putString("routeCd", routeCd);
                                editor.putInt("routeTp", routeTp);
                                editor.putInt("busType", busType);
                                editor.putString("busNum", busNum);
                                editor.commit();
                            }
                        }
                    }
                });

        FirstUserCheak(); //처음 사용자 체크하는 메소드

        //통신 시작
        sc.OpenSocket();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //location.getProvider(); 위치정보
                //location.getLongitude(); 경도
                //location.getLatitude(); 위도
                //location.getAltitude(); 고도

                Longitude = location.getLongitude();
                Latitude = location.getLatitude();

                Log.d(TAG, "Longitude : " + Longitude + " Latitude : " + Latitude);

                //버스가 사용자의 승차정류장에 도착했는지 감지
                if(users.size() > 0){
                    try {
                        //탑승문 열리길 기다림
                        Thread.sleep(1000);

                        for(int i=0; i<users.size(); i++){

                            Log.d(TAG, users.get(i).toString());

                            if(users.get(i).GetStart().GetId().equals("null") == false){
                                //Key인증실패: LIMITED NUMBER OF SERVICE REQUESTS EXCEEDS ERROR.[인증모듈 에러코드(22)] <- 트래픽 1000건 초과로인한 오류
                                //초과방지를 위해 클릭시 거리 측정
                                bus_imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        for(int i=0; i<users.size(); i++){
                                            //arsId로 버스 정류장 정보 검색
                                            GetStationByUid_Thread thread = new GetStationByUid_Thread(handler, users.get(i).GetStart().GetId());
                                            thread.start();
                                            try {
                                                thread.join();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }

                                            //버스와 장애인이 승차하는 정류장 위치 비교
                                            LocationDistance locationdistance = new LocationDistance();
                                            Double distance = locationdistance.distance(Latitude, Longitude, busStoplocation.GetLat(), busStoplocation.GetLong(), "meter");

                                            Log.d(TAG, "distance/meter : " + distance.toString());

                                            //거리가 20m 이하라면
                                            if(distance.intValue() <= 20){
                                                //버스가 장애인이 승차하는 정류장에 도착했다고 감지
                                                ArrivalStartBusStop(users.get(i));
                                            }
                                        }
                                    }
                                });
                            }

                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            public void onProviderEnabled(String provider) { }
            public void onProviderDisabled(String provider) { }
        };

        //권한 체크
        if(ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            //권한이 없는 경우 권한 요청
            ActivityCompat.requestPermissions( MainActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 0 );

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //1. 사용자가 승인 거절을 누른경우
                //앱 종료
                finish();
            } else {
                //2. 사용자가 승인 거절과 동시에 다시 표시하지 않기 옵션을 선택한 경우
                //3. 혹은 아직 승인요청을 한적이 없는 경우
            }
        }
        else{
            //권한이 승인된 상태
            //현재 위치 가져오기
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }

        //설정 클릭시
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ui
                settings_btn.setImageResource(R.drawable.ic_settings_blue);
                settings_btn.setBackgroundResource(R.drawable.ic_circle_white);

                main_layout.setVisibility(View.GONE);
                settings_layout.setVisibility(View.VISIBLE);
            }
        });

        //설정-수정 클릭시
        change_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //버스 번호(ex : 101,102,103 ...)를 가져오기 위한 호출
                //GetRouteInfo_Thread thread = new GetRouteInfo_Thread(handler, routeCd_Edit.getText().toString());
                //thread.start();

                //버스 객체 생성
                bus = new Bus();

                //버스 번호(ex : 101,102,103 ...)를 가져오기 위한 호출
                //한글 키보드 안돼서 임시값 넣고 테스트
                GetRouteInfo_Thread thread = new GetRouteInfo_Thread(handler, "30300002");
                thread.start();
            }
        });

        //설정-취소 클릭시
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ui
                settings_btn.setImageResource(R.drawable.ic_settings_white);
                settings_btn.setBackgroundResource(R.drawable.ic_circle_blue);

                main_layout.setVisibility(View.VISIBLE);
                settings_layout.setVisibility(View.GONE);
            }
        });

        //버스 안내 음성 출력 버튼 클릭시
        busNoSoundOn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tts.FuncVoiceOut(routeNo_tv.getText().toString() + " 버스가 도착했습니다.");  //버스 음성 안내
                Toast.makeText(MainActivity.this, routeNo_tv.getText().toString() + " 버스가 도착했습니다.", Toast.LENGTH_LONG).show();

                //-----test--------
                //테이블 삭제
                //DeletePassengerTableRow(users.get(0).GetId());

                //하차 요청
                //BusOutRequest(users.get(0));

                //ArrivalEndBusStop(users.get(0));
                //String text = 1 + "번 승객의 하차정류장에 도착하였습니다.\n" + 1 + "번 승객이 하차하는지 확인해주세요.";
                //Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
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

            //앱 최초 실행 시 하고싶은 작업
            Intent intent = new Intent(getApplicationContext(), OnBoardingActivity.class);
            startActivityResult.launch(intent); //다음화면으로 넘어감

        } else { //최초 실행이 아닌 경우
            Log.d(TAG, "Is first Time? not first");

            SharedPreferences sharedPreferences2 = getSharedPreferences("BusDriverInfo", MODE_PRIVATE);
            String routeNo = sharedPreferences2.getString("routeNo", null);
            String routeCd = sharedPreferences2.getString("routeCd", null);
            int routeTp = sharedPreferences2.getInt("routeTp", 0);
            String busNum = sharedPreferences2.getString("busNum", null);
            int busType = sharedPreferences2.getInt("busType", 0);

            Log.d(TAG, "routeNo : " + routeNo + " routeCd : " + routeCd + " routeTp : " + routeTp + " busType : " + busType + " busNum : " + busNum);

            //ui
            SetBusInfoUI(routeNo, routeCd, routeTp, busType, busNum);
        }
    }

    public void SetBusInfoUI(String routeNo, String routeCd, int routeTp, int busType, String busNum){
        String routeTpText="";
        switch (routeTp){
            case 1: routeTpText = "급행"; break;
            case 2: routeTpText = "간선"; break;
            case 3: routeTpText = "지선"; break;
            case 4: routeTpText = "외곽"; break;
            case 5: routeTpText = "마을"; break;
            case 6: routeTpText = "첨단"; break;
        }
        routeTp_tv.setText(routeTpText);
        routeNo_tv.setText(routeNo + "번");
        routeCd_tv.setText("(" + routeCd + ")");
        if(busType == 1){
            busType_tv.setText("일반");
        } else if(busType == 2){
            busType_tv.setText("저상");
        } else {
            busType_tv.setText("정보없음");
        }
        busNum_tv.setText(busNum);
    }

    @SuppressLint("ResourceType")   //이유 모르겠음
    public void AddPassengerTableRow(User user){
        //테이블 생성
        TableRow tableRow = new TableRow(cThis);
        tableRow.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tableRow.setBackgroundColor(Color.parseColor("#cccccc"));
        tableRow.setId(user.GetNum());    //순번 설정
        tableRow.setTag(user.GetId());    //id 설정

        //내용 생성
        Resources r = cThis.getResources();
        for(int i=0; i<7;i++){
            //*중요* 자식의 view를 설정하기 위해선 상위 레이아웃을 생성하고 적용해야됨
            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT);
            switch (i){
                case 0: layoutParams.weight = 1; break;
                case 1: layoutParams.weight = 2; break;
                case 2: layoutParams.weight = 4; break;
                case 3: layoutParams.weight = 4; break;
                case 4: layoutParams.weight = 1; break;
                case 5: layoutParams.weight = 1; break;
                case 6: layoutParams.weight = 1; break;
            }
            layoutParams.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, r.getDisplayMetrics());
            layoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, r.getDisplayMetrics());
            if(i==6){
                layoutParams.rightMargin = 0;
            }
            TextView textView = new TextView(cThis);
            textView.setLayoutParams(layoutParams);
            textView.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()));
            textView.setBackgroundColor(Color.parseColor("#ffffff"));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            switch (i){
                case 0:
                    textView.setText(String.valueOf(user.GetNum()));    //순번
                    textView.setId(10); break;
                case 1:
                    textView.setId(11);
                    if(user.GetType() == 1){                            //장애인유형
                        textView.setText("시각장애인");
                    } else if(user.GetType() == 2){
                        textView.setText("지체장애인");
                    } else if(user.GetType() == 3){
                        textView.setText("청각장애인");
                    }
                    break;
                case 2:
                    textView.setText(user.GetStart().GetName());
                    textView.setId(12); break;                           //승차정류장
                case 3:
                    textView.setText(user.GetEnd().GetName());
                    textView.setId(13); break;                           //하차정류장
                case 4:
                    textView.setText("");
                    textView.setId(14); break;                           //승차여부
                case 5:
                    textView.setText("");
                    textView.setId(15); break;                           //하차여부
                case 6:
                    textView.setText("");
                    textView.setId(16); break;                           //하차요청 default
            }

            tableRow.addView(textView); //추가
        }

        passenger_table_layout.addView(tableRow); //추가
    }

    public void DeletePassengerTableRow(User user){
        for(int i=0; i<users.size(); i++){
            //아이디로 찾기
            //수정 필요(나중에 주석풀기)
            /*
            if(user.GetId().equals(users.get(i).GetId())){
                TableRow tableRow = (TableRow) passenger_table_layout.findViewWithTag(user.GetId());
                tableRow.removeAllViews();
                passenger_table_layout.removeView(tableRow);
            }
             */
            //순번으로 찾기 (테스트를 위해)
            if(user.GetNum() == users.get(i).GetNum()){
                Log.d(TAG, "DeletePassengerTableRow - find");
                TableRow tableRow = (TableRow) passenger_table_layout.findViewById(user.GetNum());
                tableRow.removeAllViews();
                passenger_table_layout.removeView(tableRow);
            }
        }

    }

    public void BusOutRequest(User user){
        for(int i=0; i<users.size(); i++){
            //아이디로 찾기 
            //수정 필요(나중에 주석풀기)
            /*
            if(user.GetId().equals(users.get(i).GetId())){
                TableRow tableRow = (TableRow) passenger_table_layout.findViewWithTag(user.GetId());
                for(int j=0; j<7; j++){
                    TextView textView = (TextView) tableRow.findViewById(j);
                    textView.setBackgroundColor(Color.parseColor("#E53935"));
                    textView.setTextColor(Color.parseColor("#ffffff"));
                    if(j == 6){
                        textView.setText("하차");
                    }
                }
            }
             */
            //순번으로 찾기 (테스트를 위해)
            if(user.GetNum() == users.get(i).GetNum()){
                TableRow tableRow = (TableRow) passenger_table_layout.findViewById(user.GetNum());

                for(int j=10; j<17; j++){
                    int tv_id = j;
                    TextView textView = (TextView) tableRow.findViewById(tv_id);
                    textView.setBackgroundColor(Color.parseColor("#E53935"));
                    textView.setTextColor(Color.parseColor("#ffffff"));
                    if(j == 16){
                        textView.setText("하차");
                    }
                }

            }
        }
    }

    public void BusOutRequestCancel(User user){
        for(int i=0; i<users.size(); i++){
            //아이디로 찾기
            //수정 필요(나중에 주석풀기)
            /*
            if(user.GetId().equals(users.get(i).GetId())){
                TableRow tableRow = (TableRow) passenger_table_layout.findViewWithTag(user.GetId());
                for(int j=0; j<7; j++){
                    TextView textView = (TextView) tableRow.findViewById(j);
                    textView.setBackgroundColor(Color.parseColor("#ffffff"));
                    textView.setTextColor(Color.parseColor("#777777"));
                    if(j == 6){
                        textView.setText("");
                    }
                }
            }
             */
            //순번으로 찾기 (테스트를 위해)
            if(user.GetNum() == users.get(i).GetNum()){
                TableRow tableRow = (TableRow) passenger_table_layout.findViewById(user.GetNum());
                for(int j=10; j<17; j++){
                    int tv_id = j;
                    TextView textView = (TextView) tableRow.findViewById(tv_id);
                    textView.setBackgroundColor(Color.parseColor("#ffffff"));
                    textView.setTextColor(Color.parseColor("#777777"));
                    if(j == 16){
                        textView.setText("");
                    }
                }
            }
        }
    }

    public void ArrivalStartBusStop(User user){
        tts.FuncVoiceOut(routeNo_tv.getText().toString() + " 버스가 도착했습니다.");  //버스 음성 안내
        int usertype = user.GetType();
        String usertypetext="";
        if(usertype == 1){
            usertypetext = "시각장애인";
            Toast.makeText(MainActivity.this, user.GetNum() + "번 " + usertypetext +
                    "이 탑승합니다.\n안내견을 확인하시고 동반 탑승을 인도해주시기 바랍니다.", Toast.LENGTH_LONG).show();   //기사 확인

        }else if(usertype == 2){
            usertypetext = "지체장애인";
            Toast.makeText(MainActivity.this, user.GetNum() + "번 " + usertypetext +
                    "이 탑승합니다.\n휠체어 여부를 확인하시고 안전한 탑승을 인도해주시기 바랍니다.", Toast.LENGTH_LONG).show();   //기사 확인
        }else{
            usertypetext = "청각장애인";
            Toast.makeText(MainActivity.this, user.GetNum() + "번 " + usertypetext +
                    "이 탑승합니다.\n의사전달이 필요할 경우 글자를 작성하여 전달하시기 바랍니다.", Toast.LENGTH_LONG).show();   //기사 확인

        }

    }

    public void ArrivalEndBusStop(User user){
        //알림음 설정 - 테스트 필요
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), uri);
        ringtone.play();

        //텍스트 출력
        String text = user.GetNum() + "번 승객의 하차정류장에 도착하였습니다.\n" + user.GetNum() + "번 승객이 하차하는지 확인해주세요.";
        Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
    }

    public void AddUserList(User user){
        user.SetNum(users.size() + 1);    //순번 저장 1부터
        users.add(user);
    }

    public void ChangeUserListData(User user){
        for (int j = 0; j < users.size(); j++) {
            if (user.GetId().equals(users.get(j).GetId())) {
                //다시 저장
                user.SetNum(users.get(j).GetNum());
                users.set(j, user);
            }
        }
    }

    public void DeleteUserList(User user){
        for (int j = 0; j < users.size(); j++) {
            if (users.get(j).GetId().equals(user.GetId())) {
                //삭제
                users.remove(j);
            }
        }
    }

    public void PassengerBusIn_OK(User user){
        //순번으로 찾기 (테스트를 위해)
        for (int i = 0; i < users.size(); i++) {
            if(user.GetNum() == users.get(i).GetNum()){
                TableRow tableRow = (TableRow) passenger_table_layout.findViewById(user.GetNum());
                int textview_id = 14;
                TextView textView = (TextView) tableRow.findViewById(textview_id);
                textView.setText("O");
            }
        }
    }

    public void PassengerBusIn_NO(User user){
        //순번으로 찾기 (테스트를 위해)
        for (int i = 0; i < users.size(); i++) {
            if(user.GetNum() == users.get(i).GetNum()){
                TableRow tableRow = (TableRow) passenger_table_layout.findViewById(user.GetNum());
                int textview_id = 14;
                TextView textView = (TextView) tableRow.findViewById(textview_id);
                textView.setText("X");
            }
        }
    }

    public void PassengerBusOut_OK(User user){
        //순번으로 찾기 (테스트를 위해)
        for (int i = 0; i < users.size(); i++) {
            if(user.GetNum() == users.get(i).GetNum()){
                TableRow tableRow = (TableRow) passenger_table_layout.findViewById(user.GetNum());
                int textview_id = 15;
                TextView textView = (TextView) tableRow.findViewById(textview_id);
                textView.setText("O");
            }
        }
    }

    public void PassengerBusOut_NO(User user){
        //순번으로 찾기 (테스트를 위해)
        for (int i = 0; i < users.size(); i++) {
            if(user.GetNum() == users.get(i).GetNum()){
                TableRow tableRow = (TableRow) passenger_table_layout.findViewById(user.GetNum());
                int textview_id = 15;
                TextView textView = (TextView) tableRow.findViewById(textview_id);
                textView.setText("X");
            }
        }
    }

    public void UserEvent(User user){

        //이벤트 처리
        switch (user.GetEvent()){
            case 101 : //안내시작
                //리스트 저장
                AddUserList(user);
                //승차할 장애인 테이블 추가
                AddPassengerTableRow(user);
                break;
            case 102 : //버스 한정류장 가까워짐
                //리스트 수정
                ChangeUserListData(user);
                break;
            case 103 : //버스 근접
                //리스트 수정
                ChangeUserListData(user);
                //버스가 승차정류장 도착 test
                //ArrivalStartBusStop(user);
                break;
            case 104 : //버스 도착
                //리스트 수정
                ChangeUserListData(user);
                //버스가 승차정류장 도착
                ArrivalStartBusStop(user);
                break;
            case 199 : //버스 도착했는데 탑승안함
                //ui 수정
                PassengerBusIn_NO(user);
                //리스트 삭제
                DeleteUserList(user);
                break;
            case 201 : //탑승
                //리스트 수정
                ChangeUserListData(user);
                //ui 수정
                PassengerBusIn_OK(user);
                break;
            case 202 : //한 정류장 지나감
                //리스트 수정
                ChangeUserListData(user);
                break;
            case 203 : //내릴곳 거의 도착
                //리스트 수정
                ChangeUserListData(user);
                //하차 요청 ui
                BusOutRequest(user);
                break;
            case 204 : //내릴곳 도착
                //리스트 수정
                ChangeUserListData(user);
                //버스가 하차정류장 도착
                ArrivalEndBusStop(user);
                break;
            case 299 : //내릴곳 도착했는데 안내림
                //리스트 수정
                ChangeUserListData(user);
                //ui 설정
                PassengerBusOut_NO(user);
                break;
            case 301 : //중간에 내리기 요청
                //리스트 수정
                ChangeUserListData(user);
                //하차 요청 ui
                BusOutRequest(user);
                break;
            case 399 : //중간에 내리기 요청 취소
                //리스트 수정
                ChangeUserListData(user);
                //하차 요청 취소 ui
                BusOutRequestCancel(user);
                break;
            case 400 : //사용 종료
                //데이터 갱신
                ChangeUserListData(user);
                //테이블에서 삭제 ui
                DeletePassengerTableRow(user);
                //리스트 삭제
                DeleteUserList(user);
                break;
        }
    }

    //어플 종료 함수
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.EndTTS();
        }

        Log.d(TAG, "onDestroy()");
        finish();
    }
}