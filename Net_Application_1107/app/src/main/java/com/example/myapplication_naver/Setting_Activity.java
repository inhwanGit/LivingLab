package com.example.myapplication_naver;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceControl;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class Setting_Activity extends AppCompatActivity {

    private TTS tts;
    private Switch textRaise_Btn;           //글자키우기
    //private CheckBox colorEmphasis_Btn;   //색상강조
    private Switch SearchListOn_Btn;        //검색기록저장
    private Switch VibratorOn_Btn;          //진동설정
    private Button changeUserType_Btn;      //장애인 유형 변경
    private Button changeCancel_Btn;        //변경 취소
    private Button changeSave_Btn;          //변경 저장
    private LinearLayout linearLayout1;
    private LinearLayout linearLayout2;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    private RadioButton radioButton3;
    private TextView title;
    private TextView question;

    private SharedPrefManager_Setting mSharedPrefs3;
    private SharedPreferences sf;

    private Intent intent;
    private boolean textRaise;
    private boolean Vibrator;

    public void Init(){
        intent = new Intent();
        tts = new TTS(this);

        mSharedPrefs3 = SharedPrefManager_Setting.getInstance(this);
        textRaise_Btn = (Switch) findViewById(R.id.textRaise);
        SearchListOn_Btn = (Switch) findViewById(R.id.SearchListOn);
        VibratorOn_Btn = (Switch) findViewById(R.id.VibratorOn);
        //colorEmphasis_Btn = (Switch) findViewById(R.id.colorEmphasis);
        changeUserType_Btn = (Button) findViewById(R.id.changeUserType);
        changeCancel_Btn = (Button) findViewById(R.id.changeCancel);
        changeSave_Btn = (Button) findViewById(R.id.changeSave);
        linearLayout1 = (LinearLayout) findViewById(R.id.layout1);
        linearLayout2 = (LinearLayout) findViewById(R.id.layout2);
        radioButton1 = (RadioButton) findViewById(R.id.radioButton1);
        radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
        radioButton3 = (RadioButton) findViewById(R.id.radioButton3);
        title = (TextView) findViewById(R.id.title);
        question = (TextView) findViewById(R.id.question);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Init();

        Intent receive_intent = getIntent();
        textRaise = receive_intent.getBooleanExtra("TEXT_RAISE", false);
        Vibrator = receive_intent.getBooleanExtra("VIBRATOR", false);

        if(textRaise){
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40);
            textRaise_Btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
            SearchListOn_Btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
            VibratorOn_Btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
            changeUserType_Btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
            question.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);
            radioButton1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
            radioButton2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
            radioButton3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 21);
            changeSave_Btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,23);
            changeCancel_Btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 23);

        } else {
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 35);
            textRaise_Btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            SearchListOn_Btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            VibratorOn_Btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            changeUserType_Btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            question.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            radioButton1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            radioButton2.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            radioButton3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            changeSave_Btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
            changeCancel_Btn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        }

        //------------------------- 저장값 불러오기(설정 정보) Start ------------------------------
        sf = getSharedPreferences("Setting_History", MODE_PRIVATE);
        textRaise_Btn.setChecked(sf.getBoolean("textRaise", false));
        //colorEmphasis_Btn.setChecked(sf.getBoolean("colorEmphasis", false));
        SearchListOn_Btn.setChecked(sf.getBoolean("SearchListOn", true));
        VibratorOn_Btn.setChecked(sf.getBoolean("VibratorOn", true));
        //------------------------- 저장값 불러오기(설정 정보) End --------------------------------

        //레이아웃 애니메이션
        LayoutTransition lt = new LayoutTransition();
        lt.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        linearLayout1.setLayoutTransition(lt);
        linearLayout2.setLayoutTransition(lt);

        textRaise_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //진동 설정
                if(Vibrator){
                    android.os.Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }
        });

        SearchListOn_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //진동 설정
                if(Vibrator){
                    android.os.Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }
        });

        VibratorOn_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //진동 설정
                if(Vibrator){
                    android.os.Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }
            }
        });

        changeUserType_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                linearLayout1.setVisibility(View.GONE);
                linearLayout2.setVisibility(View.VISIBLE);
            }
        });

        changeCancel_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    android.os.Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                linearLayout1.setVisibility(View.VISIBLE);
                linearLayout2.setVisibility(View.GONE);
            }
        });

        changeSave_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //진동 설정
                if(Vibrator){
                    android.os.Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                if(radioButton1.isChecked() == true){
                    intent.putExtra("DisabledType", 1);
                } else if(radioButton2.isChecked() == true){
                    intent.putExtra("DisabledType", 2);
                } else if(radioButton3.isChecked() == true){
                    intent.putExtra("DisabledType", 3);
                } else {
                    Toast.makeText(Setting_Activity.this, "장애인 유형을 선택해 주세요.", Toast.LENGTH_LONG).show();
                    tts.TextToSpeech("장애인 유형을 선택해 주세요.");
                    return;
                }
                linearLayout1.setVisibility(View.VISIBLE);
                linearLayout2.setVisibility(View.GONE);
            }
        });


    }

    //뒤로가기 버튼 클릭시
    @Override
    public void onBackPressed() {
        mSharedPrefs3.add_textRaise(textRaise_Btn.isChecked());
        mSharedPrefs3.add_SearchListOn(SearchListOn_Btn.isChecked());
        mSharedPrefs3.add_VibratorOn(VibratorOn_Btn.isChecked());
        //mSharedPrefs3.add_colorEmphasis(colorEmphasis_Btn.isChecked());

        intent.putExtra("Activity", "Setting");
        intent.putExtra("textRaise_Btn", textRaise_Btn.isChecked());
        intent.putExtra("SearchListOn_Btn", SearchListOn_Btn.isChecked());
        intent.putExtra("VibratorOn_Btn", VibratorOn_Btn.isChecked());
        //intent.putExtra("colorEmphasis_Btn", colorEmphasis_Btn.isChecked());

        //메인 엑티비티로 데이터 전달
        setResult(Activity.RESULT_OK, intent);
        finish();

        //Log.d("Setting","textRaise_Btn : " + textRaise_Btn.isChecked());
        //Log.d("Setting","colorEmphasis_Btn : " + colorEmphasis_Btn.isChecked());
        //Log.d("Setting","SearchListOn_Btn : " + SearchListOn_Btn.isChecked());

    }
}