package com.example.literise.utils;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.literise.R;

public class CustomToast {

    public static void showSuccess(Activity activity, String message) {
        showToast(activity, message, R.drawable.ic_success, R.color.color_success_bg);
    }

    public static void showError(Activity activity, String message) {
        showToast(activity, message, R.drawable.ic_error, R.color.color_error_bg);
    }

    public static void showInfo(Activity activity, String message) {
        showToast(activity, message, R.drawable.ic_info, R.color.color_info_bg);
    }

    public static void showWarning(Activity activity, String message) {
        showToast(activity, message, R.drawable.ic_warning, R.color.color_warning_bg);
    }

    private static void showToast(Activity activity, String message, int iconRes, int bgColorRes) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, null);

        ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView text = layout.findViewById(R.id.toast_text);
        View background = layout.findViewById(R.id.toast_background);

        icon.setImageResource(iconRes);
        text.setText(message);
        background.setBackgroundTintList(activity.getColorStateList(bgColorRes));

        Toast toast = new Toast(activity);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }
}