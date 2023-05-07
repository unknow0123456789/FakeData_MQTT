package com.example.fakedataoffical;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class StaticValue_thread extends Thread{

    public String Topic;
    public ArrayList<JsonPropertyMinimal> data;
    public MQTTHandler client;
    public MessageHistory MH;
    private boolean flag;
    public StaticValue_thread (String tp,ArrayList<JsonPropertyMinimal> dt,MQTTHandler client,MessageHistory mh)
    {
        this.Topic=tp;
        this.data=new ArrayList<>();
        for (JsonPropertyMinimal ori:
             dt) {
            data.add(ori.CreateDeepClone());
        }
        this.client=client;
        this.MH=mh;
    }
    public void run()
    {
        flag=true;
        while(flag==true)
        {
            client.publish(Topic,data);
            for(JsonPropertyMinimal JPM : data)
                Log.d("testDataA", JPM.NAME+" : "+JPM.VALUE);
            //MH.PublishedMessage.add(data);
            try{
                TimeUnit.SECONDS.sleep(5);
            }
            catch (Exception ex)
            {

            }
        }
    }
    public void StopSign()
    {
        this.flag=false;
    }

    public StaticValue_thread CreateDeepClone()
    {
        return new StaticValue_thread(Topic,data,client,MH);
    }
}
