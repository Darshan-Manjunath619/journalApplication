package com.darshan.journalApplication.service;

import com.darshan.journalApplication.apiresponse.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private  String apiKey;

    private static final String API = "http://api.weatherstack.com/current?access_key=API_KEY&query=CITY";

    @Autowired
    private RedisService redisService;


    @Autowired
    private RestTemplate restTemplate;

    public WeatherResponse getWeather(String city){
        WeatherResponse weatherResponse = redisService.get("Weather_Of_"+city, WeatherResponse.class);
        if(weatherResponse != null){
            return weatherResponse;
        }
        else {
            String finalApi = API.replace("CITY", city).replace("API_KEY", apiKey);
            ResponseEntity<WeatherResponse> exchange = restTemplate.exchange(finalApi, HttpMethod.GET, null, WeatherResponse.class);
            WeatherResponse body = exchange.getBody();
            if(body != null){
                redisService.set("Weather_Of_"+city ,body,300l);
            }
            return body;
        }

    }

}
