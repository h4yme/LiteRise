package com.example.literise.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.literise.models.NodeView;

import java.util.ArrayList;
import java.util.List;

public class ModulePathView extends View {
    private static final String TAG = "ModulePathView";

    // Zigzag node positions (index 0 = node 1, bottom of screen drawn last)
    // X is percentage of width, Y is percentage of height — node 1 at bottom, node 13 at top
    private static final float[] NODE_X_PCT = {50, 67, 76, 67, 50, 33, 24, 33, 50, 67, 76, 60, 50};
    private static final float[] NODE_Y_PCT = {93, 86, 78, 70, 63, 56, 48, 41, 34, 27, 20, 13, 6};

    // Quarter divider Y positions (between node groups)
    // Q1=nodes 1-3, Q2=nodes 4-6, Q3=nodes 7-9, Q4=nodes 10-13
    private static final float[] DIVIDER_Y_PCT = {74f, 52f, 30f};
    private static final String[] DIVIDER_LABELS = {"Quarter 2", "Quarter 3", "Quarter 4"};

    private List<NodeView> nodes;
    private int moduleColorMain   = Color.parseColor("#667EEA");
    private int moduleColorBottom = Color.parseColor("#4A5EC4");

    // Node sizing in px (set in init from dp)
    private float nodeRadius;
    private float nodeFinalRadius;
    private float coinOffsetY;
    private float connectorStroke;
    private float touchRadius;

    // Paints
    private Paint connectorDonePaint;
    private Paint connectorTodoPaint;
    private Paint nodeShadowPaint;
    private Paint nodeBottomPaint;
    private Paint nodeTopPaint;
    private Paint lockedBottomPaint;
    private Paint lockedTopPaint;
    private Paint masteredBottomPaint;
    private Paint masteredTopPaint;
    private Paint checkPaint;
    private Paint iconPaint;
    private Paint numberPaint;
    private Paint tooltipBgPaint;
    private Paint tooltipTextPaint;
    private Paint dividerLinePaint;
    private Paint dividerTextPaint;
    private Paint pulseRingPaint;

    // Pulse animation
    private float pulseProgress = 0f;   // 0..1
    private ValueAnimator pulseAnimator;

    private OnNodeClickListener nodeClickListener;

    public interface OnNodeClickListener {
        void onNodeClick(NodeView node);
    }

    // ─── Constructors ─────────────────────────────────────────────────────────

    public ModulePathView(Context context) {
        super(context);
        init();
    }

    public ModulePathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ModulePathView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // ─── Init ─────────────────────────────────────────────────────────────────

