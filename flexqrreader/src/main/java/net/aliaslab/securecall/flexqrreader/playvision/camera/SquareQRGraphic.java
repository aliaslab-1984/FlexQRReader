package net.aliaslab.securecall.flexqrreader.playvision.camera;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import androidx.annotation.NonNull;

public class SquareQRGraphic extends GraphicOverlay.Graphic{

    private final Paint paint = new Paint();
    private final int LINE_SIZE = 10;

    public SquareQRGraphic(GraphicOverlay overlay) {
        super(overlay);
        paint.setColor(0xFFFFFF);
    }

    @Override
    public void draw(@NonNull final Canvas canvas) {

        Log.d("GRAPHIC" , "drawing square");
        final int h = canvas.getHeight();
        final int w  = canvas.getWidth();
        final int l = (int) (Math.min(h,w) * 1.85);

        canvas.drawRect((w-l)/2F,(h-l)/2F,(w-l)/2F + LINE_SIZE,(h-l)/2F + l/3F, paint);
    }
}
