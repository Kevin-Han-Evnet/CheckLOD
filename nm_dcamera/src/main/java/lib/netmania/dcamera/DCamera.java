package lib.netmania.dcamera;

import java.io.File;
import java.util.ArrayList;

import com.squareup.picasso.Picasso;
import lib.netmania.dcamera.dto.DCameraResultDto;
import lib.netmania.dcamera.log.DLog;
import lib.netmania.dcamera.photo_slider.ActivityPhotoSlider;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;

public class DCamera
{
	private final String TAG = "DCamera";
	private final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/Dotoc/";
	private static final int CHILD_REQUEST = 1;

	
	public static final String GALLERY_TYPE = "gallery_type";
	public static final float IMG_SCALED = 0.4f;

	public static final int PICK_FROM_DCAMERA = 1;
	public static final int PICK_CROP_FROM_DCAMERA = 2;
	public static final int PICK_FROM_SINGLE_FILE = 3;
	public static final int PICK_CROP_FROM_SINGLE_FILE = 4;
	public static final int PICK_FROM_MULTIPLE_FILE = 5;
	public static final int CROP_FORM_DCAMERA = 10;
	public static final int VOD_FROM_DCAMERA = 11;
	public static final int VOD_FROM_FILE = 12;
	public static final int AUDIO_FROM_DCAMERA = 20;
	
	
	public static ProgressDialog progressDialog = null;
	
	private Context mContxt = null;
	private int mImageCaptureType = PICK_FROM_DCAMERA;
	private String mImageFirstFileName = "";
	private MediaRecorder recorder = null;
	
	public static DCameraListener mDCameraListener = null;

	private static DCamera mDcamera;
	private static Picasso dPicasso;
	
	// 인터페이스
	public static interface DCameraListener
	{
		public void onResult(File file);
		public void onArrayResult(ArrayList<DCameraResultDto> array);
		public void onException(Exception e);
		public void onCancelled(String msg);
	}

	public DCamera(Context c, Picasso dPicasso)
	{
		this.mContxt = c;
		this.mDcamera = this;
		this.dPicasso = dPicasso;
	}

	public void setLogging(boolean f)
	{
		DLog.DEBUG_FLAG = f;
	}

	public void startImageCapture(int imageCaptureType, String imageFirstFileName, String userId, String roomId, DCameraListener listener) throws Exception
	{
		this.mImageCaptureType = imageCaptureType;
		this.mImageFirstFileName = imageFirstFileName;
		this.mDCameraListener = listener;
		progressDialog = ProgressDialog.show(mContxt, "", ((mImageCaptureType > 2) ? "앨범" : "카메라 ") + "준비중 입니다.");

		Intent intent = new Intent(mContxt, Bridge.class);
		intent.putExtra("ImageCaptureType", mImageCaptureType);
		intent.putExtra("ImageFirstFileName", mImageFirstFileName);
		intent.putExtra("UserId", userId);
		intent.putExtra("RoomId", roomId);
		//intent.putExtra("listener", listener);
		
		intent.putExtra("return-data", true);
		((Activity) mContxt).startActivityForResult(intent, mImageCaptureType);

	}


	/** 포토 슬라이더 실행.
	 *
	 */
	public void startImageGallery (ArrayList<String> datas, int first) {

		Intent intent = new Intent (mContxt, ActivityPhotoSlider.class);
		intent.putExtra (ActivityPhotoSlider.IMAGE_LIST, datas);
		intent.putExtra (ActivityPhotoSlider.FIRST_IDX, first);
		mContxt.startActivity(intent);
	}


	public static DCamera getInstance () {
		return mDcamera;
	}

	public static Picasso getPicasso () {
		return dPicasso;
	}

	/*
	public String getVoiceFileName()
	{
		File file = new File(SAVE_PATH);

		if (!file.exists())
		{
			file.mkdirs();
		}

		return (file.getAbsolutePath() + "/" + "dotocvoice" + ".3gp");
	}

	public void startRecording()
	{
		recorder = new MediaRecorder();

		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(getVoiceFileName());

		try
		{
			recorder.prepare();
			recorder.start();
		} 
		catch (IllegalStateException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void stopRecording()
	{
		if (null != recorder)
		{
			recorder.stop();
			recorder.reset();
			recorder.release();

			recorder = null;
		}
	}
	*/
}
