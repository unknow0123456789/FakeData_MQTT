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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class RecyclerView_Adapter extends RecyclerView.Adapter<RecyclerView_Adapter.MessageViewHolder> implements CustomResponseCallBack{

    ArrayList<MessageHistory> MHList;
    Context mContext;
    MQTTHandler client;
    CustomResponseCallBack CR;
    MessageBox_RecyclerviewAdapter MessageBoxAdapter;
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

        //MessageBox_Config
        MessageBoxAdapter=new MessageBox_RecyclerviewAdapter(mContext, this, new CustomResponseCallBack() {
            @Override
            public void OnResponse(Object obj) {
                if(holder.AsyncFlag==true)
                {
                    ArrayList<JsonPropertyMinimal> SampleList=new ArrayList<>();
                    for (JsonPropertyMinimal JPM:
                         MessageBoxAdapter.MessageControlList) {
                        SampleList.add(JPM.CreateDeepClone());
                    }
                    boolean CheckChangeFlag=false;
                    for(int i=0;i<SampleList.size();i++)
                    {
                        JsonPropertyMinimal SampleFromStaticThreadAtI=holder.staticValue_thread.data.get(i).CreateDeepClone();
                        if(!SampleList.get(i).VALUE.equals("")&&!SampleList.get(i).VALUE.equals(SampleFromStaticThreadAtI.VALUE))
                        {
                            CheckChangeFlag=true;
                        }
                    }
                    if(CheckChangeFlag==true)
                    {
                        ArrayList<JsonPropertyMinimal> CloneListA=new ArrayList<>();
                        for (JsonPropertyMinimal JPM:
                                holder.staticValue_thread.data) {
                            CloneListA.add(JPM.CreateDeepClone());
                        }
                        for(int t=0;t<CloneListA.size();t++)
                        {
                            if(!SampleList.get(t).VALUE.equals(""))
                            {
                                SampleList.get(t).NAME=CloneListA.get(t).NAME;
                                CloneListA.set(t,SampleList.get(t));
                            }
                        }
                        holder.DataB=CloneListA;
                        holder.PubABTN.setText("PubA");
                    }
                    else
                        holder.PubABTN.setText("Stop");
                }
            }
        });

        //reset to new set of MessageBox
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(mContext,RecyclerView.VERTICAL,false);
        holder.messageBoxes.setLayoutManager(linearLayoutManager);
        holder.messageBoxes.setDescendantFocusability(holder.messageBoxes.FOCUS_BEFORE_DESCENDANTS); //TODO:SHOULD USE IF RECYCLERVIEW CONTAIN AN EDIT TEXT
        holder.messageBoxes.setAdapter(MessageBoxAdapter);
        holder.ADDMessageBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageBoxAdapter.addMessageBox();
            }
        });
        //MessageBox_EndConfig

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
                    ArrayList<JsonPropertyMinimal> MessageFull=MessageBoxAdapter.MessageControlList;
                    MH.PublishedMessage.add(
                            client.publish(MH.Topic, MessageFull)
                            );
                    ResetMessageBoxesState(holder);
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
                        ArrayList<JsonPropertyMinimal> Message=MessageBoxAdapter.MessageControlList;

                        //UI CONTROL
                        holder.DelBTN.setEnabled(false);
                        holder.PubBTN.setEnabled(false);
                        SetEnableAllMessageBox_Name(holder,false);
                        SetEnableAllMessageBox_Del(holder,false);
                        holder.ADDMessageBox.setEnabled(false);
                        holder.PubABTN.setText("Stop");
                        //----------------------


                        holder.staticValue_thread=new StaticValue_thread(MH.Topic,Message,client,MH);
                        holder.staticValue_thread.start();
                        ChangeMessageBoxesHint(holder,Message);
                    }
                    else
                    {
                        ArrayList<JsonPropertyMinimal> dataA=new ArrayList<>();
                        for (JsonPropertyMinimal JPM:
                                holder.staticValue_thread.data) {
                            dataA.add(JPM.CreateDeepClone());
                        }
                        ArrayList<JsonPropertyMinimal> dataB=holder.DataB;
                        holder.staticValue_thread.StopSign();

                        //UI control
                        holder.PubABTN.setEnabled(false);
                        SetEnableAllMessageBox_Value(holder,false);
                        ChangeMessageBoxesHint(holder,"StandBy while Data is being Shifted");
                        //---------
                        DynamicValueAtoB_thread dynamicValueAtoB_thread=new DynamicValueAtoB_thread(MH.Topic, dataA, dataB, client, MH, holder.staticValue_thread, holder, new CustomResponseCallBack() {
                            @Override
                            public void OnResponse(Object obj) {

                                holder.staticValue_thread.start();
                                ((Activity)mContext).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d("testUIRUN", "UIchange_Running");
                                        holder.PubABTN.setEnabled(true);
                                        SetEnableAllMessageBox_Value(holder,true);
                                        holder.PubABTN.setText("Stop");
                                        ChangeMessageBoxesHint(holder,holder.staticValue_thread.data);
                                    }
                                });
                            }
                        });
                        dynamicValueAtoB_thread.start();
                    }
                }
                else
                {
                    holder.AsyncFlag=false;
                    holder.staticValue_thread.StopSign();
                    //UI control
                    holder.DelBTN.setEnabled(true);
                    holder.PubBTN.setEnabled(true);
                    SetEnableAllMessageBox_Name(holder,true);
                    SetEnableAllMessageBox_Del(holder,true);
                    holder.ADDMessageBox.setEnabled(true);
                    holder.PubABTN.setText("PubA");
                    //----------------
                    holder.staticValue_thread=null;
                }
            }
        });
