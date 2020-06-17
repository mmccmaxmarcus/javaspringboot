package br.com.springboot.securiy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebConfigSecurity extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private ImplementacaoUserDetailService implementacaoUserDetailService;
	
		@Override //Configura as solicitações de acesso por http
		protected void configure(HttpSecurity http) throws Exception {
			http.csrf()
			.disable() //Desabilita as configuracoes de memoria
			.authorizeRequests() // Retringe o acesso
			.antMatchers(HttpMethod.GET,"/").permitAll() //Qualquer usuario pode acessar index
			.antMatchers(HttpMethod.GET,"/cadastropessoa").hasAnyRole("ADMIN") //Da permissão somente a ADMIN
			.anyRequest().authenticated()
			.and().formLogin().permitAll() //Permite qualquer usuario 
			.loginPage("/login")
			.defaultSuccessUrl("/cadastropessoa")
			.failureUrl("/login?error=true")
			.and().logout().logoutSuccessUrl("/login")  //Mapeia URL do logout e invalida usuário autenticado 
			
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
		}
		
		@Override //Cria autenticacao do usuario com banco de dados em memoria
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.userDetailsService(implementacaoUserDetailService)
			.passwordEncoder(new BCryptPasswordEncoder());
			
			/*
				auth.inMemoryAuthentication().passwordEncoder(NoOpPasswordEncoder.getInstance())
				.withUser("Max")
				.password("admin")
				.roles("ADMIN");
				*/
		}
		
		@Override //Ignora URL especifica
		public void configure(WebSecurity web) throws Exception {
		   web.ignoring().antMatchers("/resources/**");
		}
}
