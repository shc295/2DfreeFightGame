package com.example.shuo.a2dfightgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.inputmethod.CursorAnchorInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by shuo on 7/11/2018.
 */

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {


    private GameThread gameThread;

    private final List<ChibiCharacter> chibiList = new ArrayList<ChibiCharacter>();
    private final List<Explosion> explosionList = new ArrayList<Explosion>();

    //background image
    private Bitmap background;
    private Bitmap bkgReverse;
    private int dWidth, dHeight;
    private boolean reverseBKG = false;
    int dBkg = 2;
    int bgrScroll=0;

    //background music and explosion music
    private static final int MAX_STREAMS =100;
    private int soundIdExplosion;
    private int soundIDBackground;

    private boolean soundPoolLoaded;
    private SoundPool soundPool;

    public GameSurface(Context context)  {
        super(context);

        // Make Game Surface focusable so it can handle events. .
        this.setFocusable(true);

        // Sét callback.
        this.getHolder().addCallback(this);

        this.initSoundPool();
    }

    private void initSoundPool(){
        if(Build.VERSION.SDK_INT>=21){
            AudioAttributes audioAttrib = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();

            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(audioAttrib).setMaxStreams(MAX_STREAMS);

            this.soundPool= builder.build();
        }
        else{
            this.soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC,0);
        }
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                soundPoolLoaded = true;
                //
                playSoundBackground();
            }
        });
        this.soundIDBackground = this.soundPool.load(this.getContext(),R.raw.background,1);

        this.soundIdExplosion = this.soundPool.load(this.getContext(),R.raw.explosion,1);
    }

    public void playSoundExplosion(){
        if(this.soundPoolLoaded){
            float leftVolumn = 0.8f;
            float rightVolumn = 0.8f;
            int streamId = this.soundPool.play(this.soundIdExplosion,leftVolumn,rightVolumn,1,0,1f);
        }
    }
    public void playSoundBackground(){
        if(this.soundPoolLoaded){
            float leftVolumn = 0.8f;
            float rightVolumn =0.8f;
            int streamId = this.soundPool.play(this.soundIDBackground,leftVolumn,rightVolumn,1,-1,1f);
        }
    }

    public void update()  {
        for(ChibiCharacter chibi:chibiList){
            chibi.update();
        }
        for(Explosion explosion:this.explosionList){
            explosion.update();
        }

        Iterator<Explosion> iterator = this.explosionList.iterator();
        while(iterator.hasNext()){
            Explosion explosion = iterator.next();
            if(explosion.isFinish()){
                iterator.remove();
                continue;
            }
        }
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

        for(ChibiCharacter chibi:chibiList){
            chibi.draw(canvas);
        }

        for(Explosion explosion:this.explosionList){
            explosion.draw(canvas);
        }
    }

    // Implements method of SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Bitmap chibiBitmap1 = BitmapFactory.decodeResource(this.getResources(),R.drawable.chibi1);
        Bitmap chibiBitmap2 = BitmapFactory.decodeResource(this.getResources(),R.drawable.chibi2);
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
        ChibiCharacter chibi1 = new ChibiCharacter(this,chibiBitmap1,100,50);
        ChibiCharacter chibi2 = new ChibiCharacter(this,chibiBitmap2,300,50);

        chibiList.add(chibi1);
        chibiList.add(chibi2);

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

            Iterator<ChibiCharacter> iterator = this.chibiList.iterator();

            while(iterator.hasNext()){
                ChibiCharacter chibi = iterator.next();
                if(chibi.getX()<x&&x<chibi.getX()+chibi.getWidth()
                        &&chibi.getY()<y&&y<chibi.getY()+chibi.getHeight()){
                    iterator.remove();

                    Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),R.drawable.explosion);
                    Explosion explosion = new Explosion(this,bitmap,chibi.getX(),chibi.getY());

                    this.explosionList.add(explosion);
                }
            }

            for(ChibiCharacter chibi:chibiList){
                int movingVectorX = x-chibi.getX();
                int movingVectorY = y-chibi.getY();
                chibi.setMovingVector(movingVectorX,movingVectorY);
            }
            return true;
        }
        return false;
    }

}