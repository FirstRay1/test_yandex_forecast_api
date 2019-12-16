package getforecast;

import org.testng.annotations.Test;
import utils.APIRequestBuilder;

import static io.restassured.RestAssured.given;

/*
Check for response 403 as described https://yandex.ru/dev/weather/doc/dg/concepts/errors-docpage/
 */
//TODO Need refactoring to init parametrized spec
public class InvalidAPIRequests {
    @Test
    public void CheckInvalidURL(){
        given().
        spec(APIRequestBuilder.SetDefaultRequest()).
// baseUri for wrong tariff "forecast for your website" (should be https://api.weather.yandex.ru/v1/forecast)
        baseUri("https://api.weather.yandex.ru/v1/informers").
        when().
        get().
        then().
        statusCode(403);

        System.out.printf("\nInvalidApiUrlCheck\n");
    }
    @Test
    public void CheckInvalidHeaderValue(){
        given().
        spec(APIRequestBuilder.SetDefaultRequest()).
//Key is X-Yandex-API-Key but INVALID Value
        header("X-Yandex-API-Key","INVALID").
        when().
        get().
        then().
        statusCode(403);

        System.out.printf("\nInvalidHeaderValueCheck\n");
    }
    @Test
    public void CheckInvalidHeaderKey(){
        given().
//Key is NOT X-Yandex-API-Key but valid Value
        baseUri("https://api.weather.yandex.ru/v1/forecast").
        header("NOT!X-Yandex-API-Key","72b29110-0527-4d78-85bd-d0e8a27a65bb").
        param("lan","55.839906").
        param("lon","37.490297").
        when().
        get().
        then().
        statusCode(403);

        System.out.printf("\nInvalidHeaderKeyCheck\n");
    }
}
