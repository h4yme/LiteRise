package com.example.literise.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.literise.models.NodeView;

import java.util.ArrayList;
import java.util.List;

public class ModulePathView extends View {

    // ─── Node positions ──────────────────────────────────────────────────────
    // X = % of width, Y = % of height.  Node 1 is at bottom (96 %), node 13 at top (4 %).
    private static final float[] NODE_X_PCT = {50, 67, 76, 67, 50, 33, 24, 33, 50, 67, 76, 60, 50};
    private static final float[] NODE_Y_PCT = {91, 84, 77, 70, 63, 56, 49, 42, 35, 28, 21, 13, 6};

    // Quarter-divider horizontal rules drawn BETWEEN the node groups
    // Q1 = nodes 1-3, Q2 = nodes 4-6, Q3 = nodes 7-9, Q4 = nodes 10-13
    private static final float[] DIVIDER_Y_PCT  = {67f, 45.5f, 24.5f};
    private static final String[] DIVIDER_LABEL = {"Quarter 2", "Quarter 3", "Quarter 4"};

    // ─── State ───────────────────────────────────────────────────────────────
    private List<NodeView> nodes = new ArrayList<>();

    // Module colours (set from Activity via setModuleColor())
    private int colorMain   = Color.parseColor("#7C3AED");
    private int colorBottom = Color.parseColor("#5A189A");

    // ─── Sizing (set in init, dp → px) ───────────────────────────────────────
    private float nodeR;        // normal radius
    private float finalR;       // radius for FINAL_ASSESSMENT node
    private float coinDy;       // y-offset for 3-D coin shadow
    private float connW;        // connector stroke width
    private float hitR;         // touch hit-zone radius

    // ─── Paints ──────────────────────────────────────────────────────────────
    // Connectors
    private final Paint pConnDone  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pConnTodo  = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Node coin layers
    private final Paint pCoinShadow  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCoinBottom  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCoinTop     = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pCoinHighlight = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Lock node
    private final Paint pLockBottom = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pLockTop    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pLockBorder = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Mastered (gold) node
    private final Paint pGoldBottom = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pGoldTop    = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Icons drawn on nodes
    private final Paint pCheckPath = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pNumber    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pLockNum   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pStarText  = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Pulse ring
    private final Paint pPulse = new Paint(Paint.ANTI_ALIAS_FLAG);

    // START tooltip
    private final Paint pTipBg   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTipText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pTipBorder = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Quarter dividers
    private final Paint pDivLine  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pPillBg   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pPillText = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Pulse animation
    private float pulsePhase = 0f;
    private ValueAnimator pulseAnim;

    // Click listener
    private OnNodeClickListener clickListener;

    public interface OnNodeClickListener {
        void onNodeClick(NodeView node);
    }

    // ─── Constructors ────────────────────────────────────────────────────────

    public ModulePathView(Context c) { super(c); init(); }
    public ModulePathView(Context c, AttributeSet a) { super(c, a); init(); }
    public ModulePathView(Context c, AttributeSet a, int d) { super(c, a, d); init(); }

    // ─── Init ────────────────────────────────────────────────────────────────

