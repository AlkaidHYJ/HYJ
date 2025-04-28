package com.blogspot.pointer_overloading.circlethecat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import androidx.core.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Random;

/**
 * Created by alhaad on 7/19/15.
 */
public class Board extends SurfaceView implements SurfaceHolder.Callback {
    public static final int BOARD_EDGE_SIZE = 11;
    private final SurfaceHolder mSurfaceHolder;
    private final boolean[][] mBoardContent = new boolean[BOARD_EDGE_SIZE][BOARD_EDGE_SIZE];
    private final ConcurrentLinkedQueue<Pair<Float, Float>> mTouchQueue = new ConcurrentLinkedQueue<>();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean mIsRunning = new AtomicBoolean(false);
    @SuppressWarnings("unused")
    private final Activity mActivity;
    private final Bitmap mCatBitmap;
    private final Paint mPaint = new Paint();
    
    private Cat mCat;
    private Thread mThread;
    private boolean mHasWon = false;

    public Board(Context context) {
        super(context);
        mActivity = (Activity) context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mCatBitmap = null;
        initializeBoard();
    }

    public Board(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (Activity) context;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mCatBitmap = null;
        initializeBoard();
    }

    public Board(Activity activity, Bitmap catBitmap) {
        super(activity);
        mActivity = activity;
        mCatBitmap = catBitmap;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        initializeBoard();
    }

    private void initializeBoard() {
        mCat = new Cat(mCatBitmap);
        Random r = new Random();
        for (int i = 0; i < BOARD_EDGE_SIZE; i++) {
            mBoardContent[r.nextInt(BOARD_EDGE_SIZE)][r.nextInt(BOARD_EDGE_SIZE)] = true;
        }
        mBoardContent[4][5] = false;
    }

    public void resetBoard() {
        pause();
        mTouchQueue.clear();
        mHasWon = false;
        for (int i = 0; i < BOARD_EDGE_SIZE; i++) {
            for (int j = 0; j < BOARD_EDGE_SIZE; j++) {
                mBoardContent[i][j] = false;
            }
        }
        initializeBoard();
        resume();
    }

    public void addTouchEvent(float x, float y) {
        mTouchQueue.add(new Pair<>(x, y));
        mMainHandler.post(this::render);
    }

    private final Runnable mResetActivity = this::resetBoard;

    private void render() {
        if (!mIsRunning.get() || !mSurfaceHolder.getSurface().isValid()) {
            return;
        }

        Canvas canvas = mSurfaceHolder.lockCanvas();
        if (canvas == null) return;

        try {
            float width = (float) canvas.getWidth();
            float height = (float) canvas.getHeight();
            float trans = (Math.max(width, height) - Math.min(width, height)) / 2f;
            float xTrans = height > width ? 0 : trans;
            float yTrans = height > width ? trans : 0;

            canvas.translate(xTrans, yTrans);
            canvas.drawARGB(255, 255, 255, 255);

            if (!mCat.isAnimating()) {
                processTouchEvents(canvas, xTrans, yTrans);
            }

            for (int i = 0; i < BOARD_EDGE_SIZE; i++) {
                for (int j = 0; j < BOARD_EDGE_SIZE; j++) {
                    drawCircle(canvas, i, j, mBoardContent[i][j]);
                }
            }

            mCat.draw(canvas);

            if (mCat.isAnimating()) {
                mMainHandler.postDelayed(this::render, 100);
            } else if ((mCat.hasEscaped() || mHasWon) && !mCat.isAnimating()) {
                mMainHandler.postDelayed(mResetActivity, 1500);
            }
        } finally {
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void processTouchEvents(Canvas canvas, float xTrans, float yTrans) {
        float boardSize = Math.min(canvas.getWidth(), canvas.getHeight());
        float rectBoundSize = boardSize / (BOARD_EDGE_SIZE + 0.5f);

        while (!mTouchQueue.isEmpty()) {
            Pair<Float, Float> p = mTouchQueue.poll();
            if (p == null) continue;

            float x = p.first - xTrans;
            float y = p.second - yTrans;

            int j = (int) (y / rectBoundSize);
            int i = (int) ((j % 2 == 1) ? ((x - rectBoundSize/2) / rectBoundSize) : (x / rectBoundSize));

            if (i < 0 || i >= BOARD_EDGE_SIZE || j < 0 || j >= BOARD_EDGE_SIZE) {
                continue;
            }

            Pair<Integer, Integer> catPos = mCat.position();
            if (mBoardContent[i][j] || (catPos != null && catPos.first == i && catPos.second == j)) {
                continue;
            }

            mBoardContent[i][j] = true;
            BoardAI ai = new BoardAI(mBoardContent, catPos.first, catPos.second);
            int move = ai.nextMove();
            
            if (move >= 0) {
                mCat.move(move);
            } else if (move == -2) {
                mCat.escape();
            } else if (move == -1) {
                mHasWon = true;
            }
        }
    }

    private void drawCircle(Canvas canvas, int x, int y, boolean isSelected) {
        float boardSize = Math.min(canvas.getWidth(), canvas.getHeight());
        float rectBoundSize = boardSize / (BOARD_EDGE_SIZE + 0.5f);
        float centreX = x * rectBoundSize + (rectBoundSize / 2);
        float centreY = y * rectBoundSize + (rectBoundSize / 2);
        
        if (y % 2 == 1) {
            centreX += (rectBoundSize / 2);
        }

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(isSelected ? Color.rgb(34, 139, 34) : Color.rgb(125, 255, 0));
        canvas.drawCircle(centreX, centreY, rectBoundSize / 2, mPaint);
    }

    public void pause() {
        mIsRunning.set(false);
        if (mThread != null) {
            try {
                mThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void resume() {
        mIsRunning.set(true);
        mThread = new Thread(this::render);
        mThread.start();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        resume();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // Handle surface changes if needed
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        pause();
    }
}
