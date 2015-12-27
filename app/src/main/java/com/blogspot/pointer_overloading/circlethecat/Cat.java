package com.blogspot.pointer_overloading.circlethecat;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Pair;

/**
 * Created by alhaad on 8/2/15.
 */
public class Cat {
    // Present location or destination.
    private int mX, mY;
    // Source for movement.
    private float mXCurrent, mYCurrent;

    // 0-SW, 1-W 2-NW 3-NE 4-E 5-SE
    private int mDirection;

    // 0-8 depending on the movement we want.
    private int mActionNumber;
    private Boolean mIsAnimating;
    private Boolean mHasEscaped;
    private final Bitmap mBitmap;
    private final Bitmap mFlippedBitmap;

    public Cat(Bitmap bitmap) {
        mX = 4;
        mY = 5;
        mDirection = 0;
        mActionNumber = 0;
        mIsAnimating = false;
        mHasEscaped = false;
        mBitmap = bitmap;
        Matrix m = new Matrix();
        m.preScale(-1, 1);
        mFlippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, false);
    }

    public void escape() {
        mHasEscaped = true;
        move(mDirection);
    }

    public boolean hasEscaped() {
        return mHasEscaped;
    }

    public void move(int direction) {
        mIsAnimating = true;
        mDirection = direction;
        if (direction == 0) {
            if (mY % 2 == 0) {
                mX -= 1;
            }
            mY += 1;
        } else if (direction == 1) {
            mX -= 1;
        } else if (direction == 2) {
            if (mY % 2 == 0) {
                mX -= 1;
            }
            mY -= 1;
        } else if (direction == 3) {
            if (mY % 2 == 1) {
                mX += 1;
            }
            mY -= 1;
        } else if (direction == 4) {
            mX += 1;
        } else if (direction == 5) {
            if (mY % 2 == 1) {
                mX += 1;
            }
            mY += 1;
        }
    }

    public Boolean isAnimating() {
        return mIsAnimating;
    }

    public Pair position() {
        return new Pair(mX, mY);
    }
    public void draw(Canvas canvas) {
        // Update action number.
        if (mIsAnimating) {
            mActionNumber = (mActionNumber + 1) % 4;
            if (mActionNumber == 0) {
                mIsAnimating = false;
                if (mHasEscaped) {
                    return;
                }
            }
        }

        // Compute source Rect.
        float boundWidth = mBitmap.getWidth() / 4;
        float boundHeight = mBitmap.getHeight() / 3;
        int selectedCol;
        if (mDirection == 0 || mDirection == 5) {
            selectedCol = 2;
        } else if (mDirection == 1 || mDirection == 4) {
            selectedCol = 0;
        } else {
            selectedCol = 1;
        }
        int selectedRow;
        if (mDirection > 2 && mDirection < 6) {
            selectedRow = 3 - mActionNumber;
        } else {
            selectedRow = mActionNumber;
        }
        int left = (int) (selectedRow * boundWidth);
        int top = (int) (selectedCol * boundHeight);
        int right = (int) ((selectedRow + 1) * boundWidth);
        int bottom = (int) ((selectedCol + 1) * boundHeight);
        Rect src = new Rect(left, top, right, bottom);

        // Compute destination Rect.
        float boardSize = Math.min(canvas.getWidth(), canvas.getHeight());
        float rectBoundSize = (float) (boardSize / 10.5);
        float xSpeed, ySpeed;
        if (mDirection == 0) {
            xSpeed = (float) 0.5;
            ySpeed = (float) 0.866;
        } else if (mDirection == 1) {
            xSpeed = 1;
            ySpeed = 0;
        } else if (mDirection == 2) {
            xSpeed = (float) 0.5;
            ySpeed = (float) 0.866;
        } else if (mDirection == 3) {
            xSpeed = (float) 0.5;
            ySpeed = (float) 0.866;
        } else if (mDirection == 4) {
            xSpeed = 1;
            ySpeed = 0;
        } else {
            xSpeed = (float) 0.5;
            ySpeed = (float) 0.866;
        }
        float centreX = mX * rectBoundSize + (rectBoundSize / 2);
        float centreY = mY * rectBoundSize + (rectBoundSize / 2);
        if (mY % 2 == 1) {
            centreX += (rectBoundSize / 2);
        }
        // For offset.
        centreX -= rectBoundSize / 10;
        centreY -= rectBoundSize / 5;

        // For gradual motion.
        if (!mIsAnimating) {
            mXCurrent = centreX;
            mYCurrent = centreY;
        } else {
            mXCurrent += (centreX - mXCurrent) * xSpeed / 3;
            mYCurrent += (centreY - mYCurrent) * ySpeed / 3;
        }

        RectF dest = new RectF(mXCurrent - (rectBoundSize), mYCurrent - (rectBoundSize), mXCurrent + (rectBoundSize), mYCurrent + (rectBoundSize));

        // Draw image.
        Bitmap bitmap;
        if (mDirection > 2 && mDirection < 6) {
            bitmap = mFlippedBitmap;
        } else {
            bitmap = mBitmap;
        }
        canvas.drawBitmap(bitmap, src, dest, null);

    }
}
