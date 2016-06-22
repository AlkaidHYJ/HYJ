package com.blogspot.pointer_overloading.circlethecat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.Pair;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by alhaad on 7/19/15.
 */
public class Board extends SurfaceView {
    final SurfaceHolder mSurfaceHolder;
    // The number of tiles on a square board. Prefer an odd number.
    final static int mBoardEdgeSize = 11;
    final Boolean[][] mBoardContent = new Boolean[][]{
            { false, false, false, false, false, false, false, false, false, false, false },
            { false, false, false, false, false, false, false, false, false, false, false },
            { false, false, false, false, false, false, false, false, false, false, false },
            { false, false, false, false, false, false, false, false, false, false, false },
            { false, false, false, false, false, false, false, false, false, false, false },
            { false, false, false, false, false, false, false, false, false, false, false },
            { false, false, false, false, false, false, false, false, false, false, false },
            { false, false, false, false, false, false, false, false, false, false, false },
            { false, false, false, false, false, false, false, false, false, false, false },
            { false, false, false, false, false, false, false, false, false, false, false },
            { false, false, false, false, false, false, false, false, false, false, false }
    };
    private Queue<Pair> mTouchQueue;
    private final Lock mLock;
    private Cat mCat;
    private final Activity mActivity;
    private final Bitmap mCatBitmap;

    private Boolean mIsRunning = false;
    private Thread mThread;
    private Boolean mHasWon = false;

    public Board(Activity activity, Bitmap catBitmap) {
        super(activity);
        mActivity = activity;
        mSurfaceHolder = getHolder();
        mTouchQueue = new LinkedList<Pair>();
        mLock = new ReentrantLock(true);
        mCatBitmap = catBitmap;
        initializeBoard();
    }

    private void initializeBoard() {
        mCat = new Cat(mCatBitmap);
        Random r = new Random();
        for (int i = 0; i < mBoardEdgeSize; i++) {
            mBoardContent[r.nextInt(mBoardEdgeSize)][r.nextInt(mBoardEdgeSize)] = true;
        }
        mBoardContent[4][5] = false;
    }

    public void resetBoard() {
        pause();
        mTouchQueue = new LinkedList<Pair>();
        mHasWon = false;
        for (int i = 0; i < mBoardEdgeSize; i++) {
            for (int j = 0; j < mBoardEdgeSize; j++) {
                mBoardContent[i][j] = false;
            }
        }
        initializeBoard();
        resume();
    }

    public void addTouchEvent(float x, float y) {
        mLock.lock();
        mTouchQueue.add(new Pair(x, y));
        mLock.unlock();
        synchronized (mRenderUI) {
            mRenderUI.notify();
        }
    }

    private Runnable mResetActivity = new Runnable() {
        @Override
        public void run() {
            resetBoard();
        }
    };

    private Runnable mRenderUI = new Runnable() {
        @Override
        public void run() {
            while (mIsRunning) {
                if ((mCat.hasEscaped() || mHasWon) && !mCat.isAnimating()) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    (new Handler(mActivity.getMainLooper())).post(mResetActivity);
                    return;
                }

                if (!mSurfaceHolder.getSurface().isValid()) {
                    continue;
                }

                Canvas canvas = mSurfaceHolder.lockCanvas();
                float trans = (Math.max(canvas.getWidth(), canvas.getHeight()) - Math.min(canvas.getWidth(), canvas.getHeight())) / 2;
                float xTrans, yTrans;
                if (canvas.getHeight() > canvas.getWidth()) {
                    xTrans = 0;
                    yTrans = trans;
                } else {
                    xTrans = trans;
                    yTrans = 0;
                }
                if (canvas.getHeight() > canvas.getWidth()) {
                    canvas.translate(xTrans, yTrans);
                    canvas.drawARGB(255, 255, 255, 255);
                    if (!mCat.isAnimating()) {
                        processTouchEvents(canvas, xTrans, yTrans);
                    }
                    for (int i = 0; i < mBoardContent.length; i++) {
                        for (int j = 0; j < mBoardContent[0].length; j++) {
                            drawCircle(canvas, i, j, mBoardContent[i][j]);
                        }
                    }
                    mCat.draw(canvas);
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                    if (mCat.isAnimating()) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    synchronized (mRenderUI) {
                        if (mIsRunning && !mCat.isAnimating() && !mCat.hasEscaped() && !mHasWon) {
                            try {
                                mRenderUI.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    };

    private void processTouchEvents(Canvas canvas, float xTrans, float yTrans) {
        float boardSize = Math.min(canvas.getWidth(), canvas.getHeight());
        float rectBoundSize = (float) (boardSize / ((float) mBoardEdgeSize + 0.5));
        mLock.lock();
        while (!mTouchQueue.isEmpty()) {
            Pair p = mTouchQueue.remove();
            float x = (float) p.first;
            float y = (float) p.second;
            x -= xTrans;
            y-= yTrans;

            int i;
            int j = (int) ((y / rectBoundSize));
            if (j % 2 == 1) {
                i = (int) (((x - rectBoundSize/2) / rectBoundSize));
            } else {
                i = (int) ((x / rectBoundSize));
            }

            if (i < 0 || i >= mBoardEdgeSize || j < 0 || j >= mBoardEdgeSize) {
                continue;
            }

            Pair catPos = mCat.position();
            if (mBoardContent[i][j] == true || (catPos.first == i && catPos.second == j)) {
                continue;
            }
            mBoardContent[i][j] = true;
            BoardAI ai = new BoardAI(mBoardContent, (int)catPos.first, (int)catPos.second);
            int move = ai.nextMove();
            if (move >= 0) {
                mCat.move(move);
            } else if (move == -2) {
                mCat.escape();
            } else if (move == -1) {
                mHasWon = true;
            }
        }
        mLock.unlock();
    }

    private void drawCircle(Canvas canvas, int x, int y, Boolean isSelected) {
        float boardSize = Math.min(canvas.getWidth(), canvas.getHeight());
        float rectBoundSize = (float) (boardSize / ((float) mBoardEdgeSize + 0.5));
        float centreX = x * rectBoundSize + (rectBoundSize / 2);
        float centreY = y * rectBoundSize + (rectBoundSize / 2);
        if (y % 2 == 1) {
            centreX += (rectBoundSize / 2);
        }
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        if (isSelected) {
            paint.setColor(Color.rgb(34, 139, 34));
        } else {
            paint.setColor(Color.rgb(125, 255, 0));
        }
        canvas.drawCircle(centreX, centreY, rectBoundSize / 2, paint);
    }

    public void pause() {
        synchronized (mRenderUI) {
            mIsRunning = false;
            mRenderUI.notify();
        }
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        mIsRunning = true;
        mThread = new Thread(mRenderUI);
        mThread.start();
    }
}
