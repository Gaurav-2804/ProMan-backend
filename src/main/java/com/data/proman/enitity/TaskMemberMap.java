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
@Document(collection = "TaskMemberMap")
public class TaskMemberMap {
    @Id
    private String memberId;

    private Map<String,List<String>> statusTasksMap;

    public void initializeStatusTasksMap() {
        this.statusTasksMap = new HashMap<>();
        this.statusTasksMap.put("ASSIGNEE", new ArrayList<>());
        this.statusTasksMap.put("REPORTER", new ArrayList<>());
    }
}
