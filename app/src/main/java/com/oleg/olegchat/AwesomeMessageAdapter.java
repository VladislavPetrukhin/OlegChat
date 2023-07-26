package com.oleg.olegchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.firework.imageloading.glide.GlideImageLoaderFactory;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;



public class AwesomeMessageAdapter extends ArrayAdapter<AwesomeMessage> {

    private List<AwesomeMessage> messages;
    private Activity activity;
    private Context context;

    public AwesomeMessageAdapter(Activity context, int resource, List<AwesomeMessage> messages) {
        super(context, resource, messages);
        this.messages = messages;
        this.context = context;
        this.activity = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        LayoutInflater layoutInflater =
                (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        AwesomeMessage awesomeMessage = getItem(position);
        int layoutResource = 0;
        int viewType = getItemViewType(position);

        if(viewType == 0){
            layoutResource = R.layout.my_message_item;
        }else{
            layoutResource = R.layout.your_message_item;
        }

        if(convertView != null){
            viewHolder = (ViewHolder) convertView.getTag();
        }else{
            convertView = layoutInflater.inflate(layoutResource,parent,false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        switch (awesomeMessage.getMessageType()){
            case "text":{
                viewHolder.messageTextView.setVisibility(View.VISIBLE);
                viewHolder.photoImageView.setVisibility(View.GONE);
                viewHolder.messageTextView.setText(awesomeMessage.getText());
                break;
            }
            case "image":{
                viewHolder.messageTextView.setVisibility(View.GONE);
                viewHolder.photoImageView.setVisibility(View.VISIBLE);
                Glide.with(viewHolder.photoImageView.getContext())
                        .load(awesomeMessage.getUrl()).into(viewHolder.photoImageView);
                break;
            }
            case "audio":{ // TODO
                break;
            }
            case "video":{// TODO
                break;
            }
            case "voice":{// TODO
                break;
            }
            default:{
                viewHolder.messageTextView.setVisibility(View.VISIBLE);
                viewHolder.photoImageView.setVisibility(View.GONE);
                viewHolder.messageTextView.setText(awesomeMessage.getText());
            }
        }

        viewHolder.photoImageView.setClipToOutline(true);
        //nameTextView.setText(message.getName());

        viewHolder.photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PhotoViewerActivity.class);
                intent.putExtra("image_resource", awesomeMessage.getUrl());
                intent.putExtra("message_id",awesomeMessage.getMessage_id());
                context.startActivity(intent);
            }
        });

         return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        int flag;
        AwesomeMessage awesomeMessage = messages.get(position);
        if (awesomeMessage.isMine()){
            flag = 0;
        }else{
            flag = 1;
        }
        return flag;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    private class ViewHolder{

        private TextView messageTextView;
      //  private PhotoView photoImageView;
      private PhotoView photoImageView;

        public ViewHolder(View view){
            photoImageView = view.findViewById(R.id.photoImageView);
            messageTextView = view.findViewById(R.id.messageTextView);
        }

    }

}
