package lib.netmania.dcamera.gallery.databinders;

import java.util.TreeMap;

import lib.netmania.dcamera.gallery.DGallery;
import lib.netmania.dcamera.gallery.GalleryChildActivity;
import lib.netmania.dcamera.gallery.customcomponents.RecyclingImageView;
import lib.netmania.dcamera.gallery.workers.CacheManager;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class GalleryPopulator extends DataAdapter
{
	private TreeMap<String, String> mObjects;
	private String mFolderNames[];
	
	public GalleryPopulator(Context context, int resource, TreeMap<String, String> objects, String galleryType, CacheManager cacheManager, int mGalleryStyle)
	{
		super(context, resource, galleryType, cacheManager, mGalleryStyle);
		this.mFolderNames = objects.keySet().toArray(new String[0]);
		this.mObjects = objects;
		super.setItemCount(mFolderNames.length);
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		
		GalleryHolder holder;
		if (convertView == null)
		{
			
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(lib.netmania.dcamera.R.layout.gridview_gallery_item, parent, false);
			holder = new GalleryHolder();
			holder.folderName = (TextView) convertView.findViewById(lib.netmania.dcamera.R.id.tv_folder);
			holder.folderThumbnail = (RecyclingImageView) convertView.findViewById(lib.netmania.dcamera.R.id.iv_thumbnail);
			convertView.setTag(holder);
			
		}
		else
			holder = (GalleryHolder) convertView.getTag();
		
		holder.folderName.setText(mFolderNames[position]);
		
		if (getGallerytype().equals(DGallery.IMAGES))
			loadBitmap(DGallery.IMAGES, mObjects.get(mFolderNames[position]).toString(), null, holder.folderThumbnail);
		else
			loadBitmap(DGallery.VIDEOS, mObjects.get(mFolderNames[position]).toString(), null, holder.folderThumbnail);
		
		OnClickListener clickListener = new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getContext(), GalleryChildActivity.class);
				intent.putExtra(GalleryChildActivity.EXTRA_ARGS, mFolderNames[position]);
				intent.putExtra(GalleryChildActivity.ARGS, getGallerytype());
				intent.putExtra(GalleryChildActivity.STYLE, getGallerystyle());
				((DGallery) getContext()).startChildActivity(intent);
			}
		};
		
		holder.folderThumbnail.setOnClickListener(clickListener);
		holder.folderName.setOnClickListener(clickListener);
		
		return convertView;
	}
	
}
