package lib.netmania.dcamera.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import lib.netmania.dcamera.log.DLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.view.Display;
import android.view.WindowManager;

public class BitmapUtils
{
	private final static String TAG = "BitmapUtils.java";
	private final static int BITMAP_PORTRAIT = 0;
	private final static int BITMAP_LANDSCAPE = 1;
	
	private static int bitmapOrientation = BITMAP_PORTRAIT;

	public static Bitmap decodeSampledBitmapFromFile(String filename, int screenWidth, int screenHeight, int tDegree)
	{
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		
		bmOptions.inJustDecodeBounds = true;
		
		BitmapFactory.decodeFile(filename, bmOptions);
		
		int bitmapWidth  = bmOptions.outWidth;
		int bitmapHeight = bmOptions.outHeight;
		int scaleFactor = 1;
		
		if ( bitmapWidth <= 0 && bitmapHeight <= 0 ) 
		{
			return null;
		}
			
		switch(tDegree)
		{
			case 90:
			case 270: 
				bitmapOrientation = BITMAP_LANDSCAPE; 
				scaleFactor = Math.min(bitmapWidth / screenHeight, bitmapHeight / screenWidth);
			break;
			default:  
				bitmapOrientation = BITMAP_PORTRAIT;	
				scaleFactor = Math.min(bitmapWidth / screenWidth, bitmapHeight / screenHeight);
			break;
		}
		
		if(scaleFactor <= 0)
			scaleFactor = 1;
		
		
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
		
		DLog.e(TAG, "["+tDegree+"]["+scaleFactor+"]["+bitmapWidth+"]["+screenWidth+"]["+bitmapHeight+"]["+screenHeight+"]");
		
		Bitmap bitmap1 = null;
		Bitmap bitmap2 = null;
		
		
		try
		{
			bitmap1 = BitmapFactory.decodeFile(filename, bmOptions);
			DLog.e(TAG, bitmap1.getWidth() + " * " + bitmap1.getHeight());
		}
		catch(Exception e)
		{
			bitmap1 = null;
			return bitmap1;
		}
		
		try
		{
			bitmap2 = BitmapUtils.rotateBitmap(bitmap1, tDegree);
			DLog.e(TAG, bitmap2.getWidth() + " * " + bitmap2.getHeight());
		}
		catch(Exception e)
		{
			
			bitmap2 = null;
			return bitmap1;
		} 
		
		return bitmap2;
	}
	
	public static Bitmap bitmapFromFile(String filename, int targetW, int targetH, int tDegree)
	{
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		int scaleFactor = 2;
		if ((targetW > 0) || (targetH > 0))
		{
			scaleFactor = Math.min(photoW / targetW, photoH / targetH);
		}

		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = null;
		
		try
		{
			bitmap = BitmapFactory.decodeFile(filename, bmOptions);
			bitmap = BitmapUtils.rotateBitmap(bitmap, tDegree);
			bitmap = reSizeBitmap(bitmap, 210);
		} 
		catch (OutOfMemoryError e)
		{
			DLog.e(TAG, "decodeSampledBitmapFromFile error=" + e.getLocalizedMessage());
		}
		return bitmap;
	}

	public static Bitmap rotateBitmap(Bitmap bmp, int degree)
	{
		if(bmp == null)
			return null;
		
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

		return resizedBitmap;
	}
	
