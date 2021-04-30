import com.google.gson.reflect.TypeToken;
import dto.GithubRepoDTO;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import utils.GSONSingleton;

import java.util.ArrayList;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.oauth2;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GithubTests {
    private static final String GITHUB_PERSONAL_TOKEN = System.getenv("GITHUB_PERSONAL_TOKEN");
    private final String USERS_ENDPOINT = "users";

    @BeforeAll
    static void SetUp() {
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setBaseUri("https://api.github.com/")
                .setAccept("application/vnd.github.v3+json")
                .setContentType(ContentType.JSON)
                .setAuth(oauth2(GITHUB_PERSONAL_TOKEN))
                .addFilter(new AllureRestAssured())
                .build();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    @Description("Get all github repositories test")
    @Order(1)
    public void getAllReposTest() {
        String userName = "dmitriyvusyk";

        given()
                .basePath(USERS_ENDPOINT)
                .pathParam("user", userName)
                .when()
                .get("/{user}/repos")
                .then()
                .assertThat()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("repositories_schema.json"));

    }

    /*Создать репу,
    проверить что она появилась в списке репозиториев
    и удалить,
    после чего проверить что она удалена*/

    @Test
    @Description("CREATE new repository DELETE repository")
    @Order(0)
    public void createAndDeleteRepository() {
        String userName = "dmitriyvusyk";
        ArrayList<GithubRepoDTO> existingRepos = getAllRepositories(userName);
        String expectedRepoName = "test_repository";
        JSONObject requestParams = new JSONObject();
        requestParams.put("name", expectedRepoName);

        //create repository
        String createRepositoryResponse = given()
                .body(requestParams.toString())
                .when()
                .post("/user/repos")
                .then()
                .assertThat()
                .statusCode(201)
                .and()
                .body(matchesJsonSchemaInClasspath("repository_schema.json"))
                .extract().response()
                .getBody().asString();

        GithubRepoDTO createdRepositoryDTO = GSONSingleton
                .getInstance()
                .fromJson(createRepositoryResponse, GithubRepoDTO.class);
        ArrayList<GithubRepoDTO> reposAfterPost = getAllRepositories(userName);
        Assertions.assertAll(
                () -> assertEquals(createdRepositoryDTO.getName(), expectedRepoName, "created repository has expected name"),
                () -> assertTrue(reposAfterPost.size() > existingRepos.size(), "repository is created ")
        );

        //delete repository
        given()
                .pathParam("owner", userName)
                .pathParam("repo", expectedRepoName)
                .when()
                .delete("/repos/{owner}/{repo}")
                .then()
                .assertThat()
                .statusCode(204);

        ArrayList<GithubRepoDTO> reposAfterDelete = getAllRepositories(userName);
        assertTrue(Collections.disjoint(reposAfterDelete, existingRepos), "repository is deleted ");

    }

    @Step("get all user repositories")
    private ArrayList<GithubRepoDTO> getAllRepositories(String userName) {
        String body = given()
                .basePath(USERS_ENDPOINT)
                .pathParam("user", userName)
                .when()
                .get("/{user}/repos")
                .then()
                .statusCode(200)
                .extract()
                .response()
                .getBody()
                .asString();

        return GSONSingleton.getInstance().fromJson(body, new TypeToken<ArrayList<GithubRepoDTO>>() {
        }.getType());
    }

}
