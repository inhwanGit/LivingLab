package com.example.myapplicationtablet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class OnBoardingActivity extends AppCompatActivity {

    private EditText routeCd_Edit;      //노선 번호
    private EditText busNum_Edit;       //버스의 차 번호
    private Button start_button;              //확인

    private OnBoardingHandler handler;
    private Intent intent;

    private class OnBoardingHandler extends Handler {

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

                    intent.putExtra("Activity", "OnBoardingActivity");
                    intent.putExtra("routeCd", routeCd);
                    intent.putExtra("routeNo", routeNo);
                    intent.putExtra("routeTp", routeTp);

                    GetBusRegInfoByRouteId_Thread thread = new GetBusRegInfoByRouteId_Thread(handler, routeCd);
                    thread.start();
                }
                else{
                    Log.d("GetRouteInfo_Thread", "ERROR : " + error);
                    Toast.makeText(OnBoardingActivity.this, error + " 다시 입력해주세요.", Toast.LENGTH_LONG).show();
                }
            }
            else if(bundle.getBoolean("GetBusRegInfoByRouteId_Thread")){
                ArrayList<Bus> buslist = (ArrayList<Bus>)bundle.getSerializable("BUS_LIST");

                //실제 운행되는 버스의 차 번호가 맞는지 확인
                for(int i=0; i<buslist.size(); i++){
                    //한글 키보드 안돼서 임시값 넣고 테스트
                    //String busNum = busNum_Edit.getText().toString();
                    //String busNum = "대전75자1006";
                    String busNum = "대전75자2072";   //102 저상버스


                    if(busNum.equals(buslist.get(i).GetBusCarRegNo())){
                        Log.d("GetBusRegInfoByRouteId_Thread", "CAR_REG_NO : " + buslist.get(i).GetBusCarRegNo() + " BUS_TYPE : " + buslist.get(i).GetBusType());

                        //맞으면 데이터 저장 후 메인으로 전달
                        intent.putExtra("busNum", busNum);
                        intent.putExtra("busType", buslist.get(i).GetBusType());

                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                }
            }


        }
    }

    public void Init(){
        handler = new OnBoardingHandler();
        intent = new Intent();
        routeCd_Edit = (EditText) findViewById(R.id.routeCd_Edit);
        busNum_Edit = (EditText) findViewById(R.id.busNum_Edit);
        start_button = (Button) findViewById(R.id.start_button);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding);

        Init();

        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                //버스 번호(ex : 101,102,103 ...)를 가져오기 위한 호출
                GetRouteInfo_Thread thread = new GetRouteInfo_Thread(handler, routeCd_Edit.getText().toString());
                thread.start();
                */

                //버스 번호(ex : 101,102,103 ...)를 가져오기 위한 호출
                //한글 키보드 안돼서 임시값 넣고 테스트

                GetRouteInfo_Thread thread = new GetRouteInfo_Thread(handler, "30300037");
                thread.start();
            }
        });

    }
}