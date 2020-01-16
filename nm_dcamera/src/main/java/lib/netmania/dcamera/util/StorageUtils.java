package lib.netmania.dcamera.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import lib.netmania.dcamera.dto.DCameraResultDto;

public class StorageUtils
{
	public static final int THUMBNAIL_SIZE = 210;
	
	public static DCameraResultDto localSaveFile(Context context, Uri imageCaptureUri, String imageFirstFileName, String userId, String roomId, boolean isSrcDelete)
	{
		File srcFile = new File(imageCaptureUri.getPath());
		File dstFile = context.getDir(context.getPackageName(), context.MODE_PRIVATE);
		FileInputStream fis = null;
		FileOutputStream newfos = null;
		File setResultFile = null;
		File thumbnailResultFile = null;
		DCameraResultDto result = new DCameraResultDto();
		
		if (roomId == null)
			roomId = "";
		
		if (srcFile != null && srcFile.exists())
		{
			try
			{
				String tmpFileName = "";
				String time = String.valueOf(System.currentTimeMillis());
				
				if (roomId.length() <= 0)
					tmpFileName = "org_" + imageFirstFileName + userId + "_" + time + ".jpg";
				else
					tmpFileName = "org_" + imageFirstFileName + userId + "_" + roomId + "_" + time + ".jpg";
				
				setResultFile = new File(dstFile + "/" + tmpFileName);
				fis = new FileInputStream(srcFile);
				newfos = new FileOutputStream(setResultFile);
				
				int readcount = 0;
				byte[] buffer = new byte[1024];
				
				while ((readcount = fis.read(buffer, 0, 1024)) != -1)
					newfos.write(buffer, 0, readcount);
				
				newfos.close();
				fis.close();
				
				Bitmap bitmap = BitmapUtils.bitmapFromFile(imageCaptureUri.getPath(), THUMBNAIL_SIZE, THUMBNAIL_SIZE, ExifUtils.getExifOrientation(imageCaptureUri.getPath()));
				tmpFileName = THUMBNAIL_SIZE + "_" + imageFirstFileName + userId + "_" + roomId + "_" + time + ".jpg";
				thumbnailResultFile = new File(dstFile + "/" + tmpFileName);
				BitmapUtils.SaveBitmapToFile(bitmap, thumbnailResultFile);
				if (bitmap != null)
					
					if (isSrcDelete)
						srcFile.delete();
				
				result.resultFile = setResultFile;
				result.resultCode = DCameraResultDto.RESULT_OK;
				
			} catch (Exception e)
			{
				try
				{
					newfos.close();
					fis.close();
				} catch (IOException e1)
				{
				}
				
				if (isSrcDelete)
					srcFile.delete();
				
				result.resultFile = setResultFile;
				result.resultCode = DCameraResultDto.RESULT_EXCEPTION;
				result.exception = e;
				result.message = "로컬에 이미지 저장중 오류발생";
			}
		}
		else
		{
			if (isSrcDelete)
				srcFile.delete();
			
			result.resultFile = setResultFile;
			result.message = "이미지 원본 파일이 존재하지 않음";
			result.resultCode = DCameraResultDto.RESULT_CANCELED;
		}
		
		return result;
	}
}
