package com.example.literise.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.example.literise.R;

import java.util.ArrayList;
import java.util.List;

public class LeoDialogueView extends RelativeLayout {

    private ImageView ivLeoAvatar;
    private TextView tvDialogue;
    private View cvSpeechBubble;
    private View llTapToContinue;
    private FrameLayout flHighlightContainer;

    private List<DialogueMessage> dialogueMessages;
    private int currentMessageIndex = 0;
    private DialogueCompleteListener dialogueCompleteListener;

    public interface DialogueCompleteListener {
        void onDialogueComplete();
    }

    public static class DialogueMessage {
        String text;
        int avatarRes;
        View highlightView; // Optional: view to highlight

        public DialogueMessage(String text) {
            this(text, R.drawable.leo_happy, null);
        }

        public DialogueMessage(String text, int avatarRes) {
            this(text, avatarRes, null);
        }

        public DialogueMessage(String text, int avatarRes, View highlightView) {
            this.text = text;
            this.avatarRes = avatarRes;
            this.highlightView = highlightView;
        }
    }

    public LeoDialogueView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.leo_dialogue_view, this, true);

        ivLeoAvatar = findViewById(R.id.ivLeoAvatar);
        tvDialogue = findViewById(R.id.tvDialogue);
        cvSpeechBubble = findViewById(R.id.cvSpeechBubble);
        llTapToContinue = findViewById(R.id.llTapToContinue);
        flHighlightContainer = findViewById(R.id.flHighlightContainer);

        dialogueMessages = new ArrayList<>();

        // Set click listener for tap to continue on the entire view
        this.setOnClickListener(v -> showNextMessage());

        // Also set click listener on the speech bubble to ensure clicks work
        cvSpeechBubble.setOnClickListener(v -> showNextMessage());
    }

    public void setDialogueMessages(List<DialogueMessage> messages) {
        this.dialogueMessages = messages;
        this.currentMessageIndex = 0;
        showCurrentMessage();
    }

    public void setDialogueCompleteListener(DialogueCompleteListener listener) {
        this.dialogueCompleteListener = listener;
    }

    private void showCurrentMessage() {
        if (currentMessageIndex >= dialogueMessages.size()) {
            return;
        }

        DialogueMessage message = dialogueMessages.get(currentMessageIndex);

        // Update avatar
        ivLeoAvatar.setImageResource(message.avatarRes);

        // Animate avatar (simple scale animation)
        Animation scaleAnim = AnimationUtils.loadAnimation(getContext(), R.anim.scale_bounce);
        ivLeoAvatar.startAnimation(scaleAnim);

        // Update dialogue text
        tvDialogue.setText(message.text);

        // Animate speech bubble
        Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
        cvSpeechBubble.startAnimation(slideUp);

        // Show tap to continue
        llTapToContinue.setVisibility(View.VISIBLE);

        // Handle highlight if present
        if (message.highlightView != null) {
            showHighlight(message.highlightView);
        } else {
            hideHighlight();
        }
    }

    private void showNextMessage() {
        currentMessageIndex++;

        if (currentMessageIndex < dialogueMessages.size()) {
            showCurrentMessage();
        } else {
            // All messages shown, complete dialogue
            if (dialogueCompleteListener != null) {
                dialogueCompleteListener.onDialogueComplete();
            }
            dismiss();
        }
    }

    private void showHighlight(View targetView) {
        // TODO: Implement spotlight/highlight overlay
        flHighlightContainer.setVisibility(View.VISIBLE);
        // This will highlight a specific UI element while Leo talks about it
    }

    private void hideHighlight() {
        flHighlightContainer.setVisibility(View.GONE);
    }

    public void show(ViewGroup parent) {
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
        parent.addView(this);
        this.setVisibility(View.VISIBLE);

        // Start with first message
        showCurrentMessage();
    }

    public void dismiss() {
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                LeoDialogueView.this.setVisibility(View.GONE);
                if (getParent() != null) {
                    ((ViewGroup) getParent()).removeView(LeoDialogueView.this);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        this.startAnimation(fadeOut);
    }
}
