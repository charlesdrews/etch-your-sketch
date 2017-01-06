package com.charlesdrews.etchyoursketch.gallery;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.charlesdrews.etchyoursketch.EtchActivity;
import com.charlesdrews.etchyoursketch.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

public class GalleryActivity extends AppCompatActivity implements
        GalleryRvAdapter.OnGalleryItemSelectedListener,
        GallerySelectionDialog.OnGalleryActionListener {

    private static final String TAG = "GalleryActivity";
    private static final int DESIRED_MIN_COLUMN_WIDTH_PX = 500;
    
    private GalleryRvAdapter mAdapter;

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
        File path = new File(getFilesDir(), "images");
        if (path.exists()) {
            files.addAll(Arrays.asList(path.listFiles(filter)));
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.gallery_recycler_view);

        int numColumns = getResources().getDisplayMetrics().widthPixels / DESIRED_MIN_COLUMN_WIDTH_PX;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numColumns));

        mAdapter = new GalleryRvAdapter(files, this);
        recyclerView.setAdapter(mAdapter);

        //TODO - add itemdecorator for better spacing
    }

    @Override
    public void onGalleryItemSelected(File selectedImage) {
        GallerySelectionDialog.newInstance(selectedImage)
                .show(getSupportFragmentManager(), "galleryDialog");
    }

    @Override
    public void onGalleryItemDeleted(File fileToDelete) {
        if (fileToDelete.delete()) {
            mAdapter.removeFile(fileToDelete);
        }
        //TODO - snackbar w/ undo?
    }

    @Override
    public void onGalleryItemShared(File fileToShare) {
        if (fileToShare != null && fileToShare.exists()) {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");

            Uri uri = FileProvider.getUriForFile(this, EtchActivity.FILE_PROVIDER_AUTHORITY, fileToShare);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));

            startActivity(Intent.createChooser(intent, getString(R.string.share_chooser_title)));
        } else {
            Toast.makeText(this, R.string.share_fail_message, Toast.LENGTH_SHORT).show();
        }
    }
}
