import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dataproviders.DataProvider;
import dto.GithubRepoDTO;
import io.restassured.RestAssured;
import io.restassured.authentication.OAuthSignature;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
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
    @Order(0)
    public void getAllReposTest() {
        String userName = "dmitriyvusyk";

        ArrayList<GithubRepoDTO> actualList = getAllRepositories(userName);
        ArrayList<GithubRepoDTO> expectedList = new DataProvider().getTestData();

        Assertions.assertTrue(Collections.disjoint(actualList, expectedList));
    }

    /*Второй тест будет создавать репу,
    проверять что она появилась в списке репозиториев
    и удалять,
    после чего тоже проверять что она удалена*/

    @Test
    @Order(1)
    public void createAndDeleteRepository() {
        String userName = "dmitriyvusyk";
        ArrayList<GithubRepoDTO> existingRepos = getAllRepositories(userName);
        String expectedRepoName = "test_repository";
        JSONObject requestParams = new JSONObject();
        requestParams.put("name", expectedRepoName);

        Response createRepositoryResponse = given()
                .auth().oauth2(GITHUB_PERSONAL_TOKEN, OAuthSignature.HEADER)
                .body(requestParams.toString())
                .when()
                .post("/user/repos")
                .then()
                .log().ifError()
                .statusCode(201)
                .extract().response();

        String body = createRepositoryResponse.getBody().asString();
        Gson gson = new Gson();
        GithubRepoDTO createdRepositoryDTO = gson.fromJson(body, GithubRepoDTO.class);

        Assertions.assertEquals(createdRepositoryDTO.getName(), expectedRepoName, "created repository has expected name");

        ArrayList<GithubRepoDTO> reposAfterPost = getAllRepositories(userName);

        Assertions.assertTrue(reposAfterPost.size() > existingRepos.size(), "repository is created ");

        given()
                .auth().oauth2(GITHUB_PERSONAL_TOKEN, OAuthSignature.HEADER)
                .pathParam("owner", userName)
                .pathParam("repo", expectedRepoName)
                .when()
                .delete("/repos/{owner}/{repo}")
                .then()
                .log().ifError()
                .statusCode(204);

        ArrayList<GithubRepoDTO> reposAfterDelete = getAllRepositories(userName);
        Assertions.assertTrue(Collections.disjoint(reposAfterDelete, existingRepos), "repository is deleted ");

    }

    private ArrayList<GithubRepoDTO> getAllRepositories(String userName) {
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

        return gson.fromJson(body, new TypeToken<ArrayList<GithubRepoDTO>>() {
        }.getType());
    }

}
