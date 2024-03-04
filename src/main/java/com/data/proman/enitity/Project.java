package com.data.proman.enitity;

import jakarta.persistence.GeneratedValue;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "Projects")
public class Project {
//    @Id
//    @GeneratedValue
//    private String projectId = UUID.randomUUID().toString();

    @Id
    private String projectId;

    private String key;

    private String name;

    private String description;

    private String dueDate;

    private Integer totalTasks = 0;

    private Long progress = (long)0;

    private List<String> memberIds;

    public void setProjectId(String name, String key) {
        this.projectId = key + "-" + generateProjectId(name);
    }

    private String generateProjectId(String name) {
        return Base64.getEncoder().encodeToString(name.getBytes());
    }
}
