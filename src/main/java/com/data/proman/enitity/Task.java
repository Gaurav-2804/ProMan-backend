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
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "Tasks")
public class Task {

    @Id
    private String taskId;

    private String category;

    private String status;

    private String assigneeId;

    private String reporterId;

    private String summary;

    private String startDate;

    private String endDate;

    private String description;

    private Map<String,String> filesMapping;

    @DBRef
    private List<Comment> comments;

}
