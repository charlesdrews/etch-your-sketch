package com.charlesdrews.etchyoursketch.gallery;

import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.charlesdrews.etchyoursketch.R;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by charlie on 1/5/17.
 */

public class GalleryViewHolder extends RecyclerView.ViewHolder {

    private ImageView mImageView;

    public GalleryViewHolder(View itemView) {
        super(itemView);
        mImageView = (ImageView) itemView.findViewById(R.id.gallery_entry_image);
    }

    public void setImage(File file) {
        Picasso.with(mImageView.getContext()).load(file).into(mImageView);
    }
}
