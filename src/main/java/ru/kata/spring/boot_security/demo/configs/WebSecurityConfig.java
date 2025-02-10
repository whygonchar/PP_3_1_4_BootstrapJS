package ru.kata.spring.boot_security.demo.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.service.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final SuccessUserHandler successUserHandler;
    private final UserService userService;

    public WebSecurityConfig(SuccessUserHandler successUserHandler, UserService userService) {
        this.successUserHandler = successUserHandler;
        this.userService = userService;
    }
//    Построчный разбор:
//.authorizeRequests()
//    Что делает: Включает настройку правил авторизации для HTTP-запросов.
//            Зачем: Без этого Spring Security не будет проверять права доступа к URL.

//2. Правила для /admin/**
// Указывает URL-шаблон: все пути, начинающиеся с /admin/ (например, /admin/dashboard, /admin/users).
// .hasAnyAuthority("ROLE_ADMIN")
// Требует, чтобы у пользователя была роль ADMIN (авторитет ROLE_ADMIN).
// Итог: Только пользователи с ролью ADMIN могут получить доступ к /admin/**.
//
// 3. Правила для /user/**
// URL-шаблон: все пути, начинающиеся с /user/.
// .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
// Разрешает доступ пользователям с ролью USER или ADMIN.
// Итог: Обычные пользователи (USER) и администраторы (ADMIN) могут получить доступ к /user/**.
//
// 4. Публичные пути
// Указывает корневой путь (/) и /login.
// .permitAll()
// Разрешает доступ всем (даже неаутентифицированным пользователям).
// Итог: Любой может зайти на главную страницу (/) или страницу входа (/login).
//
// 5. Все остальные запросы
// .anyRequest().authenticated()
// .anyRequest()
// Все остальные URL-адреса, не указанные выше.
// .authenticated()
// Требует, чтобы пользователь был аутентифицирован (вошел в систему), но не проверяет роли.
// Итог: Для любых других путей (например, /profile, /settings) пользователь должен быть залогинен.
//
// 6. Настройка формы входа
// .formLogin().successHandler(successUserHandler).permitAll()
// .formLogin()
// Включает стандартную HTML-форму для входа (логин/пароль).
// .successHandler(successUserHandler)
// Указывает кастомный обработчик успешного входа. Например:
// Перенаправление на разные страницы в зависимости от роли (админа → /admin, пользователя → /user).
//
//
// 7. Настройка выхода
// .logout().permitAll()
// .logout()
// Включает стандартный обработчик выхода из системы (по URL /logout).
//
// .permitAll()
// Разрешает выход всем аутентифицированным пользователям.
//
// Как это работает на примере:
// Пользователь заходит на /admin/dashboard
// Spring Security проверяет, есть ли у него роль ADMIN.
// Если нет → перенаправляет на страницу входа /login.
// Пользователь заходит на /user/profile
// Проверяет, есть ли роль USER или ADMIN.
// Если нет → перенаправляет на /login.
// Неаутентифицированный пользователь заходит на /login
// Разрешено, так как .permitAll().
// После входа
// successUserHandler решает, куда перенаправить (например, админа → /admin, пользователя → /user).
//
// Важные нюансы:
// Порядок правил важен!
// Spring Security проверяет правила сверху вниз. Например, если поменять местами .anyRequest().authenticated() и .antMatchers("/admin/**")..., правило для /admin/** не будет работать.
// Роли (Authority) vs. Права доступа
// hasAnyAuthority("ROLE_ADMIN") проверяет конкретный авторитет (например, ROLE_ADMIN).
// hasRole("ADMIN") делает то же самое, но автоматически добавляет префикс ROLE_.
// successUserHandler
// Это кастомный класс, реализующий интерфейс AuthenticationSuccessHandler.

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/", "/admin/**").hasRole("ADMIN")
                .antMatchers("/","/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
                .and()
                .formLogin().successHandler(successUserHandler)
                .permitAll()
                .and()
                .logout()
                .permitAll();
    }

//    Что делает:
//    Создает бин (компонент), который будет использоваться для шифрования паролей.
//    BCryptPasswordEncoder — это конкретная реализация, которая использует алгоритм BCrypt. Этот алгоритм:
//    Автоматически генерирует "соль" (дополнительная рандомизация для защиты от атак)
//    Устойчив к взлому (медленный алгоритм + соль усложняют подбор паролей)
//    Зачем нужен:
//    При регистрации пользователя: твой пароль хешируется и сохраняется в БД в виде "мусора" (например $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy)
//    При входе в систему: введенный пароль кодируется этим же алгоритмом и сравнивается с хешем из БД

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    Что делает:
//    Создает провайдер аутентификации, который будет связывать три ключевые вещи:
//    -Источник данных о пользователях (откуда брать логины/пароли/роли)
//    -Способ проверки паролей (как сравнивать введенный пароль с хранимым хешем)
//    -Механизм аутентификации (как вообще проводить проверку)
//
//    Построчно:
//    DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//    Создаем объект провайдера аутентификации.
//            daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
//    Указываем, какой кодировщик паролей использовать (наш бин из первого метода).
//            daoAuthenticationProvider.setUserDetailsService((UserDetailsService) userService);
//    Указываем сервис, который умеет загружать данные пользователей. Здесь:
//    UserDetailsService — стандартный интерфейс Spring Security
//    userService — твой кастомный сервис пользователей, который должен реализовывать этот интерфейс (иметь метод loadUserByUsername())
//
//    Зачем нужен:
//    Это "мозг" аутентификации. Когда пользователь вводит логин/пароль:
//    Ищет пользователя через UserDetailsService (в твоем случае — через userService)
//    Проверяет пароль через PasswordEncoder
//    Если всё совпадает — пускает в систему

//    Как это работает вместе?
//    Пользователь вводит логин/пароль
//    Spring Security через DaoAuthenticationProvider:
//    Берет твой userService, чтобы найти пользователя по логину
//    Берет BCryptPasswordEncoder, чтобы сравнить введенный пароль с хешем из БД
//    Если проверки пройдены — пользователь аутентифицирован
//
//    Почему именно так?
//    Разделение ответственности:
//    -UserService знает, где и как хранятся пользователи
//    PasswordEncoder знает, как работать с паролями
//    -DaoAuthenticationProvider объединяет их в рабочий механизм
//    -Гибкость: Можешь легко поменять кодировщик паролей или источник данных, не переписывая логику аутентификации.
//
@Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider daoauthenticationProvider = new DaoAuthenticationProvider();
        daoauthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoauthenticationProvider.setUserDetailsService((UserDetailsService) userService);
        return daoauthenticationProvider;
    }
    // аутентификация inMemory
//    @Bean
//    @Override
//    public UserDetailsService userDetailsService() {
//        UserDetails user =
//                User.withDefaultPasswordEncoder()
//                        .username("user")
//                        .password("user")
//                        .roles("USER")
//                        .build();
//
//        return new InMemoryUserDetailsManager(user);
//    }
}