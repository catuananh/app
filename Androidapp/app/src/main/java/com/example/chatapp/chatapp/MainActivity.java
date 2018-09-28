package com.example.chatapp.chatapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText edtUserName, edtChat;
    private Button btnReg, btnChat;
    private ListView lvUserName, lvChat;

    ArrayList <String> aUserName;
    ArrayList <String> aChat;

    private Socket mSocket;

    {
        try{
            //mSocket = IO.socket("https://ptanh-chatapp.herokuapp.com");
            //mSocket = IO.socket("http://192.168.1.3:3000");
            mSocket = IO.socket("http://ec2-18-224-100-96.us-east-2.compute.amazonaws.com:3000");
        } catch (URISyntaxException e) {throw new RuntimeException(e);}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSocket.connect();
        mSocket.emit("client_send_useronline");

        mSocket.on("server_send_register_fail", onNewMessage_RegisterFail);
        mSocket.on("server_send_userOnlineList", onNewMessage_RegisterSuccess);
        mSocket.on("server_send_message", onNewMessage_Chat);
        mSocket.on("server_send_register_success", onNewMessage_AddNewUser);
        mSocket.on("server_send_reset", onNewMessage_Reset);

        aUserName = new ArrayList<String>();
        aChat = new ArrayList<String>();

        edtUserName = (EditText) findViewById(R.id.txtName);
        btnReg = (Button) findViewById(R.id.btnReg);
        lvUserName = (ListView)findViewById(R.id.lvUsername);

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit("client_send_username", edtUserName.getText().toString());
            }
        });

        edtChat = (EditText) findViewById(R.id.txtChat);
        btnChat = (Button) findViewById(R.id.btnChat);
        lvChat = (ListView) findViewById(R.id.lvChat);

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSocket.emit("client_send_message", edtChat.getText().toString());
                edtChat.setText("");
            }
        });
    }

    private Emitter.Listener onNewMessage_RegisterFail = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(getApplicationContext(), "Register Fail!!!",Toast.LENGTH_SHORT ).show();
                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onNewMessage_RegisterSuccess = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    JSONArray message;
                    try {
                        message = data.getJSONArray("user");
                        for (int i=0; i<message.length(); i++){
                            aUserName.add(message.get(i).toString());
                        }
                        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, aUserName);
                        lvUserName.setAdapter(adapter);
                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onNewMessage_Chat = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String user;
                    String message;
                    try {
                        user = data.getString("username");
                        message = data.getString("msg");
                        aChat.add(user + ": " + message);
                        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, aChat);
                        lvChat.setAdapter(adapter);
                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onNewMessage_AddNewUser = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String user;
                    try {
                        btnReg.setEnabled(false);
                        edtUserName.setEnabled(false);
                        user = data.getString("username");
                        aUserName.add(user);
                        ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, aUserName);
                        lvUserName.setAdapter(adapter);
                        Toast.makeText(getApplicationContext(), "Register Successful!",Toast.LENGTH_SHORT ).show();
                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };

    private Emitter.Listener onNewMessage_Reset = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        btnReg.setEnabled(true);
                        edtUserName.setEnabled(true);
                        aUserName.clear();
                        aChat.clear();
                        ArrayAdapter adapter1 = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, aUserName);
                        ArrayAdapter adapter2 = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, aChat);
                        lvUserName.setAdapter(adapter1);
                        lvChat.setAdapter(adapter2);
                        Toast.makeText(getApplicationContext(), "Reset Successful!",Toast.LENGTH_SHORT ).show();
                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };

}
