package com.example.fakedataoffical;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CustomResponseCallBack{

    RecyclerView PubListView;
    MQTTHandler client;
    ArrayList<MessageHistory> MHList;
    RecyclerView_Adapter adapter;

    ScrollView MainLayout;
    RelativeLayout connectLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MainLayout=findViewById(R.id.ScrollToRecycle);
        connectLayout=findViewById(R.id.ConnectLayout);

        GetConnectInfo(new CustomResponseCallBack() {
            @Override
            public void OnResponse(Object obj) {

                if(CreateCLient((String)obj))
                {
                    connectLayout.setVisibility(View.GONE);
                    MainLayout.setVisibility(View.VISIBLE);
                    adapter.client=client;
                }
            }
        });
        //Create RecyclerView and config
        PubListView=findViewById(R.id.Publisher_recyclerView);
        MHList=new ArrayList<>();
        adapter=new RecyclerView_Adapter(this, client,MHList,this);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        PubListView.setLayoutManager(linearLayoutManager);
        PubListView.setDescendantFocusability(PubListView.FOCUS_BEFORE_DESCENDANTS);
        PubListView.setAdapter(adapter);


        Button AddTopicBTN=findViewById(R.id.AddTopicBTN);
        AddTopicBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("testF", "bfADD: "+adapter.getItemCount());
                int bfADD=adapter.getItemCount();
                MHList.add(new MessageHistory());
                adapter.notifyDataSetChanged();
                //adapter.notifyItemRangeChanged(bfADD,MHList.size());
                Log.d("testF", "atADD: "+adapter.getItemCount());
                for(int i=0;i<MHList.size();i++)
                {
                    Log.d("testF_List", "item "+i+" : "+adapter.MHList.get(i).Topic);
                }
            }
        });
    }

    void GetConnectInfo(CustomResponseCallBack CR)
    {
        EditText IPbox=findViewById(R.id.BrokerIPBox);
        Button ConnectBTN=findViewById(R.id.ConnectToMqttBTN);
        ConnectBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(IPbox.getText().toString()!="")
                {
                    String BrokerIP=IPbox.getText().toString();
                    CR.OnResponse(BrokerIP);
                }
            }
        });
    }
    public Boolean CreateCLient (String BrokerIP)
    {
        String ClientID= MqttClient.generateClientId();
        client=new MQTTHandler();
        return client.connect("tcp://"+BrokerIP+":1883",ClientID);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        client.disconnect();
    }

    /**
     * This Call back is use to delete an item in adapter IN THE RIGHT WAY.
     * it's Important to know how recyclerView work when it's come to its holders and views : basically, when you delete something from the "Need to be displayed list" and call the notifyDataSetChanged() it'll active sequentially onBinding() method but it gonna do some weird ASS SHIT that will FUCK your data up, what you want to do is calling notifyItemRemoved(position) so that it refresh the recyclerview without calling the onBinding() method
     * Now if your every individual view have 2 stage or more and u were planning to have them displayed at the first state when new created you might want to change the stage back to the 1st state after all the "delete protocol" we said above, this is duo to the fact that, recyclerview don't actually delete the view you just delete it just hide the view in some "invisible place", this is important because if you later add a new item in the list, the holderView that "you think" you just deleted before will be REUSED (NOT CREATE A NEW ONE) and there's nothing you can do about it, so if didn't change it back to the 1st state it will stay like it was when you last deleted it.
     * THIS IS STUPID - ME LOSING ALL FUCKING DAY JUST TO KNOW THIS FACT. So please be sure to read this "how recyclerview work" or "why my fucking data being change or data being misDisplayed" before attempting to seek any help on OpenStack because NO FUCKING ONE will tell you this. LIKE NO ONE AT ALL :)
     * YOU ARE WELCOME - B(09/04/2023)
     * @param obj this do nothing :)
     */
    @Override
    public void OnResponse(Object obj) {
        int pos=adapter.currentlyUsePOS;
        Log.d("testF", "DelPos: "+pos);
        if(pos>-1 && pos < MHList.size())
        {
            Log.d("testF", "bfDEL: "+adapter.getItemCount());
            MHList.remove(pos);
            adapter.notifyItemRemoved(pos);
            Log.d("testF", "atDEL: "+adapter.getItemCount());
            for(int i=0;i<MHList.size();i++)
            {
                Log.d("testF_List_del", "item "+i+" : "+adapter.MHList.get(i).Topic);
            }
        }
    }
}