	public static boolean SaveBitmapToFile(Bitmap bitmap, File fileCacheItem)
	{
		boolean returnValue = true;
		OutputStream out = null;

		try
		{
			fileCacheItem.createNewFile();
			out = new FileOutputStream(fileCacheItem);
			bitmap.compress(CompressFormat.JPEG, 100, out);
			returnValue = true;
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			returnValue = false;
		} 
		finally
		{
			try
			{
				out.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return returnValue;
	}
	
	public static Size getOptimalPictureSize(List<Size> sizeList, int width, int height)
	{
		Size prevSize = sizeList.get(0);
		Size optSize = sizeList.get(1);
		for (Size size : sizeList)
		{
			int diffWidth = Math.abs((size.width - width));
			int diffHeight = Math.abs((size.height - height));

			int diffWidthPrev = Math.abs((prevSize.width - width));
			int diffHeightPrev = Math.abs((prevSize.height - height));

			int diffWidthOpt = Math.abs((optSize.width - width));
			int diffHeightOpt = Math.abs((optSize.height - height));

			if (diffWidth < diffWidthPrev && diffHeight <= diffHeightOpt)
				optSize = size;
			
			if (diffHeight < diffHeightPrev && diffWidth <= diffWidthOpt)
				optSize = size;
			
			prevSize = size;
		}
		return optSize;
	}
	

	
	
	public static Bitmap loadBackgroundBitmap(Context context, String imgFilePath) throws Exception, OutOfMemoryError
	{
		Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		int displayWidth = display.getWidth();
		int displayHeight = display.getHeight();

		BitmapFactory.Options options = new BitmapFactory.Options();

		options.inPreferredConfig = Config.ARGB_8888;

		options.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(imgFilePath, options);

		float widthScale = options.outWidth / displayWidth;
		float heightScale = options.outHeight / displayHeight;
		float scale = widthScale > heightScale ? widthScale : heightScale;

		if (scale >= 8)
			options.inSampleSize = 8;
		else if (scale >= 6)
			options.inSampleSize = 6;
		else if (scale >= 4)
			options.inSampleSize = 4;
		else if (scale >= 2)
			options.inSampleSize = 2;
		else
			options.inSampleSize = 1;

		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(imgFilePath, options);

	}
	
	public static boolean reSizeBitmapToFileSaved(Bitmap bitmap, String path)
	{

		Bitmap srcBmp = bitmap;

		int iWidth = 210; // 축소시킬 너비
		int iHeight = 210; // 축소시킬 높이
		float fWidth = srcBmp.getWidth();
		float fHeight = srcBmp.getHeight();

		if (fWidth > iWidth)
		{
			float mWidth = (float) (fWidth / 100);
			float fScale = (float) (iWidth / mWidth);
			fWidth *= (fScale / 100);
			fHeight *= (fScale / 100);
		}
		else if (fHeight > iHeight)
		{
			float mHeight = (float) (fHeight / 100);
			float fScale = (float) (iHeight / mHeight);
			fWidth *= (fScale / 100);
			fHeight *= (fScale / 100);
		}

		FileOutputStream fosObj = null;
		try
		{
			Bitmap resizedBmp = Bitmap.createScaledBitmap(srcBmp, (int) fWidth, (int) fHeight, true);
			fosObj = new FileOutputStream(path);
			resizedBmp.compress(CompressFormat.JPEG, 100, fosObj);
			
			return true;
		} 
		catch (Exception e)
		{
			
		} 
		finally
		{
			try
			{
				fosObj.flush();
				fosObj.close();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
		}
		
		return false;
	}
	
	public static Bitmap reSizeBitmap(Bitmap bitmap, int s)
	{

		Bitmap srcBmp = bitmap;
		Bitmap resizedBmp;

		int iWidth = s; // 축소시킬 너비
		int iHeight = s; // 축소시킬 높이
		float fWidth = srcBmp.getWidth();
		float fHeight = srcBmp.getHeight();

		if (fWidth > iWidth)
		{
			float mWidth = (float) (fWidth / 100);
			float fScale = (float) (iWidth / mWidth);
			fWidth *= (fScale / 100);
			fHeight *= (fScale / 100);
		}
		else if (fHeight > iHeight)
		{
			float mHeight = (float) (fHeight / 100);
			float fScale = (float) (iHeight / mHeight);
			fWidth *= (fScale / 100);
			fHeight *= (fScale / 100);
		}

		FileOutputStream fosObj = null;
		try
		{
			resizedBmp = Bitmap.createScaledBitmap(srcBmp, (int) fWidth, (int) fHeight, true);
			return resizedBmp;

		} catch (Exception e)
		{
			return srcBmp;
		}
	}



	/** 비트맵 사이즈
	 *
	 * @param uri
	 * @return
	 */
	public static int[] getBitmapSizeFromFile (Uri uri) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inSampleSize = 1;
		Bitmap tmp = BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
		int imageHeight = options.outHeight;
		int imageWidth = options.outWidth;

		int[] result = {imageWidth, imageHeight};
		return result;
	}
	
	
	
}
