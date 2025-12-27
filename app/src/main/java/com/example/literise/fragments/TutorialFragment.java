package com.example.literise.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.literise.R;

public class TutorialFragment extends Fragment {

    private static final String ARG_EMOJI = "emoji";
    private static final String ARG_TITLE = "title";
    private static final String ARG_DESCRIPTION = "description";

    private String emoji;
    private String title;
    private String description;

    public static TutorialFragment newInstance(String emoji, String title, String description) {
        TutorialFragment fragment = new TutorialFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMOJI, emoji);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_DESCRIPTION, description);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            emoji = getArguments().getString(ARG_EMOJI);
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

        TextView tvLeoEmoji = view.findViewById(R.id.tvLeoEmoji);
        TextView tvTutorialTitle = view.findViewById(R.id.tvTutorialTitle);
        TextView tvTutorialDescription = view.findViewById(R.id.tvTutorialDescription);

        tvLeoEmoji.setText(emoji);
        tvTutorialTitle.setText(title);
        tvTutorialDescription.setText(description);
    }
}
