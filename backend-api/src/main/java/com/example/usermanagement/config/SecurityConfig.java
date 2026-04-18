package com.example.usermanagement.config;

import com.example.usermanagement.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security配置类
 * Spring_Security_peiZhi_lei
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码编码器
     * miMa_bianMa_qi
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置
     * anQuan_guoLv_qi_lian_peiZhi
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（因为使用JWT）
            .csrf(csrf -> csrf.disable())
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理为无状态
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置授权规则
            .authorizeRequests(authz -> authz
                // 允许认证相关的端点
                .antMatchers("/api/auth/login", "/api/auth/register").permitAll()
                // 允许健康检查端点
                .antMatchers("/actuator/health", "/health").permitAll()
                // 允许错误页面
                .antMatchers("/error").permitAll()
                // 允许AI服务接口
                .antMatchers("/api/ai/**").permitAll()
                // 允许会话历史接口
                .antMatchers("/api/conversations/**").permitAll()
                // 允许食物识别接口
                .antMatchers("/api/food/**").permitAll()
                // 允许食品营养数据库查询接口（供AI服务调用）
                .antMatchers("/api/food-nutrition/**").permitAll()
                // 允许访问上传的图片（餐食图片等静态资源）
                .antMatchers("/uploads/**").permitAll()
                .antMatchers("/api/meals/**").permitAll()
                .antMatchers("/api/drinks/**").permitAll()
                .antMatchers("/api/health-records/**").permitAll()
                .antMatchers("/api/profile/**").permitAll()
                .antMatchers("/api/user/**").permitAll()
                .antMatchers("/api/drink-preferences/**").permitAll()
                .antMatchers("/api/notifications/**").permitAll()
                .antMatchers("/api/notification-settings/**").permitAll()
                .antMatchers("/api/reports/**").permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            
            // 添加JWT认证过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS配置
     * CORS_peiZhi
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许所有来源（生产环境中应该限制具体域名）
        configuration.setAllowedOriginPatterns(List.of("*"));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许发送Cookie
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
