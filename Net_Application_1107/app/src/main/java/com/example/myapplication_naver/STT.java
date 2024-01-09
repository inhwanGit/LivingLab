package com.example.myapplication_naver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.EditText;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;

public class STT {
    private SpeechRecognizer mRecognizer;
    private Intent intent;

    private AlertDialog.Builder builder;
    private AlertDialog ad;

    private TTS tts;

    private ArrayList<String> matches;
    private String speak;
    private EditText editText;
    private SearchView searchView;


    public STT(Context cThis){
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,cThis.getPackageName());

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(cThis);
        mRecognizer.setRecognitionListener(listener);

        //------------Dialog---------------
        builder = new AlertDialog.Builder(cThis);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(editText != null){
                    editText.setText(speak);
                } else if(searchView != null){
                    searchView.setQuery(speak, true);
                }

                EndSTT();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EndSTT();
            }
        });

        builder.setTitle("음성인식");
        builder.setMessage("음성인식 결과가 여기에 표시됩니다.");
        //builder.setView(dialogView); //사용자지정 레이아웃 적용
        ad = builder.create();
    }

    public void setEditText(EditText editText){
        this.editText = editText;
    }

    public void setSearchView(SearchView searchView) {this.searchView = searchView;}

    public void sttStert(){
        mRecognizer.startListening(intent);
    }

    public void DialogShow(){
        ad.show();
    }

    public void setTTS(TTS tts){
        this.tts = tts;
    }

    public void EndSTT(){
        if(mRecognizer!=null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer=null;
        }
    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }
            Toast.makeText(MyApplication.ApplicationContext(), "Error : " + message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
            matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            for (int i = 0; i < matches.size(); i++) {

                ad.dismiss();
                builder.setMessage(matches.get(i));
                //textView.setText(matches.get(i));
                ad = builder.create();
                ad.show();

                tts.FuncVoiceOrderCheck(matches.get(i));

                speak = matches.get(i);
            }

            mRecognizer.startListening(intent); //음성인식 계속 실행
        }

        @Override
        public void onPartialResults(Bundle bundle) {

        }

        @Override
        public void onEvent(int i, Bundle bundle) {

        }
    };
}
