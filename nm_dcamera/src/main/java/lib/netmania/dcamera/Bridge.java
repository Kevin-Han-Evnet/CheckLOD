   package lib.netmania.dcamera;

import java.io.File;
import java.util.ArrayList;

import lib.netmania.dcamera.dto.DCameraResultDto;
import lib.netmania.dcamera.gallery.DGallery;
import lib.netmania.dcamera.gallery.databinders.Data;
import lib.netmania.dcamera.log.DLog;
import lib.netmania.dcamera.util.StorageUtils;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

   public class Bridge extends Activity
{
	private final String TAG = "Bridge";
	private final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/";
	
	private MediaScannerConnection mediaScanner = null;
	
	private Uri mImageCaptureUri = null;
	private Uri mTmpImageCaptureUri = null;
	
	private String mImageCapturePath = null;
	private String mImageFirstFileName = null;
	private String mUserId = null;
	private String mRoomId = null;
	
	private Bridge mBridge = null;
	private Intent mIntent = null;
	
	private boolean ismMediaScanning = false;
	
	private int mImageCaptureType = DCamera.PICK_FROM_DCAMERA;
	
	public static MediaScannerListener mMediaScannerListener = null;
	
	public static interface MediaScannerListener
	{
		public void onScanCompleted(String path, Uri uri);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView (R.layout.loading);
		
		DLog.e(TAG, "onCreate(Bundle savedInstanceState)");
		
		init();
		startImageCapture(mImageCaptureType);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
	    super.onConfigurationChanged(newConfig);
	}
	
	private void init()
	{
		initVariable();
		mImageCaptureType = (int) getIntent().getIntExtra("ImageCaptureType", DCamera.PICK_FROM_DCAMERA);
		mImageFirstFileName = (String) getIntent().getStringExtra("ImageFirstFileName");
		mUserId = (String) getIntent().getStringExtra("UserId");
		mRoomId = (String) getIntent().getStringExtra("RoomId");
		mBridge = this;
	}
	
	public void initVariable()
	{
		mImageCaptureUri = null;
		mTmpImageCaptureUri = null;
		mImageFirstFileName = null;
		mMediaScannerListener = null;
		mBridge = null;
		mImageCaptureType = DCamera.PICK_CROP_FROM_DCAMERA;
	}
	
	public void startImageCapture(int imageCaptureType)
	{
		switch (imageCaptureType)
		{
			case DCamera.PICK_FROM_DCAMERA:
			case DCamera.PICK_CROP_FROM_DCAMERA:
				startCaptureImage(imageCaptureType);
			break;
			case DCamera.PICK_CROP_FROM_SINGLE_FILE:
			case DCamera.PICK_FROM_SINGLE_FILE:
			case DCamera.PICK_FROM_MULTIPLE_FILE:
				Intent intent = new Intent(this, DGallery.class);
				intent.putExtra(DGallery.GALLERY_STYLE, imageCaptureType);
		    	startActivityForResult(intent, imageCaptureType);
			break;
			/* 
			==== 비디오 촬영 현재 미사용 	
			case DCamera.VOD_FROM_DCAMERA:
				startRecordingVideo(imageCaptureType);
			break;
			
			===== 오디오 녹음 현재 미사용
			case DCamera.AUDIO_FROM_DCAMERA:
				startCaptureAudio();
			break;
			*/
		}
		
		if( DCamera.progressDialog != null && DCamera.progressDialog.isShowing())
			DCamera.progressDialog.dismiss();
	}
	
	private void startCaptureImage(int type)
	{
		DLog.e(TAG, "startCaptureImage(int type)");
		
		Intent intent = new Intent();
		File fileFolderPath = new File(SAVE_PATH);
		fileFolderPath.mkdir();
		
		File file = new File(SAVE_PATH + "TMP.jpg");
		
		mImageCaptureUri = Uri.fromFile(file);
		mTmpImageCaptureUri = mImageCaptureUri;
		
		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
		startActivityForResult(intent, type);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != RESULT_OK)
		{
			DCamera.mDCameraListener.onCancelled("촬영을 취소하였거나 오류가 발생함");		
			Intent intent = this.getIntent();
			this.setResult(RESULT_CANCELED, intent);
			finish();
			return;
		}
		
