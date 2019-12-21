package getforecast;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.APIRequestBuilder;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class TestAPIYandexForecast {

    @Test
    public void CheckValidAuthorization(){
        given().
        spec(APIRequestBuilder.SetDefaultRequest()).
        when().
        get().
        then().
        statusCode(200);

        System.out.printf("\nDefaultParamsAuthorized\n");
    }
    @Test
    public void CheckDefaultNOHoursJSONSchema(){
        ValidatableResponse response = given().
        spec(APIRequestBuilder.SetDefaultRequest()).
        param("hours", "false").   //to get the most simple answer
        when().
        get().
        then().
        contentType(ContentType.JSON).assertThat().
        body(matchesJsonSchemaInClasspath("default.json"));
        String responseBody = response.extract().asString();
        System.out.printf("\nDefaultHoursJSONSchema checked\n");
    }
    @Test
    public void CheckDefaultWithHoursJSONSchema(){
        ValidatableResponse response = given().
                spec(APIRequestBuilder.SetDefaultRequest()).
                when().
                get().
                then().
                contentType(ContentType.JSON).assertThat().
                body(matchesJsonSchemaInClasspath("withhours.json"));
        String responseBody = response.extract().asString();
        System.out.printf("\nDefaultWithHoursJSONSchema checked\n");
    }
    @Test
    public void CheckDefaultWithHoursExtraJSONSchema(){
        ValidatableResponse response = given().
                spec(APIRequestBuilder.SetDefaultRequest()).
                param("extra", "true").   //to get precipitation
                when().
                get().
                then().
                contentType(ContentType.JSON).assertThat().
                body(matchesJsonSchemaInClasspath("withextra.json"));
        String responseBody = response.extract().asString();
        System.out.printf("\nDefaultWithHoursExtraJSONSchema checked\n");
    }
    @Test
    public void CheckDefaultNOHoursWithExtraJSONSchema(){
        ValidatableResponse response = given().
                spec(APIRequestBuilder.SetDefaultRequest()).
                param("hours", "false").
                param("extra", "true").
                when().
                get().
                then().
                contentType(ContentType.JSON).assertThat().
                body(matchesJsonSchemaInClasspath("nohourswithextra.json"));
        String responseBody = response.extract().asString();
        System.out.printf("\nDefaultNoHoursWithExtraJSONSchema checked\n");
    }
    //DataProvider contains pairs for request param "lang" and response "info.URL" due to
    // specs from https://yandex.ru/dev/weather/doc/dg/concepts/forecast-test-docpage/
    @DataProvider(name = "testLang")
    public Object[][] createData1() {
        return new Object[][] {
                { "ru_RU", "yandex.ru/pogoda"},
                { "ru_UA", "yandex.ua/pogoda"},
                { "uk_UA", "yandex.ua/pogoda"}, //In Russia equal to ru_UA
                { "be_BY", "yandex.by/pogoda"},
                { "kk_KZ", "yandex.kz/pogoda"},
                { "tr_TR", "yandex.com.tr/hava"},
                { "en_US", "yandex.com/pogoda/"},
        };
    }
    //Test for parameter "lang" from
    @Test(dataProvider = "testLang")
    public void CheckLangURL(String paramLang, String objectURL) {
        ValidatableResponse response = given().
                spec(APIRequestBuilder.SetDefaultRequest()).
                param("lang", paramLang).
                when().
                get().
                then().
                contentType(ContentType.JSON).assertThat();
        String responseURL = response.extract().jsonPath().getString("info.url");
        Assert.assertTrue(responseURL.contains(objectURL));
        System.out.printf("\nDueToLangURLResponse checked\n");
    }


/*TODO
!Logging
Check for
1. Authorization with wrong header                            --> DONE InvalidAPIRequests
2. Request without obligatory params                          --> Invalid test
Spec(https://yandex.ru/dev/weather/doc/dg/concepts/forecast-test-docpage/) error, server returns 200 and valid schema
with some data. So there are no obligatory params
3. JSONSchema with "hours"                                    --> DONE
4. JSONSchema with "extra"                                    --> DONE
5. "info.url" for all "lang"                                  --> DONE
6. Requests with different "limit"
(valid/invalid, check schema correlation to limit value)
7. Correlation between "now" and "now_dt"                     --> DONE TestRequestedParamsIndependentResponses
8. Correlation "info.lat", "info.lon" between request params  --> Failing test
 */
}
