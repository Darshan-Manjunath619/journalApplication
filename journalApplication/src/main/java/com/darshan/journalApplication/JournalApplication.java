package com.darshan.journalApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableTransactionManagement
public class JournalApplication {

	public static void main(String[] args) {
		SpringApplication.run(JournalApplication.class, args);
	}


    /*To add the transaction we add this @EnableTransactionManagement annonation.
    * EnableTransactionManagement it basically tells the springboot to check with @transaction annonation.
    * PlatformTransactionManager it is interface to implement the transaction
    * MongoTransactionManager - is a implementation
    * MongoDatabaseFactory - it provides the connection between db
     */
    @Bean
    public PlatformTransactionManager add(MongoDatabaseFactory dbFactory){
       return new MongoTransactionManager(dbFactory);
  }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

}