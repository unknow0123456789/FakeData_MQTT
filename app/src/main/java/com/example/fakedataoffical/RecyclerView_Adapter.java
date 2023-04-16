package com.example.fakedataoffical;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerView_Adapter extends RecyclerView.Adapter<RecyclerView_Adapter.MessageViewHolder> {

    ArrayList<MessageHistory> MHList;
    Context mContext;
    MQTTHandler client;
    CustomResponseCallBack CR;
    public int currentlyUsePOS;

    public RecyclerView_Adapter(Context mContext, MQTTHandler mqttHandler, ArrayList<MessageHistory> mhlist,CustomResponseCallBack cr) {
        this.mContext = mContext;
        this.MHList=mhlist;
        this.CR=cr;
        this.client=mqttHandler;
        RecyclerView test;

    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_card,parent,false);
        return new MessageViewHolder(view);
    }

    /**
     * this will be recalled if you use "notifyDataSetChange()" and will not if you instead use "notifyItemRemoved(position)"
     */
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Log.d("testF", "bindingRuningat: "+position);
        MessageHistory MH=MHList.get(holder.getAdapterPosition());
        Log.d("testF", "holder.getAdapterPos: "+holder.getAdapterPosition());
        if (MH!=null)
        {
            Log.d("testF", "running pass if null: "+holder.getAdapterPosition());
            if(MH.Topic!=null)
            {
                ToStage2(holder);
                MH.Topic=holder.TopicTitle.getText().toString();
                for(int i=0;i<MHList.size();i++)
                {
                    Log.d("testF_List", "item "+i+" : "+MHList.get(i).Topic);
                }
            }
            else
                holder.LockTopicBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.TopicTitle.getText().toString()!="")
                    {
                        ToStage2(holder);
                        MH.Topic=holder.TopicTitle.getText().toString();
                        for(int i=0;i<MHList.size();i++)
                        {
                            Log.d("testF_List", "item "+i+" : "+MHList.get(i).Topic);
                        }
                    }
                }
            });
            holder.PubBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String Message=holder.inputMessage.getText().toString();
                    Log.e("testF_EditText", Message);
                    client.publish(MH.Topic, Message);
                    MH.PublishedMessage.add(Message);
                    holder.inputMessage.setText("");
                }
            });
            holder.DelBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(CR!=null)
                    {
                        currentlyUsePOS=holder.getAdapterPosition();
                        Log.d("testF", "CUPOSchange: "+currentlyUsePOS);
                        if(currentlyUsePOS!=RecyclerView.NO_POSITION)
                        {
                            ReverseStage1(holder);
                            CR.OnResponse(null);
                        }
                    }
                }
            });
        }
        holder.PubABTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.PubABTN.getText().equals("PubA"))
                {
                    if(holder.staticValue_thread==null)
                    {
                        holder.AsyncFlag=true;
                        String RepeatMessage=holder.inputMessage.getText().toString();
                        holder.inputMessage.setText("");
                        holder.DelBTN.setEnabled(false);
                        holder.PubBTN.setEnabled(false);
                        holder.PubABTN.setText("Stop");
                        holder.staticValue_thread=new StaticValue_thread(MH.Topic,RepeatMessage,client,MH);
                        holder.staticValue_thread.start();
                    }
                    else
                    {
                        String dataA=holder.staticValue_thread.data;
                        String dataB=holder.inputMessage.getText().toString();
                        holder.staticValue_thread.StopSign();
                        holder.PubBTN.setEnabled(false);
                        holder.DelBTN.setEnabled(false);
                        holder.PubABTN.setEnabled(false);
                        holder.inputMessage.setText("StandBy while Data is being Shifted");
                        holder.inputMessage.setEnabled(false);
                        DynamicValueAtoB_thread dynamicValueAtoB_thread=new DynamicValueAtoB_thread(MH.Topic, dataA, dataB, client, MH, holder.staticValue_thread, holder, new CustomResponseCallBack() {
                            @Override
                            public void OnResponse(Object obj) {
                                ((Activity)mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        holder.inputMessage.setEnabled(true);
                                        holder.PubBTN.setEnabled(false);
                                        holder.DelBTN.setEnabled(false);
                                        holder.PubABTN.setEnabled(true);
                                        holder.PubABTN.setText("Stop");
                                        holder.inputMessage.setText("");
                                    }
                                });
                                holder.staticValue_thread.run();
                            }
                        });
                        dynamicValueAtoB_thread.start();
                    }
                }
                else
                {
                    holder.AsyncFlag=false;
                    holder.staticValue_thread.StopSign();
                    holder.DelBTN.setEnabled(true);
                    holder.PubBTN.setEnabled(true);
                    holder.PubABTN.setText("PubA");
                    holder.staticValue_thread=null;
                }
            }
        });
        holder.inputMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(holder.AsyncFlag==true)
                {
                    if (s.toString().trim().isEmpty()) {
                        holder.PubABTN.setText("Stop");
                    }
                    else holder.PubABTN.setText("PubA");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return MHList.size();
    }


    public void ToStage2(MessageViewHolder holder)
    {
        holder.TopicTitle.setText(holder.TopicTitle.getText());
        holder.TopicTitle.setEnabled(false);
        holder.LockTopicBTN.setVisibility(View.GONE);
        holder.messageBoxes.setVisibility(View.VISIBLE);
        holder.DelBTN.setVisibility(View.VISIBLE);
        holder.PubBTN.setVisibility(View.VISIBLE);
        holder.PubABTN.setVisibility(View.VISIBLE);
    }

    private void ReverseStage1(MessageViewHolder holder)
    {
        holder.TopicTitle.setText("");
        holder.TopicTitle.setEnabled(true);
        holder.LockTopicBTN.setVisibility(View.VISIBLE);
        holder.messageBoxes.setVisibility(View.GONE);
        holder.DelBTN.setVisibility(View.GONE);
        holder.PubBTN.setVisibility(View.GONE);
        holder.PubABTN.setVisibility(View.GONE);
    }
    public class MessageViewHolder extends RecyclerView.ViewHolder {
        EditText TopicTitle;
        RecyclerView messageBoxes;
        Button PubBTN, DelBTN, LockTopicBTN,PubABTN;
        StaticValue_thread staticValue_thread;
        boolean AsyncFlag;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            TopicTitle=itemView.findViewById(R.id.Card_TopicBox);
            messageBoxes=itemView.findViewById(R.id.Card_RecyclerMessageBox);
            PubBTN=itemView.findViewById(R.id.Card_PublishBTN);
            DelBTN=itemView.findViewById(R.id.Card_DeleteCardBTN);
            PubABTN=itemView.findViewById(R.id.Card_PubAsyncBTN);
            LockTopicBTN =itemView.findViewById(R.id.Card_LockBTN);
            AsyncFlag=false;
        }
    }
}
