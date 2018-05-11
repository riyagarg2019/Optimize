package com.network;

import com.data.directions.DirectionResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DirectionsAPI {
    @GET("maps/api/directions/json")
    Call<DirectionResult> getWeatherData(@Query("origin") String originLatLng,
                                         @Query("destination") String destLagLng,
                                         @Query("key") String key);
}
