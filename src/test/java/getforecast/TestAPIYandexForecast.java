package getforecast;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
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


/*TODO
!Logging
Check for
1. Authorization with wrong header                            --> DONE InvalidAPIRequests
2. Request without obligatory params                          --> Invalid test
Spec(https://yandex.ru/dev/weather/doc/dg/concepts/forecast-test-docpage/) error, server returns 200 and valid schema
with some data. So there are no obligatory params
3. JSONSchema with "hours"                                    --> DONE
4. JSONSchema with "extra"                                    --> DONE
5. "info.url" for all "lang"
6. Requests with different "limit" (valid/invalid, check schema correlation to limit value)
7. Correlation between "now" and "now_dt"                     --> DONE TestRequestedParamsIndependentResponses
8. Correlation "info.lat", "info.lon" between request params  --> Failing test
 */
}
