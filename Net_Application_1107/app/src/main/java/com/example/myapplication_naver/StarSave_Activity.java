package com.example.myapplication_naver;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class StarSave_Activity extends AppCompatActivity {

    // 마지막으로 뒤로가기 버튼을 눌렀던 시간 저장
    private long backKeyPressedTime = 0;
    private Context cThis;
    private ListView route_list;
    private ArrayList<String> save_arrList = new ArrayList<>();
    private ArrayList<String> save_depList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private Directions_ListViewAdapter adapter1;
    private final ArrayList<String> items = new ArrayList<>();
    private int count_star;
    private boolean textRaise;
    private boolean Vibrator;
    private TextView title;


    public void Init(){
        cThis = this;
        route_list = (ListView) findViewById(R.id.route_list);
        title = (TextView) findViewById(R.id.title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star_save);

        Init();

        Intent receive_intent = getIntent();
        textRaise = receive_intent.getBooleanExtra("TEXT_RAISE", false);
        Vibrator = receive_intent.getBooleanExtra("VIBRATOR", false);

        if(textRaise){
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
        } else {
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35);
        }

        route_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //진동 설정
                if(Vibrator){
                    android.os.Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                String selected_item_rou = (String) adapterView.getItemAtPosition(position);
                String[] routes = selected_item_rou.split("→");
                //Log.d("route_list.setOnItemClickListener", selected_item_rou);

                String start = routes[0];
                String end = routes[1];

//                listView.notify();
                adapter.notifyDataSetChanged();

                Intent intent = new Intent();
                intent.putExtra("Activity", "StarSave");
                intent.putExtra("route", selected_item_rou);
                intent.putExtra("start", start);
                intent.putExtra("end", end);

                //메인 엑티비티로 데이터 전달
                setResult(Activity.RESULT_OK, intent);
                finish();

            }
        });

        route_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                //진동 설정
                if(Vibrator){
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                //삭제 확인 창 띄워서 삭제하기
                String selected_item_rou = (String) adapterView.getItemAtPosition(position);

                showDialog(selected_item_rou);

                adapter.notifyDataSetChanged();

                return true;
            }
        });

        //------------------------- 저장값 불러오기(즐겨찾기 20개) Start ------------------------------
        SharedPreferences rh = getSharedPreferences("Search_History", MODE_PRIVATE);
        count_star = rh.getInt("count_star", 0);
        String[] star = new String[20];
        for(int i=0; i<20; i++) {
            star[i] = rh.getString("star"+i, "");
        }
        for(int i=0; i<20; i++) {
            if(star[i]!="") {
                items.add(0, star[i]);

                String sp[] = star[i].split("→");
                save_depList.add(0, sp[0]); save_arrList.add(0, sp[1]);
                Log.d("star"+i, sp[0] + "/" + sp[1]);

                //Log.d("star"+i, star[i]);
                if(i == 19 && count_star != 20) {
                    items.clear();
                    save_depList.clear(); save_arrList.clear();
                    for(int j=0; j<20; j++) {
                        if(count_star == j) {
                            items.add(0, star[j]);

                            String sp1[] = star[j].split("→");
                            save_depList.add(0, sp1[0]); save_arrList.add(0, sp1[1]);

                            for(int k=1; k<20; k++) {
                                if (k+j <= 19) {
                                    items.add(0, star[k + j]);
                                    items.add(0, star[j]);

                                    String sp2[] = star[k + j].split("→");
                                    save_depList.add(0, sp2[0]); save_arrList.add(0, sp2[1]);
                                    String sp3[] = star[j].split("→");
                                    save_depList.add(0, sp3[0]); save_arrList.add(0, sp3[1]);
                                }
                                else if(k+j > 19) {
                                    int zero =0;
                                    while(19-k>=0) {
                                        items.add(0, star[zero]);
                                        items.add(0, star[j]);

                                        String sp2[] = star[zero].split("→");
                                        save_depList.add(0, sp2[0]); save_arrList.add(0, sp2[1]);
                                        String sp3[] = star[j].split("→");
                                        save_depList.add(0, sp3[0]); save_arrList.add(0, sp3[1]);

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
        //------------------------- 저장값 불러오기(즐겨찾기 20개) End --------------------------------


        //즐겨찾기 리스트 설정
        adapter1 = new Directions_ListViewAdapter(this, save_depList, save_arrList, textRaise);
        route_list.setAdapter(adapter1);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        //route_list.setAdapter(adapter);
    }

    public void showDialog(String route) {
        SharedPrefManager_StarSave mSharedPrefs = SharedPrefManager_StarSave.getInstance(this);

        AlertDialog oDialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                .setTitle("삭제 확인")
                .setMessage(route + "\n즐겨찾기를 삭제 하시겠습니까?")

                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //진동 설정
                        if(Vibrator){
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                        }

                        mSharedPrefs.DelStar(count_star, route);

                        try {
                            //액티비티 화면 재갱신 시키는 코드
                            Intent intent = getIntent();
                            finish(); //현재 액티비티 종료 실시
                            overridePendingTransition(0, 0); //인텐트 애니메이션 없애기
                            startActivity(intent); //현재 액티비티 재실행 실시
                            overridePendingTransition(0, 0); //인텐트 애니메이션 없애기

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //진동 설정
                        if(Vibrator){
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                        }
                    }
                })
//                .setCancelable(false)   // 백버튼으로 팝업창이 닫히지 않도록 한다.
                .show();
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
            finish();
            return;
        }
    }
}