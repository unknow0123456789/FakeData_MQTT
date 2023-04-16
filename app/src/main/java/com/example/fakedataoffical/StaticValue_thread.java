package com.example.fakedataoffical;

import android.util.Log;

public class StaticValue_thread extends Thread{

    public String Topic,data;
    public MQTTHandler client;
    public MessageHistory MH;
    private boolean flag;
    public StaticValue_thread (String tp,String dt,MQTTHandler client,MessageHistory mh)
    {
        this.Topic=tp;
        this.data=dt;
        this.client=client;
        this.MH=mh;
    }
    public void run()
    {
        flag=true;
        while(flag==true)
        {
            Log.d("testAsync", "Static_run: ");
            client.publish(Topic,data);
            //MH.PublishedMessage.add(data);
        }
    }
    public void StopSign()
    {
        this.flag=false;
    }
}
