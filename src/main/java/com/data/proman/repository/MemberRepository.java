package com.data.proman.repository;

import com.data.proman.enitity.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MemberRepository extends MongoRepository<Member, String> {
}
