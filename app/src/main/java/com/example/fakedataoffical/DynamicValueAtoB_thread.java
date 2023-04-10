package com.example.fakedataoffical;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

public class DynamicValueAtoB_thread extends Thread{
    String dataA,dataB,Topic;
    MQTTHandler client;
    MessageHistory MH;
    StaticValue_thread RunStatic;
    RecyclerView_Adapter.MessageViewHolder holder;
    CustomResponseCallBack CR;
    public DynamicValueAtoB_thread(String topic, String dataa, String datab, MQTTHandler UsingClient, MessageHistory mh, StaticValue_thread runStatic,RecyclerView_Adapter.MessageViewHolder holder,CustomResponseCallBack cr)
    {
        this.dataA=dataa;
        this.dataB=datab;
        this.Topic=topic;
        this.client=UsingClient;
        this.MH=mh;
        this.RunStatic=runStatic;
        this.holder=holder;
        this.CR=cr;
    }
    public void run ()
    {
        double dataToValueA=Double.valueOf(dataA);
        double dataToValueB=Double.valueOf(dataB);
        while (dataToValueA!=dataToValueB)
        {
            if(AOverB(dataToValueA,dataToValueB))
            {
                if(dataToValueA-dataToValueB>0.1) dataToValueA-=0.1;
                else dataToValueA=dataToValueB;
            }
            else
            {
                if(dataToValueB-dataToValueA>0.1) dataToValueA+=0.1;
                else dataToValueA=dataToValueB;
            }
            client.publish(Topic,String.valueOf(dataToValueA));
            MH.PublishedMessage.add(String.valueOf(dataToValueA));
            try
            {
                sleep(100);
            }catch (Exception ex){
                Log.e("SleepError", ex.getMessage());
            }
        }
        RunStatic.data=dataB;
        CR.OnResponse(null);
    }

    private boolean AOverB(double a,double b)
    {
        return a>b ? true : false;
    }
}
