package com.example.shuo.a2dfightgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by shuo on 7/11/2018.
 */

public class ChibiCharacter extends GameObject {
    private static final int ROW_TOP_TO_BOTTOM = 0;
    private static final int ROW_RIGHT_TO_LEFT = 1;
    private static final int ROW_LEFT_TO_RIGHT = 2;
    private static final int ROW_BOTTOM_TO_TOP = 3;

    private int rowUsing = ROW_LEFT_TO_RIGHT;

    private int colUsing;

    private Bitmap[] leftToRights;
    private Bitmap[] rightToLefts;
    private Bitmap[] topToBottoms;
    private Bitmap[] bottomToTops;

    public static final float VELOCITY = 0.5f;

    private int movingVectorX = 10;
    private int movingVectorY = 15;

    private long lastDrawNanoTime = -1;

    private GameSurface gameSurface;

    public ChibiCharacter(GameSurface gameSurface,Bitmap image, int x, int y){
        super(image,4,3,x,y);
        this.gameSurface = gameSurface;

        this.topToBottoms = new Bitmap[colCount];
        this.bottomToTops = new Bitmap[colCount];
        this.leftToRights = new Bitmap[colCount];
        this.rightToLefts = new Bitmap[colCount];

        for(int i=0;i<this.colCount;i++){
            this.topToBottoms[i] = this.createSubImageAt(ROW_TOP_TO_BOTTOM,i);
            this.bottomToTops[i] = this.createSubImageAt(ROW_BOTTOM_TO_TOP,i);
            this.leftToRights[i] = this.createSubImageAt(ROW_LEFT_TO_RIGHT,i);
            this.rightToLefts[i] = this.createSubImageAt(ROW_RIGHT_TO_LEFT,i);
        }
    }

    public Bitmap[] getMoveBitMaps(){
        switch (rowUsing){
            case ROW_BOTTOM_TO_TOP:
                return this.bottomToTops;
            case ROW_TOP_TO_BOTTOM:
                return this.topToBottoms;
            case ROW_LEFT_TO_RIGHT:
                return this.leftToRights;
            case ROW_RIGHT_TO_LEFT:
                return this.rightToLefts;
            default:
                return null;
        }
    }

    public Bitmap getCurrentMoveBitmap(){
        Bitmap[] bitmaps = this.getMoveBitMaps();
        return bitmaps[this.colUsing];
    }

    public void update(){
        this.colUsing++;
        if(colUsing>=this.colCount){
            this.colUsing = 0;
        }

        long now = System.nanoTime();

        if(lastDrawNanoTime == -1){
            lastDrawNanoTime = now;
        }

        int deltatime = (int) ((now-lastDrawNanoTime)/1000000);

        float distance = VELOCITY*deltatime;

        double movingVectorLength = Math.sqrt(movingVectorX*movingVectorX+movingVectorY*movingVectorY);

        this.x = x+(int)(distance*movingVectorX/movingVectorLength);
        this.y = y+(int)(distance*movingVectorY/movingVectorLength);

        if(this.x<0){
            this.x = 0;
            this.movingVectorX = - movingVectorX;
        }
        else if(this.x>this.gameSurface.getWidth()-width){
            this.x = this.gameSurface.getWidth()-width;
            this.movingVectorX = -this.movingVectorX;
        }

        if(this.y<0){
            this.y = 0;
            this.movingVectorY = -this.movingVectorY;
        }
        else if(this.y>this.gameSurface.getHeight()-height){
            this.y = this.gameSurface.getHeight() -height;
            this.movingVectorY = -this.movingVectorY;
        }

        if(movingVectorX>0){
            if(movingVectorY>0&&Math.abs(movingVectorX)<Math.abs(movingVectorY)){
                rowUsing = ROW_TOP_TO_BOTTOM;
            }
            else if(movingVectorY<0&&movingVectorX<Math.abs(movingVectorY)){
                rowUsing = ROW_BOTTOM_TO_TOP;
            }else{
                rowUsing = ROW_LEFT_TO_RIGHT;
            }
        }
        else{
            if(movingVectorY>0&&Math.abs(movingVectorX)<Math.abs(movingVectorY)){
                rowUsing = ROW_TOP_TO_BOTTOM;
            }
            else if(movingVectorY<0&&Math.abs(movingVectorX)<Math.abs(movingVectorY)){
                rowUsing = ROW_BOTTOM_TO_TOP;
            }else{
                rowUsing = ROW_RIGHT_TO_LEFT;
            }
        }
    }

    public void draw(Canvas canvas){
        Bitmap bitmap = this.getCurrentMoveBitmap();
        //TODO: what is x,y points for?
        canvas.drawBitmap(bitmap,x,y,null);

        this.lastDrawNanoTime = System.nanoTime();
    }

    public void setMovingVector(int movingVectorX,int movingVectorY){
        this.movingVectorX = movingVectorX;
        this.movingVectorY = movingVectorY;
    }
}
