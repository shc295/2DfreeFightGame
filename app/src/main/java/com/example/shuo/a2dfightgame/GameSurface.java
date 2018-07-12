package com.example.shuo.a2dfightgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.inputmethod.CursorAnchorInfo;

/**
 * Created by shuo on 7/11/2018.
 */

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {


    private GameThread gameThread;

    private ChibiCharacter chibi1;

    //background image
    private Bitmap background;
    private Rect rect;
    private int dWidth, dHeight;


    public GameSurface(Context context)  {
        super(context);

        // Make Game Surface focusable so it can handle events. .
        this.setFocusable(true);

        // Sét callback.
        this.getHolder().addCallback(this);
    }

    public void update()  {
        this.chibi1.update();
    }



    @Override
    public void draw(Canvas canvas)  {
        super.draw(canvas);
        canvas.drawBitmap(background,null,rect,null);
        this.chibi1.draw(canvas);
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Bitmap chibiBitmap1 = BitmapFactory.decodeResource(this.getResources(),R.drawable.chibi1);
        background = BitmapFactory.decodeResource(this.getResources(),R.drawable.bkg);
        Display display = ((Activity)getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dWidth = size.x;
        dHeight = size.y;
        rect = new Rect(0,0,dWidth,dHeight);
        this.chibi1 = new ChibiCharacter(this,chibiBitmap1,100,50);

        this.gameThread = new GameThread(this,holder);
        this.gameThread.setRunning(true);
        this.gameThread.start();
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry= true;
        while(retry) {
            try {
                this.gameThread.setRunning(false);

                // Parent thread must wait until the end of GameThread.
                this.gameThread.join();
            }catch(InterruptedException e)  {
                e.printStackTrace();
            }
            retry= true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            int x = (int)event.getX();
            int y = (int) event.getY();

            int movingVectorX = x-this.chibi1.getX();
            int movingVectorY = y-this.chibi1.getY();

            this.chibi1.setMovingVector(movingVectorX,movingVectorY);
            return true;
        }
        return false;
    }

}