package com.darshan.journalApplication.service;

import com.darshan.journalApplication.apiresponse.weatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private  String apiKey;

    private static final String API = "http://api.weatherstack.com/current?access_key=API_KEY&query=CITY";


    @Autowired
    private RestTemplate restTemplate;

    public weatherResponse getWeather(String city){
        String finalApi = API.replace("CITY" ,city).replace("API_KEY",apiKey);
        ResponseEntity<weatherResponse> exchange = restTemplate.exchange(finalApi, HttpMethod.GET, null, weatherResponse.class);
        weatherResponse body = exchange.getBody();
        return body;

    }

}
