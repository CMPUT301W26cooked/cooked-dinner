package com.eventwise.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eventwise.R;
import com.eventwise.items.AdminImageItem;

import java.util.List;

public class AdminImagesAdapter extends RecyclerView.Adapter<AdminImagesAdapter.ViewHolder> {

    private final List<AdminImageItem> images;

    public interface OnDeleteClickListener {
        void onDeleteClicked(AdminImageItem item);
    }


    private final OnDeleteClickListener listener;
    public AdminImagesAdapter(List<AdminImageItem> images, OnDeleteClickListener listener) {
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.widget_admin_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminImageItem item = images.get(position);

        holder.imageTitle.setText(item.getImageTitle());
        holder.eventImage.setImageURI(Uri.fromFile(item.getImageFile()));
        holder.imgUploadingOrg.setText(item.getImgUploadingOrg());
        holder.timestampString.setText(item.getTimestampString());
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClicked(item));

    }

    @Override
    public int getItemCount() {
        return images.size();
    }



    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView imageTitle;
        ImageView eventImage;

        TextView imgUploadingOrg;

        TextView timestampString;

        Button deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageTitle = itemView.findViewById(R.id.image_title);
            eventImage = itemView.findViewById(R.id.event_image);
            imgUploadingOrg = itemView.findViewById(R.id.img_uploading_org);
            timestampString = itemView.findViewById(R.id.timestampString);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
