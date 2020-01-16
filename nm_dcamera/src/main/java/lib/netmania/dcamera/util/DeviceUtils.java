package lib.netmania.dcamera.util;

import android.app.Activity;
import android.util.DisplayMetrics;

public class DeviceUtils
{
	public static int getDisplayWidth(Activity context)
	{
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.widthPixels;
	}

	public static int getDisplayHeight(Activity context)
	{
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.heightPixels;
	}

}
