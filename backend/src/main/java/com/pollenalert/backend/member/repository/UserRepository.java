package com.pollenalert.backend.member.repository;

import com.pollenalert.backend.member.domain.User;
import com.pollenalert.backend.member.domain.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    //이메일로 조회
    Optional<User> findByEmail(String email);

    //소셜 아이디로 조회
    Optional<User> findByProviderAndProvider_id(Provider provider, String providerId);

    //이메일 중복 체크
    boolean existsByEmail(String email);
}
