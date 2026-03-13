package com.eventwise;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;



// Refernce Link: https://www.androidauthority.com/how-to-build-an-image-gallery-app-718976/
// TODO JAVADOCS
public class ImageGalleryBuilder {


    private static Context appContext;
    public ImageGalleryBuilder(Context context){
        this.appContext = context;
    }


    // Populate gallery with images
    public void populateGallery(ArrayList<Event> events, RecyclerView galleryRecyclerView, Context context){

        //set up the RecyclerView for the gallery
        galleryRecyclerView.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context,3);
        galleryRecyclerView.setLayoutManager(gridLayoutManager);

        ArrayList<ImageListItem> listOfImages = updateImageList(events);

        ImageGalleryAdapter galleryListAdapter = new ImageGalleryAdapter(context, listOfImages);
        galleryRecyclerView.setAdapter(galleryListAdapter);
        galleryListAdapter.notifyDataSetChanged();
    }



    // Update the list of images to be displayed in the gallery
    public static ArrayList<ImageListItem> updateImageList(ArrayList<Event> events){

        ArrayList<ImageListItem> listOfImages = new ArrayList<>();
        for (Event event : events) {
            String posterPath = event.getPosterPath();
            if (posterPath == null || posterPath.isEmpty()) {
                continue;
            }
            File imageFile = new File(appContext.getFilesDir(), posterPath);
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

                ImageListItem imageListItem = new ImageListItem();

                // Set the image
                imageListItem.setImage(bitmap);
                imageListItem.setStoragePath(posterPath);
                imageListItem.setEventId(event.getEventId());
                listOfImages.add(imageListItem);
            }
        }

        return listOfImages;
    }



}
