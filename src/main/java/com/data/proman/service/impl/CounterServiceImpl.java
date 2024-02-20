package com.data.proman.service.impl;

import com.data.proman.enitity.Counters;
import com.data.proman.service.CounterService;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class CounterServiceImpl implements CounterService {

    @Override
    public Long generateId(MongoTemplate mongoTemplate, String collectionName) {
        return getNextSequence(mongoTemplate, collectionName);
    }

    private Long getNextSequence(MongoTemplate mongoTemplate, String collectionName) {
        Query query = new Query(Criteria.where("collectionName").is(collectionName));
        Update update = new Update().inc("sequence", 1);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

        Counters counter = mongoTemplate.findAndModify(query, update, options, Counters.class);
        return (counter != null) ? counter.getSequence() : 1L;
    }
}
