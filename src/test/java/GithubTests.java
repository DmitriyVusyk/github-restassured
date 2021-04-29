import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataproviders.DataProvider;
import dto.GithubRepoDTO;
import io.restassured.RestAssured;
import io.restassured.authentication.OAuthSignature;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class GithubTests {
    private final String GITHUB_PERSONAL_TOKEN = System.getenv("GITHUB_PERSONAL_TOKEN");
    private final String USERS_ENDPOINT = "users";

    @BeforeEach
    public void SetUp() {
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://api.github.com/")
                .setAccept("application/vnd.github.v3+json")
                .setContentType(ContentType.JSON)
                .build();
    }

    @Test
    public void getAllReposTest() {
        String userName = "dmitriyvusyk";

        Response response = given()
                .auth().oauth2(GITHUB_PERSONAL_TOKEN, OAuthSignature.HEADER)
                .basePath(USERS_ENDPOINT)
                .pathParam("user", userName)
                .when()
                .get("/{user}/repos")
                .then()
                .log().ifError()
                .statusCode(200)
                .extract().response();

        String body = response.getBody().asString();
        Gson gson = new Gson();

        ArrayList<GithubRepoDTO> actualList = gson.fromJson(body, new TypeToken<ArrayList<GithubRepoDTO>>() {
        }.getType());
        ArrayList<GithubRepoDTO> expectedList = new DataProvider().getTestData();

        Assertions.assertTrue(Collections.disjoint(actualList, expectedList));
    }

}
