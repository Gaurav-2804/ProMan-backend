package com.data.proman.enitity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskDAO {
    private Long id;

    @Id
    private String taskId;

    private String category;

    private String assigneeId;

    private String reporterId;

    private String projectKey;

    private String startDate;

    private String endDate;

    private String summary;

    public void setTaskId(String projectKey) {
        this.taskId = projectKey+"-"+this.id;
    }

    public void generateId(MongoTemplate mongoTemplate) {
        this.id = getNextSequence(mongoTemplate, "taskDAO");
    }

    private Long getNextSequence(MongoTemplate mongoTemplate, String collectionName) {
        Query query = new Query(Criteria.where("collectionName").is(collectionName));
        Update update = new Update().inc("sequence", 1);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

        Counters counter = mongoTemplate.findAndModify(query, update, options, Counters.class);
        return (counter != null) ? counter.getSequence() : 1L;
    }
}
