package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Hadi on 2/2/2015 6:58 PM.
 */
public class Json {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    public static final Gson CGSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

}
