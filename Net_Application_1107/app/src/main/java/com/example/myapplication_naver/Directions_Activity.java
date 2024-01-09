package com.example.myapplication_naver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class Directions_Activity extends AppCompatActivity {
    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;

    //-------------STT-------------
    private Context cThis;//context 설정
    //음성인식용
    private STT stt;

    //음성 출력용
    private TTS tts;

    //화면처리용
    private ImageButton btn_arr_sttStart;
    private ImageButton btn_dep_sttStart;
    private TextView textView;

    //--------- 현재위치 좌표 ---------
    private Double Latitude;
    private Double Longitude;

    //--------------------------------
    private DirectionsHandler handler;
    private Intent send_intent;
    private LinearLayout layout1;
    private LinearLayout layout2;
    private EditText departure_edt;
    private EditText arrival_edt;
    private String speak;
    private ListView recent_listView;
    private ListView arr_jibunAddr_listView;
    private ListView dep_jibunAddr_listView;
    private Boolean test = false;
    private Boolean test2 = false;
    private String send_arrKeyword;
    private String send_depKeyword;
    private TextView start_tv;
    private TextView end_tv;

    private Directions_ListViewAdapter adapter;
    private SharedPrefManager_RecentRoute mSharedPrefs;
    private SharedPreferences rh;
    private ArrayList<String> save_arrList = new ArrayList<>();
    private ArrayList<String> save_depList = new ArrayList<>();
    private final ArrayList<String> items = new ArrayList<>();
    private boolean textRaise;
    private boolean Vibrator;

    private class DirectionsHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();

            if(bundle.getBoolean("ReverseGeoPointerThread")){

                try{
                    ArrayList<String> areas = new ArrayList<>();
                    areas.addAll(bundle.getStringArrayList("areas"));
                    ArrayList<String> numbers = new ArrayList<>();
                    numbers.addAll(bundle.getStringArrayList("numbers"));

                    for(int i=0; i<areas.size(); i++){
                        Log.d("ReceiveMessage",areas.get(i));
                        departure_edt.append(areas.get(i));
                        departure_edt.append(" ");
                    }

                    for(int i=0; i<numbers.size(); i++){
                        Log.d("ReceiveMessage",numbers.get(i));
                        departure_edt.append(numbers.get(i));
                        departure_edt.append(" ");
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            else if(bundle.getBoolean("AddAPIThread")) {
                //----- 건물 이름(keyword)----------
                ArrayList<String> bdNmArraylist = new ArrayList<String>(bundle.getStringArrayList("BDNM_ADDR_LIST"));

                for(int i=0; i<bdNmArraylist.size(); i++){
                    Log.d("ReceiveMessage",bdNmArraylist.get(i));
                }

                //---- 지번 주소 ----

                ArrayList<String> jibun_arrayList = new ArrayList<String>(bundle.getStringArrayList("JIBUN_ADDR_LIST"));

                for(int i=0; i<jibun_arrayList.size(); i++){
                    Log.d("ReceiveMessage",jibun_arrayList.get(i));
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

                if(test){
                    arr_jibunAddr_listView.setAdapter(jibun_arrayAdapter);

                    arr_jibunAddr_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //진동 설정
                            if(Vibrator){
                                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                            }

                            //Toast.makeText(cThis, position + " 번째 선택! ", Toast.LENGTH_LONG).show(); //확인 완료
                            Log.d("jibunAddr_listView_CLICK", jibun_arrayAdapter.getItem(position));    //확인 완료

                            arrival_edt.setText(jibun_arrayAdapter.getItem(position));
                            send_arrKeyword = bdNmArraylist.get(position);

                            recent_listView.setVisibility(View.GONE); //죄근 검색 리스트 숨기기
                            arr_jibunAddr_listView.setVisibility(View.GONE);//주소 검색 리스트 보이기

                            //메인으로 데이터 전송 및 메인 activity Start
                            send_intent.putExtra("Activity", "Directions");
                            send_intent.putExtra("arr_addr", jibun_arrayAdapter.getItem(position));
                            send_intent.putExtra("arr_keyw", send_arrKeyword);
                            //보낼 출발 주소 데이터 저장
                            send_intent.putExtra("dep_addr", departure_edt.getText().toString());

                            //---------------최근 검색 기록 저장--------------------------------
                            SharedPrefManager_StarSave mSharedPrefs = SharedPrefManager_StarSave.getInstance(cThis);
                            SharedPreferences rh = getSharedPreferences("Search_History", MODE_PRIVATE);
                            String null_text = "";
                            String start = departure_edt.getText().toString().trim();
                            String end = arrival_edt.getText().toString().trim();
                            String route = start + "→" + end;
                            int count = rh.getInt("count_route", 0);

                            if (!start.equals("") && !end.equals("")) {
                                //리스트에 저장되어있는 값이 아니라면
                                if(mSharedPrefs.findRoute(route) == false){
                                    //SharedPreferences 저장하기
                                    int reCount = mSharedPrefs.addRoute(count, route);
                                    mSharedPrefs.addCount_rou(reCount + 1);

                                    items.add(0, route);
                                } else {
                                    //리스트 순서 수정
                                    //지우고
                                    mSharedPrefs.DelRoute(count, route);

                                    for(int i=0;i<items.size();i++){
                                        if(route.equals(items.get(i))){
                                            items.remove(i);
                                        }
                                    }

                                    //다시저장
                                    int reCount = mSharedPrefs.addRoute(count, route);
                                    mSharedPrefs.addCount_rou(reCount + 1);

                                    items.add(0, route);
                                }

                            }
                            //---------------최근 검색 기록 저장--------------------------------
                            arrival_edt.clearFocus();

                            //메인 엑티비티로 데이터 전달
                            setResult(Activity.RESULT_OK, send_intent);
                            finish();
                        }
                    });
                    test = false;
                } else if(test2){
                    dep_jibunAddr_listView.setAdapter(jibun_arrayAdapter);

                    dep_jibunAddr_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //진동 설정
                            if(Vibrator){
                                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                            }

                            //Toast.makeText(cThis, position + " 번째 선택! ", Toast.LENGTH_LONG).show(); //확인 완료
                            Log.d("jibunAddr_listView_CLICK", jibun_arrayAdapter.getItem(position));    //확인 완료

                            departure_edt.setText(jibun_arrayAdapter.getItem(position));

                            recent_listView.setVisibility(View.GONE); //죄근 검색 리스트 숨기기
                            dep_jibunAddr_listView.setVisibility(View.GONE);//주소 검색 리스트 보이기
                            layout2.setVisibility(View.VISIBLE);

                            //메인으로 보낼 데이터 저장
                            //send_intent.putExtra("Directions_Activity_Send_dep_addr", jibun_arrayAdapter.getItem(position));

                            departure_edt.clearFocus();
                        }
                    });
                    test2 = false;
                }
            }
        }
    }

    public void Init(){
        cThis = this;
        handler = new DirectionsHandler();
        //send_intent = new Intent(getApplicationContext(), MainActivity.class);
        send_intent = new Intent();
        mSharedPrefs = SharedPrefManager_RecentRoute.getInstance(this);
        tts = new TTS(this);

        departure_edt = (EditText)findViewById(R.id.departure_edt);
        arrival_edt = (EditText)findViewById(R.id.arrival_edt);
        arr_jibunAddr_listView = (ListView)findViewById(R.id.arr_directions_jibunAddr_list);
        recent_listView = (ListView)findViewById(R.id.recent_listView);
        dep_jibunAddr_listView = (ListView)findViewById(R.id.dep_directions_jibunAddr_list);
        layout1 = (LinearLayout)findViewById(R.id.layout1);
        layout2 = (LinearLayout)findViewById(R.id.layout2);
        btn_arr_sttStart = (ImageButton) findViewById(R.id.btn_arr_sttStart);
        btn_dep_sttStart = (ImageButton) findViewById(R.id.btn_dep_sttStart);
        start_tv = (TextView) findViewById(R.id.start_tv);
        end_tv = (TextView) findViewById(R.id.end_tv);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        Init();

        arrival_edt.clearFocus();
        departure_edt.clearFocus();

        //main activity에서 마지막 위치 좌표 데이터를 받음
        Intent receive_intent = getIntent();
        Latitude = receive_intent.getDoubleExtra("BUS_ROUTE_CLICK_LAT", 0.0);
        Longitude = receive_intent.getDoubleExtra("BUS_ROUTE_CLICK_LONG", 0.0);
        textRaise = receive_intent.getBooleanExtra("TEXT_RAISE", false);
        Vibrator = receive_intent.getBooleanExtra("VIBRATOR", false);

        if(textRaise){
            start_tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
            end_tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
        } else {
            start_tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            end_tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        }

        Log.d("ReceiveMainActivity","Latitude " + Latitude + " Longitude " + Longitude);

        ReverseGeoPointer_Thread thread = new ReverseGeoPointer_Thread(handler, Longitude, Latitude);     //반대로 넣어야 됨 안그러면 못찾음
        thread.start();


        departure_edt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //진동 설정
                    if(Vibrator){
                        android.os.Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    }

                    AddrAPI_Thread addrAPI = new AddrAPI_Thread(handler, departure_edt.getText().toString());
                    addrAPI.start();

                    recent_listView.setVisibility(View.GONE); //죄근 검색 리스트 숨기기
                    dep_jibunAddr_listView.setVisibility(View.VISIBLE);//주소 검색 리스트 보이기

                    test2 = true;

                    //키보드 내리기
                    InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(departure_edt.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        arrival_edt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //진동 설정
                    if(Vibrator){
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    }

                    AddrAPI_Thread addrAPI = new AddrAPI_Thread(handler, arrival_edt.getText().toString());
                    addrAPI.start();

                    recent_listView.setVisibility(View.GONE); //죄근 검색 리스트 숨기기
                    arr_jibunAddr_listView.setVisibility(View.VISIBLE);//주소 검색 리스트 보이기

                    test = true;

                    //키보드 내리기
                    InputMethodManager manager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(arrival_edt.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        departure_edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if(focus){
                    //포커스를 얻었을때
                    //진동 설정
                    if(Vibrator){
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    }

                    layout2.setVisibility(View.INVISIBLE);
                    recent_listView.setVisibility(View.VISIBLE);     //죄근 검색 리스트 보이기
                    dep_jibunAddr_listView.setVisibility(View.INVISIBLE);//주소 검색 리스트 숨기기

                }
            }
        });

        arrival_edt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                if(focus){
                    //진동 설정
                    if(Vibrator){
                        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                    }

                    recent_listView.setVisibility(View.VISIBLE);     //죄근 검색 리스트 보이기
                    arr_jibunAddr_listView.setVisibility(View.INVISIBLE);//주소 검색 리스트 숨기기
                }
            }
        });

        //------------------------- 저장값 불러오기(최근 경로 20개) Start ------------------------------
        SharedPreferences rh = getSharedPreferences("Search_History", MODE_PRIVATE);
        int count_ro = rh.getInt("count_route", 0);
        String[] rou = new String[20];
        for(int i=0; i<20; i++) {
            rou[i] = rh.getString("routes"+i, "");
        }
        for(int i=0; i<20; i++) {
            if(rou[i]!="") {
                items.add(0, rou[i]);

                String sp[] = rou[i].split("→");
                save_depList.add(0, sp[0]); save_arrList.add(0, sp[1]);
                //Log.d("routes"+i, sp[0] + "/" + sp[1]);

                if(i == 19 && count_ro != 20) {
                    items.clear();
                    save_depList.clear(); save_arrList.clear();
                    for(int j=0; j<20; j++) {
                        if(count_ro == j) {
                            items.add(0, rou[j]);

                            String sp1[] = rou[j].split("→");
                            save_depList.add(0, sp1[0]); save_arrList.add(0, sp1[1]);

                            for(int k=1; k<20; k++) {
                                if (k+j <= 19) {
                                    items.add(0, rou[k + j]);

                                    String sp2[] = rou[k + j].split("→");
                                    save_depList.add(0, sp2[0]); save_arrList.add(0, sp2[1]);
                                }
                                else if(k+j > 19) {
                                    int zero =0;
                                    while(19-k>=0) {
                                        items.add(0, rou[zero]);

                                        String sp2[] = rou[zero].split("→");
                                        save_depList.add(0, sp2[0]); save_arrList.add(0, sp2[1]);

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
        //------------------------- 저장값 불러오기(최근 경로 20개) End --------------------------------

        //-------------- 최근 기록 -------------
        adapter = new Directions_ListViewAdapter(this, save_depList, save_arrList, textRaise);
        recent_listView.setAdapter(adapter);

        final ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        //recent_listView.setAdapter(adapter1);

        recent_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                //start, end 각각 넣어야 한다.
                //클릭한 아이템의 문자열 가져오기
                String selected_item_rou = (String) adapterView.getItemAtPosition(position);
                String[] routes = selected_item_rou.split("→");

                departure_edt.setText(routes[0].toString());
                arrival_edt.setText(routes[1].toString());

                adapter.notifyDataSetChanged();

                //메인으로 데이터 전송 및 메인 activity Start
                send_intent.putExtra("Activity", "Directions");
                //보낼 출발 주소 데이터 저장
                send_intent.putExtra("dep_addr", routes[0]);
                //도착
                send_intent.putExtra("arr_addr", routes[1]);
                //키워드
                String sp[] = routes[1].split(" ");
                send_intent.putExtra("arr_keyw", sp[sp.length - 1]);

                //---------------최근 검색 기록 저장--------------------------------
                SharedPrefManager_StarSave mSharedPrefs = SharedPrefManager_StarSave.getInstance(cThis);
                SharedPreferences rh = getSharedPreferences("Search_History", MODE_PRIVATE);
                String null_text = "";
                String start = departure_edt.getText().toString().trim();
                String end = arrival_edt.getText().toString().trim();
                String route = start + "→" + end;
                int count = rh.getInt("count_route", 0);

                if (!start.equals("") && !end.equals("")) {
                    //리스트에 저장되어있는 값이 아니라면
                    if(mSharedPrefs.findRoute(route) == false){
                        //SharedPreferences 저장하기
                        int reCount = mSharedPrefs.addRoute(count, route);
                        mSharedPrefs.addCount_rou(reCount + 1);

                        items.add(0, route);
                    } else {
                        //리스트 순서 수정
                        //지우고
                        mSharedPrefs.DelRoute(count, route);

                        for(int i=0;i<items.size();i++){
                            if(route.equals(items.get(i))){
                                items.remove(i);
                            }
                        }

                        //다시저장
                        int reCount = mSharedPrefs.addRoute(count, route);
                        mSharedPrefs.addCount_rou(reCount + 1);

                        items.add(0, route);
                    }

                }
                //---------------최근 검색 기록 저장--------------------------------
                //메인 엑티비티로 데이터 전달
                setResult(Activity.RESULT_OK, send_intent);
                finish();
            }
        });

        //----------------------STT&TTS--------------------------
        //음성인식 Button

        btn_arr_sttStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                stt = new STT(cThis);

                stt.setTTS(tts);
                stt.DialogShow();

                System.out.println("음성인식 시작!");
                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(Directions_Activity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                    //권한을 허용하지 않는 경우
                }else{
                    //권한을 허용한 경우
                    try {
                        stt.sttStert();
                        //speak = stt.getSpeak();
                    }catch (SecurityException e){e.printStackTrace();}
                }

                //음성인식한 단어를 arrival_edt에 setText
                stt.setEditText(arrival_edt);
            }
        });

        btn_dep_sttStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                stt = new STT(cThis);

                stt.setTTS(tts);
                stt.DialogShow();

                System.out.println("음성인식 시작!");
                if(ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(Directions_Activity.this,new String[]{Manifest.permission.RECORD_AUDIO},1);
                    //권한을 허용하지 않는 경우
                }else{
                    //권한을 허용한 경우
                    try {
                        stt.sttStert();
                        //speak = stt.getSpeak();
                    }catch (SecurityException e){e.printStackTrace();}
                }

                //음성인식한 단어를 arrival_edt에 setText
                stt.setEditText(departure_edt);
            }
        });
    }

    //뒤로가기 버튼 클릭시
    @Override
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        //super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            tts.EndTTS();
            if(stt != null){
                stt.EndSTT();
            }

            finish();
            return;
        }
    }
}