package edu.uiowa.datacollection.fbtests;

import static org.junit.Assert.assertEquals;

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

public class MessageTests
{
    public static final AccessToken APP_ACCESS_TOKEN = new AccessToken(
            "442864129167674|m5Ss-_eSF53XoKVdkyT_nkjEhj8");
    public static final String FACEBOOK_APP_ID = "442864129167674";
    public static final String FACEBOOK_APP_SECRET = "f2140fbb0148c5db21db0d07b92e6ade";

    private Facebook fSession;
    private TestUser user1, user2;
    private UserDataCollector manager1;
    private UserDataCollector manager2;

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
        
        
        manager1 = new UserDataCollector(user1.getAccessToken(), "");//"CAAGSyFcaUToBAFNHKzReLQsZCGJwJZAyX7rJiZA2jZCaiVZBYlh8PspHxGO38cnistAkkhSJM6YrqHkFa4dccJescMO6OqJVOqMitjbgOLUOpbbZAjmcvcPXQZAW5fTrhldGreXPZCRQFUFwAaRBx5jHGAQa6rTmdYnKaZCIZCd9ZAivtREQThOQTmRbH7ld8Mr38FAdZBb4WHmRi5wcNSuKB3W1", "");
        manager2 = new UserDataCollector(user2.getAccessToken(), "");//"CAAGSyFcaUToBAFNHKzReLQsZCGJwJZAyX7rJiZA2jZCaiVZBYlh8PspHxGO38cnistAkkhSJM6YrqHkFa4dccJescMO6OqJVOqMitjbgOLUOpbbZAjmcvcPXQZAW5fTrhldGreXPZCRQFUFwAaRBx5jHGAQa6rTmdYnKaZCIZCd9ZAivtREQThOQTmRbH7ld8Mr38FAdZBb4WHmRi5wcNSuKB3W1", "").collectData(false, true, true, new JSONArray());
        
//        waitForDataToBePosted();
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
    public void testMessageCollection()
    {
        openLink("https://developers.facebook.com/apps/442864129167674/roles/test-users/");
        String message1 = "Test message to Bob";
        String message2 = "Test message to Abby";
        String message3 = "Test message to Bob 2";
        
        
        System.out.println("Open up abby's page. Send a message to Bob with the following text:");
        System.out.println(message1);
        System.out.println("Log out of Abby's page and return to the original page.");
        System.out.println("Log into Bob's page and send a message to Abby with the following text:");
        System.out.println(message2);
        System.out.println("Log out of Bob's page and return to the original page.");
        System.out.println("Log into Abby's page and send a message to Bob with the following text:");
        System.out.println(message3);

        
        @SuppressWarnings("resource") //If we close scan it closes system.in for other tests
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter done when finished. ");
        scan.nextLine();
        
        // Collect the messages
        JSONObject results1 = null;
        JSONObject results2 = null;
        try
        {
            results1 = manager1.collectData(false, true, true, new JSONArray());
            results2 = manager2.collectData(false, true, false, new JSONArray());
        }
        catch (FacebookException e1)
        {
            e1.printStackTrace();
        }
        
        try
        {
            System.out.println(results1.toString());
            System.out.println(results2.toString());
            
            JSONArray commentsArray = results1.getJSONArray("conversation_data");
            
            // Check that we collected things using abby's key
            if (commentsArray.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(0).get("id").equals(user1.getId()))
            {
                assertEquals(user1.getId(), commentsArray.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(0).get("id"));
                assertEquals(user2.getId(), commentsArray.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(1).get("id"));
                assertEquals("abby", commentsArray.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(0).get("name"));
                assertEquals("bob", commentsArray.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(1).get("name"));
            }
            else
            {
                assertEquals(user1.getId(), commentsArray.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(1).get("id"));
                assertEquals(user2.getId(), commentsArray.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(0).get("id"));
                assertEquals("abby", commentsArray.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(1).get("name"));
                assertEquals("bob", commentsArray.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(0).get("name"));
            }

            assertEquals(message1,      commentsArray.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(0).get("message"));
            assertEquals(user1.getId(), commentsArray.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(0).getJSONObject("from").get("id"));
            assertEquals("abby",        commentsArray.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(0).getJSONObject("from").get("name"));

            assertEquals(message2,      commentsArray.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(1).get("message"));
            assertEquals(user2.getId(), commentsArray.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(1).getJSONObject("from").get("id"));
            assertEquals("bob",         commentsArray.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(1).getJSONObject("from").get("name"));

            assertEquals(message3,      commentsArray.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(2).get("message"));
            assertEquals(user1.getId(), commentsArray.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(2).getJSONObject("from").get("id"));
            assertEquals("abby",        commentsArray.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(2).getJSONObject("from").get("name"));
            
            
            
            

            
            JSONArray commentsArray2 = results1.getJSONArray("conversation_data");
            
            // Check that we collected things using bob's key
            if (commentsArray.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(0).get("id").equals(user1.getId()))
            {
                assertEquals(user1.getId(), commentsArray2.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(0).get("id"));
                assertEquals(user2.getId(), commentsArray2.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(1).get("id"));
                assertEquals("abby",        commentsArray2.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(0).get("name"));
                assertEquals("bob",         commentsArray2.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(1).get("name"));
            }
            else
            {
                assertEquals(user1.getId(), commentsArray2.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(1).get("id"));
                assertEquals(user2.getId(), commentsArray2.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(0).get("id"));
                assertEquals("abby",        commentsArray2.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(1).get("name"));
                assertEquals("bob",         commentsArray2.getJSONObject(0).getJSONObject("to").getJSONArray("data").getJSONObject(0).get("name"));
            }
            
            assertEquals(message1,      commentsArray2.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(0).get("message"));
            assertEquals(user1.getId(), commentsArray2.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(0).getJSONObject("from").get("id"));
            assertEquals("abby",        commentsArray2.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(0).getJSONObject("from").get("name"));

            assertEquals(message2,      commentsArray2.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(1).get("message"));
            assertEquals(user2.getId(), commentsArray2.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(1).getJSONObject("from").get("id"));
            assertEquals("bob",         commentsArray2.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(1).getJSONObject("from").get("name"));

            assertEquals(message3,      commentsArray2.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(2).get("message"));
            assertEquals(user1.getId(), commentsArray2.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(2).getJSONObject("from").get("id"));
            assertEquals("abby",        commentsArray2.getJSONObject(0).getJSONObject("comments").getJSONArray("data").getJSONObject(2).getJSONObject("from").get("name"));
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        
    }
    
    private void openLink(String link)
    {
        try
        {
            System.out.println(link);
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(link));
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
