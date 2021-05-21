package com.example.localiser;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.localiser.domains.MyVideo;
import com.example.localiser.domains.VideoViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import edmt.dev.videoplayer.VideoPlayerRecyclerView;
import edmt.dev.videoplayer.adapter.VideoPlayerRecyclerAdapter;
import edmt.dev.videoplayer.model.MediaObject;
import edmt.dev.videoplayer.utils.VerticalSpacingItemDecorator;

public class VideoActivity extends AppCompatActivity {
    RecyclerView recyclerView;

    private FirebaseAuth auth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_video);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(auth.getCurrentUser().getUid()).child("videos");
        recyclerView = findViewById(R.id.recycler_view_re);
        init();


    }

    @SuppressLint("ResourceType")
    private void init() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //   VerticalSpacingItemDecorator verticalSpacingItemDecorator = new VerticalSpacingItemDecorator(10);
        // recyclerView.addItemDecoration(verticalSpacingItemDecorator);
       /* Query query = reference;
        ArrayList<MediaObject> mediaObjects = new ArrayList<>();
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    MediaObject mediaObject = new MediaObject(ds.getKey(),
                            ds.child("video").getValue(String.class),
                            "",
                            "");
                    mediaObjects.add(mediaObject);
                }
                recyclerView.setMediaObjects(mediaObjects);
                VideoPlayerRecyclerAdapter adapter = new VideoPlayerRecyclerAdapter(mediaObjects,initGlide());
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<MyVideo, VideoViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter
                <MyVideo, VideoViewHolder>(MyVideo.class, R.layout.row_video,VideoViewHolder.class,reference
                ) {
            @Override
            protected void populateViewHolder(VideoViewHolder videoViewHolder, MyVideo myVideo, int i) {
                videoViewHolder.setVideo(getApplication(),myVideo.getThumb(),myVideo.getVideo());
            }
        };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private RequestManager initGlide () {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.white_background)
                    .error(R.drawable.white_background);
            return Glide.with(this).setDefaultRequestOptions(options);
        }


}
