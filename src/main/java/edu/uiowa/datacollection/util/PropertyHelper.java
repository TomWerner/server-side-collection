package edu.uiowa.datacollection.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PropertyHelper
{
	private static final String BASE_URL_KEY = "BASE_URL";
	private static final String GET_FACEBOOK_TOKENS_URL_KEY = "GET_FACEBOOK_TOKENS_URL";
	private static final String UPLOAD_FACEBOOK_DATA_URL_KEY = "FACEBOOK_UPLOAD_URL";
	private static final String GET_TWITTER_TOKENS_URL_KEY = "GET_TWITTER_TOKENS_URL";
	private static final String UPLOAD_TWITTER_DATA_URL_KEY = "TWITTER_UPLOAD_URL";

	private String baseUrl;
	private String facebookTokensUrl;
	private String facebookUploadURL;
	private String twitterTokensUrl;
	private String twitterUploadUrl;

	/*
	 * Other properties can be added here
	 */
	public PropertyHelper(String fileName)
	{
		BufferedReader br;
		ArrayList<String> propertyList = null;
		try
		{
			br = new BufferedReader(new FileReader(fileName));
			propertyList = new ArrayList<String>();
			String line;
			while ((line = br.readLine()) != null)
			{
				propertyList.add(line);
			}
			br.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("ERROR: Could not find file.");
			System.out.println(e.getMessage());
		}
		catch (IOException e)
		{
			System.out.println("ERROR: Could not open the file.");
			System.out.println(e.getMessage());
		}

		for (int i = 0; i < propertyList.size(); i++)
		{
			String property = propertyList.get(i);
			String temp[] = property.split("=");

			if (temp[0].equals(BASE_URL_KEY))
				baseUrl = temp[1];
			if (temp[0].equals(GET_FACEBOOK_TOKENS_URL_KEY))
				facebookTokensUrl = baseUrl + temp[1];
			if (temp[0].equals(UPLOAD_FACEBOOK_DATA_URL_KEY))
				facebookUploadURL = baseUrl + temp[1];
			if (temp[0].equals(GET_TWITTER_TOKENS_URL_KEY))
				twitterTokensUrl = baseUrl + temp[1];
			if (temp[0].equals(UPLOAD_TWITTER_DATA_URL_KEY))
				twitterUploadUrl = baseUrl + temp[1];
		}

	}
	
	public String getTwitterUploadUrl()
	{
		return twitterUploadUrl;
	}
	
	public String getTwitterTokensUrl()
	{
		return twitterTokensUrl;
	}
	
	public String getFacebookUploadUrl()
	{
		return facebookUploadURL;
	}
	
	public String getFacebookTokensUrl()
	{
		return facebookTokensUrl;
	}

	public String getBaseUrl()
	{
		return baseUrl;
	}
}
