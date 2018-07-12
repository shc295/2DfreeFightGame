package com.example.shuo.a2dfightgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by shuo on 7/12/2018.
 */

public class Explosion extends GameObject{
    private int rowIndex = 0;
    private int colIndex = -1;
    private boolean finish = false;
    private GameSurface gameSurface;
    public Explosion(GameSurface gameSurface, Bitmap image, int x, int y){
        super(image,5,5,x,y);

        this.gameSurface = gameSurface;
    }

    public void update(){
        this.colIndex++;
        if(this.colIndex==0&&this.rowIndex==0){
            this.gameSurface.playSoundExplosion();
        }

        if(colIndex>=this.colCount) {
            rowIndex++;
            colIndex = 0;
            if (rowIndex >= this.rowCount) {
                this.finish = true;
            }
        }
    }

    public void draw(Canvas canvas){
        if(!finish){
            Bitmap bitmap = this.createSubImageAt(rowIndex,colIndex);
            canvas.drawBitmap(bitmap,this.x,this.y,null);

        }
    }

    public boolean isFinish(){
        return finish;
    }
}
