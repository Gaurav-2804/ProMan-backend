package com.data.proman.service.impl;

import com.data.proman.configurations.FireStoreConstants;
import com.data.proman.enitity.Member;
import com.data.proman.enitity.Project;
import com.data.proman.exception.EntityNotFoundException;
import com.data.proman.repository.MemberRepository;
import com.data.proman.repository.ProjectRepository;
import com.data.proman.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MemberServiceImpl implements MemberService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MemberRepository memberRepository;
    @Override
    public List<Member> getMembers(String projectId) {
        Optional<Project> projectEntity = projectRepository.findById(projectId);
        if(projectEntity.isPresent()){
            Project project = projectEntity.get();
            List<Member> members = project.getMembers();
            return members;
        }
        else {
            throw new EntityNotFoundException(null, Project.class);
        }
    }

    @Override
    public void addMember(String projectId, Member member) {
        Optional<Member> memberEntity = memberRepository.findById(member.getMemberId());
        Optional<Project> projectEntity = projectRepository.findById(projectId);

        if(memberEntity.isPresent() && projectEntity.isPresent()){
            Project project = addMemberToExistingProject(projectEntity.get(),memberEntity.get());
            projectRepository.save(project);
        }
        else if(projectEntity.isPresent()){
            Project project = addMemberToExistingProject(projectEntity.get(),member);
            projectRepository.save(project);
        } else if (!projectEntity.isPresent()) {
            throw new EntityNotFoundException(null, Project.class);
        } else {
            throw new EntityNotFoundException(null, Member.class);
        }
    }

    @Override
    public Member getMember(String memberId) {
        Optional<Member> memberEntity = memberRepository.findById(memberId);
        if(memberEntity.isPresent()){
            return memberEntity.get();
        }
        else {
            throw new EntityNotFoundException(null, Member.class);
        }
    }

    private Project addMemberToExistingProject(Project project,Member member) {
        if (project.getMembers() == null) {
            project.setMembers(new ArrayList<>());
        }
        if(member.getProjectId() == null) {
            member.setProjectId(new ArrayList<>());
        }
        member = configureMember(member);
        List<String> projectIds = member.getProjectId();
        projectIds.add(project.getProjectId());
        member.setProjectId(projectIds);
        updateMemberDetails(member);
        List<Member> members = project.getMembers();
        members.add(member);
        project.setMembers(members);
        return project;
    }
    
    private void updateMemberDetails(Member member) {
        memberRepository.save(member);
    }
    private Member configureMember(Member member) {
        Member configMember = member;
        if(member.getUserImgUrl() == null || member.getUserImgUrl() == "") {
            configMember.setUserImgUrl(FireStoreConstants.defaultUserImgUrl);
        }
        return configMember;
    }


}
