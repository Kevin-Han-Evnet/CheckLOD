package lib.netmania.dcamera.gallery.databinders;

import java.util.ArrayList;

import lib.netmania.dcamera.DCamera;
import lib.netmania.dcamera.gallery.DGallery;
import lib.netmania.dcamera.gallery.GalleryChildActivity;
import lib.netmania.dcamera.gallery.customcomponents.GalleryFrameLayout;
import lib.netmania.dcamera.gallery.customcomponents.RecyclingImageView;
import lib.netmania.dcamera.gallery.workers.CacheManager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ChildGalleryPopulator extends DataAdapter
{
	
	private ArrayList<DataHolder> mFiles;
	private ArrayList<String> mSelections;
	private boolean isMultiSelectionEnabled = false;
	private int mGalleryStyle;
	private RelativeLayout mRelativeLayout = null;

	public static final int SINGLE_MAX_COUNT = 1;
	public static final int MULTIPLE_MAX_COUNT = 20;

	private GalleryChildActivity.PhotoSelectedListener listener;
	
	public ChildGalleryPopulator(Context context,
								 int resource,
								 String galleryType,
								 ArrayList<DataHolder> data,
								 CacheManager cacheManager,
								 int galleryStyle,
								 RelativeLayout relativeLayout,
								 GalleryChildActivity.PhotoSelectedListener listener)
	{
		
		super(context, resource, galleryType, cacheManager, galleryStyle);
		this.mFiles = data;
		this.mSelections = new ArrayList<String>();
		this.mGalleryStyle = galleryStyle;
		this.mRelativeLayout = relativeLayout;
		this.listener = listener;
		
		if(mGalleryStyle == DCamera.PICK_FROM_MULTIPLE_FILE)
			setMultiSelectionEnabled(true);
		
		
		super.setItemCount(mFiles.size());
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		final GalleryHolder holder;

		mFiles.get(position).setHolder(null);
		
		if (convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(lib.netmania.dcamera.R.layout.gridview_child_gallery_item, parent, false);
			holder = new GalleryHolder();
			holder.folderThumbnail = (RecyclingImageView) convertView.findViewById(lib.netmania.dcamera.R.id.iv_thumbnail);
			holder.baseLayout = (GalleryFrameLayout) convertView.findViewById(lib.netmania.dcamera.R.id.baseItemLayout);
			holder.sys_btn_zoom = (ImageView) convertView.findViewById(lib.netmania.dcamera.R.id.sys_btn_zoom);
			convertView.setTag(holder);
		}
		else
			holder = (GalleryHolder) convertView.getTag();
		
		if (mSelections.contains(String.valueOf(position)))
			holder.baseLayout.setChecked(true, mSelections.indexOf(String.valueOf(position)) + 1);
		else
			holder.baseLayout.setChecked(false, 0);
		
		if (getGallerytype().equals(DGallery.IMAGES))
			loadBitmap(DGallery.IMAGES, mFiles.get(position).getThumbnailData(), mFiles.get(position).getMediaPath(), holder.folderThumbnail);
		else
			loadBitmap(DGallery.VIDEOS, mFiles.get(position).getThumbnailData(), mFiles.get(position).getMediaPath(), holder.folderThumbnail);
		
		holder.baseLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*if (isMultiSelectionEnabled) {
					if (mSelections.size() < MULTIPLE_MAX_COUNT || holder.baseLayout.isChecked()) {
						updateViewState(holder.baseLayout, position);
					}
				} else */
				if (mGalleryStyle == DCamera.PICK_CROP_FROM_SINGLE_FILE) {
					mSelections.add(String.valueOf(position));
					((GalleryChildActivity) getContext()).gotoResult();
				} else if (mGalleryStyle == DCamera.PICK_FROM_SINGLE_FILE) {
					if (mSelections.size() < SINGLE_MAX_COUNT || holder.baseLayout.isChecked()) {
						updateViewState(holder.baseLayout, position);
					}
				} else {
					if (mSelections.size() < MULTIPLE_MAX_COUNT || holder.baseLayout.isChecked()) {
						updateViewState(holder.baseLayout, position);
					}
				}

			}
		});


		holder.sys_btn_zoom.setTag(position);
		holder.sys_btn_zoom.setOnClickListener (new OnClickListener () {

			@Override
			public void onClick(View v) {

				ArrayList<String> datas = new ArrayList<String> ();
				for (int i = 0; i < getCount(); i++) {
					datas.add (mFiles.get(i).getMediaPath());
				}

				DCamera.getInstance().startImageGallery(datas, position);

			}
		});


		/** 이놈이네 멀티 */
		mRelativeLayout.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				((GalleryChildActivity) getContext()).gotoResult();
			}
		});


		holder.myPosition = position;
		mFiles.get(position).setHolder(holder);

		
		return convertView;
	}
	
	public void setMultiSelectionEnabled(boolean enable)
	{
		isMultiSelectionEnabled = enable;
	}
	
	public void clearSelectedItemList()
	{
		mSelections.clear();
	}
	
	public ArrayList<String> getSelectedItemList()
	{
		return mSelections;
	}
	
	/**
	 * function for setting list of indices
	 * 
	 * @param items
	 *            list of indices
	 */
	public void setSelectedItemList(ArrayList<String> items)
	{
		mSelections = items;
	}
	
	/**
	 * function for updating view state <br>
	 * 
	 * @param frameLayout
	 * @param itemPosition
	 */
	private void updateViewState(GalleryFrameLayout frameLayout, int itemPosition)
	{
		// if view is already checked
		if (frameLayout.isChecked())
		{


			int tIdx = mSelections.indexOf (String.valueOf (itemPosition));

			// remove the position of view from selected item list
			mSelections.remove(String.valueOf(itemPosition));
			// unchecking the view
			frameLayout.setChecked(false, 0);


			int k = -1;
			for (int i = tIdx; i < mSelections.size (); i++) {
				k = Integer.valueOf (mSelections.get(i));

				if (k == mFiles.get(k).getHolder().myPosition) {
					mFiles.get(k).getHolder().baseLayout.setChecked(false, 0);
					mFiles.get(k).getHolder().baseLayout.setChecked(true, i + 1);
				}
			}

		}
		else
		{
			// adding the position of view to selected item list
			mSelections.add(String.valueOf(itemPosition));
			// checking the view
			frameLayout.setChecked(true, mSelections.size());
		}


		int max = (isMultiSelectionEnabled) ? MULTIPLE_MAX_COUNT : SINGLE_MAX_COUNT;
		listener.onSelected (mSelections.size (), max);
	}


	/** 뷰 업데이트
	 *
	 * @param itemPosition
	 */
	public void updateViewState (int itemPosition) {

		if (mFiles.get(itemPosition).getHolder() != null
				&& itemPosition == mFiles.get(itemPosition).getHolder().myPosition) {
			updateViewState (mFiles.get(itemPosition).getHolder().baseLayout, itemPosition);
		} else if (mSelections.contains (String.valueOf(itemPosition))) {
			mSelections.remove(String.valueOf(itemPosition));
			int max = (isMultiSelectionEnabled) ? MULTIPLE_MAX_COUNT : SINGLE_MAX_COUNT;
			listener.onSelected(mSelections.size(), max);
		} else {
			mSelections.add (String.valueOf(itemPosition));
			int max = (isMultiSelectionEnabled) ? MULTIPLE_MAX_COUNT : SINGLE_MAX_COUNT;
			listener.onSelected (mSelections.size (), max);
		}
	}

	/** 너의 선택을 내놓거라.
	 *
	 * @return
	 */
	public ArrayList<String> getSelections () {
		return mSelections;
	}


}