    private void init() {
        nodes = new ArrayList<>();
        float dp = getContext().getResources().getDisplayMetrics().density;

        nodeRadius      = 38 * dp;
        nodeFinalRadius = 46 * dp;
        coinOffsetY     = 5  * dp;
        connectorStroke = 10 * dp;
        touchRadius     = 56 * dp;

        // Connector
        connectorDonePaint = makePaint(0, Paint.Style.STROKE);
        connectorDonePaint.setStrokeWidth(connectorStroke);
        connectorDonePaint.setStrokeCap(Paint.Cap.ROUND);

        connectorTodoPaint = makePaint(Color.parseColor("#3D3D5C"), Paint.Style.STROKE);
        connectorTodoPaint.setStrokeWidth(connectorStroke);
        connectorTodoPaint.setStrokeCap(Paint.Cap.ROUND);

        // Node shadow
        nodeShadowPaint = makePaint(Color.parseColor("#33000000"), Paint.Style.FILL);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        // Normal node
        nodeBottomPaint = makePaint(0, Paint.Style.FILL);
        nodeTopPaint    = makePaint(0, Paint.Style.FILL);

        // Locked node
        lockedBottomPaint = makePaint(Color.parseColor("#1E1E38"), Paint.Style.FILL);
        lockedTopPaint    = makePaint(Color.parseColor("#2D2D47"), Paint.Style.FILL);

        // Mastered node (gold)
        masteredBottomPaint = makePaint(Color.parseColor("#C8860A"), Paint.Style.FILL);
        masteredTopPaint    = makePaint(Color.parseColor("#FFD700"), Paint.Style.FILL);

        // Icon / number paints
        checkPaint = makePaint(Color.WHITE, Paint.Style.FILL);
        checkPaint.setTextSize(32 * dp);
        checkPaint.setTextAlign(Paint.Align.CENTER);
        checkPaint.setFakeBoldText(true);

        iconPaint = makePaint(Color.parseColor("#7070A0"), Paint.Style.FILL);
        iconPaint.setTextSize(26 * dp);
        iconPaint.setTextAlign(Paint.Align.CENTER);

        numberPaint = makePaint(Color.WHITE, Paint.Style.FILL);
        numberPaint.setTextSize(28 * dp);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setFakeBoldText(true);

        // Tooltip
        tooltipBgPaint = makePaint(Color.WHITE, Paint.Style.FILL);

        tooltipTextPaint = makePaint(Color.parseColor("#1C1C2E"), Paint.Style.FILL);
        tooltipTextPaint.setTextSize(20 * dp);
        tooltipTextPaint.setTextAlign(Paint.Align.CENTER);
        tooltipTextPaint.setFakeBoldText(true);

        // Quarter divider
        dividerLinePaint = makePaint(Color.parseColor("#2D2F54"), Paint.Style.STROKE);
        dividerLinePaint.setStrokeWidth(1.5f * dp);

        dividerTextPaint = makePaint(Color.parseColor("#A0A0C0"), Paint.Style.FILL);
        dividerTextPaint.setTextSize(13 * dp);
        dividerTextPaint.setTextAlign(Paint.Align.CENTER);

        // Pulse ring
        pulseRingPaint = makePaint(0, Paint.Style.STROKE);
        pulseRingPaint.setStrokeWidth(3 * dp);

        startPulse();
    }

