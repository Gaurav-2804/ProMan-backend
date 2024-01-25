package com.data.proman.enitity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Document(collection = "Members")
public class Member {
    @Id
    private String memberId;

    private List<String> projectId;

    private String name;

    private String mailId;

    private String userImgUrl;
}
