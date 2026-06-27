package com.campuslink.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置：无状态 JWT 鉴权 + 角色权限控制。
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthHandlers.RestAuthEntryPoint authEntryPoint;
    private final RestAuthHandlers.RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 放行登录注册与找回密码
                        .requestMatchers("/auth/register", "/auth/login",
                                "/auth/password/forget", "/auth/password/reset").permitAll()
                        // 放行预检请求
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 竞赛 v1 写操作仅管理员（精确匹配，不影响 v2 新增路径）
                        .requestMatchers(HttpMethod.POST, "/competition").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/competition/{id:\\d+}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/competition/{id:\\d+}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/competition/register/*/audit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/competition/*/news").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/competition/*/attachments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/competition/attachment/*").hasRole("ADMIN")
                        // 管理后台仅管理员
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // 其余均需登录
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