    private Paint makePaint(int color, Paint.Style style) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(style);
        return p;
    }

    // ─── Pulse animation ──────────────────────────────────────────────────────

    private void startPulse() {
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new LinearInterpolator());
        pulseAnimator.addUpdateListener(a -> {
            pulseProgress = (float) a.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    // ─── Public API ───────────────────────────────────────────────────────────

    public void setModuleColor(int mainColor, int bottomColor) {
        moduleColorMain   = mainColor;
        moduleColorBottom = bottomColor;
        invalidate();
    }

    public void setNodes(List<NodeView> nodes) {
        this.nodes = nodes;
        Log.d(TAG, "setNodes: " + (nodes != null ? nodes.size() : 0));
        invalidate();
    }

    public void setOnNodeClickListener(OnNodeClickListener l) {
        this.nodeClickListener = l;
    }

    // ─── Draw ─────────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (nodes == null || nodes.isEmpty()) {
            Paint msg = makePaint(Color.parseColor("#9090B0"), Paint.Style.FILL);
            msg.setTextAlign(Paint.Align.CENTER);
            msg.setTextSize(18 * getContext().getResources().getDisplayMetrics().density);
            canvas.drawText("Loading lessons…", getWidth() / 2f, getHeight() / 2f, msg);
            return;
        }

        drawQuarterDividers(canvas);
        drawConnectors(canvas);

        for (NodeView node : nodes) {
            drawNode(canvas, node);
        }
    }

    // ─── Quarter dividers ─────────────────────────────────────────────────────

    private void drawQuarterDividers(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();
        float padH = 24 * getContext().getResources().getDisplayMetrics().density;

        for (int i = 0; i < DIVIDER_Y_PCT.length; i++) {
            float y = DIVIDER_Y_PCT[i] / 100f * h;

            // Dashed horizontal line
            canvas.drawLine(padH, y, w - padH, y, dividerLinePaint);

            // Label centred
            canvas.drawText(DIVIDER_LABELS[i], w / 2f, y - 6, dividerTextPaint);
        }
    }

    // ─── Connectors ───────────────────────────────────────────────────────────

    private void drawConnectors(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();

        for (int i = 0; i < nodes.size() - 1; i++) {
            NodeView from = nodes.get(i);
            NodeView to   = nodes.get(i + 1);

            float x1 = nodeX(from, w);
            float y1 = nodeY(from, h);
            float x2 = nodeX(to, w);
            float y2 = nodeY(to, h);

            boolean done = from.getState() == NodeView.NodeState.COMPLETED
                    || from.getState() == NodeView.NodeState.MASTERED;

            if (done) {
                connectorDonePaint.setColor(dimColor(moduleColorMain, 0.7f));
            }

            Paint paint = done ? connectorDonePaint : connectorTodoPaint;
            canvas.drawLine(x1, y1, x2, y2, paint);
        }
    }

    // ─── Single node ──────────────────────────────────────────────────────────

    private void drawNode(Canvas canvas, NodeView node) {
        float w = getWidth();
        float h = getHeight();
        float cx = nodeX(node, w);
        float cy = nodeY(node, h);
        float r  = node.isFinalAssessment() ? nodeFinalRadius : nodeRadius;

        // 1. Pulse ring for CURRENT node
        if (node.getState() == NodeView.NodeState.CURRENT) {
            float maxExtra = r * 0.7f;
            float ringR = r + maxExtra * pulseProgress;
            int alpha = (int) (160 * (1f - pulseProgress));
            pulseRingPaint.setColor(moduleColorMain);
            pulseRingPaint.setAlpha(alpha);
            canvas.drawCircle(cx, cy, ringR, pulseRingPaint);
        }

        // 2. Shadow
        nodeShadowPaint.setShadowLayer(12, 0, 6, Color.parseColor("#55000000"));
        canvas.drawCircle(cx + 2, cy + 4, r, nodeShadowPaint);

        // 3. Bottom (3D coin bottom)
        Paint btm = getBottomPaint(node);
        canvas.drawCircle(cx, cy + coinOffsetY, r, btm);

        // 4. Top circle
        Paint top = getTopPaint(node);
        canvas.drawCircle(cx, cy, r, top);

        // 5. Inner highlight ring
        Paint highlight = makePaint(Color.parseColor("#33FFFFFF"), Paint.Style.STROKE);
        highlight.setStrokeWidth(2 * getContext().getResources().getDisplayMetrics().density);
        canvas.drawCircle(cx, cy - r * 0.25f, r * 0.6f, highlight);

        // 6. Icon / number
        drawNodeIcon(canvas, node, cx, cy, r);

        // 7. START tooltip for CURRENT node
        if (node.getState() == NodeView.NodeState.CURRENT) {
            drawStartTooltip(canvas, cx, cy - r);
        }
    }

    private Paint getBottomPaint(NodeView node) {
        switch (node.getState()) {
            case LOCKED:    return lockedBottomPaint;
            case MASTERED:  return masteredBottomPaint;
            default:
                nodeBottomPaint.setColor(dimColor(moduleColorMain, 0.65f));
                return nodeBottomPaint;
        }
    }

    private Paint getTopPaint(NodeView node) {
        switch (node.getState()) {
            case LOCKED:    return lockedTopPaint;
            case MASTERED:  return masteredTopPaint;
            case COMPLETED:
                nodeTopPaint.setColor(dimColor(moduleColorMain, 0.80f));
                return nodeTopPaint;
            default:        // UNLOCKED / CURRENT
                nodeTopPaint.setColor(moduleColorMain);
                return nodeTopPaint;
        }
    }

    private void drawNodeIcon(Canvas canvas, NodeView node, float cx, float cy, float r) {
        float dp = getContext().getResources().getDisplayMetrics().density;
        float textBaseY = cy + 10 * dp;

        switch (node.getState()) {
            case LOCKED:
                iconPaint.setTextSize(24 * dp);
                canvas.drawText("🔒", cx, textBaseY, iconPaint);
                break;

            case COMPLETED:
                // Draw checkmark text
                checkPaint.setTextSize(28 * dp);
                canvas.drawText("✓", cx, textBaseY, checkPaint);
                break;

            case MASTERED:
                checkPaint.setTextSize(26 * dp);
                canvas.drawText("★", cx, textBaseY, checkPaint);
                break;

            default: // UNLOCKED / CURRENT
                if (node.isFinalAssessment()) {
                    iconPaint.setColor(Color.WHITE);
                    iconPaint.setTextSize(26 * dp);
                    canvas.drawText("⭐", cx, cy - 10 * dp, iconPaint);
                    numberPaint.setTextSize(20 * dp);
                    canvas.drawText(String.valueOf(node.getNodeNumber()), cx, textBaseY + 8 * dp, numberPaint);
                } else {
                    numberPaint.setTextSize(26 * dp);
                    canvas.drawText(String.valueOf(node.getNodeNumber()), cx, textBaseY, numberPaint);
                }
                break;
        }
    }

    // ─── START tooltip ────────────────────────────────────────────────────────

    private void drawStartTooltip(Canvas canvas, float cx, float topY) {
        float dp = getContext().getResources().getDisplayMetrics().density;
        float padX = 16 * dp;
        float padY = 8  * dp;
        float cornerR = 12 * dp;
        float arrowH = 10 * dp;
        float arrowW = 12 * dp;
        float gap    = 8  * dp;

        // Measure text
        String text = "START";
        float textW = tooltipTextPaint.measureText(text);
        float boxW  = textW + padX * 2;
        float boxH  = tooltipTextPaint.getTextSize() + padY * 2;

        float boxBottom = topY - gap;
        float boxTop    = boxBottom - boxH;
        float boxLeft   = cx - boxW / 2f;
        float boxRight  = cx + boxW / 2f;

        // Rounded rect
        RectF rect = new RectF(boxLeft, boxTop, boxRight, boxBottom);
        canvas.drawRoundRect(rect, cornerR, cornerR, tooltipBgPaint);

        // Down-pointing triangle
        Path arrow = new Path();
        arrow.moveTo(cx - arrowW, boxBottom);
        arrow.lineTo(cx + arrowW, boxBottom);
        arrow.lineTo(cx, boxBottom + arrowH);
        arrow.close();
        canvas.drawPath(arrow, tooltipBgPaint);

        // Text
        float textY = boxBottom - padY - (tooltipTextPaint.descent());
        canvas.drawText(text, cx, textY, tooltipTextPaint);
    }

    // ─── Touch ────────────────────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && nodeClickListener != null) {
            float tx = event.getX();
            float ty = event.getY();
            float w  = getWidth();
            float h  = getHeight();

            for (NodeView node : nodes) {
                float nx = nodeX(node, w);
                float ny = nodeY(node, h);
                float r  = node.isFinalAssessment() ? nodeFinalRadius : nodeRadius;
                float dist = (float) Math.hypot(tx - nx, ty - ny);
                if (dist <= Math.max(r, touchRadius)) {
                    nodeClickListener.onNodeClick(node);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private float nodeX(NodeView node, float w) {
        int idx = node.getNodeNumber() - 1;
        if (idx < 0 || idx >= NODE_X_PCT.length) return w / 2f;
        return NODE_X_PCT[idx] / 100f * w;
    }

    private float nodeY(NodeView node, float h) {
        int idx = node.getNodeNumber() - 1;
        if (idx < 0 || idx >= NODE_Y_PCT.length) return h / 2f;
        return NODE_Y_PCT[idx] / 100f * h;
    }

    /** Return a darker/lighter version of a color by the given factor (0=black, 1=same). */
    private int dimColor(int color, float factor) {
        int r = (int) (Color.red(color)   * factor);
        int g = (int) (Color.green(color) * factor);
        int b = (int) (Color.blue(color)  * factor);
        return Color.rgb(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pulseAnimator != null) pulseAnimator.cancel();
    }
}
