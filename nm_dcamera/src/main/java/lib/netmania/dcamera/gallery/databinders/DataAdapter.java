package lib.netmania.dcamera.gallery.databinders;

import java.util.concurrent.RejectedExecutionException;

import lib.netmania.dcamera.gallery.workers.CacheManager;
import lib.netmania.dcamera.gallery.workers.GalleryLoader;
import lib.netmania.dcamera.gallery.workers.GalleryLoader.AsyncDrawable;
import lib.netmania.dcamera.log.DLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class DataAdapter extends BaseAdapter
{
	
	// current context
	private Context mContext;
	
	// placeholder bitmap
	private static Bitmap mPlaceHolderBitmap;
	
	// cache for thumbnail images
	private CacheManager mCacheManager;
	
	// gallery type
	private String mGalleryType;
	private int mGalleryStyle;
	
	// total number of items in view
	private int ITEM_COUNT = 0;
	
	/**
	 * constructor
	 * 
	 * @param context
	 * @param resource
	 * @param galleryType
	 * @param cacheManager
	 */
	public DataAdapter(Context context, int resource, String galleryType, CacheManager cacheManager, int mGalleryStyle)
	{
		
		this.mContext = context;
		this.mGalleryType = galleryType;
		this.mCacheManager = cacheManager;
		this.mGalleryStyle = mGalleryStyle;
	}
	
	@Override
	public int getCount()
	{
		return ITEM_COUNT;
	}
	
	@Override
	public Object getItem(int position)
	{
		return null;
	}
	
	@Override
	public long getItemId(int position)
	{
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * function for getting current context
	 * 
	 * @return current context
	 */
	public Context getContext()
	{
		return mContext;
	}
	
	/**
	 * setting total items available in view
	 * 
	 * @param itemCount
	 */
	public void setItemCount(int itemCount)
	{
		ITEM_COUNT = itemCount;
	}
	
	/**
	 * function for getting gallery type
	 * 
	 * @return gallery type
	 */
	public String getGallerytype()
	{
		return mGalleryType;
	}
	
	public int getGallerystyle()
	{
		return mGalleryStyle;
	}
	
	/**
	 * Load bitmaps from the cache if it is available in cache otherwise process
	 * the bitmap and save it in cache
	 *
	 * @param type
	 * @param string
	 * @param imageView
	 */
	public void loadBitmap(String type, String str_resourceId, String string, ImageView imageView)
	{
		
		Long resourceId;
		
		if(string == null)
		{
			String[] result = str_resourceId.split("\\*");
			resourceId = Long.valueOf(result[0]);
			string = result[1];
		}
		else
		{
			resourceId = Long.valueOf(str_resourceId);
		}
		
		
		DLog.e("TAG", "str_resourceId : " + str_resourceId + ", resourceId : " + resourceId + "str : " + string);
		
		
		
		
		if (GalleryLoader.cancelPotentialWork(resourceId, imageView))
		{
			GalleryLoader galleryLoader = new GalleryLoader(mContext, imageView, type, mCacheManager, string);
			
			
			DLog.e("TAG", string);
			
			if(string == null)
			{
				
			}
			
			AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), mPlaceHolderBitmap, galleryLoader);
			imageView.setImageDrawable(asyncDrawable);
			try
			{
				galleryLoader.execute(resourceId);
			} 
			catch (RejectedExecutionException exception)
			{
				exception.printStackTrace();
			}
		}
		
	}
}
