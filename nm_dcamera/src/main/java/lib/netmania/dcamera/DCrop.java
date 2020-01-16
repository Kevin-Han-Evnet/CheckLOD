package lib.netmania.dcamera;

import java.io.File;

import lib.netmania.dcamera.dto.DCameraResultDto;
import lib.netmania.dcamera.log.DLog;
import lib.netmania.dcamera.util.BitmapUtils;
import lib.netmania.dcamera.util.ExifUtils;
import lib.netmania.dcamera.util.StorageUtils;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.TextView;

public class DCrop extends Activity implements OnClickListener
{
	private final static String TAG = "DCrop";
	private String mImageCapturePath = null;
	private String mImageFirstFileName = null; 
	private String mUserId = null;
	private String mRoomId = null; 
	
	private int mDeparture;


	private ImageView icon_attach_count, imgBtnAttachSubmit, imgBtnRotateImage, imgBtnBack;
	private TextView gallerySelectedCount;
	
	
	
	private CropImageView cropImageView;
	private Bitmap mBitmap = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dcamera_crop);

		init();
		layout();
	}

	private void init()
	{
		mImageCapturePath = (String) getIntent().getSerializableExtra("ImageCapturePath");
		mImageFirstFileName = (String) getIntent().getSerializableExtra("ImageFirstFileName");
		mUserId = (String) getIntent().getSerializableExtra("UserId");
		mRoomId = (String) getIntent().getSerializableExtra("RoomId");
		mDeparture = (int) getIntent().getIntExtra("Departure", DCamera.PICK_CROP_FROM_SINGLE_FILE);
		DLog.e(TAG, "mImageCapturePath " + mImageCapturePath);
		DLog.e(TAG, "mImageFirstFileName " + mImageFirstFileName);
		DLog.e(TAG, "mUserId " + mUserId);
		DLog.e(TAG, "mRoomId " + mRoomId);
	}

	private void layout()
	{
		try
		{
			mBitmap = BitmapUtils.loadBackgroundBitmap(getApplicationContext(), mImageCapturePath);
			mBitmap = BitmapUtils.rotateBitmap(mBitmap, ExifUtils.getExifOrientation(mImageCapturePath));
			cropImageView = (CropImageView) this.findViewById(R.id.CropImageView);
			cropImageView.setImageBitmap(mBitmap);





			icon_attach_count = (ImageView) findViewById (R.id.icon_attach_count);
			icon_attach_count.setVisibility (View.GONE);

			gallerySelectedCount = (TextView) findViewById (R.id.gallerySelectedCount);
			gallerySelectedCount.setVisibility (View.GONE);

			imgBtnAttachSubmit = (ImageView) findViewById (R.id.imgBtnAttachSubmit);
			imgBtnAttachSubmit.setOnClickListener(this);

			imgBtnRotateImage = (ImageView) findViewById(R.id.imgBtnRotateImage);
			imgBtnRotateImage.setOnClickListener(this);


			imgBtnBack = (ImageView) findViewById (R.id.imgBtnBack);
			imgBtnBack.setOnClickListener (kevinClickListener);



			/** 이것이 크롭 완료 버튼
			mCropBtn = (RelativeLayout) findViewById(R.id.p_edit_save_btn);
			mCropBtn.setOnClickListener(this);
			 */
		} 
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
			this.finish();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
			this.finish();
		}

	}


	private int k = 0;
	@Override
	public void onClick(View v) {


		if (v.getId() == R.id.imgBtnAttachSubmit) {

			File thumbnailResultFile = null;

			// 원본이미지를 삭제한다.
			if(mDeparture != DCamera.PICK_CROP_FROM_SINGLE_FILE)
			{
				File file = new File(mImageCapturePath);
				file.delete();
			}
			else
			{
				File dstFile = getDir(getPackageName(), MODE_PRIVATE);
				thumbnailResultFile = new File(dstFile + "/" + "tmp.jpg");
				mImageCapturePath = thumbnailResultFile.getPath();
			}
			BitmapUtils.SaveBitmapToFile(cropImageView.getCroppedImage(), new File(mImageCapturePath));
			//BitmapUtils.reSizeBitmapToFileSaved(cropImageView.getCroppedImage(), mImageCapturePath);
			DCameraResultDto result = StorageUtils.localSaveFile(getApplicationContext(), Uri.fromFile(new File(mImageCapturePath)), mImageFirstFileName, mUserId, mRoomId, false);
			endCrop(result);

			if(thumbnailResultFile != null)
				thumbnailResultFile.delete();

		} else if (v.getId() == R.id.imgBtnRotateImage) {

			mBitmap = BitmapUtils.rotateBitmap(mBitmap, 90);
			cropImageView = (CropImageView) this.findViewById(R.id.CropImageView);
			cropImageView.setImageBitmap(mBitmap);

		}
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
			{
				DCameraResultDto result = new DCameraResultDto();
				result.resultCode = RESULT_CANCELED;
				result.message = "사용자에 의한 취소";
				endCrop(result);
			}
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void endCrop(DCameraResultDto result)
	{
		if(mBitmap != null)
			mBitmap.recycle();
		
		Intent intent = this.getIntent();
		intent.putExtra("ImageCapturePath", result);
		intent.putExtra("return-data", true);
		this.setResult(result.resultCode, intent);
		finish();

	}


	//리스너 ------------------------------------------------------------------------------------------------
	private View.OnClickListener kevinClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View src) {
			//하나니까 분기하지 말자.
			DCrop.this.onBackPressed();
		}
	};

}
