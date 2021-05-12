package com.example.localiser.domains;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.localiser.R;

import java.util.List;

public class AudioAdapter extends BaseAdapter {
   private Context context;
   private List<MyAudio> players;

    public AudioAdapter(Context context, List<MyAudio> players) {
        this.context = context;
        this.players = players;
    }

    @Override
    public int getCount() {
        return players.size();
    }

    @Override
    public Object getItem(int position) {
        return players.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        @SuppressLint("ViewHolder")
        View v = View.inflate(context , R.layout.audio_layout_list , null);
        TextView name = (TextView) v.findViewById(R.id.audio_txt);
        ImageView imageView = (ImageView) v.findViewById(R.id.audio_img);
        name.setText(players.get(position).getName());
        v.setTag(players.get(position).getName());
        return v;
    }
}
