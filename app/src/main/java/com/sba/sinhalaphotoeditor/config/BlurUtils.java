package com.sba.sinhalaphotoeditor.config;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

public class BlurUtils
{
    private static final float BITMAP_SCALE = 1f;

    public Bitmap blur(Context context, Bitmap image, float blurRadius) {

        Bitmap outputBitmap = null;

        if (image != null) {

            if (blurRadius == 0)
            {
                return image;
            }

            if (blurRadius < 1)
            {
                blurRadius = 1;
            }

            if (blurRadius > 25) {
                blurRadius = 25;
            }

            //Set the radius of the Blur. Supported range 0 < radius <= 25
            float BLUR_RADIUS = blurRadius;

            int width = Math.round(image.getWidth() * BITMAP_SCALE);
            int height = Math.round(image.getHeight() * BITMAP_SCALE);

           /*
            if(width <= 0 && height <= 0)
            {
                return inputBitmap;
            }*/

            try
            {
                Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
                outputBitmap = Bitmap.createBitmap(inputBitmap);
                RenderScript rs = RenderScript.create(context);
                ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
                Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
                Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
                theIntrinsic.setRadius(BLUR_RADIUS);
                theIntrinsic.setInput(tmpIn);
                theIntrinsic.forEach(tmpOut);
                tmpOut.copyTo(outputBitmap);
            }
            catch (Exception e)
            {

            }



        }

        return outputBitmap;
    }
}
