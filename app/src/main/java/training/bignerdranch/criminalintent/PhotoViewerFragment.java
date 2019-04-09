package training.bignerdranch.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class PhotoViewerFragment extends DialogFragment {

    private static final String ARG_PHOTO = "photo";

    ImageView mZoomImageView;

    public static PhotoViewerFragment newInstance(File photoFile) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PHOTO, photoFile);

        PhotoViewerFragment fragment = new PhotoViewerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo, null);

        File imageFile = (File) getArguments().getSerializable(ARG_PHOTO);
        Bitmap bitmap = PictureUtils.getScaledBitmap(imageFile.getPath(), getActivity());

        mZoomImageView = view.findViewById(R.id.dialog_image_view);
        mZoomImageView.setImageBitmap(bitmap);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .create();
    }
}
