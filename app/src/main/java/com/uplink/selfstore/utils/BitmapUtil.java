package com.uplink.selfstore.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.DisplayMetrics;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

/**
 * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
 */
public class BitmapUtil {
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth,
                                            int dstHeight) {
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) {
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res,
                                                         int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, options);
        return createScaleBitmap(src, reqWidth, reqHeight);
    }

    public static Bitmap decodeSampledBitmapFromFd(String pathName,
                                                   int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight);
    }

    public static Bitmap createQrCodeBitmap(String str) {
        Bitmap bitmap = null;
        BitMatrix result = null;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            DisplayMetrics dm = new DisplayMetrics();
            int width = dm.widthPixels - 100;
            result = multiFormatWriter.encode(str, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

            result=updateBit(result,1);
            bitmap = barcodeEncoder.createBitmap(result);
        } catch (WriterException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException iae) { // ?
            return null;
        }
        return bitmap;
    }

    public static Bitmap createQrCodeBitmapAndLogo(String str, Bitmap logo) {
        Bitmap a = createQrCodeBitmap(str);
        if (a == null)
            return null;

        return addLogo(a, logo);
    }

    private static Bitmap addLogo(Bitmap src, Bitmap logo) {
        if (src == null) {
            return null;
        }

        if (logo == null) {
            return src;
        }

        //获取图片的宽高
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        if (srcWidth == 0 || srcHeight == 0) {
            return null;
        }

        if (logoWidth == 0 || logoHeight == 0) {
            return src;
        }

        //logo大小为二维码整体大小的1/5
        float scaleFactor = srcWidth * 1.0f / 5 / logoWidth;
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        try {
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(src, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, srcWidth / 2, srcHeight / 2);
            canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2, (srcHeight - logoHeight) / 2, null);

            canvas.save();
            canvas.restore();
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }

        return bitmap;
    }

    private static BitMatrix updateBit(BitMatrix matrix, int margin){
        int tempM = margin*2;
        int[] rec = matrix.getEnclosingRectangle();   //获取二维码图案的属性
        int resWidth = rec[2] + tempM;
        int resHeight = rec[3] + tempM;
        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight); // 按照自定义边框生成新的BitMatrix
        resMatrix.clear();
        for(int i= margin; i < resWidth- margin; i++){   //循环，将二维码图案绘制到新的bitMatrix中
            for(int j=margin; j < resHeight-margin; j++){
                if(matrix.get(i-margin + rec[0], j-margin + rec[1])){
                    resMatrix.set(i,j);
                }
            }
        }
        return resMatrix;
    }

    public static Bitmap nv12ToBitmap(byte[] data, int w, int h) {
        return spToBitmap(data, w, h, 0, 1);
    }

    public static Bitmap nv21ToBitmap(byte[] data, int w, int h) {
        return spToBitmap(data, w, h, 1, 0);
    }

    private static Bitmap spToBitmap(byte[] data, int w, int h, int uOff, int vOff) {
        int plane = w * h;
        int[] colors = new int[plane];
        int yPos = 0, uvPos = plane;
        for(int j = 0; j < h; j++) {
            for(int i = 0; i < w; i++) {
                // YUV byte to RGB int
                final int y1 = data[yPos] & 0xff;
                final int u = (data[uvPos + uOff] & 0xff) - 128;
                final int v = (data[uvPos + vOff] & 0xff) - 128;
                final int y1192 = 1192 * y1;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                r = (r < 0) ? 0 : ((r > 262143) ? 262143 : r);
                g = (g < 0) ? 0 : ((g > 262143) ? 262143 : g);
                b = (b < 0) ? 0 : ((b > 262143) ? 262143 : b);
                colors[yPos] = ((r << 6) & 0xff0000) |
                        ((g >> 2) & 0xff00) |
                        ((b >> 10) & 0xff);

                if((yPos++ & 1) == 1) uvPos += 2;
            }
            if((j & 1) == 0) uvPos -= w;
        }
        return Bitmap.createBitmap(colors, w, h, Bitmap.Config.RGB_565);
    }

    public static Bitmap i420ToBitmap(byte[] data, int w, int h) {
        return pToBitmap(data, w, h, true);
    }

    public static Bitmap yv12ToBitmap(byte[] data, int w, int h) {
        return pToBitmap(data, w, h, false);
    }

    private static Bitmap pToBitmap(byte[] data, int w, int h, boolean uv) {
        int plane = w * h;
        int[] colors = new int[plane];
        int off = plane >> 2;
        int yPos = 0, uPos = plane + (uv ? 0 : off), vPos = plane + (uv ? off : 0);
        for(int j = 0; j < h; j++) {
            for(int i = 0; i < w; i++) {
                // YUV byte to RGB int
                final int y1 = data[yPos] & 0xff;
                final int u = (data[uPos] & 0xff) - 128;
                final int v = (data[vPos] & 0xff) - 128;
                final int y1192 = 1192 * y1;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                r = (r < 0) ? 0 : ((r > 262143) ? 262143 : r);
                g = (g < 0) ? 0 : ((g > 262143) ? 262143 : g);
                b = (b < 0) ? 0 : ((b > 262143) ? 262143 : b);
                colors[yPos] = ((r << 6) & 0xff0000) |
                        ((g >> 2) & 0xff00) |
                        ((b >> 10) & 0xff);

                if((yPos++ & 1) == 1) {
                    uPos++;
                    vPos++;
                }
            }
            if((j & 1) == 0) {
                uPos -= (w >> 1);
                vPos -= (w >> 1);
            }
        }
        return Bitmap.createBitmap(colors, w, h, Bitmap.Config.RGB_565);
    }
}
