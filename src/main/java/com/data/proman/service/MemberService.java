package com.data.proman.service;

import com.data.proman.enitity.Member;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface MemberService {
    public List<Member> getProjectMembers(String projectId);

    public List<Member> getAllMembers();

    public void addMemberToProject(String projectId, String memberId);

    public String addMember(Member member);

    public Member getMember(String memberId);

    public Boolean isExistingMember(Member member);

    public Member updateMember(Member member, String memberId);

    public void removeMemberFromProject(String projectId, String memberId);

    public void removeMember(String memberId);

    public Map<String,List<String>> getMemberProjectMappings();

//    public Map<String,String> getMemberProjectMapping();
}
