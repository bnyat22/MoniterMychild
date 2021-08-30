package com.example.localiserwear.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.localiserwear.R;
import com.example.localiserwear.domains.MyMessage;

import java.util.List;

public class MessageAdapter extends BaseAdapter {
    private Context context;
    private List<MyMessage> messages;

    public MessageAdapter(Context context, List<MyMessage> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        @SuppressLint("ViewHolder")
        View v = View.inflate(context , R.layout.message_layout_list , null);
        TextView title =  v.findViewById(R.id.message_title);
        TextView body =  v.findViewById(R.id.message_body);

        title.setText(messages.get(position).getTitle());
        body.setText(messages.get(position).getBody());
        v.setTag(messages.get(position).getTitle());
        v.setTag(messages.get(position).getBody());
        return v;
    }
}
