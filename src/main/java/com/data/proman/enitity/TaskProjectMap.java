package com.data.proman.enitity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "TaskProjectMap")
public class TaskProjectMap {
    @Id
    private String projectId;

    private Map<String, List<String>> taskStatusMap;

    public void initializeTaskStatusMap() {
        this.taskStatusMap = new HashMap<>();
        this.taskStatusMap.put("OPEN", new ArrayList<>());
        this.taskStatusMap.put("IN PROGRESS", new ArrayList<>());
        this.taskStatusMap.put("COMPLETE", new ArrayList<>());
    }
}
