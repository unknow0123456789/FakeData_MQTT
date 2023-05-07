package com.example.fakedataoffical;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DynamicValueAtoB_thread extends Thread{
    String Topic;
    ArrayList<JsonPropertyMinimal> dataA,dataB;
    MQTTHandler client;
    MessageHistory MH;
    StaticValue_thread RunStatic;
    RecyclerView_Adapter.MessageViewHolder holder;
    CustomResponseCallBack CR;
    public DynamicValueAtoB_thread(String topic, ArrayList<JsonPropertyMinimal> dataa, ArrayList<JsonPropertyMinimal> datab, MQTTHandler UsingClient, MessageHistory mh, StaticValue_thread runStatic, RecyclerView_Adapter.MessageViewHolder holder, CustomResponseCallBack cr)
    {
        this.dataA=new ArrayList<>();
        for (JsonPropertyMinimal JPM:
                dataa) {
            dataA.add(JPM.CreateDeepClone());
        }
        this.dataB=new ArrayList<>();
        for (JsonPropertyMinimal JPM:
                datab) {
            dataB.add(JPM.CreateDeepClone());
        }
        this.Topic=topic;
        this.client=UsingClient;
        this.MH=mh;
        this.RunStatic=runStatic;
        this.holder=holder;
        this.CR=cr;
    }
    public void run ()
    {
        double ChangeStep=0.5;
        ArrayList<Double> dataToValueA=new ArrayList<>();
        for (JsonPropertyMinimal JPM:
             dataA) {
            dataToValueA.add(Double.valueOf(JPM.VALUE));
        }
        ArrayList<Double> dataToValueB=new ArrayList<>();
        for (JsonPropertyMinimal JPM:
                dataB) {
            dataToValueB.add(Double.valueOf(JPM.VALUE));
        }
        while (!dataToValueA.equals(dataToValueB))
        {
            for(int i=0;i<dataA.size();i++)
            {
                if(AOverB(dataToValueA.get(i),dataToValueB.get(i)))
                {
                    if(dataToValueA.get(i)-dataToValueB.get(i)>ChangeStep) dataToValueA.set(i,dataToValueA.get(i)-ChangeStep);
                    else dataToValueA.set(i,dataToValueB.get(i));
                }
                else
                {
                    if(dataToValueB.get(i)-dataToValueA.get(i)>ChangeStep) dataToValueA.set(i,dataToValueA.get(i)+ChangeStep);
                    else dataToValueA.set(i,dataToValueB.get(i));
                }
            }
            ConvertDoubleListBACKToJsonPropertyList(dataA,dataToValueA);
            client.publish(Topic,dataA);
            //MH.PublishedMessage.add(String.valueOf(dataToValueA));
            try
            {
                TimeUnit.SECONDS.sleep(5);
            }catch (Exception ex){
                Log.e("SleepError", ex.getMessage());
            }
        }
        RunStatic.data=dataB;
        //testing-----
        StaticValue_thread newContinue=RunStatic.CreateDeepClone();
        newContinue.data=dataB;
        RunStatic=newContinue.CreateDeepClone();
        //testing-----
        for(JsonPropertyMinimal JPM:dataB) Log.d("testDataB", JPM.NAME+" : "+JPM.VALUE);
        CR.OnResponse(null);
    }
    private void ConvertDoubleListBACKToJsonPropertyList(ArrayList<JsonPropertyMinimal> original,ArrayList<Double> rawlist)
    {
        for(int i=0;i<original.size();i++)
        {
            JsonPropertyMinimal temp=original.get(i);
            temp.VALUE=String.valueOf(rawlist.get(i));
            original.set(i,temp);
        }
    }
    private boolean AOverB(double a,double b)
    {
        return a>b ? true : false;
    }
}
