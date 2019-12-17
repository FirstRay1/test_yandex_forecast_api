package getforecast;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.QueryableRequestSpecification;
import io.restassured.specification.SpecificationQuerier;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.APIRequestBuilder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static io.restassured.RestAssured.given;

public class TestRequestedParamsIndependentResponses {
    @Test
    public void CheckServerTime(){
        ValidatableResponse response = given().spec(APIRequestBuilder.SetDefaultRequest()).when().get().then();
        Long serverUnixTime = response.extract().jsonPath().getLong("now") - 10800; //Moscow Time at server ;)
        String serverUTCStringTime = response.extract().jsonPath().getString("now_dt").substring(0,19);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Long serverUTCDataTime = 0L;
        try {
            serverUTCDataTime = dateFormat.parse(serverUTCStringTime).getTime() / 1000; //in seconds
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(serverUnixTime,serverUTCDataTime);
        System.out.printf("\nnow and now_dt are equal \n");
    }
    //INVALID Test return lat and lon belong to some point of nearest locality and can be different to requested lan and lon
    @Test
    public void CheckResponseLatLon(){
        ValidatableResponse response = given().spec(APIRequestBuilder.SetDefaultRequest()).when().get().then();
        String responseLat = response.extract().jsonPath().getString("info.lat");
        String responseLon = response.extract().jsonPath().getString("info.lon");
        QueryableRequestSpecification queryable = SpecificationQuerier.query(APIRequestBuilder.SetDefaultRequest());
        String requestLat = queryable.getRequestParams().get("lat");
        String requestLon = queryable.getRequestParams().get("lon");
        Assert.assertEquals(responseLat, requestLat);
        Assert.assertEquals(responseLon, requestLon);
        System.out.printf("\nlat and lon from response are equal to requested \n");
    }
}
