package com.site.webapp.service;

import com.site.webapp.dto.RegistrationDto;
import com.site.webapp.models.Role;
import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.repo.RoleRepository;
import com.site.webapp.repo.UserRepository;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, FileStorageService fileStorageService){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public String registerNewUser(RegistrationDto registrationDto) {
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            return "passwordError";
        }
        if (userRepository.findByEmail(registrationDto.getEmail()) != null) {
            log.warn("Регистрация невозможна: email {} уже существует", registrationDto.getEmail());
            return "emailError";
        }
        try{
            User user= new User();

            user.setFirstName(registrationDto.getFirstName());
            user.setLastName(registrationDto.getLastName());
            user.setEmail(registrationDto.getEmail());
            user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

            Role employeeRole = roleRepository.findById(1L).orElseThrow(() -> new RuntimeException("Роль EMPLOYEE не найдена"));
            user.setRoles(Collections.singleton(employeeRole));

            userRepository.save(user);
            log.info("Пользователь {} успешно зарегистрирован", user.getEmail());
            return "success";
        }catch (Exception e){
            log.error("Ошибка при регистрации пользователя {}: {}", registrationDto.getEmail(), e.getMessage(), e);
            return "emailError";
        }

    }


    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException{
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

    @Transactional(readOnly = true)
    public List<Task> getFavoriteTasksForUser(String email) {
        log.info("Взятие избранных задач для пользователя с email: {}", email);

        User user = userRepository.findByEmail(email);
        if (user == null) return new ArrayList<>();

        return new ArrayList<>(user.getFavouriteTasks());
    }


    @Transactional
    public void updateUserInfo(Long userId,String firstName,String lastName, Long version){
        log.info("Обновление данных для пользователя ID: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("Пользователь не найден"));
        if(user != null){
            log.debug("Старые данные: {} {}", user.getFirstName(), user.getLastName());
            if (version != null) {
                user.setVersion(version);
            }
            user.setFirstName(firstName);
            user.setLastName(lastName);
            userRepository.save(user);
        }
        else{
            log.warn("Пользователь ID {} не найден", userId);
        }
    }

    @Transactional
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

    @Transactional
    public void updateUserRoles(Long userId, List<Long> roleIds,Long version) {
        log.info("Обновление ролей для пользователя ID: {}", userId);

        User user = userRepository.findById(userId).orElseThrow(()->new RuntimeException("Пользователь не найден"));
        if (version != null) {
            user.setVersion(version);
        }
        Set<Role> newRoles = new HashSet<>();
        if (roleIds != null && !roleIds.isEmpty()){
            for(Long roleId : roleIds){
                Role role = roleRepository.findById(roleId).orElse(null);
                if(role != null){
                    newRoles.add(role);
                }
            }
        }
        log.debug("Пользователь {}: роли изменены с {} на {}", user.getEmail(), user.getRoles(), newRoles);
        user.setRoles(newRoles);
        userRepository.save(user);
        log.info("Роли пользователя ID {} обновлены", userId);
    }


    @Value("${app.upload.dir}")
    private String uploadPath;

    @Transactional
    public void updateAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        if (user.getAvatarS3Key() != null) {
            try {
                fileStorageService.deleteFile(user.getAvatarS3Key());
            } catch (Exception e) {
                log.warn("Не удалось удалить старую аватарку пользователя ID {}: {}", userId, e.getMessage());
            }
        }
        String s3Key = fileStorageService.uploadFile(file, "avatars");
        user.setOriginalAvatarFileName(file.getOriginalFilename());
        user.setAvatarS3Key(s3Key);
        userRepository.save(user);
        log.info("Аватарка для пользователя ID {} успешно обновлена в S3: {}", userId, s3Key);
    }


    public boolean deleteUser(Long userId,Long currentAdminId,String adminEmail){
        log.info("Удаление пользователя с ID: {} админом: {}", userId, adminEmail);

        if (userId.equals(currentAdminId)) {
            log.warn("Блокировка: админ {} пытался удалить сам себя", adminEmail);
            return false;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Удаление не удалось: пользователь с ID {} не существует", userId);
            return false;
        }
        try {
            userRepository.delete(user);
            log.info("Пользователь {} (ID: {}) успешно удален админом {}", user.getEmail(), userId, adminEmail);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при удалении пользователя ID {}: {}", userId, e.getMessage());
            return false;
        }
    }
}
