package edu.uiowa.datacollection.main;

import facebook4j.FacebookException;
import facebook4j.internal.org.json.JSONException;

public class CollectAllData
{
    public static void main(String[] args) throws FacebookException, JSONException
    {
        CollectFacebookData.main(args);
        CollectTwitterData.main(args);
    }
}
