package com.example.literise.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import com.example.literise.R;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Generates and shares the LiteRise completion certificate.
 * Call {@link #generateAndShare} from any Activity — it reads the data
 * that was already passed in and draws a high-resolution bitmap.
 */
public class CertificateHelper {

    private static final String TAG = "CertificateHelper";

    /**
     * @param activity    Calling activity (needed for fonts, FileProvider, startActivity).
     * @param studentName Recipient's display name.
     * @param levelName   Achieved level (e.g. "Advanced").
     * @param finalTheta  Post-assessment theta value.
     * @param preTheta    Pre-assessment theta value (for growth calculation).
     * @param accuracy    Post-assessment accuracy (0–100).
     */
    public static void generateAndShare(Activity activity,
                                        String studentName,
                                        String levelName,
                                        double finalTheta,
                                        double preTheta,
                                        double accuracy) {
        try {
            // ── Canvas setup ────────────────────────────────────────────
            int width  = 1600;
            int height = 1130;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // ── Load fonts ──────────────────────────────────────────────
            Typeface poppinsBold     = ResourcesCompat.getFont(activity, R.font.poppins_bold);
            Typeface poppinsSemiBold = ResourcesCompat.getFont(activity, R.font.poppins_semibold);
            Typeface poppinsRegular  = ResourcesCompat.getFont(activity, R.font.poppins_regular);
            Typeface serifBold       = Typeface.create(Typeface.SERIF, Typeface.BOLD);
            Typeface serifItalic     = Typeface.create(Typeface.SERIF, Typeface.ITALIC);
            Typeface serifBoldItalic = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC);

            // ── Color palette ───────────────────────────────────────────
            int cBackground  = 0xFFFDF8F0;
            int cPurpleDark  = 0xFF3B0764;
            int cPurple      = 0xFF7C3AED;
            int cPurpleLight = 0xFFDDD6FE;
            int cPurpleMid   = 0xFFEDE9FE;
            int cGold        = 0xFFAD8A20;
            int cDarkText    = 0xFF1E1B2E;
            int cGrayText    = 0xFF6B7280;
            int cGreen       = 0xFF047857;

            // ── Background ──────────────────────────────────────────────
            android.graphics.Paint bgPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(cBackground);
            canvas.drawRect(0, 0, width, height, bgPaint);

            android.graphics.Paint glowPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            glowPaint.setShader(new android.graphics.RadialGradient(
                    width / 2f, height / 2f, width * 0.65f,
                    0x08C4B5FD, 0x00C4B5FD,
                    android.graphics.Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, width, height, glowPaint);

            // ── Faint diagonal watermark ─────────────────────────────────
            android.graphics.Paint wmPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            wmPaint.setColor(0x087C3AED);
            wmPaint.setTextSize(220f);
            if (poppinsBold != null) wmPaint.setTypeface(poppinsBold);
            wmPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.save();
            canvas.rotate(-28f, width / 2f, height / 2f);
            canvas.drawText("LiteRise", width / 2f, height / 2f + 70, wmPaint);
            canvas.restore();

            // ── Triple border ────────────────────────────────────────────
            android.graphics.Paint outerBorderPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            outerBorderPaint.setColor(cPurpleDark);
            outerBorderPaint.setStyle(android.graphics.Paint.Style.STROKE);
            outerBorderPaint.setStrokeWidth(22f);
            canvas.drawRoundRect(14, 14, width - 14, height - 14, 14, 14, outerBorderPaint);

            android.graphics.Paint goldBorderPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            goldBorderPaint.setColor(cGold);
            goldBorderPaint.setStyle(android.graphics.Paint.Style.STROKE);
            goldBorderPaint.setStrokeWidth(5f);
            canvas.drawRoundRect(30, 30, width - 30, height - 30, 8, 8, goldBorderPaint);

            android.graphics.Paint innerBorderPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            innerBorderPaint.setColor(cPurpleLight);
            innerBorderPaint.setStyle(android.graphics.Paint.Style.STROKE);
            innerBorderPaint.setStrokeWidth(2f);
            canvas.drawRoundRect(44, 44, width - 44, height - 44, 4, 4, innerBorderPaint);

            // ── Corner ornaments ─────────────────────────────────────────
            android.graphics.Paint cornerLinePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            cornerLinePaint.setColor(cGold);
            cornerLinePaint.setStyle(android.graphics.Paint.Style.STROKE);
            cornerLinePaint.setStrokeWidth(3.5f);
            android.graphics.Paint cornerDotPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            cornerDotPaint.setColor(cGold);
            cornerDotPaint.setStyle(android.graphics.Paint.Style.FILL);
            float cs = 60f, cp = 60f;
            canvas.drawLine(cp, cp + cs, cp, cp, cornerLinePaint);
            canvas.drawLine(cp, cp, cp + cs, cp, cornerLinePaint);
            canvas.drawLine(width - cp - cs, cp, width - cp, cp, cornerLinePaint);
            canvas.drawLine(width - cp, cp, width - cp, cp + cs, cornerLinePaint);
            canvas.drawLine(cp, height - cp - cs, cp, height - cp, cornerLinePaint);
            canvas.drawLine(cp, height - cp, cp + cs, height - cp, cornerLinePaint);
            canvas.drawLine(width - cp - cs, height - cp, width - cp, height - cp, cornerLinePaint);
            canvas.drawLine(width - cp, height - cp, width - cp, height - cp - cs, cornerLinePaint);
            canvas.drawCircle(cp, cp, 7, cornerDotPaint);
            canvas.drawCircle(width - cp, cp, 7, cornerDotPaint);
            canvas.drawCircle(cp, height - cp, 7, cornerDotPaint);
            canvas.drawCircle(width - cp, height - cp, 7, cornerDotPaint);

            // ── Shared paint helpers ─────────────────────────────────────
            android.graphics.Paint diamondFillPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            diamondFillPaint.setColor(cGold);
            diamondFillPaint.setStyle(android.graphics.Paint.Style.FILL);

            android.graphics.Paint rulePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            rulePaint.setColor(cGold);
            rulePaint.setStrokeWidth(2.5f);

            // ── Header: brand name ───────────────────────────────────────
            android.graphics.Path topDiamond = new android.graphics.Path();
            topDiamond.moveTo(width / 2f, 82f);
            topDiamond.lineTo(width / 2f + 14, 96f);
            topDiamond.lineTo(width / 2f, 110f);
            topDiamond.lineTo(width / 2f - 14, 96f);
            topDiamond.close();
            canvas.drawPath(topDiamond, diamondFillPaint);

            android.graphics.Paint brandPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            brandPaint.setColor(cPurpleDark);
            brandPaint.setTextSize(86f);
            if (poppinsBold != null) brandPaint.setTypeface(poppinsBold);
            brandPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("LiteRise", width / 2f, 185f, brandPaint);

            android.graphics.Paint programPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            programPaint.setColor(cGrayText);
            programPaint.setTextSize(30f);
            if (poppinsRegular != null) programPaint.setTypeface(poppinsRegular);
            programPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("E N G L I S H   R E A D I N G   P R O G R A M", width / 2f, 226f, programPaint);

            // ── Ornamental rule ──────────────────────────────────────────
            float ruleY = 258f;
            canvas.drawLine(90, ruleY, width / 2f - 32, ruleY, rulePaint);
            canvas.drawLine(width / 2f + 32, ruleY, width - 90, ruleY, rulePaint);
            android.graphics.Path midDiamond = new android.graphics.Path();
            midDiamond.moveTo(width / 2f, ruleY - 14);
            midDiamond.lineTo(width / 2f + 18, ruleY);
            midDiamond.lineTo(width / 2f, ruleY + 14);
            midDiamond.lineTo(width / 2f - 18, ruleY);
            midDiamond.close();
            canvas.drawPath(midDiamond, diamondFillPaint);

            // ── "Certificate of Completion" ──────────────────────────────
            android.graphics.Paint certTitlePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            certTitlePaint.setColor(cDarkText);
            certTitlePaint.setTextSize(74f);
            certTitlePaint.setTypeface(serifBold);
            certTitlePaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("Certificate of Completion", width / 2f, 348f, certTitlePaint);

            android.graphics.Paint thinLinePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            thinLinePaint.setColor(cPurpleLight);
            thinLinePaint.setStrokeWidth(2f);
            canvas.drawLine(width / 2f - 370, 366f, width / 2f + 370, 366f, thinLinePaint);

            // ── "This certifies that" ────────────────────────────────────
            android.graphics.Paint certifiesPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            certifiesPaint.setColor(cGrayText);
            certifiesPaint.setTextSize(34f);
            certifiesPaint.setTypeface(serifItalic);
            certifiesPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("This certifies that", width / 2f, 428f, certifiesPaint);

            // ── Student name ─────────────────────────────────────────────
            android.graphics.Paint namePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            namePaint.setColor(cPurple);
            namePaint.setTextSize(90f);
            namePaint.setTypeface(serifBoldItalic);
            namePaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText(studentName, width / 2f, 534f, namePaint);

            float nw = namePaint.measureText(studentName);
            android.graphics.Paint nameUnderPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            nameUnderPaint.setColor(cPurple);
            nameUnderPaint.setStrokeWidth(3f);
            canvas.drawLine(width / 2f - nw / 2 - 28, 554f,
                    width / 2f + nw / 2 + 28, 554f, nameUnderPaint);
            canvas.drawCircle(width / 2f - nw / 2 - 28, 554f, 6, cornerDotPaint);
            canvas.drawCircle(width / 2f + nw / 2 + 28, 554f, 6, cornerDotPaint);

            // ── Completion description ───────────────────────────────────
            android.graphics.Paint completedPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            completedPaint.setColor(cGrayText);
            completedPaint.setTextSize(32f);
            completedPaint.setTypeface(serifItalic);
            completedPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("has successfully completed the", width / 2f, 610f, completedPaint);

            android.graphics.Paint progNamePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            progNamePaint.setColor(cDarkText);
            progNamePaint.setTextSize(44f);
            if (poppinsSemiBold != null) progNamePaint.setTypeface(poppinsSemiBold);
            else progNamePaint.setFakeBoldText(true);
            progNamePaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("LiteRise English Reading Program", width / 2f, 668f, progNamePaint);

            // ── Level badge ──────────────────────────────────────────────
            android.graphics.RectF badgeRect = new android.graphics.RectF(
                    width / 2f - 270, 694f, width / 2f + 270, 756f);
            android.graphics.Paint badgeBgPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            badgeBgPaint.setColor(cPurpleMid);
            canvas.drawRoundRect(badgeRect, 32, 32, badgeBgPaint);
            android.graphics.Paint badgeBorderPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            badgeBorderPaint.setColor(cPurple);
            badgeBorderPaint.setStyle(android.graphics.Paint.Style.STROKE);
            badgeBorderPaint.setStrokeWidth(2.5f);
            canvas.drawRoundRect(badgeRect, 32, 32, badgeBorderPaint);
            android.graphics.Paint badgeTextPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            badgeTextPaint.setColor(cPurpleDark);
            badgeTextPaint.setTextSize(36f);
            if (poppinsSemiBold != null) badgeTextPaint.setTypeface(poppinsSemiBold);
            else badgeTextPaint.setFakeBoldText(true);
            badgeTextPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("\u2605  Level Achieved: " + levelName + "  \u2605", width / 2f, 737f, badgeTextPaint);

            // ── Stats row ────────────────────────────────────────────────
            double thetaDiff = finalTheta - preTheta;
            String sign = thetaDiff >= 0 ? "+" : "";
            android.graphics.Paint statsPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            statsPaint.setColor(cGreen);
            statsPaint.setTextSize(30f);
            if (poppinsRegular != null) statsPaint.setTypeface(poppinsRegular);
            statsPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText(
                    "Ability Growth: " + sign + String.format(Locale.US, "%.2f", thetaDiff)
                            + "   \u2022   Accuracy: " + String.format(Locale.US, "%.0f%%", accuracy),
                    width / 2f, 806f, statsPaint);

            // ── Bottom ornamental rule ───────────────────────────────────
            float bruleY = 836f;
            canvas.drawLine(90, bruleY, width / 2f - 32, bruleY, rulePaint);
            canvas.drawLine(width / 2f + 32, bruleY, width - 90, bruleY, rulePaint);
            android.graphics.Path bDiamond = new android.graphics.Path();
            bDiamond.moveTo(width / 2f, bruleY - 14);
            bDiamond.lineTo(width / 2f + 18, bruleY);
            bDiamond.lineTo(width / 2f, bruleY + 14);
            bDiamond.lineTo(width / 2f - 18, bruleY);
            bDiamond.close();
            canvas.drawPath(bDiamond, diamondFillPaint);

            // ── Bottom section: date | seal | director ───────────────────
            String date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(new Date());
            float leftX  = width / 4f;
            float rightX = 3f * width / 4f;
            float bLineY = 920f;

            android.graphics.Paint bLabelPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            bLabelPaint.setColor(cGrayText);
            bLabelPaint.setTextSize(24f);
            if (poppinsRegular != null) bLabelPaint.setTypeface(poppinsRegular);
            bLabelPaint.setTextAlign(android.graphics.Paint.Align.CENTER);

            android.graphics.Paint bValuePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            bValuePaint.setColor(cDarkText);
            bValuePaint.setTextSize(26f);
            if (poppinsSemiBold != null) bValuePaint.setTypeface(poppinsSemiBold);
            else bValuePaint.setFakeBoldText(true);
            bValuePaint.setTextAlign(android.graphics.Paint.Align.CENTER);

            canvas.drawText("Date of Completion", leftX, bLineY - 8, bLabelPaint);
            canvas.drawLine(leftX - 110, bLineY + 8, leftX + 110, bLineY + 8, rulePaint);
            canvas.drawText(date, leftX, bLineY + 44, bValuePaint);

            canvas.drawText("Program Director", rightX, bLineY - 8, bLabelPaint);
            canvas.drawLine(rightX - 110, bLineY + 8, rightX + 110, bLineY + 8, rulePaint);
            canvas.drawText("LiteRise Academy", rightX, bLineY + 44, bValuePaint);

            // Circular seal
            float sealCX = width / 2f, sealCY = bLineY + 22;
            float sealR  = 62f;
            android.graphics.Paint sealOuterPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            sealOuterPaint.setColor(cPurpleDark);
            sealOuterPaint.setStyle(android.graphics.Paint.Style.STROKE);
            sealOuterPaint.setStrokeWidth(5f);
            canvas.drawCircle(sealCX, sealCY, sealR, sealOuterPaint);
            android.graphics.Paint sealInnerPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            sealInnerPaint.setColor(cGold);
            sealInnerPaint.setStyle(android.graphics.Paint.Style.STROKE);
            sealInnerPaint.setStrokeWidth(2.5f);
            canvas.drawCircle(sealCX, sealCY, sealR - 9, sealInnerPaint);
            android.graphics.Paint sealTextPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            sealTextPaint.setColor(cPurpleDark);
            sealTextPaint.setTextSize(38f);
            if (poppinsBold != null) sealTextPaint.setTypeface(poppinsBold);
            else sealTextPaint.setFakeBoldText(true);
            sealTextPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("LR", sealCX, sealCY + 14f, sealTextPaint);

            // ── Footer ───────────────────────────────────────────────────
            android.graphics.Paint footerPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            footerPaint.setColor(cPurple);
            footerPaint.setTextSize(24f);
            if (poppinsRegular != null) footerPaint.setTypeface(poppinsRegular);
            footerPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("www.literise.app", width / 2f, height - 30f, footerPaint);

            // ── Save and share ───────────────────────────────────────────
            File cachesDir = new File(activity.getCacheDir(), "certificates");
            if (!cachesDir.exists()) cachesDir.mkdirs();
            File certFile = new File(cachesDir, "LiteRise_Certificate.png");
            FileOutputStream fos = new FileOutputStream(certFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Uri contentUri = FileProvider.getUriForFile(
                    activity,
                    activity.getPackageName() + ".fileprovider",
                    certFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "LiteRise Certificate of Completion");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(Intent.createChooser(shareIntent, "Share Certificate"));

        } catch (Exception e) {
            Log.e(TAG, "Error generating certificate: " + e.getMessage());
            android.widget.Toast.makeText(activity, "Could not generate certificate",
                    android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
