package com.example.fakedataoffical;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageBox_RecyclerviewAdapter extends RecyclerView.Adapter<MessageBox_RecyclerviewAdapter.MessageBoxViewHolder> {
    Context mContext;
    CustomResponseCallBack CR_Del, CRTC;
    public ArrayList<JsonPropertyMinimal> MessageControlList;
    public int CurrentMessageBox;
    public MessageBox_RecyclerviewAdapter(Context context,CustomResponseCallBack crdel,CustomResponseCallBack crTC)
    {
        this.mContext=context;
        this.CR_Del = crdel;
        this.CRTC =crTC;
        this.MessageControlList =new ArrayList<>();
    }
    public void addMessageBox()
    {
        this.MessageControlList.add(new JsonPropertyMinimal("",""));
        notifyItemInserted(getItemCount());
    }
    public void State1(MessageBoxViewHolder holder)
    {
        if(holder!=null)
        {
            holder.MessageBox_NAME.setText("");
            holder.MessageBox_NAME.setHint("Name");
            holder.MessageBox_VALUE.setText("");
            holder.MessageBox_VALUE.setHint("Value");
        }
    }
    public void ChangeHintToLastValue(MessageBoxViewHolder holder)
    {
        if(holder!=null)
        {
            if(!MessageControlList.get(holder.getAdapterPosition()).VALUE.equals(""))holder.MessageBox_VALUE.setHint(MessageControlList.get(holder.getAdapterPosition()).VALUE);
            holder.MessageBox_VALUE.setText("");
        }
    }
    public void ChangeHintToString(MessageBoxViewHolder holder,String newHint)
    {
        if(holder!=null)
        {
            holder.MessageBox_VALUE.setText("");
            holder.MessageBox_VALUE.setHint(newHint);
        }
    }
    public void ChangeHintToJsonMinimal(MessageBoxViewHolder holder,JsonPropertyMinimal newHint)
    {
        JsonPropertyMinimal NewHint= newHint.CreateDeepClone();
        if(holder!=null)
        {
            if(!NewHint.NAME.equals(""))holder.MessageBox_NAME.setHint(NewHint.NAME);
            holder.MessageBox_NAME.setText("");
            if(!NewHint.VALUE.equals(""))holder.MessageBox_VALUE.setHint(NewHint.VALUE);
            holder.MessageBox_VALUE.setText("");
        }
    }

    public void SetNameBoxEnable(MessageBoxViewHolder holder,boolean mode)
    {
        holder.MessageBox_NAME.setEnabled(mode);
    }
    public void SetValueBoxEnable(MessageBoxViewHolder holder,boolean mode)
    {
        holder.MessageBox_VALUE.setEnabled(mode);
    }
    public void SetDelButtonEnable(MessageBoxViewHolder holder,boolean mode)
    {
        holder.MessageBox_DEL.setEnabled(mode);
    }

    @NonNull
    @Override
    public MessageBoxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.message_box,parent,false);
        return new MessageBoxViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MessageBoxViewHolder holder, int position) {
        holder.MessageBox_NAME.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MessageControlList.get(holder.getAdapterPosition()).NAME=s.toString().trim();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        holder.MessageBox_VALUE.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MessageControlList.get(holder.getAdapterPosition()).VALUE=s.toString().trim();
                CRTC.OnResponse(null);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        holder.MessageBox_DEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                State1(holder);
                CurrentMessageBox=holder.getAdapterPosition();
                CR_Del.OnResponse(null);
            }
        });
    }
    @Override
    public int getItemCount() {
        return MessageControlList.size();
    }

    public class MessageBoxViewHolder  extends RecyclerView.ViewHolder{
        EditText MessageBox_NAME,MessageBox_VALUE;
        Button MessageBox_DEL;
        public MessageBoxViewHolder(@NonNull View itemView) {
            super(itemView);
            MessageBox_NAME=itemView.findViewById(R.id.Message_Name);
            MessageBox_VALUE=itemView.findViewById(R.id.Message_Value);
            MessageBox_DEL=itemView.findViewById(R.id.Message_Del);
        }
    }
}
