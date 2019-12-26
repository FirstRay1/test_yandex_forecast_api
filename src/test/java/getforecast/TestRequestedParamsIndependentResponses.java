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
        Long serverUnixTime = response.extract().jsonPath().getLong("now") -10800; //Moscow TimeZone at server ;)
        String serverUTCStringTime = response.extract().jsonPath().getString("now_dt").substring(0,19);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Long serverUTCDataTime = 0L;
        try {
            serverUTCDataTime = dateFormat.parse(serverUTCStringTime).getTime() / 1000; //in seconds
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(serverUnixTime,serverUTCDataTime);
        System.out.printf("\nnow and now_dt are equal at response \n");
    }
    @Test
    public void CheckResponseLatLon(){
        ValidatableResponse response = given().spec(APIRequestBuilder.SetDefaultRequest()).when().get().then();
        Float responseLat = response.extract().jsonPath().getFloat("info.lat");
        Float responseLon = response.extract().jsonPath().getFloat("info.lon");
        QueryableRequestSpecification queryable = SpecificationQuerier.query(APIRequestBuilder.SetDefaultRequest());
        Float requestLat = Float.parseFloat(queryable.getRequestParams().get("lat"));
        Float requestLon = Float.parseFloat(queryable.getRequestParams().get("lon"));
        Assert.assertEquals(responseLat, requestLat, "Response and request lat are different");
        Assert.assertEquals(responseLon, requestLon, "Response and request lon are different");
        System.out.printf("\nlat and lon from response are equal to requested \n");
    }
}
