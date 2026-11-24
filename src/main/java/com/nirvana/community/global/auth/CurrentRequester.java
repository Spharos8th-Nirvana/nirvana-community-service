package com.nirvana.community.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 컨트롤러의 Requester 파라미터에 붙인다.
// required = false는 비회원도 볼 수 있는 조회용이다. 인증 헤더가 둘 다 없으면 파라미터에 null이 들어온다.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentRequester {

	boolean required() default true;
}
