package com.example.literise.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.literise.R;

public class TutorialFragment extends Fragment {

    private static final String ARG_IMAGE_RES = "image_res";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";

    private int imageRes;
    private String title;
    private String description;

    public static TutorialFragment newInstance(int imageRes, String title, String description) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_IMAGE_RES, imageRes);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageRes = getArguments().getInt(ARG_IMAGE_RES);
            title = getArguments().getString(ARG_TITLE);
            description = getArguments().getString(ARG_DESCRIPTION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tutorial_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView ivLeoImage = view.findViewById(R.id.ivLeoImage);
        TextView tvTutorialTitle = view.findViewById(R.id.tvTutorialTitle);
        TextView tvTutorialDescription = view.findViewById(R.id.tvTutorialDescription);

        ivLeoImage.setImageResource(imageRes);
        tvTutorialTitle.setText(title);
        tvTutorialDescription.setText(description);
    }
}
