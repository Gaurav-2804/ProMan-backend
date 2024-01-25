package com.data.proman.service;

import com.data.proman.enitity.Member;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MemberService {
    public List<Member> getMembers(String projectId);

    public void addMember(String projectId, Member member);

    public Member getMember(String memberId);
}
