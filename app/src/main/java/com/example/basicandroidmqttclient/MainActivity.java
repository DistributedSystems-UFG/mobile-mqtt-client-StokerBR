package com.example.basicandroidmqttclient;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String brokerURI = "ec2-44-217-65-214.compute-1.amazonaws.com";

    Activity thisActivity;
    private List<MessageData> messageList;
    private ArrayAdapter<MessageData> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thisActivity = this;

        messageList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_2, android.R.id.text1, messageList);

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        subscribeLuminosity();
    }

    public void publishMessage(View view) {
        EditText minTime = (EditText) findViewById(R.id.minTime);
        EditText maxTime = (EditText) findViewById(R.id.maxTime);

        Log.d("Teste", "teste");

        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();
        client.publishWith()
                .topic("minTime")
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(minTime.getText().toString().getBytes())
                .send();
        client.publishWith()
                .topic("maxTime")
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(maxTime.getText().toString().getBytes())
                .send();
        client.disconnect();
    }

    private void subscribeLuminosity() {

        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();

        client.toAsync().subscribeWith()
                .topicFilter("luminosity")
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(msg -> {
                    thisActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                // Obter a mensagem JSON
                                String jsonString = new String(msg.getPayloadAsBytes(), StandardCharsets.UTF_8);
                                JSONObject jsonObject = new JSONObject(jsonString);

                                Log.d("Teste", jsonString);

                                // Extrair os valores de "luminosity" e "time"
                                String luminosity = jsonObject.getString("luminosity");
                                String time = jsonObject.getString("time");

                                MessageData messageData = new MessageData(luminosity, time);
                                adapter.insert(messageData, 0);  // Insere no início da lista
                                adapter.notifyDataSetChanged();

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                })
                .send();
    }

}