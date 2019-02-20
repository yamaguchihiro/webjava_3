package jp.co.systena.tigerscave.ShoppingSite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, JdbcTemplate jdbcTemplate) throws Exception {
        // アカウントの設定
        auth.inMemoryAuthentication().withUser("admin").password("{noop}password").roles("ADMIN");
        auth.inMemoryAuthentication().withUser("user1").password("{noop}password").roles("USER");

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        auth.jdbcAuthentication().dataSource(jdbcTemplate.getDataSource()).passwordEncoder(passwordEncoder);

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/ShoppingSite/**").authenticated().anyRequest().permitAll();

        // ログイン
        http.formLogin().loginPage("/login").usernameParameter("username").passwordParameter("password")
                .loginProcessingUrl("/loginProcess").defaultSuccessUrl("/ShoppingSite/ListView").failureUrl("/login?error");

        // ログアウト
        http.logout().logoutUrl("/logout").logoutSuccessUrl("/login");
    }
}