//        holder.inputMessage.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if(holder.AsyncFlag==true)
//                {
//                    if (s.toString().trim().isEmpty()) {
//                        holder.PubABTN.setText("Stop");
//                    }
//                    else holder.PubABTN.setText("PubA");
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });
    }
    private void ResetMessageBoxesState(MessageViewHolder holder)
    {
        for (int i=0;i<MessageBoxAdapter.getItemCount();i++)
        {
            MessageBox_RecyclerviewAdapter.MessageBoxViewHolder VH=(MessageBox_RecyclerviewAdapter.MessageBoxViewHolder) holder.messageBoxes.findViewHolderForAdapterPosition(i);
            MessageBoxAdapter.State1(VH);
        }
    }
    public  void SetEnableAllMessageBox_Name(MessageViewHolder holder, boolean mode)
    {
        for (int i=0;i<MessageBoxAdapter.getItemCount();i++)
        {
            MessageBox_RecyclerviewAdapter.MessageBoxViewHolder VH=(MessageBox_RecyclerviewAdapter.MessageBoxViewHolder) holder.messageBoxes.findViewHolderForAdapterPosition(i);
            MessageBoxAdapter.SetNameBoxEnable(VH,mode);
        }
    }
    public  void SetEnableAllMessageBox_Value(MessageViewHolder holder, boolean mode)
    {
        for (int i=0;i<MessageBoxAdapter.getItemCount();i++)
        {
            MessageBox_RecyclerviewAdapter.MessageBoxViewHolder VH=(MessageBox_RecyclerviewAdapter.MessageBoxViewHolder) holder.messageBoxes.findViewHolderForAdapterPosition(i);
            MessageBoxAdapter.SetValueBoxEnable(VH,mode);
        }
    }
    public  void SetEnableAllMessageBox_Del(MessageViewHolder holder, boolean mode)
    {
        for (int i=0;i<MessageBoxAdapter.getItemCount();i++)
        {
            MessageBox_RecyclerviewAdapter.MessageBoxViewHolder VH=(MessageBox_RecyclerviewAdapter.MessageBoxViewHolder) holder.messageBoxes.findViewHolderForAdapterPosition(i);
            MessageBoxAdapter.SetDelButtonEnable(VH,mode);
        }
    }
    private void ChangeMessageBoxesHint(MessageViewHolder holder, String changeTo)
    {
        for (int i=0;i<MessageBoxAdapter.getItemCount();i++)
        {
            MessageBox_RecyclerviewAdapter.MessageBoxViewHolder VH=(MessageBox_RecyclerviewAdapter.MessageBoxViewHolder) holder.messageBoxes.findViewHolderForAdapterPosition(i);
            if(changeTo.equals(""))MessageBoxAdapter.ChangeHintToLastValue(VH);
            else MessageBoxAdapter.ChangeHintToString(VH,changeTo);
        }
    }
    private void ChangeMessageBoxesHint(MessageViewHolder holder, ArrayList<JsonPropertyMinimal> changeTo)
    {
        for (int i=0;i<MessageBoxAdapter.getItemCount();i++)
        {
            MessageBox_RecyclerviewAdapter.MessageBoxViewHolder VH=(MessageBox_RecyclerviewAdapter.MessageBoxViewHolder) holder.messageBoxes.findViewHolderForAdapterPosition(i);
            JsonPropertyMinimal temp= changeTo.get(i);
            MessageBoxAdapter.ChangeHintToJsonMinimal(VH,temp);
        }
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
        holder.ADDMessageBox.setVisibility(View.VISIBLE);
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
        holder.ADDMessageBox.setVisibility(View.GONE);
//        while(!MessageBoxAdapter.MessageControlList.isEmpty())
//        {
//            View viewToRemove = holder.messageBoxes.getChildAt(0);
//            holder.messageBoxes.removeView(viewToRemove);
//            MessageBoxAdapter.MessageControlList.remove(0);
//            MessageBoxAdapter.notifyItemRemoved(0);
//        }
//        MessageBoxAdapter=null;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        EditText TopicTitle;
        RecyclerView messageBoxes;
        Button PubBTN, DelBTN, LockTopicBTN,PubABTN,ADDMessageBox;
        StaticValue_thread staticValue_thread;
        boolean AsyncFlag;
        ArrayList<JsonPropertyMinimal> DataB;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            TopicTitle=itemView.findViewById(R.id.Card_TopicBox);
            messageBoxes=itemView.findViewById(R.id.Card_RecyclerMessageBox);
            PubBTN=itemView.findViewById(R.id.Card_PublishBTN);
            DelBTN=itemView.findViewById(R.id.Card_DeleteCardBTN);
            PubABTN=itemView.findViewById(R.id.Card_PubAsyncBTN);
            LockTopicBTN =itemView.findViewById(R.id.Card_LockBTN);
            ADDMessageBox=itemView.findViewById(R.id.Card_AddMessage);
            AsyncFlag=false;
        }
    }
    @Override
    public void OnResponse(Object obj) {
        if(obj==null)
        {
            int pos=MessageBoxAdapter.CurrentMessageBox;
            if(pos!=-1){
                MessageBoxAdapter.MessageControlList.remove(pos);
                MessageBoxAdapter.notifyItemRemoved(pos);
            }
        }
    }
}
