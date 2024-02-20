package com.data.proman.service.impl;

import com.data.proman.configurations.FireStoreConstants;
import com.data.proman.enitity.Member;
import com.data.proman.enitity.Project;
import com.data.proman.enitity.Task;
import com.data.proman.exception.EntityNotFoundException;
import com.data.proman.repository.MemberRepository;
import com.data.proman.repository.ProjectRepository;
import com.data.proman.service.CounterService;
import com.data.proman.service.MemberService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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

    @Autowired
    private CounterService counterService;

    private final MongoTemplate mongoTemplate;

    private final ModelMapper modelMapper;

    private MemberServiceImpl(MongoTemplate mongoTemplate, ModelMapper modelMapper) {
        this.mongoTemplate = mongoTemplate;
        this.modelMapper = modelMapper;
    };

    @Override
    public List<Member> getProjectMembers(String projectId) {
        Optional<Project> projectEntity = projectRepository.findById(projectId);
        if(projectEntity.isPresent()){
            Project project = projectEntity.get();
            return project.getMembers();
        }
        else {
            throw new EntityNotFoundException(null, Project.class);
        }
    }

    @Override
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    @Override
    public String addMemberToProject(String projectId, Member member) {
        Optional<Member> memberEntity = memberRepository.findByMemberId(member.getMemberId());
        Optional<Project> projectEntity = projectRepository.findById(projectId);

        if(memberEntity.isPresent() && projectEntity.isPresent()){
            addMemberToExistingProject(projectEntity.get(),memberEntity.get());
        }
        else if(projectEntity.isPresent()){
            addMemberToExistingProject(projectEntity.get(),member);
        } else if(memberEntity.isEmpty()){
            throw new EntityNotFoundException(null, Member.class);
        }
        else {
            throw new EntityNotFoundException(null, Project.class);
        }
        return member.getMailId();
    }

    @Override
    public String addMember(Member member) {
        Member memberDb = configureMember(member);
        memberDb.setMemberId(member.getMailId());
        memberRepository.save(member);
        return member.getMailId();
    }

    @Override
    public Member getMember(String memberId) {
        Optional<Member> memberEntity = memberRepository.findByMemberId(memberId);
        if(memberEntity.isPresent()){
            return memberEntity.get();
        }
        else {
            throw new EntityNotFoundException(null, Member.class);
        }
    }

    @Override
    public Boolean isExistingMember(Member member) {
        Optional<Member> isExistingMember = memberRepository.findByMailId(member.getMailId());
        return isExistingMember.isPresent();
    }

    @Override
    public Member updateMember(Member member, String memberId) {
        Optional<Member> memberDeatilsEntity = memberRepository.findByMemberId(memberId);
        if(memberDeatilsEntity.isPresent()) {
            Member memberDetails = memberDeatilsEntity.get();
            modelMapper.getConfiguration().setSkipNullEnabled(true);
            modelMapper.map(member,memberDetails);
            return memberRepository.save(memberDetails);
        }
        else {
            throw new EntityNotFoundException(404L, Member.class);
        }
    }

    @Override
    public void removeMemberFromProject(String projectId, String memberId) {
        Optional<Member> memberEntity = memberRepository.findByMemberId(memberId);
        Optional<Project> projectEntity = projectRepository.findById(projectId);
        if(projectEntity.isPresent() && memberEntity.isPresent()) {
            updateProjectDetails(memberEntity.get(),projectEntity.get(),"REMOVE");
            updateMemberDetails(memberEntity.get(),projectEntity.get().getProjectId(),"REMOVE");
        }
        else {
            throw new EntityNotFoundException(null, Project.class);
        }
    }

    @Override
    public void removeMember(String memberId) {
        memberRepository.deleteById(memberId);
    }

    private void addMemberToExistingProject(Project project, Member member) {
        if (project.getMembers() == null) {
            project.setMembers(new ArrayList<>());
        }
        if(member.getProjectId() == null) {
            member.setProjectId(new ArrayList<>());
        }
        updateMemberDetails(member, project.getProjectId(), "ADD");
        updateProjectDetails(member, project, "ADD");
    }
    
    private void updateMemberDetails(Member member, String projectId, String action) {
        List<String> projectIds = member.getProjectId();
        if(action.equals("ADD")){
            projectIds.add(projectId);
        }
        else {
            projectIds.removeIf(projId -> projId.equals(projectId));
        }
        member.setProjectId(projectIds);
        memberRepository.save(member);
    }

    private void updateProjectDetails(Member member, Project project, String action) {
        List<Member> membersList = project.getMembers();
        if(action.equals("ADD")) {
            membersList.add(member);
        }
        else {
            membersList.removeIf(tempMember -> tempMember.getMemberId().equals(member.getMemberId()));
        }
        project.setMembers(membersList);
        projectRepository.save(project);
    }
    private Member configureMember(Member member) {
        if(member.getUserImgUrl() == null || member.getUserImgUrl() == "") {
            member.setUserImgUrl(FireStoreConstants.defaultUserImgUrl);
        }
        return member;
    }


}
