package com.nirvana.community.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 일반 회원(USER)만 부를 수 있는 컨트롤러나 메서드에 붙인다. UserOnlyInterceptor가 Service 호출 전에 확인한다.
// 예: 좋아요는 USER만 누른다(COM-LIK-01). 사찰 운영자·관리자는 게시판 열람 권한이 있어도 403이다.
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserOnly {
}
