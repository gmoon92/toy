package com.gmoon.springsecuritywhiteship.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.PARAMETER)
@AuthenticationPrincipal(expression = " #this == 'anonymousUser' ? null : account")
public @interface CurrentUser {

}
