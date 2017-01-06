package com.charlesdrews.etchyoursketch.gallery;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by charlie on 1/5/17.
 */

public class GallerySelectionDialog extends DialogFragment {

    private static final String SELECTED_IMAGE_KEY = "selectedImage";

    private static final float IMAGE_WIDTH_AS_PERCENT_OF_SCREEN = 1f;

    private File mSelectedImage;

    public static GallerySelectionDialog newInstance(File selectedImage) {
        GallerySelectionDialog dialog = new GallerySelectionDialog();

        Bundle args = new Bundle();
        args.putSerializable(SELECTED_IMAGE_KEY, selectedImage);
        dialog.setArguments(args);

        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        // Set up image view
        ImageView imageView = new ImageView(getContext());

        int width = (int) (getResources().getDisplayMetrics().widthPixels
                * IMAGE_WIDTH_AS_PERCENT_OF_SCREEN);
        imageView.setLayoutParams(new ActionBar.LayoutParams(width,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setScaleType(ImageView.ScaleType.FIT_START);
        imageView.setAdjustViewBounds(true);

        // Get file and load into image view
        mSelectedImage = (File) getArguments().getSerializable(SELECTED_IMAGE_KEY);
        Picasso.with(getContext()).load(mSelectedImage).into(imageView);

        builder.setView(imageView);
        return builder.create();
    }
}
