package com.data.proman.controller;

import com.data.proman.enitity.Member;
import com.data.proman.service.MemberService;
import com.data.proman.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private ProjectService projectService;

    @PostMapping("/api/{projectId}/addMember")
    public ResponseEntity<Map<String, Object>> addMemberToProject(@RequestBody Map<String,String> payloadObject,
                                                                  @PathVariable String projectId) {
        String memberId = payloadObject.get("memberId");
        memberService.addMemberToProject(projectId, memberId);
        Map<String, Object> response = createResponseObject(memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/api/addMember")
    public ResponseEntity<Object> addMember(@RequestBody Member member) {
        if(memberService.isExistingMember(member)){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Member already exists");
        }
        String memberId = memberService.addMember(member);
        Map<String, Object> response = createResponseObject(memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/{projectId}/getMembers")
    public ResponseEntity<List<Member>> getProjectMembers(@PathVariable String projectId){
        List<Member> members = memberService.getProjectMembers(projectId);
        return new ResponseEntity<>(members, HttpStatus.OK);
    }

    @GetMapping("/api/getAllMembers")
    public ResponseEntity<List<Member>> getAllMembers(){
        List<Member> members = memberService.getAllMembers();
        return new ResponseEntity<>(members, HttpStatus.OK);
    }

    @GetMapping("/api/{memberId}/getMember")
    public ResponseEntity<Member> getMember(@PathVariable String memberId) {
        Member member = memberService.getMember(memberId);
        return new ResponseEntity<>(member, HttpStatus.OK);
    }

    @PostMapping("/api/{memberId}/updateMember")
    public ResponseEntity<Member> updateMember(@RequestBody Member member, @PathVariable String memberId) {
        Member memberDetails = memberService.updateMember(member, memberId);
        return new ResponseEntity<>(memberDetails, HttpStatus.OK);
    }

    @PostMapping("api/{projectId}/removeMember")
    public ResponseEntity<HttpStatus>removeMemberFromProject(@PathVariable String projectId
            , @RequestBody Map<String,String> payloadObject) {
        memberService.removeMemberFromProject(projectId,payloadObject.get("memberId"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("api/removeMember")
    public ResponseEntity<HttpStatus>removeMember(@RequestBody Map<String,String> payloadObject) {
        memberService.removeMember(payloadObject.get("memberId"));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/api/getProjectsByMember")
    public ResponseEntity<Map<String,List<String>>> getProjectsByMember() {
        Map<String,List<String>> memberProjectMappings = memberService.getMemberProjectMappings();
        return new ResponseEntity<>(memberProjectMappings, HttpStatus.OK);
    }


    private Map<String, Object> createResponseObject(String memberId) {
        Map<String, Object> response = new HashMap<>();
        response.put("memberId", memberId);
        response.put("status", "added");
        return  response;
    }
}
