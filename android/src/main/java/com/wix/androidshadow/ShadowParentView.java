package com.wix.androidshadow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.views.view.ReactViewGroup;

/**
 * Created by zachik on 10/10/2017.
 */

public class ShadowParentView extends ReactViewGroup {
    private static final String TAG = "ReactNativeJS";

    private static final float BLUR_SCALE = 0.125f;

    private Context mContext;
    private Bitmap viewBmp;
    private Bitmap blurBitmap;
    private int shadowPadding = 80;
    private float shadowRadius = 25f;
    private float shadowOpacity = 1f;
    private int shadowColor;
    private boolean hasShadowColor = false;
    private float shadowOffsetX = 0f;
    private float shadowOffsetY = 0f;
    private Paint shadowPaint;
    private boolean firstLayout = true;

    public ShadowParentView(Context context) {
        super(context);
        mContext = context;
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setShadowParams(ReadableMap readableMap) {
//        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
//        while (iterator.hasNextKey()) {
//            String key = iterator.nextKey();
//            ReadableType type = readableMap.getType(key);
//            Log.d(TAG,"ShadowParentView setShadowParams! key = " + key + " typ = " + type);
//        }

        shadowRadius = (float) readableMap.getDouble("shadowRadius");
        shadowColor = Color.parseColor(readableMap.getString("shadowColor"));
        shadowOpacity = (float) readableMap.getDouble("shadowOpacity");
        if (readableMap.hasKey("shadowOffset")) {
            ReadableMap offsetMap = readableMap.getMap("shadowOffset");
            shadowOffsetX = (float) offsetMap.getDouble("width");
            shadowOffsetY = (float) offsetMap.getDouble("height") * getResources().getDisplayMetrics().density;
        }

        Log.d(TAG,"ShadowParentView setShadowParams! shadowRadius = " + shadowRadius + " shadowOffsetY = " + shadowOffsetY);


        shadowPaint.setColorFilter(new PorterDuffColorFilter(shadowColor, PorterDuff.Mode.SRC_IN));
        shadowPaint.setAlpha((int) (shadowOpacity * 255));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
//        Log.d(TAG,"ShadowParentView onMeasure! before child w = " + getChildAt(0).getMeasuredWidth());

//        measureChildren(widthMeasureSpec,heightMeasureSpec);
//        Log.d(TAG,"ShadowParentView onMeasure! after child w = " + getChildAt(0).getMeasuredWidth());
//        super.onMeasure(MeasureSpec.makeMeasureSpec(getChildAt(0).getMeasuredWidth(),MeasureSpec.EXACTLY), heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d(TAG,"ShadowParentView onMeasure! w = " + getMeasuredWidth() + " widthMode " + widthMode + " widthSize " + widthSize);
        Log.d(TAG,"ShadowParentView onMeasure! child w = " + getChildAt(0).getMeasuredWidth());
//        Log.d(TAG,"ShadowParentView onMeasure! param w = " + getLayoutParams().width );
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG, "ShadowParentView onLayout! w = " + getWidth() + " changed = " + changed);
        try {
            ((ViewGroup) getParent()).setClipChildren(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (firstLayout && getWidth() > 0){
            firstLayout = false;

            if (blurBitmap == null) {
                new BlurTask().execute();
//                blurBitmap = BlurHelper.blur(getContext(),createPaddedBitmap(),BLUR_SCALE,shadowRadius);
            }
        }
//        blurBitmap = captureView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.d(TAG,"ShadowParentView onAttachedToWindow! w = " + getWidth());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d(TAG,"ShadowParentView onFinishInflate! w = " + getWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        Log.d(TAG,"ShadowParentView onDraw! " );
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        Log.d(TAG,"ShadowParentView dispatchDraw! " );

        drawShadow(canvas);


        super.dispatchDraw(canvas);
    }

    private void connectChildAlpha() {
        final View child = getChildAt(0);
        child.getViewTreeObserver().addOnDrawListener(new ViewTreeObserver.OnDrawListener() {
            @Override
            public void onDraw() {
                Log.d(TAG,"ShadowParentView child onDraw! alpha = " + child.getAlpha());
//                setAlpha(child.getAlpha());
            }
        });
    }

    private void drawShadow(Canvas canvas) {
        if (blurBitmap != null) {

            Rect newRect = canvas.getClipBounds();
            Log.d(TAG,"ShadowParentView has blur! " + newRect);
//            newRect.inset(-shadowPadding, -shadowPadding - ((int)shadowOffsetY));
            newRect.inset(-shadowPadding, -shadowPadding);
            canvas.clipRect (newRect, Region.Op.REPLACE);

//            canvas.drawBitmap(blurBitmap,shadowOffsetX -shadowPadding,
//                    -shadowPadding + shadowOffsetY, null);

            canvas.drawBitmap(blurBitmap, -shadowPadding,
                    -shadowPadding, shadowPaint);
        }
    }

    private Bitmap captureView() {
        View view = getChildAt(0);
        view.setDrawingCacheEnabled(true);
        view.destroyDrawingCache();
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        Bitmap bitmap = view.getDrawingCache();
        Log.d(TAG,"ShadowParentView captureView w = " + bitmap.getWidth());
        return bitmap;
    }

    private Bitmap createPaddedBitmap() {
        int width = getWidth() + 2 * shadowPadding;
        int height = getHeight() + 2 * shadowPadding;

        Bitmap output = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Matrix matrix = new Matrix();
        matrix.postTranslate(shadowPadding + shadowOffsetX,shadowPadding + shadowOffsetY);
        canvas.setMatrix(matrix);
        draw(canvas);
//        getChildAt(0).draw(canvas);

//        canvas.drawColor(Color.RED);
//        canvas.drawBitmap(captureView(),shadowPadding,shadowPadding,null);
        return output;
    }

    private class BlurTask extends AsyncTask<Void,Void,Bitmap> {

        @Override
        protected void onPreExecute() {
            viewBmp = createPaddedBitmap();
            Log.d(TAG,"ShadowParentView starting blur! w = " + viewBmp.getWidth() );
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap blurredBmp = BlurHelper.blur(mContext,viewBmp,BLUR_SCALE,shadowRadius);
            Paint pnt = new Paint();
            pnt.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            Canvas canvas = new Canvas(blurredBmp);
            canvas.drawBitmap(viewBmp,-shadowOffsetX,-shadowOffsetY,pnt);
            return blurredBmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                Log.d(TAG,"ShadowParentView finished blur! w = " + bitmap.getWidth() );

                blurBitmap = bitmap;
                invalidate();
            }
        }
    }



}