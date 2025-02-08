package com.example.Dividend.service;

import com.example.Dividend.exception.impl.AlreadyExistUserException;
import com.example.Dividend.model.Auth;
import com.example.Dividend.persist.MemberRepository;
import com.example.Dividend.persist.entity.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.memberRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("couldn't find user -> " + username));
    }

    // 회원가입
    public MemberEntity register(Auth.SingUp member) {
        boolean exists = this.memberRepository.existsByUsername(member.getUsername());
        if (exists) {
            throw new AlreadyExistUserException();
        }

        member.setPassword(this.passwordEncoder.encode(member.getPassword())); // 인코딩 된 패스워드로 set 설정
        var result = this.memberRepository.save(member.toEntity());
        return result;
    }

    // 로그인 시 검증
    public MemberEntity authenticate(Auth.SingIn member) {

        // ID와 Password가 일치하는지 확인
        var user = this.memberRepository.findByUsername(member.getUsername())
                .orElseThrow(()-> new RuntimeException("존재하지 않는 ID 입니다. "));

        // 입력받은 비밀번호가 인코딩 된 비밀번호와 일치하는지 확인하기 위한 작업
        if (!this.passwordEncoder.matches(member.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }
}