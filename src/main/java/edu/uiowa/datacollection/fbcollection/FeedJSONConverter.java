package edu.uiowa.datacollection.fbcollection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import facebook4j.Category;
import facebook4j.Comment;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.IdNameEntity;
import facebook4j.Like;
import facebook4j.PagableList;
import facebook4j.Paging;
import facebook4j.Place;
import facebook4j.Post;
import facebook4j.Privacy;
import facebook4j.Tag;
import facebook4j.internal.org.json.JSONArray;
import facebook4j.internal.org.json.JSONException;
import facebook4j.internal.org.json.JSONObject;

public class FeedJSONConverter
{
    public static JSONObject createPostJSONObject(Facebook session, Post post) throws FacebookException
    {
        JSONObject result = new JSONObject();
        try
        {
            // Add the simple stuff
            result.put("caption", post.getCaption());
            result.put("description", post.getDescription());
            result.put("id", post.getId());
            result.put("message", post.getMessage());
            result.put("name", post.getName());
            result.put("object_id", post.getObjectId());
            result.put("status_type", post.getStatusType());
            result.put("story", post.getStory());
            result.put("type", post.getType());
            result.put("created_time", post.getCreatedTime());
            result.put("link", post.getLink());
            result.put("source", post.getSource());
            result.put("updated_time", post.getUpdatedTime());

            // Get the comments
            ArrayList<Comment> comments = getAllOfPageableList(session, post.getComments());
            JSONArray jsonComments = new JSONArray();
            for (Comment comment : comments)
            {
                jsonComments.put(createCommentJSONObject(session, comment));
            }
            result.put("comments", new JSONObject().put("data", jsonComments));

            // Get the likes
            ArrayList<Like> likes = getAllOfPageableList(session, post.getLikes());
            JSONArray jsonLikes = new JSONArray();
            for (Like like : likes)
            {
                jsonLikes.put(createLikeJSONObject(session, like));
            }
            result.put("likes", new JSONObject().put("data", jsonLikes));

            // Add the from object
            result.put("from", createFromJSONObject(post.getFrom()));

            // Add the to object
            JSONArray jsonTo = new JSONArray();
            for (IdNameEntity entity : post.getTo())
            {
                jsonTo.put(createIdNameJSONObject(entity));
            }
            result.put("to", new JSONObject().put("data", jsonTo));

            // Add the story tags, deprecated? but still showing up
            result.put("story_tags", createStoryTagJSONObject(post.getStoryTags()));

            // Add with tags
            JSONArray jsonWithTags = new JSONArray();
            for (IdNameEntity entity : post.getWithTags())
            {
                jsonTo.put(createIdNameJSONObject(entity));
            }
            result.put("with_tags", new JSONObject().put("data", jsonWithTags));
            
            // Add message tags
            result.put("message_tags", createMessageTagJSONObject(post.getMessageTags()));
            
            // Add privacy
            result.put("privacy", createPrivacyJSONObject(post.getPrivacy()));
            
            // Add place
            result.put("place", createPlaceJSONObject(post.getPlace()));
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    public static JSONObject createPlaceJSONObject(Place place)
    {
        JSONObject result = new JSONObject();
        if (place == null)
            return result;
        
        try
        {
            result.put("id", place.getId());
            result.put("name", place.getName());
            
            // Create the location JSON
            JSONObject locJSON = new JSONObject();
            locJSON.put("city", place.getLocation().getCity());
            locJSON.put("country", place.getLocation().getCountry());
            locJSON.put("latitude", place.getLocation().getLatitude());
            locJSON.put("longitude", place.getLocation().getLongitude());
            locJSON.put("state", place.getLocation().getState());
            locJSON.put("street", place.getLocation().getStreet());
            locJSON.put("zip", place.getLocation().getZip());
            
            result.put("location", locJSON);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public static JSONObject createPrivacyJSONObject(Privacy privacy)
    {
        JSONObject result = new JSONObject();
        if (privacy == null)
            return result;

        try
        {
            result.put("description", privacy.getDescription());
            result.put("value", privacy.getValue());
            result.put("friends", privacy.getFriends());
            result.put("networks", privacy.getNetworks());
            result.put("allow", privacy.getAllow());
            result.put("deny", privacy.getDeny());
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public static JSONObject createMessageTagJSONObject(List<Tag> messageTags)
    {
        JSONObject result = new JSONObject();
        if (messageTags == null)
            return result;

        try
        {
            for (Tag tag : messageTags)
            {
                JSONObject tagJSON = new JSONObject();
                tagJSON.put("id", tag.getId());
                tagJSON.put("name", tag.getName());
                tagJSON.put("offset", tag.getOffset());
                tagJSON.put("length", tag.getLength());
                tagJSON.put("type", tag.getType());
                
                /*
                 * THIS IS WEIRD.
                 * But I did this to match what comes up in the Graph API explorer
                 * 
                 * 
                 {
                  "message_tags": {
                    "41": [
                      {
                        "id": "100001085717161", 
                        "name": "Tom Werner", 
                        "type": "user", 
                        "offset": 41, 
                        "length": 10
                      }
                    ]
                  }, 
                  "id": "100001085717161_679748188738032", 
                  "created_time": "2014-04-14T02:13:21+0000"
                 }, 
                 * This is what I get in the explorer, and the key is always the offset. 
                 */
                result.put(tag.getOffset().toString(), new JSONArray().put(tagJSON));
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public static JSONObject createStoryTagJSONObject(Map<String, Tag[]> storyTags)
    {
        JSONObject result = new JSONObject();
        if (storyTags == null)
            return result;

        try
        {
            for (String key : storyTags.keySet())
            {
                JSONArray tagArrayJSON = new JSONArray();
                Tag[] tags = storyTags.get(key);

                for (Tag tag : tags)
                {
                    JSONObject tagJSON = new JSONObject();
                    tagJSON.put("id", tag.getId());
                    tagJSON.put("name", tag.getName());
                    tagJSON.put("offset", tag.getOffset());
                    tagJSON.put("length", tag.getLength());
                    tagJSON.put("type", tag.getType());

                    tagArrayJSON.put(tagJSON);
                }

                result.put(key, tagArrayJSON);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public static JSONObject createLikeJSONObject(Facebook session, Like like)
    {
        JSONObject result = new JSONObject();
        if (like == null)
            return result;

        try
        {
            result.put("id", like.getId());
            result.put("name", like.getName());
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

    public static <T> ArrayList<T> getAllOfPageableList(Facebook session, PagableList<T> list) throws FacebookException
    {
        ArrayList<T> result = new ArrayList<T>();

        result.addAll(list);
        Paging<T> paging = list.getPaging();
        while (paging != null && paging.getNext() != null && paging.getNext().toString().length() != 0)
        {
            PagableList<T> page2 = session.fetchNext(paging);
            result.addAll(page2);
            paging = page2.getPaging();
        }

        return result;
    }
}
