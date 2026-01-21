package com.example.literise.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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

        // Trail path paint
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(Color.parseColor("#E8C59C")); // Sandy trail color
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeWidth(50);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);

        // Node paint
        nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Text paint for node numbers
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(48);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);

        // Label paint for quarters
        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(36);
        labelPaint.setAlpha(180);
        labelPaint.setShadowLayer(4, 2, 2, Color.parseColor("#80000000"));

        // Frame paint for "YOU ARE HERE"
        framePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        framePaint.setColor(Color.parseColor("#FF1493")); // Pink frame
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(10);

        // Background paint for "YOU ARE HERE"
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#333333"));
        bgPaint.setAlpha(220);

        Log.d(TAG, "ModulePathView initialized");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Log.d(TAG, "onDraw called, nodes: " + (nodes != null ? nodes.size() : "null"));
        Log.d(TAG, "Canvas size: " + getWidth() + "x" + getHeight());

        if (nodes == null || nodes.isEmpty()) {
            Log.w(TAG, "No nodes to draw!");
            // Draw a message
            Paint msgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            msgPaint.setColor(Color.RED);
            msgPaint.setTextSize(40);
            msgPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No nodes loaded", getWidth() / 2f, getHeight() / 2f, msgPaint);
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
                // Create curved path between nodes
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

        int size = node.isFinalAssessment() ? 140 : 100;

        // Draw circle background based on state
        Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL);

        switch (node.getState()) {
            case LOCKED:
                circlePaint.setColor(Color.parseColor("#999999"));
                break;
            case UNLOCKED:
                circlePaint.setColor(Color.parseColor("#4CAF50"));
                break;
            case CURRENT:
                circlePaint.setColor(Color.parseColor("#FF9800"));
                break;
            case COMPLETED:
                circlePaint.setColor(Color.parseColor("#2196F3"));
                break;
            case MASTERED:
                circlePaint.setColor(Color.parseColor("#FFD700"));
                break;
        }

        canvas.drawCircle(x, y, size / 2f, circlePaint);

        // Draw border
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(6);
        borderPaint.setColor(Color.WHITE);
        canvas.drawCircle(x, y, size / 2f, borderPaint);

        // Draw node number
        if (node.getState() != NodeView.NodeState.LOCKED) {
            float textSize = node.isFinalAssessment() ? 56 : 48;
            textPaint.setTextSize(textSize);
            canvas.drawText(
                    String.valueOf(node.getNodeNumber()),
                    x, y + (textSize / 3), textPaint
            );
        } else {
            // Draw lock icon for locked nodes
            Paint lockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            lockPaint.setColor(Color.WHITE);
            lockPaint.setTextSize(48);
            lockPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("🔒", x, y + 16, lockPaint);
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

        // Draw "YOU ARE HERE" frame
        RectF frame = new RectF(x - 80, y - 120, x + 80, y - 30);

        // Draw background
        canvas.drawRoundRect(frame, 15, 15, bgPaint);

        // Draw border
        canvas.drawRoundRect(frame, 15, 15, framePaint);

        // Draw text
        Paint textPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint2.setColor(Color.WHITE);
        textPaint2.setTextSize(28);
        textPaint2.setTextAlign(Paint.Align.CENTER);
        textPaint2.setFakeBoldText(true);

        canvas.drawText("YOU ARE", x, y - 85, textPaint2);
        canvas.drawText("HERE", x, y - 55, textPaint2);
    }

    private void drawQuarterMarkers(Canvas canvas) {
        // Draw "Quarter 1", "Quarter 2" labels at appropriate positions
        // Q1 marker (around node 3)
        canvas.drawText("Quarter 1", getWidth() * 0.15f, getHeight() * 0.68f, labelPaint);
        // Q2 marker (around node 6)
        canvas.drawText("Quarter 2", getWidth() * 0.70f, getHeight() * 0.45f, labelPaint);
        // Q3 marker (around node 9)
        canvas.drawText("Quarter 3", getWidth() * 0.15f, getHeight() * 0.25f, labelPaint);
        // Q4 marker (around node 12)
        canvas.drawText("Quarter 4", getWidth() * 0.60f, getHeight() * 0.10f, labelPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && nodeClickListener != null) {
            float touchX = event.getX();
            float touchY = event.getY();

            // Check if any node was clicked
            for (NodeView node : nodes) {
                float nodeX = node.getX() * getWidth() / 100f;
                float nodeY = node.getY() * getHeight() / 100f;
                float touchRadius = node.isFinalAssessment() ? 70 : 50;

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

        invalidate(); // Force redraw
        Log.d(TAG, "invalidate() called");
    }

    public void setOnNodeClickListener(OnNodeClickListener listener) {
        this.nodeClickListener = listener;
    }
}
