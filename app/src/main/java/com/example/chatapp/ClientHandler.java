package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

public class ClientHandler implements Runnable {
    static Socket socket;
    static DataOutputStream dout;
    static DataInputStream din;
    Context context;

    public ClientHandler(Context context)
    {
        this.context = context;
    }

    private void connect(ArrayList<String> ip, int i) {
        if(ip.isEmpty() || ip.size() == i)
            return;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(ip.get(i), 1201), 100);
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
                for(int j=0; j<10; j++) {
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
            Log.e("CLIENTDETAILS", "run: " + otherDeviceData);
            String otherDeviceCode = otherDeviceData.split(" ")[0];
            String otherDeviceName = otherDeviceData.split(" ")[1];
            editor.putString("databaseName", otherDeviceCode);
            editor.putString("otherDeviceName", otherDeviceName);
            editor.apply();
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("isServer", "0");
            context.startActivity(intent);
        } catch (IOException e) {
            connect(ip, i+1);
        }
    }

    @Override
    public void run() {
        ArrayList<String> ips = null;
        try {
            ips = getIPs();
            socket = new Socket();
            socket.connect(new InetSocketAddress(ips.get(0), 1201), 100);
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
            Log.e("CLIENTDETAILS", "run: " + otherDeviceData);
            String otherDeviceCode = otherDeviceData.split(" ")[0];
            String otherDeviceName = otherDeviceData.split(" ")[1];
            editor.putString("databaseName", otherDeviceCode);
            editor.putString("otherDeviceName", otherDeviceName);
            editor.apply();
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("isServer", "0");
            context.startActivity(intent);
        } catch (IOException e) {
            connect(ips, 1);
            e.printStackTrace();
        }
    }

    public ArrayList<String> getIPs() throws IOException {
        ArrayList<String> ips = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
        String line;
        while((line = br.readLine()) != null) {
            String[] splitted = line.split(" +");
            if((splitted != null) && (splitted.length >= 4)) {
                if(splitted[3].matches("..:..:..:..:..:.."))
                    ips.add(splitted[0]);
            }
        }
        ips.add("192.168.43.1");
        ips.add(getHotspotIP());
        return ips;
    }

    private String getHotspotIP() {
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
        if(ip != null)
            return ip;
        return "0.0.0.0";
    }
    static DataInputStream getInputStream()
    {
        return din;
    }

    static DataOutputStream getOutputStream()
    {
        return dout;
    }

    static public void stopConnection() {
        try {
            din.close();
            dout.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}