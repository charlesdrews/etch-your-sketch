package com.charlesdrews.etchyoursketch.gallery;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.charlesdrews.etchyoursketch.R;

import java.io.File;
import java.util.List;

/**
 * Created by charlie on 1/5/17.
 */

public class GalleryRvAdapter extends RecyclerView.Adapter<GalleryViewHolder> {

    private List<File> mFiles;
    private OnGalleryItemSelectedListener mListener;

    public GalleryRvAdapter(List<File> files, OnGalleryItemSelectedListener listener) {
        mFiles = files;
        mListener = listener;
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_entry, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GalleryViewHolder holder, int position) {
        final File selectedImage = mFiles.get(position);
        holder.setImage(selectedImage);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onGalleryItemSelected(selectedImage);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public interface OnGalleryItemSelectedListener {
        void onGalleryItemSelected(File selectedImage);
    }
}
