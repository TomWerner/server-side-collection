package edu.uiowa.datacollection.main;

import java.util.Calendar;
import java.util.Date;

import edu.uiowa.datacollection.twitter.DataManager;
import edu.uiowa.datacollection.twitter.User;
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
	 * @throws JSONException 
	 * @throws Exception
	 */
	public static void main(String[] args) throws JSONException
	{
		PropertyHelper ph = new PropertyHelper("dataCollection.properties");

		String baseFilename = "twitter_data";
		boolean saveJsonDataLocally = false;

//		System.out.print("Save JSON data locally? (y/n): ");
//		Scanner scan = new Scanner(System.in);
//		if (scan.nextLine().toUpperCase().charAt(0) == 'Y')
//			saveJsonDataLocally = true;
//		scan.close();

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
            

			System.out.println("Currently twitter accessing data for "
					+ user.getTwitterID());
			System.out.println("\tAccess Token: " + user.getOauthToken());

			if (!user.getOauthToken().equals(BLANK_TWITTER_TOKEN ))
			{
	            long registerDate = (long)Double.parseDouble(userToken.getString("registerDate")) * 1000L;
	            Date date = new Date(registerDate);
	            Calendar lastUploadTimeOrRegistration = Calendar.getInstance();
	            lastUploadTimeOrRegistration.setTime(date);
	            lastUploadTimeOrRegistration.add(Calendar.HOUR, 5);
	            System.out.println(lastUploadTimeOrRegistration.getTime());
	            
				DataManager manager = new DataManager(user);

				manager.collectData(true, // Collect direct conversations
						true); // Collect Twitter timeline

				if (saveJsonDataLocally)
				{
					manager.saveJsonData(baseFilename + "_"
							+ user.getTwitterID(), lastUploadTimeOrRegistration);
				}

				JsonHelper.postJsonData(ph.getTwitterUploadUrl(),
						manager.getJsonData(lastUploadTimeOrRegistration));
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
