package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Hadi on 2/2/2015 6:58 PM.
 */
public class Json {

    private static GsonBuilder builder = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation();
    private static Gson sGson = builder.create();

    public static Gson gson() {
        return sGson;
    }

}
