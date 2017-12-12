package com.sharedream.geek.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

/**
 * Created by young on 2017/6/21.
 */

public class MyUtils {
    public static Bitmap convert2RoundBitmap(Bitmap bitmap, int strokeWidth, Context context) {
        if (bitmap == null)
            return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //        int roundPx = width / 2;
        int roundPx = (width > height ? height : width) / 2;

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(output);
        final int color = 0xFFCFCFCF;
        final Paint paint = new Paint();
        //        final Rect rect = new Rect(0, 0, width, height);
        final Rect rect = new Rect(0, 0, width, width);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dip2px(context, strokeWidth)); // dp转px
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        return output;
    }

    public static Bitmap convert2BorderBitmap(Bitmap bitmap, int strokeWidth, Context context) {
        if (bitmap == null)
            return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //        int roundPx = width / 2;
//        int roundPx = (width > height ? height : width) / 2;
        int roundPx = dip2px(context, 12);

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(output);
//        final int color = Color.parseColor("#DBDBDB");
        final Paint paint = new Paint();
        //        final Rect rect = new Rect(0, 0, width, height);
        final Rect rect = new Rect(0, 0, width, width);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
//        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(MyUtils.dip2px(context, strokeWidth)); // dp转px
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        return output;
    }


    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    // 方式二：
    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmap(Context context, int imgResId) {
        if (context == null) {
            return null;
        }

        return BitmapFactory.decodeResource(context.getResources(), imgResId);
    }

    public static String getFootprint(Context context, String key) {
        SharedPreferences spf = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        String value = spf.getString(key, "");
        return value;
    }

    public static void saveField(Context context, String key, String value) {
        SharedPreferences spf = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void saveField(Context context, String key, int value) {
        SharedPreferences spf = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static String getPackageName(Context context) {
        String packageName = "";
        if (context != null) {
            packageName = context.getPackageName();
            if (!TextUtils.isEmpty(packageName)) {
                return packageName;
            }
        }
        return packageName;
    }
}
