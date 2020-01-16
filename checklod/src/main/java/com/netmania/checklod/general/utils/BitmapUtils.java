package com.netmania.checklod.general.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.DisplayMetrics;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * 전송 이미지 저장 및 리사이즈 처리 유틸리티
 * 
 * @author jungeh
 * 
 */
public class BitmapUtils
{
	OnProgresListener onProgresListener = null;

	public interface OnProgresListener
	{
		void onProgress(int progress);

		void onFinish(Bitmap bitmap);

		void onStart();

		void onError();
	}

	public void setOnProgresListener(OnProgresListener listener)
	{
		onProgresListener = listener;
	}

	private void onProgresListener(int progress)
	{

		if (onProgresListener != null)
			onProgresListener.onProgress(progress);
	}

	private void onFinishListener(Bitmap bitmap)
	{
		if (onProgresListener != null)
			onProgresListener.onFinish(bitmap);
	}

	private void onStartListener()
	{
		if (onProgresListener != null)
			onProgresListener.onStart();
	}

	private void onErrorListener()
	{
		if (onProgresListener != null)
			onProgresListener.onError();
	}

	


	
	public static File saveBitmap(Context ctx, String filePath, Bitmap bitmap)
	{

		File f = null;
		try
		{
			f = new File(filePath);
			FileOutputStream fos = new FileOutputStream(f);
			bitmap.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (Exception e)
		{
			/** 이제그만~*///LogUtil.E("saveBitmap Error=" + e.toString());
		}
		return f;
	}

	public static File load(Context ctx, String filePath)
	{

		File f = null;
		try
		{
			f = new File(filePath);
			FileInputStream fis = new FileInputStream(f);

			int readcount = (int) f.length();
			byte[] buffer = new byte[readcount];
			fis.read(buffer);
			fis.close();
		} catch (Exception e)
		{
			/** 이제그만~*///LogUtil.E("File Load Error=" + e.toString());
		}
		return f;
	}

	public static Bitmap decodeSampledBitmapFromFile(String filename, int targetW, int targetH, DisplayMetrics mDisplayMatrix)
	{

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if (targetW <= mDisplayMatrix.widthPixels || targetH <= mDisplayMatrix.heightPixels) {
			scaleFactor = 1;
		} else if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW / targetW, photoH / targetH);
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = null;
		try
		{
			bitmap = BitmapFactory.decodeFile(filename, bmOptions);
		} catch (OutOfMemoryError e)
		{
			// TODO: handle exception
			/** 이제그만~*///LogUtil.E("decodeSampledBitmapFromFile error=" + e.getLocalizedMessage());
		}
		return bitmap;
	}

	public static Bitmap decodeSampledBitmapFromFile1(String filename, int targetW, int targetH) throws Exception
	{
		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
		int scaleFactor = 2;
		if ((targetW > 0) || (targetH > 0))
		{
			if (photoW > photoH)
				scaleFactor = Math.min(photoW / targetH, photoH / targetW);
			else
				scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = null;
		bitmap = BitmapFactory.decodeFile(filename, bmOptions);
		
		return bitmap;
	}

	/**
	 * 폴더 및 하위 파일 삭제
	 * 
	 * @param fileDir
	 */
	public static void deleteDir(File fileDir)
	{
		File[] childFileList = fileDir.listFiles();
		for (File childFile : childFileList)
		{
			if (childFile.isDirectory())
			{
				deleteDir(childFile);
			} else
			{
				childFile.delete();
			}
		}
		if (fileDir.delete())
		{
			fileDir.delete();
		}
	}

	/**
	 * 비트맵 회전
	 */
	public static Bitmap rotateBitmap (Bitmap bitmap, int degrees)
	{
		if(degrees != 0 && bitmap != null)
	  {
	    Matrix m = new Matrix();
	    m.setRotate(degrees, (float) bitmap.getWidth() / 2, 
	    (float) bitmap.getHeight() / 2);
	    
	    try
	    {
	      Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
				  bitmap.getWidth(), bitmap.getHeight(), m, true);
	      if(bitmap != converted)
	      {
	        bitmap.recycle();
	        bitmap = converted;
	      }
	    }
	    catch(OutOfMemoryError ex)
	    {
	      // 메모리가 부족하여 회전을 시키지 못할 경우 그냥 원본을 반환합니다.
	    }
	  }
		return bitmap; 
	}

