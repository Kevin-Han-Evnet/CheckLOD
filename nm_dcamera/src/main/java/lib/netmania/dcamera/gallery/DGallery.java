package lib.netmania.dcamera.gallery;

import lib.netmania.dcamera.DCamera;
import lib.netmania.dcamera.log.DLog;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

public class DGallery extends Activity
{
	private final String TAG = "DGallery";
	
	public static final String GALLERY_TYPE  = "gallery_type";
	public static final String GALLERY_STYLE = "gallery_style";
	
	private static final int CHILD_REQUEST = 1;
	
	public static final String RESULT = "result";
	public static final String VIDEOS = "videos";
	public static final String IMAGES = "images";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		
		super.onCreate(savedInstanceState);
		setContentView(lib.netmania.dcamera.R.layout.activity_sams_gallery);
		
		String galleryType;
		int galleryStyle = DCamera.PICK_FROM_SINGLE_FILE;
		
		if (getIntent().getExtras() != null)
		{
			galleryType = IMAGES;
			galleryStyle = getIntent().getIntExtra(GALLERY_STYLE, DCamera.PICK_FROM_SINGLE_FILE);
		}
		else
		{
			galleryType = IMAGES;
			galleryStyle = DCamera.PICK_FROM_SINGLE_FILE;
		}
		
		DLog.e(TAG, "galleryType : " + galleryType);
		DLog.e(TAG, "galleryStyle : " + galleryStyle);
		
		Fragment fragment = new GallerySwitcherFragment();
		String argsData = galleryType + "*" + galleryStyle;
		Bundle args = new Bundle();
		
		DLog.e(TAG, "argsData : " + argsData);
		
		args.putString(GallerySwitcherFragment.ARGS, argsData);
		fragment.setArguments(args);
		getFragmentManager().beginTransaction().replace(lib.netmania.dcamera.R.id.ll_gridHolder, fragment).commit();
		
		fragment = null;
		args = null;
		
	}
	
	public void startChildActivity(Intent intent)
	{
		startActivityForResult(intent, CHILD_REQUEST);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		
		if (requestCode == CHILD_REQUEST)
		{
			switch (resultCode)
			{
			
				case RESULT_CANCELED:
					//finish(); //--> 왜끝내??? 미쳤나...
					break;
				case RESULT_OK:
					Intent intent = new Intent();
					intent.putParcelableArrayListExtra(DGallery.RESULT, data.getParcelableArrayListExtra(RESULT));
					this.setResult(RESULT_OK, intent);
					finish(); //--> 끝내는 시점은 내가 정한다. 나중에..ㅎㅎㅎ 이거 엉망이네.
					break;
				default:
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}







