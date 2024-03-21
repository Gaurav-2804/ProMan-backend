package com.data.proman.enitity.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDAO {
    @Id
    private String taskId;

    private String category;

    private String status;

    private String assignee;

    private String reporter;

    private String summary;

    private String startDate;

    private String endDate;

    private String description;

    private Map<String,String> filesMapping;
}
