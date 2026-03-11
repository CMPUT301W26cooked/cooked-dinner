package com.eventwise;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * takes images and binds them to view holders
 * Reference link: https://www.androidauthority.com/how-to-build-an-image-gallery-app-718976/
 */

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.ViewHolder> {

    private static ArrayList<ImageListItem> galleryList;
    private static Context context;
    private static ImageGalleryAdapter galleryAdapterInstance;

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

    @NonNull
    @Override
    public ImageGalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(ImageGalleryAdapter.ViewHolder viewHolder, int i) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public ViewHolder(View view) {
            super(view);
        }
    }
}
