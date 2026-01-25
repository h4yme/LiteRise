package com.example.literise.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.example.literise.models.NodeView;

import java.util.ArrayList;
import java.util.List;

public class ModulePathView extends View {
    private static final String TAG = "ModulePathView";

    private List<NodeView> nodes;
    private Paint pathPaint;
    private Paint nodePaint;
    private Paint textPaint;
    private Paint labelPaint;
    private Paint framePaint;
    private Paint bgPaint;
    private Paint shadowPaint;
    private Path trailPath;
    private OnNodeClickListener nodeClickListener;

    public interface OnNodeClickListener {
        void onNodeClick(NodeView node);
    }

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

    private void init() {
        nodes = new ArrayList<>();

        // Trail path paint with gradient
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(Color.parseColor("#E8C59C"));
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(65);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setShadowLayer(8, 0, 4, Color.parseColor("#80000000"));

        // Node paint
        nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Shadow paint for nodes
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.parseColor("#40000000"));
        shadowPaint.setStyle(Paint.Style.FILL);

        // Text paint for node numbers
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(64);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
        textPaint.setShadowLayer(4, 0, 2, Color.parseColor("#80000000"));

        // Label paint for quarters
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(44);
        labelPaint.setAlpha(220);
        labelPaint.setShadowLayer(8, 2, 2, Color.parseColor("#80000000"));
        labelPaint.setFakeBoldText(true);

        // Frame paint for "YOU ARE HERE"
        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setColor(Color.parseColor("#FF1493"));
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(14);
        framePaint.setShadowLayer(6, 0, 3, Color.parseColor("#80000000"));

        // Background paint for "YOU ARE HERE"
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#2D2D2D"));
        bgPaint.setAlpha(240);

