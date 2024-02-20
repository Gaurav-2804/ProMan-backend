package com.data.proman.service;

import com.data.proman.enitity.Member;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MemberService {
    public List<Member> getProjectMembers(String projectId);

    public List<Member> getAllMembers();

    public String addMemberToProject(String projectId, Member member);

    public String addMember(Member member);

    public Member getMember(String memberId);

    public Boolean isExistingMember(Member member);

    public Member updateMember(Member member, String memberId);

    public void removeMemberFromProject(String projectId, String memberId);

    public void removeMember(String memberId);
}
