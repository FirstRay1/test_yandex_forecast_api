package getforecast;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.APIRequestBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
                { "uk_UA", "yandex.ua/pogoda"},     //In Russia equal to ru_UA
                { "be_BY", "yandex.by/pogoda"},
                { "kk_KZ", "yandex.kz/pogoda"},
                { "tr_TR", "yandex.com.tr/hava"},
                { "en_US", "yandex.com/pogoda/"},
                { "invalid", "yandex.com/pogoda/"}, //In Russia equal to en_US
        };
    }
    //Test for parameter "lang"
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
    // "limit" param contains amount of days for forecast including current day
    @DataProvider(name = "testLimits")
    public Object[][] createData2() {
        return new Object[][]{
                {"2"},
                {"3"},
                {"4"},
                {"5"},
                {"6"},
                {"7"},
                {"8"}, //for limit > 7 we get 7 days forecast
                {"9"}, //for limit > 7 we get 7 days forecast
                {"1"}, //for limit < 2 we get 7 days forecast
                {"-1"}, //for limit < 2 we get 7 days forecast
                {"ha"}, //for not number limit we get 7 days forecast
        };
    }
    //Test for parameter "limit"
    //TODO need to be independent from location in response
    @Test(dataProvider = "testLimits")
    public void CheckLimitParam(String paramLimit) {
        ValidatableResponse response = given().
                spec(APIRequestBuilder.SetDefaultRequest()).
                param("limit", paramLimit).
                when().
                get().
                then().
                contentType(ContentType.JSON).assertThat();
        String fulResponse = response.extract().body().asString();
        String responseNow_dt = response.extract().jsonPath().getString("now_dt");
        DateFormat onlyDate = new SimpleDateFormat("yyyy-MM-dd");
        Long serverUTCDataTime = 0L;
        try {
            Instant iDateTime = Instant.parse(responseNow_dt);
            LocalDateTime ldt = LocalDateTime.ofInstant(iDateTime, ZoneId.of("Europe/Moscow")); //Server is located in Moscow
            serverUTCDataTime = onlyDate.parse(ldt.toString().substring(0,10)).getTime() / 1000; //in seconds
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int limit = 0;
        try {
            limit = Integer.parseInt(paramLimit);
        } catch (Exception e){
            limit = 7; //server for invalid amount always returns 200 and forecast fo 7 days
            for(int i=1; i<limit; i++) {
                Long forecastDate = serverUTCDataTime + i * 86400;
                String stringForecastDate = Long.toString(forecastDate);
                Assert.assertTrue(fulResponse.contains(stringForecastDate), "limit = " + paramLimit);
            }
        }
        if (limit < 2) {
            limit = 7; //server for invalid amount always returns 200 and forecast fo 7 days
            for (int i = 1; i < limit; i++) {
                Long forecastDate = serverUTCDataTime + i * 86400;
                String stringForecastDate = Long.toString(forecastDate);
                Assert.assertTrue(fulResponse.contains(stringForecastDate), "limit = " + paramLimit);

            }
        }
        if (limit > 8) limit = 8; // max valid limit = 7, but server for invalid amount always returns 200 and forecast fo 7 days
        for(int i=1; i < limit; i++) {
            Long forecastDate = serverUTCDataTime + i * 86400; // adding days
            String stringForecastDate = Long.toString(forecastDate);
            if(i < 7) // max valid limit = 7, but server for invalid amount always returns 200 and forecast fo 7 days
                Assert.assertTrue(fulResponse.contains(stringForecastDate), "limit = " + paramLimit);
            else
                Assert.assertFalse(fulResponse.contains(stringForecastDate),"limit = " + paramLimit +
                        " stringForecastDate " + stringForecastDate);
        }
        System.out.printf("\nDueToLimitDaysAmount " + paramLimit + " checked\n");
    }


/*TODO
!Logging Now you need to look test code to know what test exactly failed
Check for
1. Authorization with wrong header                            --> DONE InvalidAPIRequests
2. Request without obligatory params                          --> Invalid test
Spec(https://yandex.ru/dev/weather/doc/dg/concepts/forecast-test-docpage/) error, server returns 200 and valid schema
with some data without any params. So there are no obligatory params
3. JSONSchema with "hours"                                    --> DONE
4. JSONSchema with "extra"                                    --> DONE
5. "info.url" for all "lang"                                  --> DONE
6. Requests with different "limit"                            --> DONE
(valid/invalid, check schema correlation to limit value)
7. Correlation between "now" and "now_dt"                     --> DONE TestRequestedParamsIndependentResponses
8. Correlation "info.lat", "info.lon" between request params  --> DONE
 */
}
