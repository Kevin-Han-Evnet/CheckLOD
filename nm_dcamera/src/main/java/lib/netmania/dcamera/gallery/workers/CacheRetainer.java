package lib.netmania.dcamera.gallery.workers;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.util.LruCache;

public class CacheRetainer extends Fragment
{
	
	private static final String TAG = "RetainFragment";
	public LruCache<String, BitmapDrawable> mRetainedCache;
	
	public CacheRetainer()
	{
	}
	
	public static CacheRetainer findOrCreateRetainFragment(FragmentManager fm)
	{
		CacheRetainer fragment = (CacheRetainer) fm.findFragmentByTag(TAG);
		if (fragment == null)
		{
			fragment = new CacheRetainer();
			fm.beginTransaction().add(fragment, TAG).commit();
		}
		return fragment;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
}