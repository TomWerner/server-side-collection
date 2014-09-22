package edu.uiowa.datacollection.fbcollection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import facebook4j.Category;
import facebook4j.Comment;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.IdNameEntity;
import facebook4j.Message;
import facebook4j.PagableList;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class MessageJSONConverter
{

    public static HashMap<String, Long> getOldTimes(JSONArray lastConvoTimes)
    {
        HashMap<String, Long> result = new HashMap<String, Long>();
        for (int i = 0; i < lastConvoTimes.length(); i++)
        {
            try
            {
                JSONObject obj = lastConvoTimes.getJSONObject(i);
                result.put(obj.getString("thread_id"), obj.getLong("updated_time"));
            }
            catch (JSONException e)
            {
                System.out.println("ERROR: JSON improperly formatted.");
                System.out.println(e.getMessage());
            }
        }

        return result;
    }

    public static JSONObject createMessageJSONObject(Facebook session, Message conversation, Calendar notBefore) throws FacebookException
    {
        JSONObject result = new JSONObject();
        try
        {
            // Add the simple stuff
            result.put("created_time", conversation.getCreatedTime());
            result.put("id", conversation.getId());
            result.put("message", conversation.getMessage());
            result.put("unread", conversation.getUnread());
            result.put("unseen", conversation.getUnseen());
            result.put("updated_time", conversation.getUpdatedTime());

            // Get the comments
            ArrayList<Comment> allowedComments = collectMessageComments(session, conversation, notBefore);           
            JSONArray jsonComments = new JSONArray();
            for (Comment comment : allowedComments)
            {
                jsonComments.put(createCommentJSONObject(session, comment));
            }
            result.put("comments", new JSONObject().put("data", jsonComments));

            // Add the from object
            result.put("from", createIdNameJSONObject(conversation.getFrom()));

            // Add the to object
            JSONArray jsonTo = new JSONArray();
            for (IdNameEntity entity : conversation.getTo())
            {
                jsonTo.put(createIdNameJSONObject(entity));
            }
            result.put("to", new JSONObject().put("data", jsonTo));
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    private static ArrayList<Comment> collectMessageComments(Facebook session, Message conversation, Calendar notBefore) throws FacebookException
    {
        ArrayList<Comment> allowedComments = new ArrayList<Comment>();
        
        PagableList<Comment> pageList = conversation.getComments();
        for (int i = pageList.size() - 1; i >= 0; i--)
        {
            if (pageList.get(i).getCreatedTime().after(notBefore.getTime()))
            {
                allowedComments.add(0, pageList.get(i));
            }
        }
        
        while ((pageList.size() == 0 || pageList.get(0).getCreatedTime().after(notBefore.getTime())) && pageList.getPaging() != null)
        {
            pageList = session.fetchNext(pageList.getPaging());
            for (int i = pageList.size() - 1; i >= 0; i--)
            {
                if (pageList.get(i).getCreatedTime().after(notBefore.getTime()))
                {
                    allowedComments.add(0, pageList.get(i));
                }
            }
        }
        
        return allowedComments;
    }

    public static JSONObject createIdNameJSONObject(IdNameEntity entity)
    {
        JSONObject result = new JSONObject();
        if (entity == null)
            return result;
        try
        {
            result.put("id", entity.getId());
            result.put("name", entity.getName());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject createCommentJSONObject(Facebook session, Comment comment) throws FacebookException
    {
        JSONObject result = new JSONObject();
        if (comment == null)
            return result;
        try
        {
            result.put("id", comment.getId());
            result.put("created_time", comment.getCreatedTime());
            result.put("like_count", comment.getLikeCount());
            result.put("message", comment.getMessage());
            result.put("from", createFromJSONObject(comment.getFrom()));
            result.put("user_likes", comment.isUserLikes());
            result.put("can_remove", comment.canRemove());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject createFromJSONObject(Category from)
    {
        JSONObject result = new JSONObject();
        if (from == null)
            return result;
        try
        {
            result.put("id", from.getId());
            result.put("name", from.getName());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return result;
    }
    
    public static ArrayList<Message> getAllAllowedConversations(Facebook session, Calendar notBefore) throws FacebookException
    {
        PagableList<Message> pageList = session.getInbox();
        ArrayList<Message> allowedMessages = new ArrayList<Message>();
        for (int i = 0; i < pageList.size(); i++)
        {
            if (pageList.get(i).getUpdatedTime().after(notBefore.getTime()))
            {
                allowedMessages.add(pageList.get(i));
            }
        }
        
        while ((pageList.size() == 0 || pageList.get(pageList.size() - 1).getUpdatedTime().after(notBefore.getTime())) && pageList.getPaging() != null)
        {
            for (int i = 0; i < pageList.size(); i++)
            {
                if (pageList.get(i).getUpdatedTime().after(notBefore.getTime()))
                {
                    allowedMessages.add(pageList.get(i));
                }
            }
            pageList = session.fetchNext(pageList.getPaging());
        }

        return allowedMessages;
    }

}
