package kg.attractor.xfood.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static kg.attractor.xfood.enums.Role.ADMIN;
import static kg.attractor.xfood.enums.Role.EXPERT;
import static kg.attractor.xfood.enums.Role.SUPERVISOR;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
	
	private static final String[] WHITE_LIST_URL = {
			"/auth/**",
			"/DataTables/**",
			"/js/validation.js",
			"/css/layout.css",
			"/api/appeal/**",
			"/checks/**",
			"/api/oauth/**",
			"/api/checklist/criteria/percentage/**",
			"/api/checks/points/**",
			"/appeals/**",
			"/",
			"/api/comments/**"
			
	};
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer :: disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
				.httpBasic(Customizer.withDefaults())
				.formLogin(form -> form
						.loginPage("/auth/login")
						.loginProcessingUrl("/auth/login")
						.defaultSuccessUrl("/checks")
						.failureUrl("/auth/login?error=true")
						.permitAll())
				.logout(logout -> logout
						.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
						.permitAll())
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(WHITE_LIST_URL).permitAll()
						.anyRequest().authenticated()
				);
		return http.build();
	}
	
	@Bean
	static RoleHierarchy roleHierarchy() {
		return RoleHierarchyImpl.withDefaultRolePrefix()
				.role(ADMIN.toString()).implies(SUPERVISOR.toString())
				.role(SUPERVISOR.toString()).implies(EXPERT.toString())
				.build();
	}
	
	@Bean
	static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
		DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
		expressionHandler.setRoleHierarchy(roleHierarchy);
		return expressionHandler;
	}
}
