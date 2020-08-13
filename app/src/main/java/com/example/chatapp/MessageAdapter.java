package com.example.chatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<BaseMessage> mMessageList;
    private final int SENT = 1;
    private final int RECEIVED = 2;
    String name;

    public MessageAdapter(ArrayList<BaseMessage> list, String clientName) {
        mMessageList = list;
        name = clientName;
    }

    private String getMessage(int position) {
        return mMessageList.get(position).getMessage();
    }

    private String getTime(int position) {
        return mMessageList.get(position).getTime();
    }

    private String getStatus(int position) {
        return mMessageList.get(position).getStatus();
    }

    @Override
    public int getItemViewType(int position) {
        if(getStatus(position).equals("sent"))
            return SENT;
        return RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = null;
        switch (i) {
            case SENT:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.messages_client, viewGroup, false);
                return new ViewHolderSent(v);

            case RECEIVED:
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.received_messages, viewGroup, false);
                return new ViewHolderReceived(v);

        }

        return new ViewHolderSent(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        switch (viewHolder.getItemViewType()) {
            case SENT:
                ViewHolderSent vh = (ViewHolderSent) viewHolder;
                vh.messageView.setText(getMessage(i));
                vh.timeView.setText(getTime(i));
                break;

            case RECEIVED:
                ViewHolderReceived vh1 = (ViewHolderReceived) viewHolder;
                vh1.messageView.setText(getMessage(i));
                vh1.timeView.setText(getTime(i));
                vh1.clientName.setText(name);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolderSent extends RecyclerView.ViewHolder {
        TextView messageView, timeView;
        public ViewHolderSent(View v) {
            super(v);
            messageView = v.findViewById(R.id.text_message);
            timeView = v.findViewById(R.id.text_message_time);
        }
    }

    public static class ViewHolderReceived extends RecyclerView.ViewHolder {
        TextView messageView, timeView, clientName;

        public ViewHolderReceived(@NonNull View itemView) {
            super(itemView);
            messageView = itemView.findViewById(R.id.text_message_body);
            timeView = itemView.findViewById(R.id.server_message_time);
            clientName = itemView.findViewById(R.id.text_message_name);
        }
    }
}