	/**
	 * 엑시프정보의 회전각에 따라 각도 리턴
	 */
	public static int exifOrientationToDegrees(int exifOrientation)
	{
		int degree = 0;

		switch (exifOrientation)
		{
		case ExifInterface.ORIENTATION_ROTATE_90:
			degree = 90;
			break;

		case ExifInterface.ORIENTATION_ROTATE_180:
			degree = 180;
			break;

		case ExifInterface.ORIENTATION_ROTATE_270:
			degree = 270;
			break;
		}

		return degree;
	}

	// 비트맵 둥글게
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels)
	{

		Bitmap output = null;
		if (bitmap != null)
		{
			output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);

			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			final RectF rectF = new RectF(rect);
			final float roundPx = pixels;

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);
			bitmap.recycle();
		}
		return output;
	}

	// asset에 있는 비트맵 반환
	public static Bitmap getLocalBitmap(Context context, String category, String name)
	{
		AssetManager mngr = context.getAssets();
		InputStream in = null;

		try
		{
			in = mngr.open(category + "/" + name);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		Bitmap temp = BitmapFactory.decodeStream(in, null, null);
		return temp;
	}

	public Bitmap getBitmapFromURL(final String imageUrl, boolean f)
	{
		try
		{
			if(f) onStartListener();
			
			URL url = new URL(imageUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			connection.disconnect();
			if(f) onFinishListener(myBitmap);
			return myBitmap;

		} catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public class DownloadImage extends AsyncTask<String, String, String>
	{
		@Override
		protected String doInBackground(String... arg0)
		{
			try
			{
				getBitmapFromURL((String) arg0[0], true);
				return null;
			} catch (Exception e)
			{
				return "download failed";
			}
		}

		
		protected void onPostExecute(String result)
		{
			cancel(true);
		}

    }
	
	
	/*/이미지 블러
	public static Bitmap applyGaussianBlur (Context context, Bitmap sentBitmap, int radius) {
		
		if (BaseApplication.currentAPIVersion > 16) {
            Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);
 
            final RenderScript rs = RenderScript.create (context);
            final Allocation input = Allocation.createFromBitmap(rs, sentBitmap, Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT);
            final Allocation output = Allocation.createTyped(rs, input.getType());
            final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4 (rs));
            script.setRadius(radius); 
            script.setInput(input);
            script.forEach(output);
            output.copyTo(bitmap);
            return bitmap;
        }
		
        return sentBitmap;
	}*/
	
	
	//블러 다시 해보자.
	public static Bitmap getBlurBitmap (Bitmap sentBitmap, int radius) {

        // Stack Blur v1.0 from
        // http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
        //
        // Java Author: Mario Klingemann <mario at quasimondo.com>
        // http://incubator.quasimondo.com
        // created Feburary 29, 2004
        // Android port : Yahel Bouaziz <yahel at kayenko.com>
        // http://www.kayenko.com
        // ported april 5th, 2012

        // This is a compromise between Gaussian Blur and Box blur
        // It creates much better looking blurs than Box Blur, but is
        // 7x faster than my Gaussian Blur implementation.
        //
        // I called it Stack Blur because this describes best how this
        // filter works internally: it creates a kind of moving stack
        // of colors whilst scanning through the image. Thereby it
        // just has to add one new block of color to the right side
        // of the stack and remove the leftmost color. The remaining
        // colors on the topmost layer of the stack are either added on
        // or reduced by one, depending on if they are on the right or
        // on the left side of the stack.
        //
        // If you are using this algorithm in your code please add
        // the following line:
        //
        // Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }
	
	public static String SaveBitmapToFileCache(Context context, Bitmap bitmap, String filename)
	{
		
		//DLog.e("SaveBitmapToFileCache", filename);
		
		if(filename.isEmpty() || filename.length() <= 0)
			return null;
		
		File file = context.getDir(context.getPackageName(), context.MODE_PRIVATE);
		File fileCacheItem = new File(file+ "/" + filename);
		
		if(fileCacheItem.exists())
			return null;
		
		OutputStream out = null;

		try
		{
			fileCacheItem.createNewFile();
			out = new FileOutputStream(fileCacheItem);

			bitmap.compress(CompressFormat.JPEG, 100, out);
			out.close();
			return fileCacheItem.getPath();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		} finally
		{
			try
			{
				out.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return null;
    }
	
	
	/** 돌려라 비트맵 그리고 파일로 가자.
	 * @param context
	 * @param file
	 * @return
	 */
	public static File rotatePhotoFromFile (Context context, File file, int MAX_IMAGE_SIZE) {
		File result = file;
		
		//파일 로드하여 비트맵 파싱
		Bitmap t = BitmapFactory.decodeFile(file.getAbsolutePath());
		
		ExifInterface exif;
		try {
			exif = new ExifInterface(file.getAbsolutePath());
			
			int exifOrientation = exif.getAttributeInt(
			ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			int exifDegree = exifOrientationToDegrees(exifOrientation);
			Bitmap rotatedBitmap = BitmapUtils.rotateBitmap (t, exifDegree);
			
			
			
			int width = rotatedBitmap.getWidth();
			int height = rotatedBitmap.getHeight();
			
			if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
				
				float prop;
				if (width >= height) {
					prop = Float.valueOf(MAX_IMAGE_SIZE) / Float.valueOf(width);
					width = MAX_IMAGE_SIZE;
					height = Math.round(height * prop);
				} else {
					prop = Float.valueOf(MAX_IMAGE_SIZE) / Float.valueOf(height);
					width = Math.round(width * prop);
					height = MAX_IMAGE_SIZE;
				}
				
			}

			result = BitmapUtils.saveBitmap (context.getApplicationContext (), file.getAbsolutePath(), Bitmap.createScaledBitmap(rotatedBitmap, width, height, false));
			rotatedBitmap.recycle();
			
					
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	
	
	/** 돌려라 비트맵 그리고 파일로 가자.
	 * @param context
	 * @param file
	 * @return
	 */
	public static File resizePhotoFromFile (Context context, File file, int MAX_IMAGE_SIZE) {
		File result = file;
		
		//파일 로드하여 비트맵 파싱
		Bitmap t = BitmapFactory.decodeFile(file.getAbsolutePath());
		
		int width = t.getWidth ();
		int height = t.getHeight();
		
		if (width > MAX_IMAGE_SIZE || height > MAX_IMAGE_SIZE) {
			
			float prop;
			if (width >= height) {
				prop = Float.valueOf(MAX_IMAGE_SIZE) / Float.valueOf(width);
				width = MAX_IMAGE_SIZE;
				height = Math.round(height * prop);
			} else {
				prop = Float.valueOf(MAX_IMAGE_SIZE) / Float.valueOf(height);
				width = Math.round(width * prop);
				height = MAX_IMAGE_SIZE;
			}
			
		}
		
		result = BitmapUtils.saveBitmap (context.getApplicationContext (), file.getAbsolutePath(), Bitmap.createScaledBitmap(t, width, height, false));
		
		return result;
	}
	
	
	
	public static boolean isRotated (File file) {
		
		boolean result = false;
		
		ExifInterface exif;
		try {
			
			
			exif = new ExifInterface(file.getAbsolutePath());
			int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			
			if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90
					|| exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
				
				result = true;
				
			} else {
				result = false;
			}
			
					
		} catch (Exception e) {
			result = false;
		}
		
		return result;
	}


	/** Get Bitmap's Width **/
	public static int getBitmapOfWidth( String fileName ){

		boolean isRotated = isRotated(new File(fileName));

		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeFile(fileName, options);
			if (tmp != null && !tmp.isRecycled()) tmp.recycle();
			return isRotated ? options.outHeight : options.outWidth;
		} catch(Exception e) {
			return 0;
		}
	}


	/** Get Bitmap's height **/
	public static int getBitmapOfHeight( String fileName ){

		boolean isRotated = isRotated(new File(fileName));

		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Bitmap tmp = BitmapFactory.decodeFile(fileName, options);
			if (tmp != null && !tmp.isRecycled()) tmp.recycle();
			return isRotated ? options.outWidth : options.outHeight;
		} catch(Exception e) {
			return 0;
		}
	}


	/** 크롭하자
	 *
	 * @param bitmap
	 * @return
     */
	public static Bitmap getCroppedBitmap (Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()) ;/*(bitmap.getWidth() >= bitmap.getHeight()) ? new Rect(0, 0, bitmap.getHeight(), bitmap.getHeight())
																	: new Rect(0, 0, bitmap.getWidth(), bitmap.getWidth());*/

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		// canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		float radious = (bitmap.getWidth() >= bitmap.getHeight()) ? bitmap.getHeight() / 2
																	: bitmap.getWidth() / 2;
		canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, radious, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		//Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
		//return _bmp;
		return output;
	}


	/** URL에서 비트맵 얻기
	 *
	 * @param strImageURL
	 * @return
     */
	public static Bitmap getImageFromURL (String strImageURL) {
		Bitmap imgBitmap = null;

		try {
			URL url = new URL(strImageURL);
			URLConnection conn = url.openConnection();
			conn.connect();

			int nSize = conn.getContentLength();
			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), nSize);
			imgBitmap = BitmapFactory.decodeStream(bis);

			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return imgBitmap;
	}
}
