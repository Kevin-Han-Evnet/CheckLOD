package lib.netmania.dcamera.log;

import android.util.Log;

public class DLog
{
	public static boolean DEBUG_FLAG = false;

	public static void d(String tag, String message)
	{
		if (DEBUG_FLAG)
		{
			String log = buildLogMsg(message);
			Log.d(tag, log);
		}
	}

	public static void e(String tag, String message)
	{
		if ( DEBUG_FLAG)
		{
			String log = buildLogMsg(message);
			Log.e(tag, log);
		}
	}

	public static void i(String tag, String message)
	{
		if ( DEBUG_FLAG)
		{
			String log = buildLogMsg(message);
			Log.i(tag, log);
		}
	}

	public static void w(String tag, String message)
	{
		if ( DEBUG_FLAG)
		{
			String log = buildLogMsg(message);
			Log.w(tag, log);
		}
	}

	public static void v(String tag, String message)
	{
		if ( DEBUG_FLAG)
		{
			String log = buildLogMsg(message);
			Log.v(tag, log);
		}
	}

	private static String buildLogMsg(String message)
	{
		StackTraceElement ste = Thread.currentThread().getStackTrace()[4];
		StringBuilder sb = new StringBuilder();

		sb.append("[ ");
		sb.append(ste.getFileName());
		sb.append("| ");
		sb.append(ste.getMethodName());
		sb.append("() | ");
		sb.append(ste.getLineNumber());
		sb.append(" Line ] ");
		sb.append(message);
		return sb.toString();
	}

}
