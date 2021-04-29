package dataproviders;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import dto.GithubRepoDTO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;

public class DataProvider {

    public ArrayList<GithubRepoDTO> getTestData() {
        ArrayList<GithubRepoDTO> result = new ArrayList<>();
        Gson gson = new Gson();
        JsonReader reader = null;
        String fileName = "data.json";
        try {
            reader = new JsonReader(new FileReader(readFileFromResources(fileName)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (reader != null) {
            result = gson.fromJson(reader, new TypeToken<ArrayList<GithubRepoDTO>>() {}.getType());
        }
        return result;
    }

    public File readFileFromResources(String fileName){
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(classLoader.getResource(fileName).getFile());
    }
}
