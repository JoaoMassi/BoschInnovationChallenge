package com.example.joaom.cup;

import retrofit2.Call;
import retrofit2.http.GET;


public interface ThingSpeakService {

    //https://api.thingspeak.com
    @GET("channels/345441/feeds.json?api_key=7FH56PCG4F9EHOVJ&results=1")
    Call<Resposta> getThingSpeak();

}