    private void init() {
        float dp = getContext().getResources().getDisplayMetrics().density;

        nodeR  = 30 * dp;
        finalR = 38 * dp;
        coinDy = 5  * dp;
        connW  = 9  * dp;
        hitR   = 46 * dp;

        // ---- Connectors ----
        pConnDone.setStyle(Paint.Style.STROKE);
        pConnDone.setStrokeWidth(connW);
        pConnDone.setStrokeCap(Paint.Cap.ROUND);

        pConnTodo.setStyle(Paint.Style.STROKE);
        pConnTodo.setStrokeWidth(connW);
        pConnTodo.setStrokeCap(Paint.Cap.ROUND);
        pConnTodo.setColor(Color.parseColor("#DDD7F0"));

        // ---- Coin shadow (light, not harsh) ----
        pCoinShadow.setStyle(Paint.Style.FILL);
        pCoinShadow.setColor(Color.parseColor("#22000000"));

        // ---- Coin top/bottom (set in draw per state) ----
        pCoinBottom.setStyle(Paint.Style.FILL);
        pCoinTop.setStyle(Paint.Style.FILL);

        // ---- Inner highlight ring ----
        pCoinHighlight.setStyle(Paint.Style.STROKE);
        pCoinHighlight.setStrokeWidth(2.5f * dp);
        pCoinHighlight.setColor(Color.parseColor("#55FFFFFF"));

        // ---- Locked node ----
        pLockBottom.setStyle(Paint.Style.FILL);
        pLockBottom.setColor(Color.parseColor("#C8BEE8"));
        pLockTop.setStyle(Paint.Style.FILL);
        pLockTop.setColor(Color.parseColor("#E5DDF8"));
        pLockBorder.setStyle(Paint.Style.STROKE);
        pLockBorder.setStrokeWidth(2.5f * dp);
        pLockBorder.setColor(Color.parseColor("#BFB3E0"));

        // ---- Gold (mastered) ----
        pGoldBottom.setStyle(Paint.Style.FILL);
        pGoldBottom.setColor(Color.parseColor("#C8860A"));
        pGoldTop.setStyle(Paint.Style.FILL);
        pGoldTop.setColor(Color.parseColor("#FFD700"));

        // ---- Number on active nodes ----
        pNumber.setStyle(Paint.Style.FILL);
        pNumber.setColor(Color.WHITE);
        pNumber.setTextAlign(Paint.Align.CENTER);
        pNumber.setFakeBoldText(true);
        pNumber.setTextSize(22 * dp);

        // ---- Number on locked nodes ----
        pLockNum.setStyle(Paint.Style.FILL);
        pLockNum.setColor(Color.parseColor("#9B8DC0"));
        pLockNum.setTextAlign(Paint.Align.CENTER);
        pLockNum.setTextSize(20 * dp);

        // ---- Checkmark path ----
        pCheckPath.setStyle(Paint.Style.STROKE);
        pCheckPath.setColor(Color.WHITE);
        pCheckPath.setStrokeWidth(3.5f * dp);
        pCheckPath.setStrokeCap(Paint.Cap.ROUND);
        pCheckPath.setStrokeJoin(Paint.Join.ROUND);

        // ---- Star text (mastered) ----
        pStarText.setStyle(Paint.Style.FILL);
        pStarText.setColor(Color.WHITE);
        pStarText.setTextAlign(Paint.Align.CENTER);
        pStarText.setTextSize(24 * dp);
        pStarText.setFakeBoldText(true);

        // ---- Pulse ring ----
        pPulse.setStyle(Paint.Style.STROKE);
        pPulse.setStrokeWidth(3f * dp);

        // ---- Tooltip ----
        pTipBg.setStyle(Paint.Style.FILL);
        pTipBg.setColor(Color.WHITE);

        pTipBorder.setStyle(Paint.Style.STROKE);
        pTipBorder.setStrokeWidth(2f * dp);

        pTipText.setStyle(Paint.Style.FILL);
        pTipText.setTextAlign(Paint.Align.CENTER);
        pTipText.setFakeBoldText(true);
        pTipText.setTextSize(14 * dp);

        // ---- Quarter dividers ----
        pDivLine.setStyle(Paint.Style.STROKE);
        pDivLine.setStrokeWidth(1.5f * dp);
        pDivLine.setColor(Color.parseColor("#C8BEE8"));

        pPillBg.setStyle(Paint.Style.FILL);

        pPillText.setStyle(Paint.Style.FILL);
        pPillText.setColor(Color.WHITE);
        pPillText.setTextAlign(Paint.Align.CENTER);
        pPillText.setFakeBoldText(true);
        pPillText.setTextSize(11 * dp);

        // ---- Pulse animator ----
        pulseAnim = ValueAnimator.ofFloat(0f, 1f);
        pulseAnim.setDuration(1400);
        pulseAnim.setRepeatMode(ValueAnimator.REVERSE);
        pulseAnim.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnim.setInterpolator(new LinearInterpolator());
        pulseAnim.addUpdateListener(a -> {
            pulsePhase = (float) a.getAnimatedValue();
            invalidate();
        });
        pulseAnim.start();

        // Software layer is required for reliable custom drawing (text + translucent fills)
        // across all API levels and devices. Hardware layer caused blank renders on some devices.
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    public void setModuleColor(int main, int bottom) {
        colorMain   = main;
        colorBottom = bottom;
        invalidate();
    }

    public void setNodes(List<NodeView> list) {
        nodes = list != null ? list : new ArrayList<>();
        invalidate();
    }

    public void setOnNodeClickListener(OnNodeClickListener l) {
        clickListener = l;
    }

    // ─── Draw ────────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // View not yet measured — skip until we have real dimensions
        if (getWidth() == 0 || getHeight() == 0) return;

        if (nodes.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }

        drawDividers(canvas);
        drawConnectors(canvas);

        // Draw completed/locked nodes first, CURRENT on top
        for (NodeView n : nodes) {
            if (n.getState() != NodeView.NodeState.CURRENT) drawNode(canvas, n);
        }
        for (NodeView n : nodes) {
            if (n.getState() == NodeView.NodeState.CURRENT) drawNode(canvas, n);
        }
    }

