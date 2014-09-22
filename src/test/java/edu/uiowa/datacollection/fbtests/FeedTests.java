package edu.uiowa.datacollection.fbtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;

import edu.uiowa.datacollection.fbcollection.UserDataCollector;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.TestUser;
import facebook4j.auth.AccessToken;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class FeedTests
{
    public static final AccessToken APP_ACCESS_TOKEN = new AccessToken(
            "442864129167674|m5Ss-_eSF53XoKVdkyT_nkjEhj8");
    public static final String FACEBOOK_APP_ID = "442864129167674";
    public static final String FACEBOOK_APP_SECRET = "f2140fbb0148c5db21db0d07b92e6ade";

    private Facebook fSession;
    private TestUser user1, user2;
    private UserDataCollector manager1;
    private UserDataCollector manager2;
    private String statusText;
    private String statusID;
    private String commentText;

    @Before
    public void setUp() throws Exception
    {
        fSession = new FacebookFactory().getInstance();

        resetFacebookSession();
        clearTestUsers();

        // Create our users and make them friends
        user1 = createTestUser("abby");
        user2 = createTestUser("bob");
        fSession.makeFriendTestUser(user1, user2);
        
        
        manager1 = new UserDataCollector(user1.getAccessToken(), "");//"CAAGSyFcaUToBAFNHKzReLQsZCGJwJZAyX7rJiZA2jZCaiVZBYlh8PspHxGO38cnistAkkhSJM6YrqHkFa4dccJescMO6OqJVOqMitjbgOLUOpbbZAjmcvcPXQZAW5fTrhldGreXPZCRQFUFwAaRBx5jHGAQa6rTmdYnKaZCIZCd9ZAivtREQThOQTmRbH7ld8Mr38FAdZBb4WHmRi5wcNSuKB3W1", "").collectData(false, true, true, new JSONArray());
        manager2 = new UserDataCollector(user2.getAccessToken(), "");//"CAAGSyFcaUToBAFNHKzReLQsZCGJwJZAyX7rJiZA2jZCaiVZBYlh8PspHxGO38cnistAkkhSJM6YrqHkFa4dccJescMO6OqJVOqMitjbgOLUOpbbZAjmcvcPXQZAW5fTrhldGreXPZCRQFUFwAaRBx5jHGAQa6rTmdYnKaZCIZCd9ZAivtREQThOQTmRbH7ld8Mr38FAdZBb4WHmRi5wcNSuKB3W1", "").collectData(false, true, true, new JSONArray());
        
        fSession.setOAuthAccessToken(new AccessToken(user1.getAccessToken()));
        statusText = "This is a test status posted by user 1";
        statusID = fSession.postStatusMessage(user1.getId(), statusText);
        
        // Have the other users comment
        fSession.setOAuthAccessToken(new AccessToken(user2.getAccessToken()));
        commentText = "This is a test comment posted by user 2";
        fSession.commentPost(statusID, commentText);
        fSession.likePost(statusID);
        
        
        waitForDataToBePosted();
    }
    
    /**
     * This function resets our facebook session from one with authenticated
     * user to one with an authenticated app
     */
    private void resetFacebookSession()
    {
        try
        {
            fSession.setOAuthAppId(FACEBOOK_APP_ID, FACEBOOK_APP_SECRET);
        }
        catch (IllegalStateException e)
        {
            // App id / secret pair already set
        }
        fSession.setOAuthAccessToken(APP_ACCESS_TOKEN);
    }
    
    /**
     * This method deletes all current Facebook test users
     */
    private void clearTestUsers()
    {
        List<TestUser> users;
        try
        {
            users = fSession.getTestUsers(FACEBOOK_APP_ID);
            for (TestUser tu : users)
            {
                if (tu.getAccessToken() != null)
                {
                    fSession.setOAuthAccessToken(new AccessToken(tu.getAccessToken()));
                    fSession.deleteTestUser(tu.getId());
                }
            }
            resetFacebookSession();
        }
        catch (FacebookException e)
        {
            e.printStackTrace();
        }
    }
    
    private TestUser createTestUser(String username)
    {
        try
        {
            TestUser tu = fSession.createTestUser("442864129167674", // App ID
                    username, // Name of test User
                    "en_US", // Locale
                    "read_mailbox,read_stream,publish_stream");
            return tu;
        }
        catch (FacebookException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void testStatus()
    {
        // Don't collect messages, but collect the user's feed 
        JSONObject results1 = null;
        JSONObject results2 = null;
        try
        {
            results1 = manager1.collectData(true, false, false, new JSONArray());
            results2 = manager2.collectData(true, false, false, new JSONArray());
        }
        catch (FacebookException e1)
        {
            e1.printStackTrace();
        }
        
        try
        {
            /*
             * Check all of abby's data for the status
             */
            
            JSONArray feedResults = results1.getJSONArray("stream_data");
            JSONObject statusPost = null;
            for (int i = 0; i < feedResults.length(); i++)
                if (feedResults.getJSONObject(i).optString("message", "").equals(statusText))
                    statusPost = results1.getJSONArray("stream_data").getJSONObject(i);
            
            assertNotNull(statusPost);
            
            // Check that we abby posted it
            assertEquals(user1.getId(), statusPost.getJSONObject("from").get("id"));
            assertEquals("abby", statusPost.getJSONObject("from").get("name"));
            
            // Check that the message is right
            assertEquals(statusText, statusPost.get("message"));
            
            // Check that the id is right
            assertEquals(statusID, statusPost.get("id"));
            
            // Check comment info
            assertEquals(commentText, statusPost.getJSONObject("comments").getJSONArray("data").getJSONObject(0).get("message"));
            assertEquals(user2.getId(), statusPost.getJSONObject("comments").getJSONArray("data").getJSONObject(0).getJSONObject("from").get("id"));
            assertEquals("bob", statusPost.getJSONObject("comments").getJSONArray("data").getJSONObject(0).getJSONObject("from").get("name"));
            assertEquals(0, statusPost.getJSONObject("comments").getJSONArray("data").getJSONObject(0).getInt("like_count"));
            
            // Check like info
            assertEquals(user2.getId(), statusPost.getJSONObject("likes").getJSONArray("data").getJSONObject(0).get("id"));
            assertEquals("bob", statusPost.getJSONObject("likes").getJSONArray("data").getJSONObject(0).get("name"));
            
            
            /*
             * Check all of bobs's data
             * Checking that commenting on the status shows up
             * Checking that liking the status shows up
             */
            
            JSONArray feedResults2 = results2.getJSONArray("stream_data");
            boolean foundComment = false;
            boolean foundLike = false;
            for (int i = 0; i < feedResults2.length(); i++)
            {
                if (feedResults2.getJSONObject(i).optString("story", "").equals("bob commented on a status."))
                    foundComment = true;
                if (feedResults2.getJSONObject(i).optString("story", "").equals("bob likes a status."))
                    foundLike = true;
            }
            assertTrue(foundComment);
            assertTrue(foundLike);
        }
        catch (JSONException e)
        {
            System.out.println("There was an error with the JSON while running this test.");
            System.out.println(e.getMessage());
            System.out.println(java.util.Arrays.toString(e.getStackTrace()));
            System.out.println("\n\n");
            
            System.out.println("Abby's JSON:\n" + results1 + "\n");
            System.out.println("Bob's JSON:\n" + results2 + "\n");
        }
    }

    @Test
    public void testWallPost()
    {
        openLink("https://developers.facebook.com/apps/442864129167674/roles/test-users/");
        
        String wallPost1 = "Wall post 1";
        String comment1 = "Test comment 1";
        String comment2 = "Test comment 2";
        
        System.out.println("Open up abby's page. Make a wall post on Bob's wall with the following text:");
        System.out.println(wallPost1);
        System.out.println("Make a comment on that wall post with the following text:");
        System.out.println(comment1);
        System.out.println("Log out of Abby's page and return to the original page.");
        System.out.println("Log into Bob's page and comment on the wall post with the following text:");
        System.out.println(comment2);
        
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter done when finished. ");
        scan.nextLine();
        
        

     // Don't collect messages, but collect the user's feed 
        JSONObject results1 = null;
        JSONObject results2 = null;
        try
        {
            results1 = manager1.collectData(true, false, false, new JSONArray());
            results2 = manager2.collectData(true, false, false, new JSONArray());
        }
        catch (FacebookException e1)
        {
            e1.printStackTrace();
        }
        
        try
        {
            /*
             * Check all of abby's data for the status
             */
            
            JSONArray feedResults = results1.getJSONArray("stream_data");
            JSONObject statusPost = null;
            for (int i = 0; i < feedResults.length(); i++)
                if (feedResults.getJSONObject(i).optString("message", "").equals(statusText))
                    statusPost = results1.getJSONArray("stream_data").getJSONObject(i);
            
            assertNotNull(statusPost);
            
            // Check that we abby posted it
            assertEquals(user1.getId(), statusPost.getJSONObject("from").get("id"));
            assertEquals("abby", statusPost.getJSONObject("from").get("name"));
            
            // Check that the message is right
            assertEquals(statusText, statusPost.get("message"));
            
            // Check that the id is right
            assertEquals(statusID, statusPost.get("id"));
            
            // Check comment info
            assertEquals(commentText, statusPost.getJSONObject("comments").getJSONArray("data").getJSONObject(0).get("message"));
            assertEquals(user2.getId(), statusPost.getJSONObject("comments").getJSONArray("data").getJSONObject(0).getJSONObject("from").get("id"));
            assertEquals("bob", statusPost.getJSONObject("comments").getJSONArray("data").getJSONObject(0).getJSONObject("from").get("name"));
            assertEquals(0, statusPost.getJSONObject("comments").getJSONArray("data").getJSONObject(0).getInt("like_count"));
            
            // Check like info
            assertEquals(user2.getId(), statusPost.getJSONObject("likes").getJSONArray("data").getJSONObject(0).get("id"));
            assertEquals("bob", statusPost.getJSONObject("likes").getJSONArray("data").getJSONObject(0).get("name"));
            
            
            /*
             * Check all of bobs's data
             * Checking that commenting on the status shows up
             * Checking that liking the status shows up
             */
            
            JSONArray feedResults2 = results2.getJSONArray("stream_data");
            boolean foundComment = false;
            boolean foundLike = false;
            for (int i = 0; i < feedResults2.length(); i++)
            {
                if (feedResults2.getJSONObject(i).optString("story", "").equals("bob commented on a status."))
                    foundComment = true;
                if (feedResults2.getJSONObject(i).optString("story", "").equals("bob likes a status."))
                    foundLike = true;
            }
            assertTrue(foundComment);
            assertTrue(foundLike);
        }
        catch (JSONException e)
        {
            System.out.println("There was an error with the JSON while running this test.");
            System.out.println(e.getMessage());
            System.out.println(java.util.Arrays.toString(e.getStackTrace()));
            System.out.println("\n\n");
            
            System.out.println("Abby's JSON:\n" + results1 + "\n");
            System.out.println("Bob's JSON:\n" + results2 + "\n");
        }
    }
    
    
    
    private void waitForDataToBePosted()
    {
        try
        {
            Thread.sleep(1000 * 60 * 1);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void openLink(String link)
    {
        try
        {
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(link));
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
