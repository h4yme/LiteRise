package com.example.literise.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.literise.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RegisterStep4Fragment extends Fragment {

    private EditText etEmail, etPassword, etConfirmPassword;
    private CheckBox cbTerms;
    private TextView tvTermsLabel;
    private StepValidationListener validationListener;

    public interface StepValidationListener {
        void onValidationChanged(boolean isValid);
    }

    public void setValidationListener(StepValidationListener listener) {
        this.validationListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_step4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail           = view.findViewById(R.id.etEmail);
        etPassword        = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        cbTerms           = view.findViewById(R.id.cbTerms);
        tvTermsLabel      = view.findViewById(R.id.tvTermsLabel);

        setupTermsLabel();

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateStep(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(watcher);
        etPassword.addTextChangedListener(watcher);
        etConfirmPassword.addTextChangedListener(watcher);
        cbTerms.setOnCheckedChangeListener((btn, checked) -> validateStep());

        // Keyboard navigation
        etEmail.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) { etPassword.requestFocus(); return true; }
            return false;
        });
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) { etConfirmPassword.requestFocus(); return true; }
            return false;
        });

        // Scroll to focused field
        etEmail.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) v.post(() -> v.getParent().requestChildFocus(v, v)); });
        etPassword.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) v.post(() -> v.getParent().requestChildFocus(v, v)); });
        etConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> { if (hasFocus) v.post(() -> v.getParent().requestChildFocus(v, v)); });
    }

    // ─── Terms label with clickable link ──────────────────────────────────────

    private void setupTermsLabel() {
        String full  = "I have read and agree to the Terms and Conditions";
        String link  = "Terms and Conditions";
        int start    = full.indexOf(link);
        int end      = start + link.length();

        SpannableString spannable = new SpannableString(full);

        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showTermsDialog();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(0xFF6C5CE7);   // purple
                ds.setUnderlineText(true);
                ds.setFakeBoldText(true);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTermsLabel.setText(spannable);
        tvTermsLabel.setMovementMethod(LinkMovementMethod.getInstance());
        tvTermsLabel.setHighlightColor(Color.TRANSPARENT);
    }

    // ─── Terms & Conditions dialog ────────────────────────────────────────────

    private void showTermsDialog() {
        // Scrollable content view
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.setFillViewport(true);

        TextView tvContent = new TextView(requireContext());
        int pad = (int) (16 * requireContext().getResources().getDisplayMetrics().density);
        tvContent.setPadding(pad, pad, pad, pad);
        tvContent.setTextSize(14f);
        tvContent.setTextColor(0xFF2D3436);
        tvContent.setLineSpacing(6f, 1f);
        try {
            Typeface tf = ResourcesCompat.getFont(requireContext(), R.font.visby_regular);
            if (tf != null) tvContent.setTypeface(tf);
        } catch (Exception ignored) {}

        tvContent.setText(buildTermsText());
        scrollView.addView(tvContent);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Terms & Conditions")
                .setView(scrollView)
                .setPositiveButton("I Agree", (dialog, which) -> {
                    cbTerms.setChecked(true);
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String buildTermsText() {
        return
                "By registering an account on LITERISE, you agree to the following:\n\n" +

                        "1. Eligibility\n" +
                        "LITERISE is designed for Grade 3 learners. Account registration must be completed or " +
                        "approved by a parent, guardian, or authorized school representative.\n\n" +

                        "2. Account Information\n" +
                        "You agree to provide accurate and complete information during registration. You are " +
                        "responsible for keeping login credentials secure and for all activity under the account.\n\n" +

                        "3. Educational Purpose\n" +
                        "LITERISE is intended for educational use only. Users agree to use the application for " +
                        "learning activities and not for unlawful, harmful, or disruptive behavior.\n\n" +

                        "4. Data & Progress Tracking\n" +
                        "The application uses assessment methods, including Item Response Theory (IRT), to track " +
                        "learner progress and improve learning outcomes. By registering, you consent to the " +
                        "collection and use of academic performance data for educational purposes.\n\n" +

                        "5. Account Suspension\n" +
                        "We reserve the right to suspend or terminate accounts that violate these Terms or " +
                        "misuse the application.\n\n" +

                        "6. Changes to Terms\n" +
                        "These Terms may be updated as needed. Continued use of LITERISE constitutes acceptance " +
                        "of any revisions.\n\n" +

                        "By creating an account, you confirm that you have read and agreed to these Terms & Conditions.";
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    private void validateStep() {
        if (validationListener != null) {
            validationListener.onValidationChanged(isValid());
        }
    }

    public boolean isValid() {
        String email   = etEmail.getText().toString().trim();
        String pass    = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        return !email.isEmpty()
                && email.contains("@")
                && pass.length() >= 6
                && pass.equals(confirm)
                && cbTerms.isChecked();
    }

    public String getEmail() {
        return etEmail.getText().toString().trim();
    }

    public String getPassword() {
        return etPassword.getText().toString().trim();
    }
}
