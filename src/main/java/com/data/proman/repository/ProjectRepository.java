package com.data.proman.repository;

import com.data.proman.enitity.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {
    Optional<Project> findByKey(String key);


}
