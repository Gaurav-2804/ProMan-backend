package com.data.proman.controller;

import com.data.proman.enitity.Member;
import com.data.proman.service.MemberService;
import com.data.proman.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private ProjectService projectService;

    @PostMapping("/api/{projectId}/addmember")
    public ResponseEntity<HttpStatus> createProject(@RequestBody Member member, @PathVariable String projectId) {
        memberService.addMember(projectId,member);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/api/{projectId}/getMembers")
    public ResponseEntity<List<Member>> getMembers(@PathVariable String projectId){
        List<Member> members = memberService.getMembers(projectId);
        return new ResponseEntity<>(members, HttpStatus.OK);
    }
}
