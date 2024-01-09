package com.example.myapplicationtablet;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

public class TTS {
    //음성 출력용
    private TextToSpeech tts;

    public TTS(Context cThis){
        tts = new TextToSpeech(cThis, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    tts.setLanguage(Locale.KOREAN);
                }
                else{
                    Log.d("MainActivity","tts 객체 초기화 도중 문제가 발생했습니다.");
                }
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {

            }

            @Override
            public void onError(String utteranceId) {
                Log.d("MainActivity",utteranceId + " 재생중 에러가 발생했습니다.");
            }
        });
    }

    //입력된 음성 메세지 확인 후 동작 처리
    public void FuncVoiceOrderCheck(String VoiceMsg) {
        if(VoiceMsg.length()<1)return;

        VoiceMsg=VoiceMsg.replace(" ","");//공백제거

        if(VoiceMsg.indexOf("전등꺼")>-1 || VoiceMsg.indexOf("불꺼")>-1){
            FuncVoiceOut("전등을 끕니다");//전등을 끕니다 라는 음성 출력
        }
    }

    //음성 메세지 출력용
    public void FuncVoiceOut(String OutMsg) {
        if(OutMsg.length()<1)return;

        tts.setPitch(1.0f);//목소리 톤1.0
        tts.setSpeechRate(1.0f);//목소리 속도
        //tts.speak(OutMsg, TextToSpeech.QUEUE_ADD,null);
        tts.speak(OutMsg, TextToSpeech.QUEUE_ADD, null, "0");

        //TextToSpeech.QUEUE_FLUSH(진행중인 음성 출력을 끊고 이번 TTS의 음성 출력을 한다).
        //TextToSpeech.QUEUE_ADD(진행중인 음성 출력이 끝난 후에 이번 TTS의 음성 출력을 진행한다.)
    }

    public void EndTTS(){
        if(tts!=null){
            tts.stop();
            tts.shutdown();
            tts=null;
        }
    }

}
