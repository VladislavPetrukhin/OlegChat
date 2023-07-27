package com.oleg.olegchat;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;



public class MessageAdapter extends ArrayAdapter<Message> {

    private List<Message> messages;
    private Activity activity;
    private Context context;

    public MessageAdapter(Activity context, int resource, List<Message> messages) {
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

        Message message = getItem(position);
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
        switch (message.getMessageType()){
            case "text":{
                viewHolder.messageTextView.setVisibility(View.VISIBLE);
                viewHolder.photoImageView.setVisibility(View.GONE);
                viewHolder.timeTextView.setVisibility(View.VISIBLE);
                viewHolder.imageTimeTextView.setVisibility(View.GONE);
                viewHolder.videoView.setVisibility(View.GONE);
                viewHolder.videoTimeTextView.setVisibility(View.GONE);
                viewHolder.messageTextView.setText(message.getText());
                viewHolder.timeTextView.setText(convertDate(message.getDate()));
                break;
            }
            case "image":{
                viewHolder.messageTextView.setVisibility(View.GONE);
                viewHolder.photoImageView.setVisibility(View.VISIBLE);
                viewHolder.timeTextView.setVisibility(View.GONE);
                viewHolder.imageTimeTextView.setVisibility(View.VISIBLE);
                viewHolder.videoView.setVisibility(View.GONE);
                viewHolder.videoTimeTextView.setVisibility(View.GONE);
                Glide.with(viewHolder.photoImageView.getContext())
                        .load(message.getUrl()).into(viewHolder.photoImageView);
                viewHolder.imageTimeTextView.setText(convertDate(message.getDate()));
                break;
            }
            case "audio":{ // TODO
                break;
            }
            case "video":{
                viewHolder.messageTextView.setVisibility(View.GONE);
                viewHolder.photoImageView.setVisibility(View.GONE);
                viewHolder.timeTextView.setVisibility(View.GONE);
                viewHolder.imageTimeTextView.setVisibility(View.GONE);
                viewHolder.videoView.setVisibility(View.VISIBLE);
                viewHolder.videoTimeTextView.setVisibility(View.VISIBLE);
                viewHolder.videoView.setVideoURI(Uri.parse(message.getUrl()));
                MediaController mediaController = new MediaController(context);
                mediaController.setAnchorView(viewHolder.videoView);
                viewHolder.videoView.setMediaController(mediaController);
                viewHolder.videoTimeTextView.setText(convertDate(message.getDate()));
                viewHolder.videoView.start();
                viewHolder.videoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(viewHolder.videoPlay){
                            viewHolder.videoView.pause();
                        }else{
                            viewHolder.videoView.start();
                        }
                    }
                });
                break;
            }
            case "voice":{// TODO
                break;
            }
            default:{
                viewHolder.messageTextView.setVisibility(View.VISIBLE);
                viewHolder.photoImageView.setVisibility(View.GONE);
                viewHolder.messageTextView.setText(message.getText());
            }
        }

        viewHolder.photoImageView.setClipToOutline(true);
        //nameTextView.setText(message.getName());

        viewHolder.photoImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PhotoViewerActivity.class);
                intent.putExtra("image_resource", message.getUrl());
                intent.putExtra("message_id", message.getMessage_id());
                context.startActivity(intent);
            }
        });
        viewHolder.messageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view, message);
            }
        });


         return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        int flag;
        Message message = messages.get(position);
        if (message.isMine()){
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
      private PhotoView photoImageView;
      private TextView timeTextView;
      private TextView imageTimeTextView;
      private TextView videoTimeTextView;
      private VideoView videoView;
      private Boolean videoPlay=false;

        public ViewHolder(View view){
            photoImageView = view.findViewById(R.id.photoImageView);
            messageTextView = view.findViewById(R.id.messageTextView);
            timeTextView = view.findViewById(R.id.messageTimeTextView);
            imageTimeTextView = view.findViewById(R.id.imageTimeTextView);
            videoView = view.findViewById(R.id.videoView);
            videoTimeTextView = view.findViewById(R.id.videoTimeTextView);
        }

    }
    private void showPopupMenu(View view, Message message) {
        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.message_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.messageCopyButton:
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("text", message.getText());
                        clipboard.setPrimaryClip(clip);
//                        Toast.makeText(context, "Copied",
//                                Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.messageReplyButton:

                        return true;
                    case R.id.messageDeleteButton:

                        return true;
                    case R.id.messageEditButton:

                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }
    private String convertDate(String dateTime){
        String date = dateTime.split("T")[0];
        String time = dateTime.split("T")[1].substring(0,5);
        int timeDif=0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.systemDefault());
            int myZone=0;
            int messageZone =0;
            if (zonedDateTime.toString().contains("+")){
                myZone = Integer.parseInt(zonedDateTime.toString().split("\\+")[1].substring(0,2));
            }else{
                myZone = -(Integer.parseInt(zonedDateTime.toString().split("-")[1].substring(0,2)));
            }
            if(zonedDateTime.toString().contains("+")){
                messageZone = Integer.parseInt(dateTime.split("\\+")[1].substring(0,2));
            }else{
                messageZone = -(Integer.parseInt(dateTime.split("-")[1].substring(0,2)));
            }
            Log.d("DateTimeLog","myZone: "+myZone);
            Log.d("DateTimeLog","messageZone: "+messageZone);
            timeDif = myZone - messageZone;
        }
        time = String.valueOf((Integer.parseInt(time.substring(0,2))+timeDif)) + time.substring(2,5);
        Log.d("DateTimeLog","time: "+time);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneId.systemDefault());
            if (!currentDateTime.toString().split("T")[0].equals(date)){
                Log.d("DateTimeLog","year: "+date.substring(0,4));
                if (currentDateTime.toString().substring(0,4).equals(date.substring(0,4))){
                    return date.substring(5,10).replace("-",".") +" "+time;
                }else{
                    return date.replace("-",".") + " " + time;
                }
            }else{
                return time;
            }
        }
        return time;
    }

}
