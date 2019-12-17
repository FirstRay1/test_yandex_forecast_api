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
    public void CheckDefaultHoursJSONSchema(){
        ValidatableResponse response = given().
        spec(APIRequestBuilder.SetDefaultRequest()).
        param("hours", "false").   //to get the most simple answer
        when().
        get().
        then().
        contentType(ContentType.JSON).assertThat().
        body(matchesJsonSchemaInClasspath("default.json"));
        String responseBody = response.extract().asString();
        System.out.printf("\nDefaultHoursJSONSchemaChecked\n");
    }

/*TODO
!Logging
Check for:
1. Authorization with wrong header                            --> DONE InvalidAPIRequests
2. Request without obligatory params                          --> Invalid test
Spec(https://yandex.ru/dev/weather/doc/dg/concepts/forecast-test-docpage/) error, server returns 200 and valid schema
with some data. So there are no obligatory params
3. JSONSchema with "hours"
4. JSONSchema with "extra"
5. "info.url" for all "lang"
6. Requests with different "limit" (valid/invalid)
7. Correlation between "now" and "now_dt"                     --> DONE TestRequestedParamsIndependentResponses
8. Correlation "info.lat", "info.lon" between request params  --> Failing test

 */
}
