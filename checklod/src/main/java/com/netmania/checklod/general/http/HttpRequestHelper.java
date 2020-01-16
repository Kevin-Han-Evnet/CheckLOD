package com.netmania.checklod.general.http;

import android.net.http.AndroidHttpClient;


import com.netmania.checklod.general.utils.IOUtils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class HttpRequestHelper
{
	public File download(String url, File toFile) throws IOException
	{
		AndroidHttpClient client = AndroidHttpClient.newInstance("ANDROID");
		HttpGet getRequest = new HttpGet(url);
		try
		{
			HttpResponse response = client.execute(getRequest);
			HttpParams httpParams = new BasicHttpParams();
		    HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
		    HttpConnectionParams.setSoTimeout(httpParams, 10000);
			checkStatusAndThrowExceptionWhenStatusIsNotOK(response);
			return writeResponseToFileAndGet(response.getEntity(), toFile);
		} catch (IOException e)
		{
			getRequest.abort();
			throw e;
		} finally
		{
			client.close();
		}
	}

	private void checkStatusAndThrowExceptionWhenStatusIsNotOK(HttpResponse response) throws IOException
	{
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK)
		{
			throw new IOException("invalid response code:" + statusCode);
		}
	}

	private File writeResponseToFileAndGet(HttpEntity entity, File toFile) throws IOException
	{
		InputStream in = null;
		try
		{
			IOUtils.copy(entity.getContent(), toFile);
			return toFile;
		} finally
		{
			IOUtils.close(in);
			entity.consumeContent();
		}
	}

	public static HttpRequestHelper getInstance()
	{
		return new HttpRequestHelper();
	}
}
