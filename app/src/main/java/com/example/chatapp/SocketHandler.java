package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Random;

public class SocketHandler implements Runnable{

    static ServerSocket ss;
    static Socket socket;
    static DataInputStream din;
    static DataOutputStream dout;
    static Context context;

    public SocketHandler(Context context) {
        this.context = context;
    }

    @Override
    public void run() {
        try {
            ss = new ServerSocket(1201);
            socket = ss.accept();
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());
            SharedPreferences prefs = context.getSharedPreferences("userPref", Context.MODE_PRIVATE);
            Random r = new Random();
            char[] c = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            String code = "";
            SharedPreferences.Editor editor = prefs.edit();
            if(prefs.getBoolean("hasKey", false)) {
                code = prefs.getString("code", "notfound");
            } else {
                for(int i=0; i<10; i++) {
                    code += Character.toString(c[r.nextInt(26)]);
                }
                editor.putBoolean("hasKey", true);
                editor.putString("code", code);
                editor.apply();
            }
            String deviceName = prefs.getString("username", "server");
            dout.writeUTF(code + " " + deviceName);
            dout.flush();
            String otherDeviceData = din.readUTF();
            //Log.e("CLIENTDETAILS", "run: " + otherDeviceData);
            String otherDeviceCode = otherDeviceData.split(" ")[0];
            String otherDeviceName = otherDeviceData.split(" ")[1];
            editor.putString("databaseName", otherDeviceCode);
            editor.putString("otherDeviceName", otherDeviceName);
            editor.apply();
            Intent intentServer = new Intent(context, ChatActivity.class);
            intentServer.putExtra("isServer", "1");
            context.startActivity(intentServer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static DataInputStream getInputStream()
    {
        return din;
    }

    static DataOutputStream getOutputStream()
    {
        return dout;
    }

    static void stopThread()
    {
        try
        {
            din.close();
            dout.close();
            socket.close();
            ss.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
