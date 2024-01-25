package com.data.proman.enitity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "Projects")
public class Project {
    @Id
    private String projectId;

    private String name;

    private String description;

    private String key;

    private String dueDate;

    private Integer totalTasks;

    @DBRef
    private List<Member> members;
}
