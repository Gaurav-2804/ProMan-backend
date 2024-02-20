package com.data.proman.service;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public interface CounterService {
    public Long generateId(MongoTemplate mongoTemplate, String collectionName);
}