        Log.d(TAG, "ModulePathView initialized");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG, "onDraw called, nodes: " + (nodes != null ? nodes.size() : "null"));
        Log.d(TAG, "Canvas size: " + getWidth() + "x" + getHeight());

        if (nodes == null || nodes.isEmpty()) {
            Log.w(TAG, "No nodes to draw!");
            Paint msgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            msgPaint.setColor(Color.parseColor("#FF5252"));
            msgPaint.setTextSize(44);
            msgPaint.setTextAlign(Paint.Align.CENTER);
            msgPaint.setFakeBoldText(true);
            canvas.drawText("Loading lessons...", getWidth() / 2f, getHeight() / 2f, msgPaint);
            return;
        }

        // Draw winding trail path
        drawTrailPath(canvas);

        // Draw quarter markers
        drawQuarterMarkers(canvas);

        // Draw nodes
        for (NodeView node : nodes) {
            drawNode(canvas, node);
        }

        // Draw "YOU ARE HERE" indicator
        drawCurrentIndicator(canvas);

        Log.d(TAG, "onDraw finished");
    }

    private void drawTrailPath(Canvas canvas) {
        trailPath = new Path();

        for (int i = 0; i < nodes.size(); i++) {
            NodeView node = nodes.get(i);
            float x = node.getX() * getWidth() / 100f;
            float y = node.getY() * getHeight() / 100f;

            if (i == 0) {
                trailPath.moveTo(x, y);
            } else {
                NodeView prevNode = nodes.get(i - 1);
                float prevX = prevNode.getX() * getWidth() / 100f;
                float prevY = prevNode.getY() * getHeight() / 100f;

                float controlX = (x + prevX) / 2;
                float controlY = (y + prevY) / 2;

                trailPath.quadTo(controlX, controlY, x, y);
            }
        }

        canvas.drawPath(trailPath, pathPaint);
    }

    private void drawNode(Canvas canvas, NodeView node) {
        float x = node.getX() * getWidth() / 100f;
        float y = node.getY() * getHeight() / 100f;

        int size = node.isFinalAssessment() ? 200 : 140; // Even bigger!

        // Draw shadow first
        canvas.drawCircle(x + 4, y + 6, size / 2f, shadowPaint);

        // Draw gradient circle based on state
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL);

        RadialGradient gradient;

        switch (node.getState()) {
            case LOCKED:
                // Gray gradient
                gradient = new RadialGradient(x, y - size/4, size / 2f,
                        Color.parseColor("#9E9E9E"),
                        Color.parseColor("#757575"),
                        Shader.TileMode.CLAMP);
                break;
            case UNLOCKED:
                // Green gradient - Ready to start!
                gradient = new RadialGradient(x, y - size/4, size / 2f,
                        Color.parseColor("#66BB6A"),
                        Color.parseColor("#43A047"),
                        Shader.TileMode.CLAMP);
                break;
            case CURRENT:
                // Orange gradient - Active!
                gradient = new RadialGradient(x, y - size/4, size / 2f,
                        Color.parseColor("#FFA726"),
                        Color.parseColor("#FB8C00"),
                        Shader.TileMode.CLAMP);
                break;
            case COMPLETED:
                // Blue gradient - Completed!
                gradient = new RadialGradient(x, y - size/4, size / 2f,
                        Color.parseColor("#42A5F5"),
                        Color.parseColor("#1E88E5"),
                        Shader.TileMode.CLAMP);
                break;
            case MASTERED:
                // Gold gradient - Mastered!
                gradient = new RadialGradient(x, y - size/4, size / 2f,
                        Color.parseColor("#FFD54F"),
                        Color.parseColor("#FFA000"),
                        Shader.TileMode.CLAMP);
                break;
            default:
                gradient = new RadialGradient(x, y - size/4, size / 2f,
                        Color.parseColor("#9E9E9E"),
                        Color.parseColor("#757575"),
                        Shader.TileMode.CLAMP);
        }

        circlePaint.setShader(gradient);
        canvas.drawCircle(x, y, size / 2f, circlePaint);

        // Draw white border with glow effect
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(10);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setShadowLayer(8, 0, 0, Color.WHITE);
        canvas.drawCircle(x, y, size / 2f, borderPaint);

        // Draw inner circle for depth effect
        Paint innerBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        innerBorderPaint.setStyle(Paint.Style.STROKE);
        innerBorderPaint.setStrokeWidth(6);
        innerBorderPaint.setColor(Color.parseColor("#40FFFFFF"));
        canvas.drawCircle(x, y, (size / 2f) - 8, innerBorderPaint);

        // Draw node number or lock icon
        if (node.getState() != NodeView.NodeState.LOCKED) {
            float textSize = node.isFinalAssessment() ? 80 : 64;
            textPaint.setTextSize(textSize);

            // Add star emoji for final assessment
            if (node.isFinalAssessment()) {
                Paint starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                starPaint.setTextSize(50);
                starPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("⭐", x, y - 30, starPaint);
            }

            canvas.drawText(
                    String.valueOf(node.getNodeNumber()),
                    x, y + (node.isFinalAssessment() ? 20 : (textSize / 3)), textPaint
            );
        } else {
            // Draw bigger lock icon with glow
            Paint lockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            lockPaint.setTextSize(70);
            lockPaint.setTextAlign(Paint.Align.CENTER);
            lockPaint.setShadowLayer(6, 0, 2, Color.parseColor("#80000000"));
            canvas.drawText("🔒", x, y + 24, lockPaint);
        }
    }

    private void drawCurrentIndicator(Canvas canvas) {
        NodeView current = null;
        for (NodeView node : nodes) {
            if (node.getState() == NodeView.NodeState.CURRENT) {
                current = node;
                break;
            }
        }

        if (current == null) return;

        float x = current.getX() * getWidth() / 100f;
        float y = current.getY() * getHeight() / 100f;

        // Draw animated pulsing effect
        RectF frame = new RectF(x - 110, y - 150, x + 110, y - 25);

        // Shadow
        Paint shadowPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint2.setColor(Color.parseColor("#40000000"));
        shadowPaint2.setStyle(Paint.Style.FILL);
        RectF shadowFrame = new RectF(x - 108, y - 146, x + 112, y - 21);
        canvas.drawRoundRect(shadowFrame, 20, 20, shadowPaint2);

        // Background with gradient
        Paint bgGradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient bgGradient = new LinearGradient(
                x, y - 150, x, y - 25,
                Color.parseColor("#FF1493"),
                Color.parseColor("#C71585"),
                Shader.TileMode.CLAMP
        );
        bgGradientPaint.setShader(bgGradient);
        canvas.drawRoundRect(frame, 20, 20, bgGradientPaint);

        // Border
        canvas.drawRoundRect(frame, 20, 20, framePaint);

        // Text with better styling
        Paint textPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint2.setColor(Color.WHITE);
        textPaint2.setTextSize(36);
        textPaint2.setTextAlign(Paint.Align.CENTER);
        textPaint2.setFakeBoldText(true);
        textPaint2.setShadowLayer(4, 0, 2, Color.parseColor("#80000000"));

        // Emoji
        canvas.drawText("👆", x, y - 115, textPaint2);

        textPaint2.setTextSize(28);
        canvas.drawText("YOU ARE", x, y - 80, textPaint2);
        canvas.drawText("HERE", x, y - 50, textPaint2);
    }

    private void drawQuarterMarkers(Canvas canvas) {
        // Draw "Quarter" labels with better positioning
        canvas.drawText("Quarter 1", getWidth() * 0.12f, getHeight() * 0.76f, labelPaint);
        canvas.drawText("Quarter 2", getWidth() * 0.74f, getHeight() * 0.56f, labelPaint);
        canvas.drawText("Quarter 3", getWidth() * 0.75f, getHeight() * 0.42f, labelPaint);
        canvas.drawText("Quarter 4", getWidth() * 0.20f, getHeight() * 0.24f, labelPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && nodeClickListener != null) {
            float touchX = event.getX();
            float touchY = event.getY();

            for (NodeView node : nodes) {
                float nodeX = node.getX() * getWidth() / 100f;
                float nodeY = node.getY() * getHeight() / 100f;
                float touchRadius = node.isFinalAssessment() ? 100 : 70;

                float distance = (float) Math.sqrt(
                        Math.pow(touchX - nodeX, 2) + Math.pow(touchY - nodeY, 2)
                );

                if (distance <= touchRadius) {
                    Log.d(TAG, "Node clicked: " + node.getNodeNumber());
                    nodeClickListener.onNodeClick(node);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public void setNodes(List<NodeView> nodes) {
        Log.d(TAG, "setNodes called with " + (nodes != null ? nodes.size() : "null") + " nodes");
        this.nodes = nodes;

        if (nodes != null) {
            for (NodeView node : nodes) {
                Log.d(TAG, "Node " + node.getNodeNumber() + " at (" +
                        node.getX() + ", " + node.getY() + ") state: " + node.getState());
            }
        }

        invalidate();
        Log.d(TAG, "invalidate() called");
    }

    public void setOnNodeClickListener(OnNodeClickListener listener) {
        this.nodeClickListener = listener;
    }
}
