package com.data.proman.repository;

import com.data.proman.enitity.TaskProjectMap;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskProjectMapRepository extends MongoRepository<TaskProjectMap, String> {

}
