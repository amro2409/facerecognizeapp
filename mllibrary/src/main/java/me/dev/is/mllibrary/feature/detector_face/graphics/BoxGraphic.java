package me.dev.is.mllibrary.feature.detector_face.graphics;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.mlkit.vision.face.Face;

/*draw box bound face*/
public class BoxGraphic extends GraphicOverlay.Graphic {
    private static final String TAG =BoxGraphic.class.getSimpleName() ;

    private static final float BOX_STROKE_WIDTH = 5.0f;

    private final Paint boxPaint;
    private final Face face;

    public BoxGraphic(GraphicOverlay overlay, Face face) {
        super(overlay);
        this.face = face;

        boxPaint = new Paint();
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    @Override
    public void draw(Canvas canvas) {
        if (face == null) {
            return;
        }

        Log.d(TAG, "draw: نحصل على الإحداثيات والمقاييس من وجه المكتشف ");
        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());
        float xOffset = scale(face.getBoundingBox().width() / 2.0f);
        float yOffset = scale(face.getBoundingBox().height() / 2.0f);

        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;

        Log.d(TAG, "draw:رسم المربع حول الوجه");
        canvas.drawRect(left, top, right, bottom, boxPaint);
    }
}

