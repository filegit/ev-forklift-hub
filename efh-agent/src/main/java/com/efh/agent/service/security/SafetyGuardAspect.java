package com.efh.agent.service.security;

import com.efh.agent.vo.ChatRequestVO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SafetyGuardAspect {

    @Autowired
    private PromptSafetyService promptSafetyService;

    @Before("@annotation(com.efh.agent.service.security.RequireSafety) || @within(com.efh.agent.service.security.RequireSafety)")
    public void guard(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof ChatRequestVO) {
                promptSafetyService.validateUserInput(((ChatRequestVO) arg).getQuestion());
            }
        }
    }
}
