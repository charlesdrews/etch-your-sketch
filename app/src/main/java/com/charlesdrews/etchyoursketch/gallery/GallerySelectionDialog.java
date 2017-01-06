package com.charlesdrews.etchyoursketch.gallery;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.charlesdrews.etchyoursketch.R;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by charlie on 1/5/17.
 */

public class GallerySelectionDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "GallerySelectionDialog";

    private static final String SELECTED_IMAGE_KEY = "selectedImage";
    private static final float IMAGE_WIDTH_AS_PERCENT_OF_SCREEN = 1f;

    private OnGalleryActionListener mListener;
    private File mSelectedFile;
    private FrameLayout mContainer;

    public static GallerySelectionDialog newInstance(File selectedImage) {
        GallerySelectionDialog dialog = new GallerySelectionDialog();

        Bundle args = new Bundle();
        args.putSerializable(SELECTED_IMAGE_KEY, selectedImage);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSelectedFile = (File) getArguments().getSerializable(SELECTED_IMAGE_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.gallery_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mContainer = (FrameLayout) view.findViewById(R.id.gallery_dialog_container);

        ImageView image = (ImageView) view.findViewById(R.id.gallery_dialog_image);
        Picasso.with(getContext()).load(mSelectedFile).into(image);

        view.findViewById(R.id.gallery_dialog_close).setOnClickListener(this);
        view.findViewById(R.id.gallery_dialog_delete).setOnClickListener(this);
        view.findViewById(R.id.gallery_dialog_share).setOnClickListener(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnGalleryActionListener) {
            mListener = (OnGalleryActionListener) context;
        } else {
            throw new ClassCastException(context.getClass().getName()
                    + " must implement " + OnGalleryActionListener.class.getName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Window window = getDialog().getWindow();
        if (window != null) {
            int newWidth = (int) (getResources().getDisplayMetrics().widthPixels
                    * IMAGE_WIDTH_AS_PERCENT_OF_SCREEN);
            mContainer.setMinimumWidth(newWidth);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gallery_dialog_close:
                dismiss();
                break;

            case R.id.gallery_dialog_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.delete_confirmation_title)
                        .setNegativeButton(R.string.delete_neg_button, null)
                        .setPositiveButton(R.string.delete_pos_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mListener.onGalleryItemDeleted(mSelectedFile);
                                GallerySelectionDialog.this.dismiss();
                            }
                        })
                        .show();
                break;

            case R.id.gallery_dialog_share:
                mListener.onGalleryItemShared(mSelectedFile);
                break;
        }
    }

    public interface OnGalleryActionListener {
        void onGalleryItemDeleted(File fileToDelete);
        void onGalleryItemShared(File fileToShare);
    }
}
