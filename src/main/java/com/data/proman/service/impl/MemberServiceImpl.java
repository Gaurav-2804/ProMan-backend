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

import java.util.*;

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
            List<String> memberIds = Collections.emptyList();
            if(project.getMemberIds() != null) {
               memberIds  = project.getMemberIds();
            }

            return memberIds.stream()
                    .map((memberId) -> {
                        Optional<Member> memberEntity= memberRepository.findByMemberId(memberId);
                        if(memberEntity.isPresent()) {
                            return memberEntity.get();
                        }
                        else {
                            throw new EntityNotFoundException(404L, Member.class);
                        }
                    }).toList();
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
    public void addMemberToProject(String projectId, String memberId) {
        Optional<Member> memberEntity = memberRepository.findByMemberId(memberId);
        Optional<Project> projectEntity = projectRepository.findById(projectId);

        if(memberEntity.isPresent() && projectEntity.isPresent()){
            addMemberToExistingProject(projectEntity.get(), memberEntity.get(), memberId);
        }
        else if(projectEntity.isPresent()){
            addMemberToExistingProject(projectEntity.get(),memberEntity.get(), memberId);
        } else if(memberEntity.isEmpty()){
            throw new EntityNotFoundException(null, Member.class);
        }
        else {
            throw new EntityNotFoundException(null, Project.class);
        }
    }

    @Override
    public String addMember(Member member) {
        Member memberDb = configureMember(member);
        memberRepository.save(memberDb);
        return member.getMemberId();
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
        Optional<Member> isExistingMember = memberRepository.findByMemberId(member.getMemberId());
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
            updateProjectDetails(memberId,projectEntity.get(),"REMOVE");
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

    @Override
    public Map<String, List<String>> getMemberProjectMappings() {
        Map<String,List<String>> memberProjectMap = new HashMap<>();
        List<Member> members = memberRepository.findAll();
        members.forEach((member) -> {
            List<String> projectIds = member.getProjectId();
            List<String> projectNames = new ArrayList<>(Collections.emptyList());
            projectIds.forEach((prId) -> {
                Optional<Project> projectEntity = projectRepository.findById(prId);
                if (projectEntity.isPresent()) {
                    String projName = projectEntity.get().getName();
                    projectNames.add(projName);
                } else
                    throw new EntityNotFoundException(404L, Project.class);
            });
            memberProjectMap.put(member.getName(), projectNames);
        });

        return memberProjectMap;
    }

    private void addMemberToExistingProject(Project project, Member member, String memberId) {
        if (project.getMemberIds() == null || project.getMemberIds().isEmpty()) {
            project.setMemberIds(Collections.emptyList());
        }
        if(member.getProjectId() == null || member.getProjectId().isEmpty()) {
            member.setProjectId(Collections.emptyList());
        }
        updateMemberDetails(member, project.getProjectId(), "ADD");
        updateProjectDetails(memberId, project, "ADD");
    }
    
    private void updateMemberDetails(Member member, String projectId, String action) {
        List<String> projectIds = new ArrayList<>(member.getProjectId());
        if(action.equals("ADD")){
            projectIds.add(projectId);
        }
        else {
            projectIds.removeIf(projId -> projId.equals(projectId));
        }
        member.setProjectId(projectIds);
        memberRepository.save(member);
    }

    private void updateProjectDetails(String memberId, Project project, String action) {
        List<String> memberIds = new ArrayList<>(project.getMemberIds());
        if(action.equals("ADD")) {
            memberIds.add(memberId);
        }
        else {
            memberIds.removeIf(tempMember -> tempMember.equals(memberId));
        }
        project.setMemberIds(memberIds);
        projectRepository.save(project);
    }
    private Member configureMember(Member member) {
        if(member.getUserImgUrl().equals("null") || member.getUserImgUrl() == "") {
            member.setUserImgUrl(FireStoreConstants.defaultUserImgUrl);
        }
        return member;
    }


}
