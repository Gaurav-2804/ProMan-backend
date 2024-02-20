package com.data.proman.repository;

import com.data.proman.enitity.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByMemberId(String memberId);

    Optional<Member> findByMailId(String mailId);
}
