
package com.is.efacerecognitionmodule.ui.custom_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * A simple graphic View providing a render callback to other classes.
 */
public class GraphicOverlayView extends View {

    private final List<DrawCallback> callbacks = new LinkedList<>();
    private final CircleOverlayView circleOverlayView;

    public GraphicOverlayView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        circleOverlayView = new CircleOverlayView();
    }

    public void addCallback(final DrawCallback callback) {
        callbacks.add(callback);
    }

    @Override
    public synchronized void draw(final Canvas canvas) {
        super.draw(canvas);
        for (final DrawCallback callback : callbacks) {
            callback.drawCallback(canvas);
        }

        circleOverlayView.draw(canvas);

    }


    /**
     * Interface defining the callback for client classes.
     */
    public interface DrawCallback {
        void drawCallback(final Canvas canvas);
    }

    // TODO: 05/09/2024
    class CircleOverlayView {
        private Paint outerPaint;
        private Paint clearPaint;
        private Paint borderPaint;

        private float centerX;
        private float centerY;
        private float radius;

        public CircleOverlayView() {
            init();
        }

        private void init() {
            // إعداد الطلاء (Paint) للجزء الخارجي
            outerPaint = new Paint();
            outerPaint.setColor(Color.BLACK);  // اللون الأسود للجزء الخارجي
            outerPaint.setStyle(Paint.Style.FILL);  // تعبئة اللون
            outerPaint.setAlpha(90);  // شفافية اللون الأسود

            // إعداد الطلاء لإزالة الجزء الداخلي (الدائرة الشفافة)
            //clearPaint = new Paint();
            //clearPaint.setColor(Color.BLACK);  // لون شفاف
            //clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));  // وضع الماسك
            //clearPaint.setAntiAlias(true);  // تحسين الحواف
            //clearPaint.setAlpha(10);  // شفافية اللون الأسود

            // إعداد الطلاء لرسم الحدود الزرقاء
            borderPaint = new Paint();
            borderPaint.setColor(Color.BLUE);  // لون الحدود (أزرق)
            borderPaint.setStyle(Paint.Style.STROKE);  // نمط الحدود (رسم فقط)
            borderPaint.setStrokeWidth(10);  // سمك الحدود (5 بكسل)
            borderPaint.setAntiAlias(true);  // تحسين الحواف

            // إعداد الإحداثيات الخاصة بمركز الدائرة
            centerX = -1;
            centerY = -1;
            radius = 300;  // نصف قطر الدائرة (يمكنك تخصيصه لاحقًا)
        }

        /**
         * @since 24-09-04
         * دالة لضبط حجم وموقع الدائرة
         * //overlayView.setMaskPosition(getWidth() / 2.0f, getHeight() / 4.0f, 300);
         */
        public void setMaskPosition(float x, float y, float r) {
            centerX = x;
            centerY = y;
            radius = r;
            invalidate();  // لإعادة رسم العرض
        }

        public synchronized void draw(@NotNull final Canvas canvas) {
            // رسم الجزء الخارجي (الخلفية المظللة)
            canvas.drawRect(0, 0, getWidth(), getHeight(), outerPaint);
            // رسم الدائرة الشفافة في المنتصف أو في الموقع المحدد
            if (centerX == -1 || centerY == -1) {
                // إذا لم يتم تحديد الإحداثيات، ارسم في مركز الشاشة
                centerX = getWidth() / 2.0f;
                centerY = getHeight() / 3.0f;
            }
            //  canvas.drawCircle(centerX, centerY, radius, clearPaint);
            // رسم الحدود الزرقاء حول الدائرة
            canvas.drawCircle(centerX, centerY, radius, borderPaint);
        }
    }

}
