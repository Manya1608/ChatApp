package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView mRecyclerView;
    static private MessageAdapter mMessageAdapter;
    LinearLayoutManager mLinearLayoutManager;
    Button sendButton;
    EditText txt;
    static Handler messageHandler;
    static ArrayList<BaseMessage> list;
    static DataInputStream din;
    static DataOutputStream dout;
    static final int RECIEVED = 1, NOT_RECIEVED = 0;
    static DatabaseHelper dbHelper;
    static String code;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //shared pref
       // SharedPreferences sharedPreferences = getSharedPreferences("userPref", MODE_PRIVATE);
        //String username = sharedPreferences.getString("username", "Server");

        sendButton = (Button) findViewById(R.id.send_button);
        txt = (EditText) findViewById(R.id.edittext_chatbox);
        sendButton.setOnClickListener(this);
        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("userPref", MODE_PRIVATE);
        code = prefs.getString("databaseName", "server");
        list = getMessageList(code);

        Intent intent = getIntent();
        String check = intent.getStringExtra("isServer");
        if(check.equals("1")) {
            din = SocketHandler.getInputStream();
            dout = SocketHandler.getOutputStream();
        } else {
            din = ClientHandler.getInputStream();
            dout = ClientHandler.getOutputStream();
        }

        messageHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case RECIEVED:
                        mMessageAdapter.notifyDataSetChanged();
                        mRecyclerView.smoothScrollToPosition(mMessageAdapter.getItemCount() - 1);
                        break;
                    case NOT_RECIEVED:
                        break;
                }
            }
        };

        String clientName = prefs.getString("otherDeviceName", "server");
        mRecyclerView = findViewById(R.id.recyclerview_message_list);
        mMessageAdapter = new MessageAdapter(list, clientName);
        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mMessageAdapter);

        ReceiveData receiveData = new ReceiveData();
        Thread rd = new Thread(receiveData);
        rd.start();
    }

    private ArrayList<BaseMessage> getMessageList(String code) {
        ArrayList<BaseMessage> arrayList = new ArrayList<>();
        Cursor cursor = dbHelper.fetch(code);
        while(cursor != null && !cursor.isAfterLast())
        {
            String[] str = cursor.getString(0).split("%@");
            String time = str[2];
            String msg = str[1];
            String status;
            if(str[0].equals("r"))
            {
                status = "received";
            }
            else
            {
                status = "sent";
            }
            arrayList.add(new BaseMessage(msg, time, status));
            cursor.moveToNext();
        }
        cursor.close();
        return arrayList;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send_button:
                new SendData().execute();
                txt.setText("");
                mRecyclerView.smoothScrollToPosition(mMessageAdapter.getItemCount() - 1);
                break;
        }
    }

    class SendData extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            @SuppressLint("WrongThread") String message = txt.getText().toString().trim();
            Date currentTime = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");
            mMessageAdapter.mMessageList.add(new BaseMessage(message, simpleDateFormat.format(currentTime), "sent"));

            dbHelper.insert(code, "s", message + "%@" + simpleDateFormat.format(currentTime));

            try {
                dout.writeUTF(message);
                dout.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            mMessageAdapter.notifyDataSetChanged();
            mRecyclerView.smoothScrollToPosition(mMessageAdapter.getItemCount() - 1);
        }
    }

    static class ReceiveData implements Runnable{
        @Override
        public void run() {
            String msgin = "";
            while (!msgin.equals("disconnect"))
            {
                try{
                    msgin = din.readUTF();
                    Date currentTime = new Date();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm");
                    Message msg = Message.obtain();
                    msg.what = NOT_RECIEVED;
                    if (!msgin.equals("")){
                        mMessageAdapter.mMessageList.add(new BaseMessage(msgin, simpleDateFormat.format(currentTime), "received"));
                        dbHelper.insert(code,"r", msgin + "%@" + simpleDateFormat.format(currentTime));
                        msg.what = RECIEVED;
                        messageHandler.sendMessage(msg);
                    }

                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Disconnect");
        builder.setMessage("Are you sure you want to disconnect?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(SocketHandler.getInputStream() != null)
                {
                    SocketHandler.stopThread();
                }
                else {
                    ClientHandler.stopConnection();
                }
                finish();
                System.exit(0);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothing
            }
        });
        builder.show();
    }
}
