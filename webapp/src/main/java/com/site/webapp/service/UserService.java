package com.site.webapp.service;

import com.site.webapp.config.EncoderConfig;
import com.site.webapp.models.Role;
import com.site.webapp.models.Tasks;
import com.site.webapp.models.User;
import com.site.webapp.repo.RoleRepository;
import com.site.webapp.repo.TasksRepository;
import com.site.webapp.repo.UserRepository;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @PersistenceContext
    private EntityManager em;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TasksRepository tasksRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,RoleRepository roleRepository,TasksRepository tasksRepository,PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tasksRepository = tasksRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        log.info("Попытка входа пользователя с email: {}", email);
        User user = userRepository.findByEmail(email);
        if (user == null){
            log.warn("Пользователь с email {} не найден",email);
            throw new UsernameNotFoundException("User not found" + email);
        }
        log.info("Пользователь {} найден, ID: {}, роли: {}",email, user.getId(), user.getRoles());
        return user;
    }

    public User findUserById(Long userId){
        log.debug("Поиск пользователя по ID: {}", userId);
        Optional<User> userFromDb = userRepository.findById(userId);
        if (userFromDb.isPresent()) {
            log.debug("Пользователь найден: {}", userFromDb.get().getEmail());
            return userFromDb.get();
        } else {
            log.debug("Пользователь с ID {} не найден", userId);
            return null;
        }
    }
    public User findByEmail(String email) {
        log.debug("Поиск пользователя по email: {}", email);
        return userRepository.findByEmail(email);
    }
    public List<User> allUsers(){
        log.debug("Запрос списка всех пользователей");
        List<User> users = userRepository.findAll();
        log.debug("Найдено {} пользователей", users.size());
        return users;
    }
    public List<Role> getAllRoles() {
        log.debug("Запрос списка всех ролей");
        return roleRepository.findAll();
    }

    public boolean saveUser(User user){
        log.info("Попытка регистрации с email: {}", user.getEmail());

        User userFromDB = userRepository.findByEmail(user.getEmail());
        if (userFromDB != null){
            log.warn("Регистрация невозможна email {} уже существует",user.getEmail());
            return false;
        }
        try{
            Role employeeRole = roleRepository.findById(1L).orElseThrow(() -> new RuntimeException("Роль EMPLOYEE не найдена"));
            user.setRoles(Collections.singleton(employeeRole));
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            log.info("Пользователь {} успешно зарегистрирован с ID: {}",user.getEmail(), user.getId());
            return true;
        }catch (Exception e){
            log.error("Ошибка при регистрации пользователя {}",e.getMessage(),e);
            return false;
        }
    }
    public boolean deleteUser(Long userId){
        log.info("Удаление пользователя с ID: {}", userId);
        if (userRepository.findById(userId).isPresent()){
            userRepository.deleteById(userId);
            log.info("Пользователь ID {} успешно удален", userId);
            return true;
        }
        log.warn("Пользователь ID {} не найден", userId);
        return false;
    }
    @Transactional(readOnly = true)
    public List<Tasks> getFavoriteTasksForUser(String email) {
        log.info("Взятие избранных задач для пользователя с email: {}", email);

        User user = userRepository.findByEmail(email);
        if (user == null) return new ArrayList<>();

        return new ArrayList<>(user.getFavouriteTasks());
    }
    @Transactional
    public void toggleFavorite(String email, Long taskId) {
        log.info("Переключение избранного для пользователя {} и задачи {}", email, taskId);

        User user = userRepository.findByEmail(email);
        if (user == null) throw new UsernameNotFoundException("Пользователь не найден");

        Tasks task = tasksRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));
        if (user.getFavouriteTasks().contains(task)) {
            user.getFavouriteTasks().remove(task);
            log.info("Задача удалена из избранного");
        } else {
            user.getFavouriteTasks().add(task);
            log.info("Задача добавлена в избранное");
        }
    }
    public void updateUserInfo(Long userId,String firstName,String lastName){
        log.info("Обновление данных для пользователя ID: {}", userId);
        User user = findUserById(userId);
        if(user != null){
            log.debug("Старые данные: {} {}", user.getFirstName(), user.getLastName());
            user.setFirstName(firstName);
            user.setLastName(lastName);
            userRepository.save(user);
        }
        else{
            log.warn("Пользователь ID {} не найден", userId);
        }
    }
    public String updatePassword(Long userId, String oldPassword,String newPassword,String confirmPassword){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Неверный старый пароль для пользователя: {}", user.getEmail());
            return "oldPasswordError";
        }
        if (!newPassword.equals(confirmPassword)) {
            log.warn("Новые пароли не совпадают для пользователя {}", user.getEmail());
            return "matchError";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Пароль для пользователя {} успешно обновлен", user.getEmail());
        return "success";
    }
    public void updateUserRoles(Long userId, List<Long> roleIds) {
        log.info("Обновление ролей для пользователя ID: {}", userId);
        log.debug("Новые роли: {}", roleIds);
        User user = findUserById(userId);
        if(user != null){
            Set<Role> newRoles = new HashSet<>();
            if (roleIds != null && !roleIds.isEmpty()){
                for(Long roleId : roleIds){
                    Role role = roleRepository.findById(roleId).orElse(null);
                    if(role != null){
                        newRoles.add(role);
                    }
                }
            }
            log.debug("Старые роли: {}", user.getRoles());
            user.setRoles(newRoles);
            userRepository.save(user);
            log.info("✅ Роли пользователя ID {} обновлены", userId);
        }
        else{
            log.warn("Пользователь ID {} не найден", userId);
        }
    }
}
