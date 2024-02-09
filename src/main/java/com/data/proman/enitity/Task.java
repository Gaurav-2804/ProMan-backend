package com.data.proman.enitity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "Tasks")
public class Task {

    @Id
    private String taskId;

    private String category;

    private String status;

    @DBRef
    private Member assignee;

    @DBRef
    private Member reporter;

    private String summary;

    private String startDate;

    private String endDate;

    private String description;

    private List<String> imgUrls;

    @DBRef
    private List<Comment> comments;

}
