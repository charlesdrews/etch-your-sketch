package com.charlesdrews.etchyoursketch.gallery;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.charlesdrews.etchyoursketch.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class GalleryActivity extends AppCompatActivity
        implements GalleryRvAdapter.OnGalleryItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        ArrayList<File> files = new ArrayList<>();
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().matches(".*\\.png$");
            }
        };
        files.addAll(Arrays.asList(getFilesDir().listFiles(filter)));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.gallery_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(new GalleryRvAdapter(files, this));
    }

    @Override
    public void onGalleryItemSelected(File selectedImage) {
        GallerySelectionDialog.newInstance(selectedImage)
                .show(getSupportFragmentManager(), "galleryDialog");
    }
}
