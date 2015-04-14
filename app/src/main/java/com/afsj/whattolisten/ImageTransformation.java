package com.afsj.whattolisten;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import com.squareup.picasso.Transformation;

/**
 * Created by ilia on 14.04.15.
 */
public class ImageTransformation implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {
        int brightness = -50;
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        1, 0, 0, 0, brightness,
                        0, 1, 0, 0, brightness,
                        0, 0, 1, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(source, 0, 0, paint);

        source.recycle();
        return ret;
    }

    @Override
    public String key() { return "square()"; }
}