package com.example.Dividend.model;

import com.example.Dividend.persist.entity.MemberEntity;
import lombok.Data;

import java.util.List;

public class Auth {

    @Data
    public static class SingIn {
        private String username;
        private String password;
    }

    @Data
    public static class SingUp {
        private String username;
        private String password;
        private List<String> roles;

        // SingUp 클래스의 내용을 MemberEntity로 바꾸는 메소드
        public MemberEntity toEntity() {
            return MemberEntity.builder()
                    .username(this.username)
                    .password(this.password)
                    .roles(this.roles)
                    .build();
        }
    }
}
