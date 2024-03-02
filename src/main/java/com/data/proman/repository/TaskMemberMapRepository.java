package com.data.proman.repository;

import com.data.proman.enitity.TaskMemberMap;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaskMemberMapRepository extends MongoRepository<TaskMemberMap, String> {
}
