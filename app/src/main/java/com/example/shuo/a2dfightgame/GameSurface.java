package com.example.shuo.a2dfightgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
    private Bitmap bkgReverse;
    private int dWidth, dHeight;
    private boolean reverseBKG = false;
    int dBkg = 2;
    int bgrScroll=0;

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
        Rect fromRect1 = new Rect(0,0,dWidth-bgrScroll,dHeight);
        Rect toRect1 = new Rect(bgrScroll,0,dWidth,dHeight);

        Rect fromRect2 = new Rect(dWidth-bgrScroll,0,dWidth,dHeight);
        Rect toRect2 = new Rect(0,0,bgrScroll,dHeight);
        if (!reverseBKG) {
            canvas.drawBitmap(background, fromRect1, toRect1, null);
            canvas.drawBitmap(bkgReverse, fromRect2, toRect2, null);
        }
        else{
            canvas.drawBitmap(background, fromRect2, toRect2, null);
            canvas.drawBitmap(bkgReverse, fromRect1, toRect1, null);
        }

        if ( (bgrScroll += dBkg) >= dWidth) {
            bgrScroll = 0;
            reverseBKG = !reverseBKG;
        }

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
        background = Bitmap.createScaledBitmap(background,dWidth,dHeight,true);
        Matrix matrix = new Matrix();  //Like a frame or mould for an image.
        matrix.setScale(-1, 1); //Horizontal mirror effect.
        bkgReverse = Bitmap.createBitmap(background, 0, 0, dWidth, dHeight, matrix, true);
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