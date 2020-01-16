package lib.netmania.dcamera.gallery;

import java.util.ArrayList;

import lib.netmania.dcamera.DCamera;
import lib.netmania.dcamera.R;
import lib.netmania.dcamera.gallery.databinders.ChildGalleryPopulator;
import lib.netmania.dcamera.gallery.databinders.Data;
import lib.netmania.dcamera.gallery.databinders.DataHolder;
import lib.netmania.dcamera.gallery.databinders.SavedIndexes;
import lib.netmania.dcamera.gallery.workers.CacheManager;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GalleryChildActivity extends Activity
{
	
	private GridView mGalleryView;
	private ArrayList<DataHolder> mFiles;
	private ArrayList<String> mSelectedIndices;
	private ArrayList<Data> mSelectedFiles;
	private ArrayList<SavedIndexes> mSavedSelectedIndices;
	private CacheManager mCacheManager = null;
	private ChildGalleryPopulator mGallerypopulator;
	private String mGalleryType;
	private static String[] mProjection;
	private static Uri mGalleryUri;
	private static String mSelection;
	private static String mFilterData;
	private boolean mShowMenuItems = false;
	private int mItemCounter = 0;
	private int mGalleryStyle;

	//UI
	private ImageView imgBtnAttachSubmit, dsTitleImg, icon_attach_count, imgBtnBack;
	private TextView gallerySelectedCount;
	
	public static final String EXTRA_ARGS = "args";
	public static final String ARGS = "arguments";
	public static final String STYLE = "style";
	public static final String VIDEOS = "videos";
	public static final String IMAGES = "images";
	public static final String STATE_SELECTED_ITEM_COUNT = "itemcount";
	public static final String STATE_SAVED_ITEMS = "selecteditems";
	public static final int PREVIEW_REQUEST = 0;
	
	private RelativeLayout mRelativeLayout = null;
	private static GalleryChildActivity mActivity;
	private int attachMaxCount = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_gallery_view);


		Log.i("FUCK", "내가 열렸다 !!! --> " + getClass().getSimpleName());

		mActivity = this;


		mFilterData = getIntent().getStringExtra(EXTRA_ARGS);
		mGalleryType = getIntent().getStringExtra(ARGS);
		mGalleryStyle =  getIntent().getIntExtra(STYLE, DCamera.PICK_CROP_FROM_SINGLE_FILE);

		if (savedInstanceState != null)
		{
			mItemCounter = savedInstanceState.getInt(STATE_SELECTED_ITEM_COUNT);
			mSavedSelectedIndices = savedInstanceState.getParcelableArrayList(STATE_SAVED_ITEMS);
			mSelectedIndices = new ArrayList<String>();
			if (mSavedSelectedIndices != null && mSavedSelectedIndices.size() > 0)
			{
				for (SavedIndexes index : mSavedSelectedIndices)
				{
					mSelectedIndices.add(index.getPosition());
				}
			}

		}
		
		mCacheManager = new CacheManager(this, getFragmentManager());
		
		if (mGalleryType.equals(IMAGES))
		{
			mGalleryUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			mProjection = new String[]
			{ MediaStore.Images.Media.DATA, MediaStore.Images.Thumbnails._ID };
			mSelection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + " = ?";
		}
		else
		{
			mGalleryUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			mProjection = new String[]
			{ MediaStore.Video.Media.DATA, MediaStore.Video.Thumbnails._ID };
			mSelection = MediaStore.Video.Media.BUCKET_DISPLAY_NAME + " = ?";
		}
		
		mGalleryView = (GridView) findViewById(R.id.gv_gallery);
		mRelativeLayout = (RelativeLayout) findViewById(R.id.multi_save_btn);
		/*if(mGalleryStyle == DCamera.PICK_FROM_MULTIPLE_FILE)
			mRelativeLayout.setVisibility(View.VISIBLE);*/


		gallerySelectedCount = (TextView) findViewById (R.id.gallerySelectedCount);

		imgBtnAttachSubmit = (ImageView) findViewById (R.id.imgBtnAttachSubmit);
		imgBtnAttachSubmit.setOnClickListener(photoSelectSubmitListener);


		if (mGalleryStyle == DCamera.PICK_FROM_SINGLE_FILE) {
			imgBtnAttachSubmit.setImageResource(R.drawable.selector_btn_attach_done);
		} else {
			imgBtnAttachSubmit.setImageResource(R.drawable.selector_topper_btn_attach);
		}


		dsTitleImg = (ImageView) findViewById (R.id.dsTitleImg);
		icon_attach_count = (ImageView) findViewById(R.id.icon_attach_count);

		int max = attachMaxCount = (mGalleryStyle == DCamera.PICK_FROM_SINGLE_FILE) ? ChildGalleryPopulator.SINGLE_MAX_COUNT : ChildGalleryPopulator.MULTIPLE_MAX_COUNT;
		int count = (mSelectedIndices != null) ? mSelectedIndices.size() : 0;
		gallerySelectedCount.setText(count + "/" + max);

		if (mSelectedIndices!= null && mSelectedIndices.size() > 0) {
			imgBtnAttachSubmit.setEnabled (true);
		} else {
			imgBtnAttachSubmit.setEnabled(false);
		}


		//탑메뉴 제어
		if (mGalleryStyle == DCamera.PICK_CROP_FROM_SINGLE_FILE) {
			gallerySelectedCount.setVisibility(View.GONE);
			icon_attach_count.setVisibility (View.GONE);
			dsTitleImg.setVisibility (View.VISIBLE);
			imgBtnAttachSubmit.setVisibility (View.GONE);
		} else {
			gallerySelectedCount.setVisibility(View.VISIBLE);
			icon_attach_count.setVisibility (View.VISIBLE);
			dsTitleImg.setVisibility (View.GONE);
			imgBtnAttachSubmit.setVisibility (View.VISIBLE);
		}


		findViewById(R.id.imgBtnRotateImage).setVisibility(View.GONE);

		
		this.populateChildGallery();
	}
	
	private void populateChildGallery()
	{
		
		mFiles = readGallery(this);
		System.gc();
		
		mGallerypopulator = new ChildGalleryPopulator(
				this,
				R.layout.gridview_child_gallery_item,
				mGalleryType,
				mFiles,
				mCacheManager,
				mGalleryStyle,
				mRelativeLayout,
				new PhotoSelectedListener() {
					@Override
					public void onSelected(int count, int max) {
						gallerySelectedCount.setText (count + "/" + max);


						if (count > 0) {
							imgBtnAttachSubmit.setEnabled (true);
						} else {
							imgBtnAttachSubmit.setEnabled(false);
						}
					}
				}
		);
		mGalleryView.setAdapter(mGallerypopulator);



		imgBtnBack = (ImageView) findViewById (R.id.imgBtnBack);
		imgBtnBack.setOnClickListener(kevinClickListener);
		
	}
	
	@Override
	protected void onResume()
	{
		if (mItemCounter > 0)
		{
			mGallerypopulator.setMultiSelectionEnabled(true);
			mGallerypopulator.setSelectedItemList(mSelectedIndices);
		}


		if (mGallerypopulator != null) mGallerypopulator.notifyDataSetChanged();

		super.onResume();
	}
	
	public static ArrayList<DataHolder> readGallery(Activity activity)
	{
		
		Cursor cursor;
		int column_thumbnail_data, column_file_path;
		ArrayList<DataHolder> listOfAllImages = new ArrayList<DataHolder>();
		cursor = activity.getContentResolver().query(mGalleryUri, mProjection, mSelection, new String[]
				{mFilterData}, MediaStore.Images.Media.DATE_ADDED + " DESC");
		
		if (cursor == null)
			return listOfAllImages;
		
		column_thumbnail_data = cursor.getColumnIndexOrThrow(mProjection[1]);
		column_file_path = cursor.getColumnIndexOrThrow(mProjection[0]);
		
		while (cursor.moveToNext())
		{
			DataHolder holder = new DataHolder();
			holder.setMediapath(cursor.getString(column_file_path));
			holder.setThumbnailData(cursor.getString(column_thumbnail_data));
			listOfAllImages.add(holder);
		}
		
		mGalleryUri = null;
		cursor = null;
		column_file_path = 0;
		column_thumbnail_data = 0;
		mProjection = null;
		
		return listOfAllImages;
	}

	/** 서브밋
	 *
	 *
	 */
	public void gotoResult()
	{


		View loadingView = LayoutInflater.from (GalleryChildActivity.this).inflate(R.layout.layout_loading_screen, null);
		loadingView.setLayoutParams (new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
		((RelativeLayout) findViewById(R.id.root_container)).addView (loadingView);


		mSelectedIndices = mGallerypopulator.getSelectedItemList();
		
		if (mSelectedIndices.size() <= 0)
			return;
		
		mSelectedFiles = new ArrayList<Data>();
		
		Data dataMover;
		
		for (String position : mSelectedIndices)
		{
			dataMover = new Data();
			dataMover.setMediapath(mFiles.get(Integer.valueOf(position)).getMediaPath());
			dataMover.setThumbnailData(mFiles.get(Integer.valueOf(position)).getThumbnailData());
			mSelectedFiles.add(dataMover);

		}
		
		Intent intent = new Intent();
		intent.putParcelableArrayListExtra(DGallery.RESULT, mSelectedFiles);
		this.setResult(RESULT_OK, intent);
		finish();
	}



	/** 선낵 반영
	 *
	 * @param itemPosition
	 */
	public void updateAdapterView (final int itemPosition) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mGallerypopulator.updateViewState(itemPosition);
			}
		});
	}

	public ArrayList<String> getSelections () {
		return mGallerypopulator.getSelections();
	}

	public int getAttachMaxCount () {
		return attachMaxCount;
	}

	public int getGalleryStyle () {
		return mGalleryStyle;
	}

	/** 나!!
	 *
	 * @return
	 */
	public static GalleryChildActivity getInstance () {
		return mActivity;
	}


	@Override
	public void onDestroy () {
		super.onDestroy();
		//Runtime.getRuntime().gc();
	}

	@Override
	public void onBackPressed () {
		super.onBackPressed ();
	}


	//리스너 -----------------------------------------------------------------------------
	View.OnClickListener photoSelectSubmitListener = new View.OnClickListener () {

		@Override
		public void onClick(View src) {
			gotoResult();
		}
	};

	public interface PhotoSelectedListener {
		void onSelected (int count, int max);
	}


	private View.OnClickListener kevinClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View src) {
			//하나니까 분기하지 말자.
			GalleryChildActivity.this.onBackPressed();
		}
	};
}
