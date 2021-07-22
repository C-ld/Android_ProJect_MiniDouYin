package com.example.project;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.project.model.Message;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.VideoViewHolder>{
    private List<Message> data;
    private Context mContext;
    public FeedAdapter(Context context) {
        this.mContext = context;
    }

    public void setData(List<Message> messageList){
        data = messageList;
        notifyDataSetChanged();
    }

    public List<Message> getData(){
        return data;
    }

    public Message getItem(int position){
        return data.get(position);
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    private OnItemClickListener listener;

    //第二步， 写一个公共的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View root =LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed,parent,false);
        return new VideoViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.bind(data.get(position));
        Glide.with(mContext)
                .load(data.get(position).getImageUrl())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(50)))
                .transition(withCrossFade())
                .into(holder.coverSD);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data==null?0:data.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder{
        private ImageView coverSD;
        private TextView username;
        private TextView update;
        //private TextView contentTV;
        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            update = itemView.findViewById(R.id.update);
            coverSD = itemView.findViewById(R.id.sd_cover);
            //contentTV = itemView.findViewById(R.id.tv_content);
        }
        public void bind(Message message){
            username.setText("From: "+message.getUsername());
            //contentTV.setText(message.getContent());
            update.setText("Updated at: "+message.getUpdatedAt());
        }

//        public void bind(Message message){
//            username.setText("From: "+message.getUsername());
//            //contentTV.setText(message.getContent());
//            update.setText("Updated at: "+message.getUpdatedAt());
//        }
    }

}
