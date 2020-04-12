package com.example.simplesocket;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SimpleSocket extends Activity implements View.OnClickListener
{
    private static final String CLASSTAG = SimpleSocket.class.getSimpleName();

    private EditText ipAddress;
    private EditText port;
    private EditText socketInput;
    private TextView socketOutput;
    Handler handler;

    //private static final String SERVER_IP = "10.0.2.2";

    @Override
    protected void onCreate(final Bundle icicle) {
        Button socketButton;

        super.onCreate(icicle);
        this.setContentView(R.layout.simple_socket);

        this.ipAddress = (EditText) findViewById(R.id.socket_ip);
        this.port = (EditText) findViewById(R.id.socket_port);
        this.socketInput = (EditText) findViewById(R.id.socket_input);
        this.socketOutput = (TextView) findViewById(R.id.socket_output);
        socketButton = (Button) findViewById(R.id.socket_button);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());

        this.handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                //process incoming messages here
                switch (msg.what) {
                    case 0:
                        socketOutput.setText((String) msg.obj);
                        break;
                }
                super.handleMessage(msg);
            }
        };
        socketButton.setOnClickListener(this);
    }


    private String callSocket(final InetAddress ad, final String port, final String socketData) {
        Socket socket = null;
        BufferedWriter writer = null;
        BufferedReader reader = null;
        String output = null;

        try {
            socket = new Socket(ad, Integer.parseInt(port));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String input = socketData;
            writer.write(input + "\n", 0, input.length() + 1);
            writer.flush();

            //read back output
            output = reader.readLine();
            Log.d("NetworkExplorer".toString(), " " + SimpleSocket.CLASSTAG + " output - " + output);

            //send Exit and close
            writer.write("EXIT\n", 0,5);
            writer.flush();
        } catch (IOException e) {
            Log.e("NetworkExplorer".toString(), " " + SimpleSocket.CLASSTAG + " IOException calling socket", e);
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
            }
            try {
                if (writer != null)
                    reader.close();
            } catch (IOException e) {
            }
        }
        return output;
    };


    @Override
    public void onClick(View view) {
        final Thread t1 = new Thread() {
            public void run() {
                try {
                    InetAddress serverAddr = InetAddress.getByName(ipAddress.getText().toString());
                    String line = callSocket(serverAddr, port.getText().toString(), socketInput.getText().toString());
                    Message lmsg = new Message();
                    lmsg.obj = line;
                    lmsg.what = 0;
                    SimpleSocket.this.handler.sendMessage(lmsg);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        };
        t1.start();
    }
}