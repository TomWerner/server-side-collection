package edu.uiowa.datacollection.fbcollection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Message;
import facebook4j.Post;
import facebook4j.ResponseList;
import facebook4j.auth.AccessToken;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class UserDataCollector
{
    private Facebook session;
    private String phoneNumber;

    private static final String APP_ID = "442864129167674";
    private static final String APP_SECRET = "f2140fbb0148c5db21db0d07b92e6ade";

    public UserDataCollector(String accessToken, String phoneNumber)
    {
        AccessToken token = new AccessToken(accessToken, null);
        this.phoneNumber = phoneNumber;

        session = new FacebookFactory().getInstance();
        session.setOAuthAppId(APP_ID, APP_SECRET);
        session.setOAuthAccessToken(token);
    }

    public JSONObject collectData(boolean collectFeed, boolean collectMessage, JSONArray lastConvoTimes, Calendar notBefore) throws FacebookException
    {
        JSONObject result = new JSONObject();

        try
        {
            result.put("conversation_data", collectMessages(collectMessage, lastConvoTimes, notBefore));
            result.put("stream_data", collectFeed(collectFeed));
            result.put("user", phoneNumber);
        }
        catch (JSONException e)
        {
            System.out.println("ERROR: JSON improperly formatted.");
            System.out.println(e.getMessage());
        }
        return result;
    }

    private JSONArray collectFeed(boolean collectFeed) throws FacebookException
    {
        if (!collectFeed)
            return new JSONArray();

        ResponseList<Post> feed = session.getFeed();
        ArrayList<Post> posts = FeedJSONConverter.getAllOfPageableList(session, feed);

        // Now that we have collected all of the posts, add them into a
        // JSONArray to return them
        // Also load their comments and likes to put in the array as well.
        JSONArray result = new JSONArray();

        for (Post post : posts)
        {
            // While constructing the JSON it also collects the comments and
            // likes
            result.put(FeedJSONConverter.createPostJSONObject(session, post));
        }

        return result;
    }

    private JSONArray collectMessages(boolean collectMessage, JSONArray lastConvoTimes, Calendar notBefore) throws FacebookException
    {
        if (!collectMessage)
            return new JSONArray();
//
//        // This has a mapping between thread id's and the last message we have
//        // from that thread
        HashMap<String, Long> lastTimes = MessageJSONConverter.getOldTimes(lastConvoTimes);
        

        JSONArray result = new JSONArray();
        ArrayList<Message> allowedMessages = MessageJSONConverter.getAllAllowedConversations(session, notBefore);
        for (Message message : allowedMessages)
        {
            result.put(MessageJSONConverter.createMessageJSONObject(session, message, notBefore));
        }
        
        return result;
    }
}
