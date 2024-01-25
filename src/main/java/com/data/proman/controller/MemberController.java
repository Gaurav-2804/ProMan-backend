package com.data.proman.controller;

import com.data.proman.enitity.Member;
import com.data.proman.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MemberController {
    @Autowired
    private MemberService memberService;

    @PostMapping("/api/{projectId}/addmember")
    public ResponseEntity<HttpStatus> createProject(@RequestBody Member member, @PathVariable String projectId) {
        memberService.addMember(projectId,member);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
