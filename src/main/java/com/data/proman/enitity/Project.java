package com.data.proman.enitity;

import jakarta.persistence.GeneratedValue;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "Projects")
public class Project {
    @Id
    @GeneratedValue
    private String projectId = UUID.randomUUID().toString();

    private String name;

    private String description;

    private String key;

    private String dueDate;

    private Integer totalTasks;

    private Long progress;

    @DBRef
    private List<Member> members;
}
