package com.data.proman.service;

import com.data.proman.enitity.Comment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {
    public List<Comment> getAllComments(String taskId);

    public void addComment(Comment comment, String taskId);
}
