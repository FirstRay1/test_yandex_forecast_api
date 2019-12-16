package utils;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

//TODO
// Add methods to construct request with params
public class APIRequestBuilder {
    public static RequestSpecification SetDefaultRequest() {
//API basepath
        RestAssured.baseURI = "https://api.weather.yandex.ru/v1/forecast";
//Key for testing Yandex Forecast
        String headerKey = "X-Yandex-API-Key";
        String headerValue = "72b29110-0527-4d78-85bd-d0e8a27a65bb";
//office shopping center Vodnii
        String testLat = "55.839906";
        String testLon = "37.490297";
//        String testHours ="hours=false";  //to get the most simple answer

        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.addHeader(headerKey, headerValue);
        builder.addParam(testLat);
        builder.addParam(testLon);
//        builder.addParam(testHours);
        RequestSpecification requestSpec = builder.build();
        return requestSpec;
    }
}
