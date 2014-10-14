package edu.uiowa.datacollection.main;

import java.util.Calendar;
import java.util.Date;

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
        boolean saveJsonDataLocally = true;

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
            long registerDate = (long)Double.parseDouble(user.getString("registerDate")) * 1000L;
            JSONArray lastConvoTimes = user.getJSONArray("info");

            System.out.println("Currently facebook accessing data for " + phoneNumber);

            if (!accessToken.equals(BLANK_ACCESS_TOKEN))
            {
                UserDataCollector manager = new UserDataCollector(accessToken, phoneNumber);

                boolean collectFeed = true;
                boolean collectMessages = true;
                
                Date date = new Date(registerDate);
                Calendar lastUploadTimeOrRegistration = Calendar.getInstance();
                lastUploadTimeOrRegistration.setTime(date);
                lastUploadTimeOrRegistration.add(Calendar.HOUR, 5);
                System.out.println(lastUploadTimeOrRegistration.getTime());
                System.out.println("\t\tBeginning collection");
                JSONObject jsonData = manager.collectData(collectFeed, collectMessages, lastConvoTimes, lastUploadTimeOrRegistration);
                System.out.println("\t\tFinshed collection");

                if (saveJsonDataLocally)
                    JsonHelper.saveJsonData(jsonData, baseFilename + "_" + phoneNumber);
                
                System.out.println("\t\tBeginning upload");
                JsonHelper.postJsonData(ph.getFacebookUploadUrl(), jsonData);
                System.out.println("\t\tFinished upload");
            }
            else
            {
                System.out.println("\tSkipping user.");
            }
        }
    }
}
