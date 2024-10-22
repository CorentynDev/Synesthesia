package com.example.synesthesia.utilities;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.synesthesia.R;

public class ImagesUtils {

    /**
     * Upload image with Glide, with a default image if the URL is empty or null.
     *
     * @param context the application context
     * @param imageUrl the URL to upload
     * @param imageView the ImageView to place the image
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_image);
        }
    }
}
