package utils;

import com.google.gson.Gson;

public class GSONSingleton {
    private static volatile Gson instance;

    public static Gson getInstance() {
        Gson localInstance = instance;
        if (localInstance == null) {
            synchronized (Gson.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Gson();
                }
            }
        }
        return localInstance;
    }
}