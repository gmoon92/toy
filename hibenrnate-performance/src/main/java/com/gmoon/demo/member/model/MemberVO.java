package com.gmoon.demo.member.model;

import com.querydsl.core.annotations.QueryProjection;

public class MemberVO {

    public static class Data {

        private Long id;

        private String name;

        private boolean enabled;

        @QueryProjection
        public Data(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        @QueryProjection
        public Data(Long id, String name, boolean enabled) {
            this.id = id;
            this.name = name;
            this.enabled = enabled;
        }
    }

}
