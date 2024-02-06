package com.data.proman.repository;

import com.data.proman.enitity.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, Long> {

}
