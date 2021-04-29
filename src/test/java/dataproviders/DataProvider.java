package dataproviders;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import dto.GithubRepoDTO;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class DataProvider {

    public ArrayList<GithubRepoDTO> getTestData() {
        ArrayList<GithubRepoDTO> result = new ArrayList<>();
        String filename = "data.json";
        Gson gson = new Gson();
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (reader != null) {
            result = gson.fromJson(reader, new TypeToken<ArrayList<GithubRepoDTO>>() {
            }.getType());
        }
        return result;
    }
}
