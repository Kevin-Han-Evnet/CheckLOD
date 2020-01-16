package com.netmania.checklod.general.http;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;


import com.netmania.checklod.general.BaseApplication;
import com.netmania.checklod.general.activities.BaseActivity;
import com.netmania.checklod.general.manage.Constants;
import com.netmania.checklod.general.utils.GeneralUtils;
import com.netmania.checklod.general.utils.LogUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class UpdateApp extends AsyncTask<String,Void,Void> {
	private Context context;
	private UpdateAppListener listener;
	
	public void setContext (Context context, UpdateAppListener listener){
	   this.context = context;
	   this.listener = listener;
	}
	
	@Override
	protected Void doInBackground(String... arg0) {
		
		int count;
        try {

            URL url = new URL(arg0[0]);
            URLConnection conexion = url.openConnection();
            conexion.connect();

            int lenghtOfFile = conexion.getContentLength();

            new File ("/sdcard/" + Constants.APP_ID).mkdir();

            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream("/sdcard/" + Constants.APP_ID + "/tmp.apk");

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                listener.onProgress ((int)((total*100)/lenghtOfFile));
                output.write (data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            listener.onComplete ();

            File file = new File ("/sdcard/" + Constants.APP_ID + "/tmp.apk");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {

                Uri fileUri = FileProvider.getUriForFile(context.getApplicationContext(), Constants.APP_ID + ".provider", file);
                Intent intent = new Intent(Intent.ACTION_QUICK_VIEW, fileUri);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);

                context.startActivity(intent);

                BaseApplication.getInstance().finishAll();

            } else {

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);



                BaseApplication.getInstance().finishAll();

            }


        } catch (Exception e) {
            Log.e("UpdateAPP", "Update error! " + e.getMessage());
            e.printStackTrace();
        }
	    return null;
	}
	
	//리스너
	public interface UpdateAppListener {
		public void onProgress(int current);
		public void onComplete();
	}
}  