    // ─── Quarter dividers ────────────────────────────────────────────────────

    private void drawDividers(Canvas canvas) {
        float w  = getWidth();
        float h  = getHeight();
        float dp = density();
        float pH = 14 * dp;   // pill half-height
        float pR = 12 * dp;   // pill corner radius

        pPillBg.setColor(colorMain);

        for (int i = 0; i < DIVIDER_Y_PCT.length; i++) {
            float cy = DIVIDER_Y_PCT[i] / 100f * h;

            // Measure pill width
            float textW = pPillText.measureText(DIVIDER_LABEL[i]);
            float pW = textW / 2 + 18 * dp;   // half-width

            // Lines either side of pill
            canvas.drawLine(32 * dp, cy, w / 2 - pW - 8 * dp, cy, pDivLine);
            canvas.drawLine(w / 2 + pW + 8 * dp, cy, w - 32 * dp, cy, pDivLine);

            // Pill
            RectF pill = new RectF(w / 2 - pW, cy - pH, w / 2 + pW, cy + pH);
            canvas.drawRoundRect(pill, pR, pR, pPillBg);
            canvas.drawText(DIVIDER_LABEL[i], w / 2, cy + pPillText.getTextSize() * 0.35f, pPillText);
        }
    }

    // ─── Connectors ──────────────────────────────────────────────────────────

    private void drawConnectors(Canvas canvas) {
        float w = getWidth();
        float h = getHeight();

        pConnDone.setColor(alphaColor(colorMain, 0.45f));

        for (int i = 0; i < nodes.size() - 1; i++) {
            NodeView a = nodes.get(i);
            NodeView b = nodes.get(i + 1);

            float x1 = nx(a, w), y1 = ny(a, h);
            float x2 = nx(b, w), y2 = ny(b, h);

            boolean done = a.getState() == NodeView.NodeState.COMPLETED
                    || a.getState() == NodeView.NodeState.MASTERED;

            canvas.drawLine(x1, y1, x2, y2, done ? pConnDone : pConnTodo);
        }
    }

    // ─── Single node ─────────────────────────────────────────────────────────

