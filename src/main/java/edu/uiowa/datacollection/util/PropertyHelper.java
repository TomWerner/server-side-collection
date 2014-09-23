package edu.uiowa.datacollection.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

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
	public PropertyHelper(String filename)
	{
        ArrayList<String> propertyList = new ArrayList<String>();
        try
        {
            Scanner fileScanner = new Scanner(new File(filename));
            while (fileScanner.hasNextLine())
                propertyList.add(fileScanner.nextLine());
            fileScanner.close();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("There's been a mishap. Please attemp to locate " + filename + "\nWe looked in the absolute path of \n\n" + new File(filename).getAbsolutePath());
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
