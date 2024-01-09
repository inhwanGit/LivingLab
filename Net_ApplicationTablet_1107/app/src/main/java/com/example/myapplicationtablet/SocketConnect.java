package com.example.myapplicationtablet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.transports.WebSocket;
import okhttp3.OkHttpClient;
import java.net.URI;

public class SocketConnect {
    private String uri = "http://112.150.84.136:5010";

    private Socket socket;
    private IO.Options options;
    private Handler handler;



    //소켓 초기화
    public SocketConnect(Handler handler){
        this.handler = handler;

        try {
            options = IO.Options.builder().setTransports(new String[]{WebSocket.NAME}).setSecure(true).build();
            OkHttpClient okHttpClient = getOkHttpClient();
            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
            IO.setDefaultOkHttpCallFactory(okHttpClient);

            options.callFactory = okHttpClient;
            options.webSocketFactory = okHttpClient;
            socket = IO.socket(uri, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }



    //https://aslamanver.medium.com/socket-io-android-https-ssl-696d9f1602a1==========================================
    //보안 통과
    public static OkHttpClient getOkHttpClient() {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                @Override
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            }}, new java.security.SecureRandom());

            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            builder.sslSocketFactory(sc.getSocketFactory(), new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

                @Override
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            });
            return builder.build();

        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            ex.printStackTrace();
        }

        return null;
    }



    //소켓 열기/닫기
    public void OpenSocket(){
        try {
            socket.on(Socket.EVENT_CONNECT, onConnect);
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.on("message", onMessage);
            socket.on("all", onAll);
            socket.on("reflect", onReflect);
            socket.on("user", onUser);
            socket.on("location", onLocation);
            socket.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CloseSocket(){
        try {
            socket.disconnect();
            socket.off(Socket.EVENT_CONNECT, onConnect);
            socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
            socket.off("message", onMessage);
            socket.off("all", onAll);
            socket.off("reflect", onReflect);
            socket.off("user", onUser);
            socket.off("location", onLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //json 메시지 보내기
    public void sendMessage(JsonObject json) {
        socket.emit("message", json);
    }

    public void sendAll(JsonObject json) {
        socket.emit("all", json);
    }

    public void sendReflect(JsonObject json) {
        socket.emit("reflect", json);
    }

    public void sendUser(JsonObject json) {
        socket.emit("user", json);
    }

    public void sendLocation(JsonObject json) {
        socket.emit("location", json);
    }



    //리스너
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("connection success!");
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("connection error!");
            System.out.println(args[0]);
        }
    };

    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("Response : onMessage");
            for(int i=0; i<args.length; i++){
                System.out.println(i + " : " + args[i].toString());
            }
        }
    };

    private Emitter.Listener onAll = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("Response : onAll");
            for(int i=0; i<args.length; i++){
                System.out.println(i + " : " + args[i].toString());

                try {
                    JSONObject json = new JSONObject(args[i].toString());
                    String id = json.getString("id");
                    int type = json.getInt("type");
                    int event = json.getInt("event");
                    String bus = json.getString("bus");
                    String destination = json.getString("destination");
                    JSONObject currentobj = json.getJSONObject("current");
                    JSONObject locationobj = json.getJSONObject("location");
                    JSONObject startobj = json.getJSONObject("start");
                    JSONObject endobj = json.getJSONObject("end");

                    Location location = new Location(locationobj.getDouble("lat"), locationobj.getDouble("lng"));
                    BusStop current = new BusStop(currentobj.getString("id"),currentobj.getString("name"));
                    BusStop start = new BusStop(startobj.getString("id"),startobj.getString("name"));
                    BusStop end = new BusStop(endobj.getString("id"),startobj.getString("name"));

                    User user = new User(id, type, event, bus, location, current, start, end, destination);

                    new Thread() {
                        public void run() {
                            //메세지 저장 및 전송
                            Message message = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("User", true);
                            bundle.putSerializable("user", user);
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }.start();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Emitter.Listener onReflect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("Response : onReflect");
            for(int i=0; i<args.length; i++){
                System.out.println(i + " : " + args[i].toString());
            }
        }
    };

    private Emitter.Listener onUser = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("Response : onUser");
            for(int i=0; i<args.length; i++){
                System.out.println(i + " : " + args[i].toString());

                try {
                    JSONObject json = new JSONObject(args[i].toString());
                    String id = json.getString("id");
                    int type = json.getInt("type");
                    int event = json.getInt("event");
                    String bus = json.getString("bus");
                    String destination = json.getString("destination");
                    JSONObject currentobj = json.getJSONObject("current");
                    JSONObject locationobj = json.getJSONObject("location");
                    JSONObject startobj = json.getJSONObject("start");
                    JSONObject endobj = json.getJSONObject("end");

                    Location location = new Location(locationobj.getDouble("lat"), locationobj.getDouble("lng"));
                    BusStop current = new BusStop(currentobj.getString("id"),currentobj.getString("name"));
                    BusStop start = new BusStop(startobj.getString("id"),startobj.getString("name"));
                    BusStop end = new BusStop(endobj.getString("id"),endobj.getString("name"));

                    User user = new User(id, type, event, bus, location, current, start, end, destination);

                    new Thread() {
                        public void run() {
                            //메세지 저장 및 전송
                            Message message = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("User", true);
                            bundle.putSerializable("user", user);
                            message.setData(bundle);
                            handler.sendMessage(message);
                        }
                    }.start();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Emitter.Listener onLocation = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("Response : onLocation");
            for(int i=0; i<args.length; i++){
                System.out.println(i + " : " + args[i].toString());
            }
        }
    };
}