package com.blogspot.pointer_overloading.circlethecat;

import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;


public class MainActivity extends ActionBarActivity implements View.OnTouchListener {
    Board mBoard;
    AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.SMART_BANNER);
        mAdView.setAdUnitId("ca-app-pub-5442216621719819/6934816282");
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mAdView.setLayoutParams(lp);
        mAdView.loadAd(new AdRequest.Builder().build());

        super.onCreate(savedInstanceState);
        mBoard = new Board(this, BitmapFactory.decodeResource(getResources(), R.drawable.cat_sprite));
        mBoard.setOnTouchListener(this);

        RelativeLayout layout = new RelativeLayout(this);
        layout.addView(mBoard);
        layout.addView(mAdView);
        setContentView(layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBoard.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBoard.resume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_reset) {
            mBoard.resetBoard();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mBoard.addTouchEvent(event.getX(), event.getY());
        return true;
    }
}