    private void drawNode(Canvas canvas, NodeView node) {
        float w  = getWidth();
        float h  = getHeight();
        float cx = nx(node, w);
        float cy = ny(node, h);
        float r  = node.isFinalAssessment() ? finalR : nodeR;

        NodeView.NodeState state = node.getState();

        // 1. Pulse ring (CURRENT only)
        if (state == NodeView.NodeState.CURRENT) {
            float ext = r * 0.50f * pulsePhase;
            int alpha = (int) (180 * (1f - pulsePhase));
            pPulse.setColor(colorMain);
            pPulse.setAlpha(alpha);
            canvas.drawCircle(cx, cy, r + ext, pPulse);
        }

        // 2. Coin shadow
        canvas.drawCircle(cx + 1.5f, cy + 5, r, pCoinShadow);

        // 3. Bottom (3-D edge)
        Paint btm = coinBottomPaint(state);
        canvas.drawCircle(cx, cy + coinDy, r, btm);

        // 4. Top face
        Paint top = coinTopPaint(state);
        canvas.drawCircle(cx, cy, r, top);

        // 5. Border / highlight
        if (state == NodeView.NodeState.LOCKED) {
            canvas.drawCircle(cx, cy, r, pLockBorder);
        } else {
            canvas.drawCircle(cx, cy - r * 0.2f, r * 0.65f, pCoinHighlight);
        }

        // 6. Icon
        drawIcon(canvas, node, cx, cy, r);

        // 7. START badge above CURRENT
        if (state == NodeView.NodeState.CURRENT) {
            drawStartBadge(canvas, cx, cy - r);
        }
    }

    private Paint coinBottomPaint(NodeView.NodeState state) {
        switch (state) {
            case LOCKED: return pLockBottom;
            case MASTERED: return pGoldBottom;
            case COMPLETED:
                pCoinBottom.setColor(dimColor(colorMain, 0.72f));
                return pCoinBottom;
            default:
                pCoinBottom.setColor(dimColor(colorMain, 0.65f));
                return pCoinBottom;
        }
    }

    private Paint coinTopPaint(NodeView.NodeState state) {
        switch (state) {
            case LOCKED: return pLockTop;
            case MASTERED: return pGoldTop;
            case COMPLETED:
                pCoinTop.setColor(dimColor(colorMain, 0.82f));
                return pCoinTop;
            default:
                pCoinTop.setColor(colorMain);
                return pCoinTop;
        }
    }

    private void drawIcon(Canvas canvas, NodeView node, float cx, float cy, float r) {
        float dp = density();

        switch (node.getState()) {
            case LOCKED:
                // Node number in muted purple
                pLockNum.setTextSize(r * 0.62f);
                canvas.drawText(String.valueOf(node.getNodeNumber()),
                        cx, cy + pLockNum.getTextSize() * 0.37f, pLockNum);
                break;

            case COMPLETED:
                // Drawn checkmark — no emoji
                drawCheckmark(canvas, cx, cy, r * 0.42f);
                break;

            case MASTERED:
                pStarText.setTextSize(r * 0.72f);
                canvas.drawText("★", cx, cy + pStarText.getTextSize() * 0.36f, pStarText);
                break;

            default: // UNLOCKED / CURRENT
                if (node.isFinalAssessment()) {
                    // Trophy icon drawn
                    drawTrophy(canvas, cx, cy, r * 0.46f);
                } else {
                    pNumber.setTextSize(r * 0.68f);
                    canvas.drawText(String.valueOf(node.getNodeNumber()),
                            cx, cy + pNumber.getTextSize() * 0.37f, pNumber);
                }
                break;
        }
    }

    /** Draw a simple ✓ checkmark using Path */
    private void drawCheckmark(Canvas canvas, float cx, float cy, float size) {
        Path p = new Path();
        p.moveTo(cx - size, cy);
        p.lineTo(cx - size * 0.2f, cy + size * 0.8f);
        p.lineTo(cx + size, cy - size * 0.7f);
        pCheckPath.setColor(Color.WHITE);
        pCheckPath.setStrokeWidth(density() * 3.5f);
        canvas.drawPath(p, pCheckPath);
    }

