package com.campuslink.aspect;

import com.campuslink.entity.OperationLog;
import com.campuslink.mapper.OperationLogMapper;
import com.campuslink.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * 操作日志切面，v2 新增。
 *
 * <p>拦截所有 Controller 的 {@code POST/PUT/DELETE} 请求，写 {@code operation_log} 表。
 * 入参中包含 password / code 等敏感字段会做 mask，文件参数会被忽略。
 * 日志写入采用 {@link Async} 避免影响主流程。
 *
 * @author liuguangyuan
 * @since 2026/6/27
 */
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationLogAspect.class);

    private final OperationLogMapper operationLogMapper;

    @Around("execution(* com.campuslink.controller..*Controller.*(..)) " +
            "&& (@annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public Object aroundWriteOps(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();//开始时间
        Throwable error = null;//异常
        Object result = null;//返回结果
        try {
            result = pjp.proceed();
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            long cost = System.currentTimeMillis() - start;//耗时
            try {
                writeLog(pjp, cost, error);
            } catch (Exception e) {
                LOGGER.warn("写操作日志失败", e);
            }
        }
        return result;
    }

    @Async
    protected void writeLog(ProceedingJoinPoint pjp, long cost, Throwable error) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();//当前请求
        String method = request.getMethod();//HTTP 方法
        String uri = request.getRequestURI();//请求路径
        String ip = request.getRemoteAddr();//IP

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;//操作人 id
        String username = null;//操作人账号
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            userId = loginUser.getUserId();
            username = loginUser.getUsername();
        }

        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setMethod(method);
        log.setPath(uri);
        log.setIp(ip);
        log.setCostMs((int) cost);
        log.setParams(buildParams(pjp.getArgs()));

        if (error == null) {//成功
            log.setStatus("SUCCESS");
        } else {//失败
            log.setStatus("FAIL");
            String msg = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
            log.setErrorMsg(msg.length() > 480 ? msg.substring(0, 480) : msg);
            LOGGER.warn("操作执行失败, path={}, error={}", uri, msg);
        }

        operationLogMapper.insert(log);
    }

    private String buildParams(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object arg : args) {
            if (arg instanceof MultipartFile multipartFile) {
                sb.append(first ? "" : ",").append("<file:").append(multipartFile.getOriginalFilename()).append(">");
            } else if (arg instanceof jakarta.servlet.http.HttpServletRequest
                    || arg instanceof jakarta.servlet.http.HttpServletResponse) {
                continue;
            } else {
                String text = arg == null ? "null" : maskSensitive(arg.toString());
                if (text.length() > 200) {
                    text = text.substring(0, 200) + "...";
                }
                sb.append(first ? "" : ",").append(text);
            }
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private String maskSensitive(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("(?i)(password|verifyCode|code)=([^,)]+)", "$1=***");
    }

    /**
     * 兼容入参为 Object[] 直接打印的调试场景。
     */
    public static String debugArgs(Object[] args) {
        return Arrays.toString(args);
    }
}
