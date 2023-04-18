package com.example.fakedataoffical;

import android.util.Log;

import com.google.gson.JsonObject;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MQTTHandler {

    public MqttClient client;

    public Boolean connect(String brokerUrl, String clientId) {
        try {
            // Set up the persistence layer
            MemoryPersistence persistence = new MemoryPersistence();

            // Initialize the MQTT client
            client = new MqttClient(brokerUrl, clientId, persistence);

            // Set up the connection options
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

            // Connect to the broker
            client.connect(connectOptions);
            Log.e("MqttConnection", "Success");
            return true;

        } catch (MqttException e) {
            Log.e("MqttConnection", e.getCause().getMessage());
            return false;
        }
    }

    public void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            Log.e("MqttPub", e.fillInStackTrace()+"");
        }
    }
    public JsonObject publish(String topic, ArrayList<JsonPropertyMinimal> theRawMessage) {
        try {
            JsonObject JsonMessage=ConvertingRawMessageToJson(theRawMessage);
            MqttMessage mqttMessage = new MqttMessage(JsonMessage.toString().getBytes(StandardCharsets.UTF_8));
            client.publish(topic, mqttMessage);
            return JsonMessage;
        } catch (MqttException e) {
            Log.e("MqttPub", e.fillInStackTrace()+"");
            return null;
        }
    }
    public JsonObject ConvertingRawMessageToJson(ArrayList<JsonPropertyMinimal> raw)
    {
        if(raw==null) return null;
        JsonObject newJson=new JsonObject();
        for (JsonPropertyMinimal jsonProperty:
             raw) {
            newJson.addProperty(jsonProperty.NAME,jsonProperty.VALUE);
        }
        return newJson;
    }
    public void subscribe(String topic, CustomResponseCallBack CR) {
        try {
            client.subscribe(topic);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e("MqttConnection", "connectionLost: "+ cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    CR.OnResponse(message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d("MqttConnection", "deliveryCompleted");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