    /** Simple filled trophy silhouette */
    private void drawTrophy(Canvas canvas, float cx, float cy, float size) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.WHITE);

        // Cup body: rounded rect
        RectF cup = new RectF(cx - size * 0.8f, cy - size * 0.9f,
                cx + size * 0.8f, cy + size * 0.4f);
        canvas.drawRoundRect(cup, size * 0.3f, size * 0.3f, p);

        // Stem
        RectF stem = new RectF(cx - size * 0.2f, cy + size * 0.4f,
                cx + size * 0.2f, cy + size * 0.85f);
        canvas.drawRect(stem, p);

        // Base
        RectF base = new RectF(cx - size * 0.55f, cy + size * 0.8f,
                cx + size * 0.55f, cy + size * 1.0f);
        canvas.drawRoundRect(base, size * 0.1f, size * 0.1f, p);
    }

    // ─── START tooltip ───────────────────────────────────────────────────────

    private void drawStartBadge(Canvas canvas, float cx, float topEdge) {
        float dp   = density();
        float gap  = 10 * dp;
        float padX = 16 * dp;
        float padY =  7 * dp;
        float cr   = 20 * dp;    // corner radius
        float arrH =  9 * dp;    // arrow height

        String text = "START";
        pTipText.setColor(colorMain);
        pTipText.setTextSize(13 * dp);

        float textW = pTipText.measureText(text);
        float bW    = textW + padX * 2;
        float bH    = pTipText.getTextSize() + padY * 2;

        float bBot  = topEdge - gap;
        float bTop  = bBot - bH;
        float bL    = cx - bW / 2;
        float bR    = cx + bW / 2;

        // Pill background
        RectF rect = new RectF(bL, bTop, bR, bBot);
        canvas.drawRoundRect(rect, cr, cr, pTipBg);

        // Border in module colour
        pTipBorder.setColor(alphaColor(colorMain, 0.5f));
        canvas.drawRoundRect(rect, cr, cr, pTipBorder);

        // Arrow pointer
        Path arrow = new Path();
        arrow.moveTo(cx - 7 * dp, bBot);
        arrow.lineTo(cx + 7 * dp, bBot);
        arrow.lineTo(cx, bBot + arrH);
        arrow.close();
        canvas.drawPath(arrow, pTipBg);

        // Text
        float textY = bBot - padY - pTipText.descent();
        canvas.drawText(text, cx, textY, pTipText);
    }

    // ─── Touch ───────────────────────────────────────────────────────────────

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && clickListener != null) {
            float tx = event.getX(), ty = event.getY();
            float w = getWidth(), h = getHeight();
            for (NodeView n : nodes) {
                float dist = dist(tx, ty, nx(n, w), ny(n, h));
                float r    = n.isFinalAssessment() ? finalR : nodeR;
                if (dist <= Math.max(r, hitR)) {
                    clickListener.onNodeClick(n);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private float nx(NodeView n, float w) {
        int i = n.getNodeNumber() - 1;
        return (i >= 0 && i < NODE_X_PCT.length) ? NODE_X_PCT[i] / 100f * w : w / 2;
    }

    private float ny(NodeView n, float h) {
        int i = n.getNodeNumber() - 1;
        return (i >= 0 && i < NODE_Y_PCT.length) ? NODE_Y_PCT[i] / 100f * h : h / 2;
    }

    private float dist(float x1, float y1, float x2, float y2) {
        float dx = x1 - x2, dy = y1 - y2;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float density() {
        return getContext().getResources().getDisplayMetrics().density;
    }

    /** Darken/lighten a colour by factor (0 = black, 1 = original). */
    private int dimColor(int c, float f) {
        return Color.rgb(
                Math.min(255, (int) (Color.red(c)   * f)),
                Math.min(255, (int) (Color.green(c) * f)),
                Math.min(255, (int) (Color.blue(c)  * f)));
    }

    /** Apply alpha (0–1) to a colour. */
    private int alphaColor(int c, float alpha) {
        return Color.argb((int) (alpha * 255),
                Color.red(c), Color.green(c), Color.blue(c));
    }

    private void drawEmptyState(Canvas canvas) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(Color.parseColor("#9B8DC0"));
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(16 * density());
        canvas.drawText("Loading lessons…", getWidth() / 2f, getHeight() / 2f, p);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (pulseAnim != null) pulseAnim.cancel();
    }
}