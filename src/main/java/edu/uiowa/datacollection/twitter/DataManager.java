package edu.uiowa.datacollection.twitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class DataManager
{
	// private List<User> userList = new ArrayList<User>();
	private User user;

	private Twitter twitter;

	private ArrayList<Conversation> conversations;

	private ArrayList<Message> statusList;

	public static final String TWITTER_CONSUMER_KEY = "BaWtyknv1RwsU60jVccA";
	public static final String TWITTER_CONSUMER_SECRET = "EDopj7ySkVstUTD294ODgUlmhctGi3PBSkW2OljhhPY";

	public DataManager(User user)
	{
		twitter = TwitterFactory.getSingleton();

		try
		{
			twitter.setOAuthConsumer(TWITTER_CONSUMER_KEY,
					TWITTER_CONSUMER_SECRET);
		}
		catch (IllegalStateException e)
		{
			// This is here because for the tests to work we need to set the
			// OAuthConsumer to post things
			// This happens if the consumer key/secret is already set
		}
		this.loadAccessToken(user);
		this.setUser(user);

	}

	public void loadAccessToken(User user)
	{
		twitter.setOAuthAccessToken(new AccessToken(user.getOauthToken(), user
				.getTokenSecret(), Long.parseLong(user.getTwitterID())));
	}

	public List<Message> collectRetweetsOfMe()
	{
		List<Message> retweetList = new ArrayList<Message>();
		ResponseList<Status> rl = null;
		Paging paging = new Paging();
		int count = 100;
		paging.setCount(count);
		try
		{
			rl = twitter.getRetweetsOfMe(paging);
		}
		catch (TwitterException e)
		{
			System.out
					.println("ERROR: Something went wrong interacting with Twitter.");
			System.out.println(e.getMessage());
		}
		if (rl != null)
		{
			for (int i = 0; i < rl.size(); i++)
			{
				Message msg = new Message(rl.get(i));
				retweetList.add(msg);
			}
		}
		return retweetList;
	}

	/**
	 * Collect User TimeLine, each request can return at most 200 timelines.
	 * This method can return at most 3200 recent user timelines. RateLimit: 180
	 * requests per user per 15 minutes.
	 * 
	 */
	public List<Message> collectUserTimeLine(Long sinceId)
	{
		List<Message> statusList = new ArrayList<Message>();
		Paging paging = new Paging();
		int count = 100;
		paging.setCount(count);
		paging.setSinceId(user.getUserTimeLineSinceID());
		ResponseList<Status> rl = null;
		boolean collectAllData = false;
		while (!collectAllData)
		{
			try
			{
				ResponseList<Status> tl = twitter.getUserTimeline(paging);
				if (tl.size() == 0)
				{
					collectAllData = true;
				}
				else
				{
					if (rl == null)
					{
						rl = tl;
					}
					else
						rl.addAll(tl);
					paging.setMaxId(tl.get(tl.size() - 1).getId() - 1);
				}
			}
			catch (TwitterException e)
			{
				// rate Limit exceed
				if (e.getStatusCode() == 429)
				{
					// Actually it should not wait, it should continue to handle
					// other users' requests.
					System.err
							.println("Rate Limit exceeds.Thread sleep for 3 minutes.");
					try
					{
						Thread.sleep(1000 * 60 * 3);
					}
					catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}

				}
				else
					e.printStackTrace();
			}
		}
		if (rl != null)
		{
			if (rl.size() >= 1)
				user.setUserTimeLineSinceID(rl.get(0).getId());
			for (int i = 0; i < rl.size(); i++)
			{
				Message msg = new Message(rl.get(i));
				statusList.add(msg);
			}
		}
		return statusList;
	}

	/**
	 * Collect Mentions TimeLine, each request can return at most 200 timelines.
	 * This method can return at most 800 recent mention timelines. RateLimit:
	 * 15 requests per user per 15 minutes.
	 */
	public List<Message> collectMentionsTimeLine(long sinceId)
	{
		Paging paging = new Paging();
		int count = 100;
		paging.setCount(count);
		paging.setSinceId(user.getMentionTimeLineSinceID());
		ResponseList<Status> rl = null;
		boolean collectAllData = false;
		List<Message> statusList = new ArrayList<Message>();
		while (!collectAllData)
		{
			try
			{
				ResponseList<Status> tl = twitter.getMentionsTimeline(paging);
				if (tl.size() == 0)
				{
					collectAllData = true;
				}
				else
				{
					if (rl == null)
						rl = tl;
					else
						rl.addAll(tl);
					paging.setMaxId(tl.get(tl.size() - 1).getId() - 1);
				}
			}
			catch (TwitterException e)
			{
				// rate Limit exceed
				if (e.getStatusCode() == 429)
				{
					try
					{
						Thread.sleep(1000 * 60 * 3);
					}
					catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}
				}
				else
					e.printStackTrace();
			}
		}
		if (rl != null)
		{
			if (rl.size() >= 1)
				user.setMentionTimeLineSinceID(rl.get(0).getId());
			for (int i = 0; i < rl.size(); i++)
			{
				Message msg = new Message(rl.get(i));
				statusList.add(msg);
			}
		}
		return statusList;
	}

	/**
	 * Collect direct messages, each request can return at most 200 Messages.
	 * This method can return at most 800 recent direct messages. RateLimit: 15
	 * requests per user per 15 minutes.
	 */
	public List<Message> collectInstantMessages()
	{
		List<Message> msgList = new ArrayList<Message>();
		Paging paging1 = new Paging();
		paging1.setCount(100);
		paging1.setSinceId(user.getDirectMessageSinceID());

		Paging paging2 = new Paging();
		paging2.setCount(100);
		paging2.setSinceId(user.getSentDirectMessageSinceID());

		ResponseList<DirectMessage> rl1 = null;
		ResponseList<DirectMessage> rl2 = null;

		boolean collectAllData = false;
		while (!collectAllData)
		{
			try
			{
				ResponseList<DirectMessage> tl1 = twitter
						.getDirectMessages(paging1);
				ResponseList<DirectMessage> tl2 = twitter
						.getSentDirectMessages(paging2);
				if (tl1.size() == 0 && tl2.size() == 0)
				{
					collectAllData = true;
				}
				else
				{
					if (rl1 == null && rl2 == null)
					{
						if (tl1.size() >= 1)
							user.setDirectMessageSinceID(tl1.get(0).getId());
						if (tl2.size() >= 1)
							user.setSentDirectMessageSinceID(tl2.get(0).getId());
						rl1 = tl1;
						rl2 = tl2;
					}
					else
					{
						rl1.addAll(tl1);
						rl2.addAll(tl2);
					}
					if (tl1.size() != 0)
						paging1.setMaxId(tl1.get(tl1.size() - 1).getId() - 1);
					if (tl2.size() != 0)
						paging2.setMaxId(tl2.get(tl2.size() - 1).getId() - 1);
				}
			}
			catch (TwitterException e)
			{
				// rate Limit exceed
				if (e.getStatusCode() == 429)
				{
					try
					{
						Thread.sleep(1000 * 60 * 3);
					}
					catch (InterruptedException e1)
					{
						e1.printStackTrace();
					}
				}
				else
					e.printStackTrace();
			}
		}
		if (rl1 != null)
		{
			rl1.addAll(rl2);
			for (int i = 0; i < rl1.size(); i++)
			{
				Message msg = new Message(rl1.get(i));
				msgList.add(msg);
			}
		}
		Collections.sort(msgList);
		return msgList;
	}

	/**
	 * Collect the timeline which does not appear in User_TimeLine and
	 * Mention_TimeLine. RateLimit: 180 requests per user per 15 minutes.
	 */
	public void collectExtraTimeLine(List<Conversation> conversList)
	{

		for (int i = 0; i < conversList.size(); i++)
		{
			Conversation c = conversList.get(i);
			while (c.getMessageList().get(0).getInReplytoMessageID() != null)
			{
				try
				{
					Status s = twitter.showStatus(Long.parseLong(c
							.getMessageList().get(0).getInReplytoMessageID()));
					Message msg = new Message(s);
					c.getMessageList().add(0, msg);
				}
				catch (NumberFormatException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (TwitterException e)
				{
					// it is possible the original status has been deleted
					// e.printStackTrace();
					if (e.getStatusCode() == 404)
					{
						System.err.println("The original Status: "
								+ c.getMessageList().get(0)
										.getInReplytoMessageID()
								+ " no longer exists.");
						break;
					}
					// Rate Limit exceeds.
					else if (e.getStatusCode() == 429)
					{
						try
						{
							Thread.sleep(1000 * 60 * 3);
						}
						catch (InterruptedException e1)
						{
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}

	public ArrayList<Conversation> collectDirectConversations()
	{
		List<Message> msgList = collectInstantMessages();
		ArrayList<Conversation> conversList = constructDirectConversations(msgList);
		return conversList;
	}

	public ArrayList<Message> collectStatusList()
	{
		Long sinceId = (long) 1;
		ArrayList<Message> result = new ArrayList<Message>();
		List<Message> usertimeline = collectUserTimeLine(sinceId);
		List<Message> mentiontimeline = collectMentionsTimeLine(sinceId);
		result.addAll(usertimeline);
		result.addAll(mentiontimeline);
		return result;
	}

	public ArrayList<Conversation> constructDirectConversations(
			List<Message> msgList)
	{
		/*
		 * Contruct Conversations for instant Message.
		 */
		ArrayList<Conversation> conversationList = new ArrayList<Conversation>();
		Collections.sort(msgList);
		List<Conversation> directConversation = new ArrayList<Conversation>();
		for (int i = 0; i < msgList.size(); i++)
		{
			Message m = msgList.get(i);
			boolean startNewConversation = true;
			for (int j = 0; j < directConversation.size(); j++)
			{
				Conversation c = directConversation.get(j);
				if (c.getParticipantList().contains(m.getSender())
						&& c.getParticipantList().contains(
								m.getRecipients().get(0)))
				{
					c.addMessage(m);
					startNewConversation = false;
				}
			}
			if (startNewConversation)
			{
				Conversation newC = new Conversation(1);
				newC.addMessage(m);
				directConversation.add(newC);
			}
		}
		conversationList.addAll(directConversation);

		for (int i = 0; i < conversationList.size(); i++)
		{
			conversationList.get(i).setStartEndTime();
			conversationList.get(i).setStartEndIDCount();
		}
		return conversationList;
	}

	public User getUser()
	{
		return user;
	}

	public void setUser(User user)
	{
		this.user = user;
	}

	public JSONObject getJsonData() throws JSONException
	{
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("user", user.getTwitterID());
		jsonObject.put("userTimeLineSinceID", user.getUserTimeLineSinceID());
		jsonObject.put("mentionTimeLineSinceID",
				user.getMentionTimeLineSinceID());
		jsonObject.put("directMsgSinceID", user.getDirectMessageSinceID());
		jsonObject.put("sentDirectMsgSinceID",
				user.getSentDirectMessageSinceID());

		JSONArray convoJsonArray = new JSONArray();
		for (Conversation c : conversations)
		{
			convoJsonArray.put(c.getJSONRepresentation());
		}
		jsonObject.put("conversationData", convoJsonArray);

		JSONArray statusJsonArray = new JSONArray();
		for (Message msg : statusList)
		{
			statusJsonArray.put(msg.getJSONRepresentation());
		}
		jsonObject.put("statusData", statusJsonArray);

		return jsonObject;
	}

	public void collectData(boolean collectDirectMessages,
			boolean collectTimeline)
	{
		if (collectDirectMessages)
		{
			conversations = collectDirectConversations();
		}

		if (collectTimeline)
		{
			statusList = collectStatusList();
		}
	}

	public void saveJsonData(String filename)
	{
		File file = new File(filename);

		try
		{
			FileOutputStream f = new FileOutputStream(file);
			PrintWriter pw = new PrintWriter(f);
			JSONObject result = getJsonData();

			pw.append(result.toString(1) + "\n");

			pw.flush();
			pw.close();
			f.close();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("ERROR: File not found.");
			System.out.println(e.getMessage());
		}
		catch (IOException e)
		{
			System.out.println("ERROR: Could not save JSON.");
			System.out.println(e.getMessage());
		}
		catch (JSONException e)
		{
			System.out.println("ERROR: JSON improperly formatted.");
			System.out.println(e.getMessage());
		}
	}
}
