package com.eventwise.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.eventwise.items.ImageListItem;
import com.eventwise.R;

import java.util.ArrayList;

/**
 * takes images and binds them to view holders
 * Reference link: https://www.androidauthority.com/how-to-build-an-image-gallery-app-718976/
 */

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.ViewHolder> {

    private static ArrayList<ImageListItem> galleryList;
    private static Context context;
    private static ImageGalleryAdapter galleryAdapterInstance;
    private static OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(int position, ImageListItem item);
    }

    /**
     * ImageGalleryAdapter constructor.
     * @param context
     *   context is where the adapter is being used
     * @param galleryList
     *  galleryList An ArrayList of ImageListItem objects representing the images to display.
     */
    public ImageGalleryAdapter(Context context, ArrayList<ImageListItem> galleryList) {
        this.galleryList = galleryList;
        this.context = context;

    }

  
    public void setOnImageClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }


    @NonNull
    @Override
    public ImageGalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_cell_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    
    @Override
    public void onBindViewHolder(ImageGalleryAdapter.ViewHolder viewHolder, int i) {
        viewHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        String url = galleryList.get(i).getImageUrl();
        if (url != null && !url.isEmpty()) {
            Glide.with(viewHolder.itemView.getContext())
                    .load(url)
                    .centerCrop()
                    .into(viewHolder.image);
        } else {
            viewHolder.image.setImageBitmap(galleryList.get(i).getImage());
        }
    }

    // Returns the number of items in the gallery
    @Override
    public int getItemCount() {
        return galleryList.size();
    }

  //Remove an image from gallery.
    public void removeImage(ImageListItem imageItem){
        galleryList.remove(imageItem);
        notifyDataSetChanged();
    }

    // Creates the ViewHolder for iteration through images
    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView image;
        public ViewHolder(View view) {
            super(view);

            image = view.findViewById(R.id.image);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getBindingAdapterPosition();
                    ImageListItem item = galleryList.get(position);
                    if (listener != null) {
                        listener.onImageClick(position, item);
                    }
                }
            });

        }
    }

 
    public static ImageGalleryAdapter getInstance() {
        if (galleryAdapterInstance == null) {
            galleryAdapterInstance = new ImageGalleryAdapter(context,galleryList);
        }
        return galleryAdapterInstance;
    }


}
