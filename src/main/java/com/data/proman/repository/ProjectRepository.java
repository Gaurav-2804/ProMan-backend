package com.data.proman.repository;

import com.data.proman.enitity.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectRepository extends MongoRepository<Project, String> {
}
