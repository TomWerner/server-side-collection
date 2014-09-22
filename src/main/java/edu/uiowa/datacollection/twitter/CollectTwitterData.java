package edu.uiowa.datacollection.twitter;

import java.util.Scanner;

import edu.uiowa.datacollection.util.JsonHelper;
import edu.uiowa.datacollection.util.PropertyHelper;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class CollectTwitterData
{

	public static final String BLANK_TWITTER_TOKEN = "";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		PropertyHelper ph = new PropertyHelper("dataCollection.properties");

		String baseFilename = "facebook_data";
		boolean saveJsonDataLocally = false;

		System.out.print("Save JSON data locally? (y/n): ");
		Scanner scan = new Scanner(System.in);
		if (scan.nextLine().toUpperCase().charAt(0) == 'Y')
			saveJsonDataLocally = true;
		scan.close();

		JSONObject obj = null;
		try
		{
			obj = JsonHelper.readJsonFromUrl(ph.getTwitterTokensUrl());
			if (obj == null)
			{
				System.out
						.println("ERROR: Null was returned from the server.");
				return;
			}
		}
		catch (JSONException e)
		{
			System.out.println("ERROR: could not read JSON"
					+ " data from the server.");
			System.out.println(e.getMessage());
			return;
		}

		JSONArray userList = obj.getJSONArray("data");
		for (int i = 0; i < userList.length(); i++)
		{
			JSONObject userToken = userList.getJSONObject(i);
			User user = createUser(userToken);


			System.out.println("Currently accessing data for "
					+ user.getTwitterID());
			System.out.println("\tAccess Token: " + user.getOauthToken());

			if (!user.getOauthToken().equals(BLANK_TWITTER_TOKEN ))
			{
				DataManager manager = new DataManager(user);

				manager.collectData(true, // Collect direct conversations
						true); // Collect Twitter timeline

				if (saveJsonDataLocally)
				{
					manager.saveJsonData(baseFilename + "_"
							+ user.getTwitterID());
				}

				JsonHelper.postJsonData(ph.getTwitterUploadUrl(),
						manager.getJsonData());
			}
			else
			{
				System.out.println("\tSkipping user");
			}
		}

	}

	public static User createUser(JSONObject userToken) throws JSONException
	{
		User u = new User(userToken.getString("twitter_id"), 2);
		u.setOauthToken(userToken.getString("twitter_token"));
		u.setTokenSecret(userToken.getString("twitter_secret"));
		u.setUserTimeLineSinceID(userToken.getLong("userTimeLineSinceID"));
		u.setMentionTimeLineSinceID(userToken.getLong("mentionTimeLineSinceID"));
		u.setDirectMessageSinceID(userToken.getLong("directMsgSinceID"));
		u.setSentDirectMessageSinceID(userToken.getLong("sentDirectMsgSinceID"));
		return u;
	}

}
