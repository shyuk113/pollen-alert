package com.pollenalert.backend.member.service;

import com.pollenalert.backend.member.domain.AllergySetting;
import com.pollenalert.backend.member.domain.User;
import com.pollenalert.backend.member.dto.AllergyRequestDto;
import com.pollenalert.backend.member.dto.AllergyResponseDto;
import com.pollenalert.backend.member.dto.MemberResponseDto;
import com.pollenalert.backend.member.dto.MemberUpdateRequestDto;
import com.pollenalert.backend.member.repository.AllergySettingRepository;
import com.pollenalert.backend.member.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final UserRepository userRepository;
    private final AllergySettingRepository allergySettingRepository;

    //유저 정보 조회
    @Transactional(readOnly = true)
    public MemberResponseDto getMember(Long userId, Long requesterId){
        validateAccess(userId, requesterId);
        User user = findUser(userId);
        return MemberResponseDto.from(user);
    }

    //유저 정보 수정
    @Transactional
    public MemberResponseDto updateMember(Long userId, Long requesterId, MemberUpdateRequestDto request){
        validateAccess(userId, requesterId);
        User user = findUser(userId);
        user.updateProfile(request.name(),request.region());
        return MemberResponseDto.from(user);
    }

    //유저 탈퇴
    @Transactional
    public void deleteMember(Long userId, Long requesterId){
        validateAccess(userId,requesterId);
        User user = findUser(userId);
        userRepository.delete(user);
    }

    //알러지 설정 저장
    @Transactional
    public AllergyResponseDto saveAllergy(Long userId, Long requesterId, AllergyRequestDto request){
        validateAccess(userId, requesterId);
        User user = findUser(userId);
        String types = String.join(",", request.types());

        AllergySetting setting = allergySettingRepository.findByUser_id(userId).orElse(null);

        if (setting ==null) {
            setting = AllergySetting.create(user, request.hasPollenAllergy(), types);
            allergySettingRepository.save(setting);
        } else {
            setting.update(request.hasPollenAllergy(), types);
        }

        return AllergyResponseDto.from(setting);
    }

    //알러지 설정 조회
    @Transactional(readOnly = true)
    public AllergyResponseDto getAllergy(Long userId, Long requesterId){
        validateAccess(userId,requesterId);
        AllergySetting setting = allergySettingRepository.findByUser_id(userId).orElseThrow(()->new IllegalArgumentException("알러지 설정이 없습니다."));
        return AllergyResponseDto.from(setting);
    }

    //본인의 정보만 접근 허용을 위한 유효성 검증
    private void validateAccess(Long userId, Long requesterId){
        if(!userId.equals(requesterId)){
            throw new IllegalArgumentException("본인의 정보만 접근할 수 있습니다");
        }
    }

    //반복 되는 코드 줄이기 위해 findUser 메서드 추가
    private User findUser(Long userId){
        return userRepository.findById(userId).orElseThrow(()->new IllegalArgumentException("존재하지 않는 유저입니다."));
    }
}