		switch (requestCode)
		{
			case DCamera.PICK_FROM_DCAMERA:
			{
				mediaScan(mTmpImageCaptureUri.getPath(), new MediaScannerListener()
				{
					@Override
					public void onScanCompleted(String path, Uri uri)
					{
						DCameraResultDto retult = StorageUtils.localSaveFile(mBridge, uri, mImageFirstFileName, mUserId, mRoomId, true);
						
						switch( retult.resultCode )
						{
							case DCameraResultDto.RESULT_OK: 
								DCamera.mDCameraListener.onResult(retult.resultFile); 
							break;
							case DCameraResultDto.RESULT_EXCEPTION: 
								DCamera.mDCameraListener.onException(retult.exception); 
							break;
							case DCameraResultDto.RESULT_CANCELED: 
								DCamera.mDCameraListener.onCancelled(retult.message);
							break;
						}
					}
					
				});
				
				finish();
			}
			break;
			case DCamera.PICK_CROP_FROM_DCAMERA:
			{
				mediaScan(mTmpImageCaptureUri.getPath(), new MediaScannerListener()
				{
					@Override
					public void onScanCompleted(String path, Uri uri)
					{
						mImageCaptureType = DCamera.CROP_FORM_DCAMERA;
						mIntent = new Intent(mBridge, DCrop.class);
						mIntent.putExtra("ImageCapturePath", path);
						mIntent.putExtra("ImageFirstFileName", mImageFirstFileName);
						mIntent.putExtra("UserId", mUserId);
						mIntent.putExtra("RoomId", mRoomId);
						mIntent.putExtra("Departure", DCamera.PICK_CROP_FROM_DCAMERA);
						mIntent.putExtra("return-data", true);
						mBridge.startActivityForResult(mIntent, mImageCaptureType);
					}
				});
			}
			break;
			case DCamera.CROP_FORM_DCAMERA:
			{
				DCameraResultDto retult = (DCameraResultDto) data.getSerializableExtra("ImageCapturePath");
				
				if(retult == null)
				{
					DCamera.mDCameraListener.onCancelled("결과값을 정상적으로 가져오지 못했습니다.");
					finish();
				}
				
				switch( retult.resultCode )
				{
					case DCameraResultDto.RESULT_OK: 
						DCamera.mDCameraListener.onResult(retult.resultFile); 
					break;
					case DCameraResultDto.RESULT_EXCEPTION: 
						DCamera.mDCameraListener.onException(retult.exception); 
					break;
					case DCameraResultDto.RESULT_CANCELED: 
						DCamera.mDCameraListener.onCancelled(retult.message);
					break;
				}
				finish();
			}
			break;
			case DCamera.PICK_FROM_MULTIPLE_FILE:
			{
				DLog.e(TAG, "#### onActivityResult (PICK_FROM_MULTIPLE_FILE)");
				ArrayList<Data> result = data.getParcelableArrayListExtra(DGallery.RESULT);
				ArrayList<DCameraResultDto> arrayResult = new ArrayList<DCameraResultDto>();
				
				for( int i=0; i<result.size(); i++)
				{
					DCameraResultDto retult = StorageUtils.localSaveFile(getApplicationContext(), Uri.fromFile(new File(result.get(i).getMediaPath())), mImageFirstFileName, mUserId, mRoomId, false);
					arrayResult.add(retult);
				}
				
				DCamera.mDCameraListener.onArrayResult(arrayResult); 
				finish();
			}
			break;
			case DCamera.PICK_FROM_SINGLE_FILE:
			{
				DLog.e(TAG, "#### onActivityResult (PICK_FROM_SINGLE_FILE)");
				ArrayList<Data> result = data.getParcelableArrayListExtra(DGallery.RESULT);	
				DCameraResultDto retult = StorageUtils.localSaveFile(getApplicationContext(), Uri.fromFile(new File(result.get(0).getMediaPath())), mImageFirstFileName, mUserId, mRoomId, false);
				
				switch( retult.resultCode )
				{
					case DCameraResultDto.RESULT_OK: 
						DCamera.mDCameraListener.onResult(retult.resultFile); 
					break;
					case DCameraResultDto.RESULT_EXCEPTION: 
						DCamera.mDCameraListener.onException(retult.exception); 
					break;
					case DCameraResultDto.RESULT_CANCELED: 
						DCamera.mDCameraListener.onCancelled(retult.message);
					break;
				}
				
				finish();
			}
			break;
			
			case DCamera.PICK_CROP_FROM_SINGLE_FILE:
			{
				DLog.e(TAG, " ## DCamera.PICK_CROP_FROM_SINGLE_FILE");
				ArrayList<Data> result = data.getParcelableArrayListExtra(DGallery.RESULT);
				
				mImageCaptureType = DCamera.CROP_FORM_DCAMERA;
				Intent intent = new Intent(this, DCrop.class);
				intent.putExtra("ImageCapturePath", result.get(0).getMediaPath());
				intent.putExtra("ImageFirstFileName", mImageFirstFileName);
				intent.putExtra("UserId", mUserId);
				intent.putExtra("RoomId", mRoomId);
				intent.putExtra("Departure", DCamera.PICK_CROP_FROM_SINGLE_FILE);
				intent.putExtra("return-data", true);
				startActivityForResult(intent, mImageCaptureType);
			}
			break;
			case DCamera.VOD_FROM_DCAMERA:
			{
				Intent intent = this.getIntent();
				this.setResult(RESULT_OK, intent);
				finish();
			}
			break;
			default:
			{
				DCamera.mDCameraListener.onCancelled("해당정보를 처리할수없음[-99]");
				finish();
			}
			break;
		
		}
	}
	
	private void mediaScan(final String path, MediaScannerListener listener)
	{
		mMediaScannerListener = listener;
		
		if(mediaScanner == null)
		{
			mediaScanner = new MediaScannerConnection(getBaseContext(), new MediaScannerConnection.MediaScannerConnectionClient()
			{
				@Override
				public void onScanCompleted(String path, Uri uri)
				{
					DLog.e(TAG, "onScanCompleted called.");
					ismMediaScanning = false;
					mediaScanner.disconnect(); 
					mMediaScannerListener.onScanCompleted(path, Uri.fromFile(new File(path)));
				}
				
				@Override
				public void onMediaScannerConnected()
				{
					mediaScanner.scanFile(path, null);
				}
			});
		}
			
		if(!ismMediaScanning)
		{
			mediaScanner.connect();
			ismMediaScanning = true;	
		}
	}
	
	/*
	==== 현재 오디오 및 비디오는 미사용
	public void startRecordingVideo(int type)
	{
		DLog.e(TAG, "#### startRecordingVideo (" + type + ")");
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		mImageCaptureUri = Uri.fromFile(new File(SAVE_PATH + String.valueOf(System.currentTimeMillis()) + ".mp4"));
		mTmpImageCaptureUri = mImageCaptureUri;
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
		intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
		intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (long) (1024 * 1024 * 30));
		startActivityForResult(intent, type);
	}
	
	@SuppressLint("NewApi")
	public void startCaptureAudio()
	{
		String mFileName;
		
		PackageManager pmanager = this.getPackageManager();
		if (pmanager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE))
		{
			try
			{
				
				mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
				mFileName += "/audiorecordtest.3gp";
				
				MediaRecorder mediaRecorder = new MediaRecorder();
				mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				mediaRecorder.setOutputFile(mFileName);
				mediaRecorder.prepare();
				mediaRecorder.start();
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
		}
		else
		{ // no mic on device
			Toast.makeText(this, "This device doesn't have a mic!", Toast.LENGTH_LONG).show();
		}
	}
	*/
}
