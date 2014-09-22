package edu.uiowa.datacollection.main;

import java.util.Calendar;

import edu.uiowa.datacollection.fbcollection.UserDataCollector;
import edu.uiowa.datacollection.util.JsonHelper;
import edu.uiowa.datacollection.util.PropertyHelper;
import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class CollectFacebookData
{
    private static final String BLANK_ACCESS_TOKEN = "";

    public static void main(String[] args) throws FacebookException, JSONException
    {
        PropertyHelper ph = new PropertyHelper("dataCollection.properties");
        JSONObject obj = null;

        String baseFilename = "facebook_data";
        boolean saveJsonDataLocally = false;

        // System.out.print("Save JSON data locally? (y/n): ");
        // Scanner scan = new Scanner(System.in);
        // if (scan.nextLine().toUpperCase().charAt(0) == 'Y')
        // saveJsonDataLocally = true;
        // scan.close();

        try
        {
            obj = JsonHelper.readJsonFromUrl(ph.getFacebookTokensUrl());

            if (obj == null)
            {
                System.out.println("ERROR: Could not load data from the server.");
                return;
            }
        }
        catch (JSONException e)
        {
            System.out.println("ERROR: could not read JSON" + " data from the server.");
            System.out.println(e.getMessage());
            return;
        }

        JSONArray users = obj.getJSONArray("data");
        System.out.println("Loaded " + users.length() + " users from the database");
        for (int i = 0; i < users.length(); i++)
        {
            JSONObject user = users.getJSONObject(i);

            String accessToken = user.getString("token");
            String phoneNumber = user.getString("phone");
            JSONArray lastConvoTimes = user.getJSONArray("info");

            System.out.println("Currently accessing data for " + phoneNumber);
            System.out.println("\tAccess Token: " + accessToken);

            if (!accessToken.equals(BLANK_ACCESS_TOKEN))
            {
                UserDataCollector manager = new UserDataCollector(accessToken, phoneNumber);

                boolean collectFeed = true;
                boolean collectMessages = true;
                
                // TODO: Set this to the date the user entered the survey
                Calendar oneYearAgo = Calendar.getInstance();
                oneYearAgo.add(Calendar.YEAR, -1);
                JSONObject jsonData = manager.collectData(collectFeed, collectMessages, lastConvoTimes, oneYearAgo);

                if (saveJsonDataLocally)
                    JsonHelper.saveJsonData(jsonData, baseFilename + "_" + phoneNumber);

                JsonHelper.postJsonData(ph.getFacebookUploadUrl(), jsonData);
            }
            else
            {
                System.out.println("\tSkipping user.");
            }
        }
    }
}
