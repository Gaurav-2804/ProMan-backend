package com.data.proman.configurations;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseConfig {
    String connectionString  = "mongodb+srv://ProMan:PromanDb%40123@proman.cl4kdsm.mongodb.net/?retryWrites=true&w=majority";

    ServerApi serverApi = ServerApi.builder()
            .version(ServerApiVersion.V1)
            .build();

    MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .serverApi(serverApi)
            .build();

    public void connectToDatabase() {
        try (MongoClient mongoClient = MongoClients.create(settings)) {
            try {
                MongoDatabase database = mongoClient.getDatabase("ProjectsDB");
                // You can perform operations on the database here
            } catch (MongoException e) {
                e.printStackTrace();
            }
        }
    }
}
