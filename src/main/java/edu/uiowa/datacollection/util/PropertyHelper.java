package edu.uiowa.datacollection.util;


public class PropertyHelper
{

	private String baseUrl = "http://128.255.45.52/DataCollection";
	private String facebookTokensUrl = "/getfacetoken/";
	private String facebookUploadURL = "/postfacebook/";
	private String twitterTokensUrl = "/gettwittertoken/";
	private String twitterUploadUrl = "/posttwitterseparate/";

	/*
	 * Other properties can be added here
	 */
	public PropertyHelper(String filename)
	{
	}
	
	public String getTwitterUploadUrl()
	{
		return baseUrl + twitterUploadUrl;
	}
	
	public String getTwitterTokensUrl()
	{
		return baseUrl + twitterTokensUrl;
	}
	
	public String getFacebookUploadUrl()
	{
		return baseUrl + facebookUploadURL;
	}
	
	public String getFacebookTokensUrl()
	{
		return baseUrl + facebookTokensUrl;
	}

	public String getBaseUrl()
	{
		return baseUrl;
	}
}
