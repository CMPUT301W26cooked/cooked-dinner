package com.eventwise.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eventwise.R;
import com.eventwise.database.AdminDatabaseManager;
import com.eventwise.database.EntrantDatabaseManager;
import com.eventwise.items.AdminImageItem;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        //Display Poster
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.eventImage);

        //Display User Name
        //Dont know how to feel about this as it pulls from the database each time creating a small delay when updating...
        AdminDatabaseManager adminDBManager = new AdminDatabaseManager();
        adminDBManager.getProfileFromId(item.getImgUploadingOrg())
                .addOnSuccessListener(profile -> {
                    if (profile == null) {
                        holder.imgUploadingOrg.setText("Unknown User");
                        return;
                    } else {
                        holder.imgUploadingOrg.setText(profile.getName());
                    }})
                .addOnFailureListener(e->{
                    holder.imgUploadingOrg.setText("Unknown User");
                    Log.d("AdminImages", "Error getting entrant: " + e.getMessage());
                });



        //Display Timestamp
        //Try catch needed since url might be invalid and crash app
        try {
            StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(item.getImageUrl());
            ref.getMetadata().addOnSuccessListener(metadata -> {
                long timeCreatedMs = metadata.getCreationTimeMillis();
                String timestamp = new SimpleDateFormat("MMM d yyyy, h:mm a", Locale.getDefault())
                        .format(new Date(timeCreatedMs));
                holder.timestampString.setText(timestamp);
            }).addOnFailureListener(e -> {
                holder.timestampString.setText("Unknown Time");
                Log.d("AdminImages", "Failed to get metadata", e);
            });
        }
        catch (Exception e) {
            holder.timestampString.setText("Unknown Time");
            Log.d("AdminImages", "Failed to get metadata", e);
            return;
        }





        holder.imgUploadingOrg.setText(item.getImgUploadingOrg());
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
