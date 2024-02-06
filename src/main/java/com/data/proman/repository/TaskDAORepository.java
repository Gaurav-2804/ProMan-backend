package com.data.proman.repository;

import com.data.proman.enitity.TaskDAO;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskDAORepository extends MongoRepository<TaskDAO, String> {
    List<TaskDAO> findAllByProjectKey(String projectKey);
}
