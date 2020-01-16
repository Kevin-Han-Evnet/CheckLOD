package lib.netmania.dcamera.gallery;

import java.util.TreeMap;

import lib.netmania.dcamera.R;
import lib.netmania.dcamera.gallery.databinders.GalleryPopulator;
import lib.netmania.dcamera.gallery.workers.CacheManager;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class GallerySwitcherFragment extends Fragment
{
	public static final String ARGS = "arguments";
	public static final String STYLE = "style";
	public static final String MAX_FILE_COUNT = "maxFileCount";
	
	private static String[] mProjection;
	private static Uri mGalleryUri;
	
	private TreeMap<String, String> mFolders;
	private GridView mGalleryView;
	private String mGalleryType;
	private int mGalleryStyle;
	private CacheManager mCacheManager = null;

	private ImageView imgBtnAttachSubmit, dsTitleImg, icon_attach_count, imgBtnBack, imgBtnRotateImage;
	private TextView gallerySelectedCount;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		
		View rootView = inflater.inflate(R.layout.fragment_gallery_view, container, false);
		
		Bundle args = getArguments();
		
		String bundleArrayData = args.get(ARGS).toString();
		String bundleData[] = bundleArrayData.split("\\*");
		
		mGalleryType = bundleData[0];
		mGalleryStyle = Integer.parseInt(bundleData[1]);
		
		mCacheManager = new CacheManager(getActivity(), getActivity().getFragmentManager());
		
		if (mGalleryType.equals(DGallery.IMAGES))
		{
			mGalleryUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			mProjection = new String[]
			{ 
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Thumbnails._ID, MediaStore.Images.Media.DATA 
			};
		}
		else
		{
			mGalleryUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			mProjection = new String[]
			{
				MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Thumbnails._ID , MediaStore.Video.Media.DATA
			};
		}
		
		mGalleryView = (GridView) rootView.findViewById(R.id.gv_gallery);
		this.populateGallery();



		imgBtnAttachSubmit = (ImageView) rootView.findViewById(R.id.imgBtnAttachSubmit);
		imgBtnAttachSubmit.setEnabled(false);

		imgBtnRotateImage = (ImageView) rootView.findViewById(R.id.imgBtnRotateImage);
		imgBtnRotateImage.setEnabled(false);

		gallerySelectedCount = (TextView) rootView.findViewById(R.id.gallerySelectedCount);
		dsTitleImg = (ImageView) rootView.findViewById(R.id.dsTitleImg);
		icon_attach_count = (ImageView) rootView.findViewById(R.id.icon_attach_count);


		//탑메뉴 제어
		gallerySelectedCount.setVisibility(View.GONE);
		icon_attach_count.setVisibility (View.GONE);
		dsTitleImg.setVisibility (View.VISIBLE);
		imgBtnAttachSubmit.setVisibility (View.GONE);
		imgBtnRotateImage.setVisibility (View.GONE);
		((TextView) rootView.findViewById(R.id.label_upload)).setVisibility(View.GONE);


		imgBtnBack = (ImageView) rootView.findViewById (R.id.imgBtnBack);
		imgBtnBack.setOnClickListener(kevinClickListener);

		
		return rootView;
	}
	
	private void populateGallery()
	{
		mFolders = readGallery(getActivity());
		System.gc();
		GalleryPopulator populator = new GalleryPopulator(getActivity(), R.layout.gridview_gallery_item, mFolders, mGalleryType, mCacheManager, mGalleryStyle);
		mGalleryView.setAdapter(populator);
		
	}
	
	public static TreeMap<String, String> readGallery(Activity activity)
	{
		
		Cursor cursor;
		int column_thumbnail_data, column_index_folder_name, column_thumbnail_data_path;
		TreeMap<String, String> listOfAllImages = new TreeMap<String, String>();
		cursor = activity.getContentResolver().query(mGalleryUri, mProjection, null, null, null);
		
		if (cursor == null)
			return listOfAllImages;
		
		column_thumbnail_data = cursor.getColumnIndexOrThrow(mProjection[1]);
		column_index_folder_name = cursor.getColumnIndexOrThrow(mProjection[0]);
		column_thumbnail_data_path = cursor.getColumnIndexOrThrow(mProjection[2]);
		while (cursor.moveToNext())
		{
			listOfAllImages.put(cursor.getString(column_index_folder_name), cursor.getString(column_thumbnail_data) + "*" + cursor.getString(column_thumbnail_data_path));
		}
		
		mGalleryUri = null;
		cursor = null;
		column_index_folder_name = 0;
		column_thumbnail_data = 0;
		mProjection = null;
		
		return listOfAllImages;
	}


	//리스너 -----------------------------------------------------------------------------
	private View.OnClickListener kevinClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View src) {
			//하나니까 분기하지 말자.
			getActivity().onBackPressed();
		}
	};
	
}
