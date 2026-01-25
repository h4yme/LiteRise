package com.example.literise.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom view for displaying the module learning path with nodes
 * Draws a winding path with lesson nodes arranged in a zigzag pattern
 */
public class ModulePathView extends View {

    private Paint pathPaint;
    private Paint nodePaint;
    private Path path;

    private int moduleId;
    private int totalLessons = 13; // 13 lessons per module
    private int currentLesson = 1;

    private OnNodeClickListener nodeClickListener;

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
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setColor(0xFFBB86FC); // Purple path color
        pathPaint.setStrokeWidth(8f);
        pathPaint.setStyle(Paint.Style.STROKE);

        nodePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        nodePaint.setColor(0xFF6200EE);
        nodePaint.setStyle(Paint.Style.FILL);

        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: Implement path and node drawing
        // This will draw the winding path and lesson nodes
        // For now, this is a placeholder
    }

    /**
     * Set the module data
     */
    public void setModuleData(int moduleId, int currentLesson, int totalLessons) {
        this.moduleId = moduleId;
        this.currentLesson = currentLesson;
        this.totalLessons = totalLessons;
        invalidate(); // Redraw
    }

    /**
     * Set click listener for node clicks
     */
    public void setOnNodeClickListener(OnNodeClickListener listener) {
        this.nodeClickListener = listener;
    }

    /**
     * Interface for handling node clicks
     */
    public interface OnNodeClickListener {
        void onNodeClicked(int lessonNumber);
    }
}
