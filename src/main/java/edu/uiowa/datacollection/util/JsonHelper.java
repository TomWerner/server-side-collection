package edu.uiowa.datacollection.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

@SuppressWarnings("deprecation")
public class JsonHelper
{

	private static String readAll(Reader rd) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1)
		{
			sb.append((char) cp);
		}
		return sb.toString();
	}

	/**
	 * This method creates a JSON object from data returned by an HTTP GET
	 * request to the given url.
	 * 
	 * @param url
	 *            The url to request data from
	 * @return JSONObject of the data returned by the url
	 * @throws IOException
	 * @throws JSONException
	 */
	public static JSONObject readJsonFromUrl(String url) throws JSONException
	{
		try
		{
			InputStream is = new URL(url).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is,
					Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);

			is.close();

			return json;
		}
		catch (IOException e)
		{
			System.out.println("ERROR: Connection to the server refused.");
			System.out.println(e.getMessage());
		}

		return null;
	}

	/**
	 * This function posts the given JSONObject to the given url using HTTP
	 * POST.
	 * 
	 * @param url
	 *            The url to post the data to
	 * @param data
	 *            The data to post
	 * @return The HttpResponse from the server
	 */
	public static HttpResponse postJsonData(String url, JSONObject data)
	{
		HttpResponse resp = null;

		try
		{
			HttpPost post = new HttpPost(url);
			post.setEntity(new ByteArrayEntity(data.toString().getBytes()));

			@SuppressWarnings({ "resource" })
			HttpClient httpclient = new DefaultHttpClient();
			try
			{
				resp = httpclient.execute(post);
			}
			catch (ClientProtocolException e)
			{
				System.out.println("ERROR: Could not connect to the server.");
				System.out.println(e.getMessage());				
			}
			catch (IOException e)
			{
				System.out.println("ERROR: Could not connect to the server.");
				System.out.println(e.getMessage());
			}
		}
		catch (IllegalStateException e)
		{
			System.out.println("ERROR: Illegal state.");
			System.out.println(e.getMessage());
		}

		return resp;
	}
}
