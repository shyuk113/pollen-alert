package com.pollenalert.backend.member.controller;

import com.pollenalert.backend.member.dto.AllergyRequestDto;
import com.pollenalert.backend.member.dto.AllergyResponseDto;
import com.pollenalert.backend.member.dto.MemberResponseDto;
import com.pollenalert.backend.member.dto.MemberUpdateRequestDto;
import com.pollenalert.backend.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    //유저 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponseDto> getMember(@PathVariable Long id, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(memberService.getMember(id,userId));
    }

    //유저 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<MemberResponseDto> updateMember(@PathVariable Long id, @AuthenticationPrincipal Long userId, @RequestBody MemberUpdateRequestDto request){
        return ResponseEntity.ok(memberService.updateMember(id,userId,request));
    }

    //유저 탈퇴
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id, @AuthenticationPrincipal Long userId){
        memberService.deleteMember(id,userId);
        return ResponseEntity.noContent().build();
    }

    //알러지 설정 조회
    @GetMapping("/{id}/allergy")
    public ResponseEntity<AllergyResponseDto> getAllergy(@PathVariable Long id, @AuthenticationPrincipal Long userId){
        return ResponseEntity.ok(memberService.getAllergy(id,userId));
    }

    //알러지 설정 등록 및 수정
    @PostMapping("/{id}/allergy")
    public ResponseEntity<AllergyResponseDto> updateAllergy(@PathVariable Long id, @AuthenticationPrincipal Long userId, @RequestBody AllergyRequestDto request){
        return ResponseEntity.ok(memberService.saveAllergy(id,userId,request));
    }
}
