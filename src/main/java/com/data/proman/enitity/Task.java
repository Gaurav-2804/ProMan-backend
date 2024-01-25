package com.data.proman.enitity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "Tasks")
public class Task {
    @Id
    private Integer taskId;

    private String issueType;

    private String status;

    private String assignee;

    private String reporter;

    private String summary;

    private String timeEstimate;

    private String parentName;
}
