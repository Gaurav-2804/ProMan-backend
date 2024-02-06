package com.data.proman.enitity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "Members")
public class Member {
//    @Id
    private Long id;

    @Id
    private String memberId;

    private List<String> projectId;

    private String name;

    private String mailId;

    private String userImgUrl;

    public void setMemberId() {
        this.memberId = "M-" + this.id;
    }

    public void generateId(MongoTemplate mongoTemplate) {
        this.id = getNextSequence(mongoTemplate, "Members");
    }

    private Long getNextSequence(MongoTemplate mongoTemplate, String collectionName) {
        Query query = new Query(Criteria.where("collectionName").is(collectionName));
        Update update = new Update().inc("sequence", 1);
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

        Counters counter = mongoTemplate.findAndModify(query, update, options, Counters.class);
        return (counter != null) ? counter.getSequence() : 1L;
    }
}