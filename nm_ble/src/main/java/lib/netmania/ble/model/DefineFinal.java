package lib.netmania.ble.model;

import android.text.format.DateFormat;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by syJ_Mac on 16. 4. 19..
 */
public class DefineFinal
{
	public static final String LOG_TAG = "TKS";


	// num of temps on eatch EPC
	public static final int LIST_TEMP_MAX	= 25;


	// for beacon
	// define becon item size

	// chart setting values
	/**
	 * for graph
	 */
	public static final int CHART_PADDING_TOP_BOTTOM	= 50;
	public static final int CHART_PADDING_LEFT	 		= 80;
	public static final int CHART_PADDING_RIGHT			= 10;
	public static final int CHART_MARGIN				= 0;
	public static final int CHART_Y_MAX					= 40;
	public static final int CHART_Y_STEP				= 5;

	public static String getDate(long time)
	{
		Calendar cal = Calendar.getInstance(Locale.KOREA);
		cal.setTimeInMillis(time);
		String date = DateFormat.format("yy-MM-dd", cal).toString();
		return date;
	}

	public static String getTime(long time)
	{
		Calendar cal = Calendar.getInstance(Locale.KOREA);
		cal.setTimeInMillis(time);
		String date = DateFormat.format("hh:mm:ss", cal).toString();
		return date;
	}
}
