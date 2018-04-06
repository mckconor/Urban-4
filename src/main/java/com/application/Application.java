package com.application;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.helpers.FirebaseHelper;

@SpringBootApplication
@ComponentScan({"com.application","com.components"})
@EntityScan("com.entities")
@EnableJpaRepositories("com.repositories")
public class Application{

    public static void main(String[] args) {
    	try{
    		initFirebase();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

        SpringApplication.run(Application.class, args);
    }

    static void initFirebase () throws IOException {
    	FileInputStream serviceAccount = new FileInputStream("serviceAccountKey.json");

    	FirebaseOptions options = new FirebaseOptions.Builder()
    			  .setCredentials(GoogleCredentials.fromStream(serviceAccount))
    			  .setDatabaseUrl("https://urbancomputing2.firebaseio.com")
    			  .build();

		FirebaseApp.initializeApp(options);
    }
    
}