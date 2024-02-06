package com.data.proman.service.impl;

import com.data.proman.enitity.Comment;
import com.data.proman.enitity.Task;
import com.data.proman.exception.EntityNotFoundException;
import com.data.proman.repository.TaskRepository;
import com.data.proman.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    TaskRepository taskRepository;

    @Override
    public List<Comment> getAllComments(String taskId) {
        Optional<Task> taskEntity = taskRepository.findById(taskId);
        if(taskEntity.isPresent()) {
            Task task = taskEntity.get();
            List<Comment> comments = task.getComments();
            return comments;
        }
        else {
            throw new EntityNotFoundException(null, Comment.class);
        }
    }

    @Override
    public void addComment(Comment comment, String taskId) {

    }
}
