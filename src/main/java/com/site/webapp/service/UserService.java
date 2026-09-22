package com.site.webapp.service;


import com.site.webapp.exception.InvalidPasswordException;
import com.site.webapp.exception.SelfDeleteException;
import com.site.webapp.exception.UserNotFoundException;
import com.site.webapp.exception.RoleNotFoundException;
import com.site.webapp.models.ArchivedUser;
import com.site.webapp.models.Role;
import com.site.webapp.models.Task;
import com.site.webapp.models.User;
import com.site.webapp.repo.ArchivedUserRepository;
import com.site.webapp.repo.RoleRepository;
import com.site.webapp.repo.TaskRepository;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.security.CustomUserDetails;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;
    private final ArchivedUserRepository archivedUserRepository;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       TaskRepository taskRepository,
                       PasswordEncoder passwordEncoder,
                       ArchivedUserRepository archivedUserRepository){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
        this.archivedUserRepository = archivedUserRepository;
    }

    @Transactional
    public User saveRegisteredUser(User user) {
        if(user == null){
            throw new IllegalArgumentException("User cannot be null");
        }
        return userRepository.save(user);
    }


    @Override
    @NonNull
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException{
        log.info("Попытка входа пользователя с email: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(() ->new UserNotFoundException(email));

        if (user == null){
            log.warn("Пользователь с email {} не найден",email);
            throw new UsernameNotFoundException("User not found" + email);
        }

        log.info("Пользователь {} найден, ID: {}, роли: {}",email, user.getId(), user.getRoles());
        return new CustomUserDetails(user);
    }

    @Transactional(readOnly = true)
    public User findUserById(Long userId){
        log.debug("Поиск пользователя по ID: {}", userId);
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    public User findByEmail(String email) {
        log.debug("Поиск пользователя по email: {}", email);
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("Пользователь с email " + email + " не найден"));
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

        User user = userRepository.findByEmail(email).orElseThrow(() ->new UserNotFoundException(email));
        if (user == null){
            return new ArrayList<>();
        }

        return new ArrayList<>(user.getFavouriteTasks());
    }

    public long getActiveTaskCount(Long artistId) {
        if (artistId == null) return 0;
        LocalDateTime limitDate = LocalDateTime.now().plusDays(14);
        return taskRepository.countByArtistIdAndStatusNot(artistId, Task.TaskStatus.COMPLETED);
    }

    @Transactional
    public void updateUserInfo(Long userId,String firstName,String lastName, Long version){
        log.info("Обновление данных для пользователя ID: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(userId));

        log.debug("Старые данные: {} {}", user.getFirstName(), user.getLastName());
        if (version != null) {
            user.setVersion(version);
        }
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userRepository.save(user);
    }

    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword,String confirmPassword){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("Неверный старый пароль для пользователя: {}", user.getEmail());
            throw new InvalidPasswordException("Неверный текущий пароль");
        }

        if (!newPassword.equals(confirmPassword)) {
            log.warn("Новые пароли не совпадают для пользователя {}", user.getEmail());
            throw new InvalidPasswordException("Новый пароль и подтверждение не совпадают");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        log.info("Пароль для пользователя {} успешно обновлен", user.getEmail());
    }

    @Transactional
    public void updateUserRoles(Long userId, List<Long> roleIds,Long version) {
        log.info("Обновление ролей для пользователя ID: {}", userId);

        User user = userRepository.findById(userId).orElseThrow(()->new UserNotFoundException(userId));

        if (version != null && !version.equals(user.getVersion())) {
            log.warn("Конфликт версий при обновлении ролей пользователя ID {}: версия в форме {}, в БД {}", userId, version, user.getVersion());
            throw new ObjectOptimisticLockingFailureException(User.class, userId);
        }

        Set<Role> newRoles = new HashSet<>();

        if (roleIds != null && !roleIds.isEmpty()){
            List<Role> foundRoles = roleRepository.findAllById(roleIds);

            if (foundRoles.size() != new HashSet<>(roleIds).size()) {
                throw new RoleNotFoundException("Некоторые из указанных ролей не существуют");
            }

            newRoles.addAll(foundRoles);
        }
        log.debug("Пользователь {}: роли изменены с {} на {}", user.getEmail(), user.getRoles(), newRoles);

        user.setRoles(newRoles);

        log.info("Роли пользователя ID {} обновлены", userId);
    }


//    @Value("${app.upload.dir}")
//    private String uploadPath;

//    @Transactional
//    public void updateAvatar(Long userId, MultipartFile file) {
//        if (file == null || file.isEmpty()) {
//            return;
//        }
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new UserNotFoundException(userId));
//        if (user.getAvatarS3Key() != null) {
//            try {
//                fileStorageService.deleteFile(user.getAvatarS3Key());
//            } catch (Exception e) {
//                log.warn("Не удалось удалить старую аватарку пользователя ID {}: {}", userId, e.getMessage());
//            }
//        }
//        String s3Key = fileStorageService.uploadFile(file, "avatars");
//        user.setOriginalAvatarFileName(file.getOriginalFilename());
//        user.setAvatarS3Key(s3Key);
//        userRepository.save(user);
//        log.info("Аватарка для пользователя ID {} успешно обновлена в S3: {}", userId, s3Key);
//    }


//    public boolean deleteUser(Long userId,Long currentAdminId,String adminEmail){
//        log.info("Удаление пользователя с ID: {} админом: {}", userId, adminEmail);
//
//        if (userId.equals(currentAdminId)) {
//            log.warn("Блокировка: админ {} пытался удалить сам себя", adminEmail);
//            return false;
//        }
//
//        User user = userRepository.findById(userId).orElse(null);
//        if (user == null) {
//            log.warn("Удаление не удалось: пользователь с ID {} не существует", userId);
//            return false;
//        }
//        try {
//            userRepository.delete(user);
//            log.info("Пользователь {} (ID: {}) успешно удален админом {}", user.getEmail(), userId, adminEmail);
//            return true;
//        } catch (Exception e) {
//            log.error("Ошибка при удалении пользователя ID {}: {}", userId, e.getMessage());
//            return false;
//        }
//    }

    @Transactional
    public void softDeleteUser(Long userId, CustomUserDetails initiator){

        if (initiator == null) {
            throw new IllegalArgumentException("Инициатор действия не может быть null");
        }

        log.info("Удаление пользователя с ID: {} пользователем {}", userId, initiator.getUsername() );

        if(Objects.equals(userId, initiator.getId())){
            log.warn("Пользователь {} с id {} пытался удалить себя", initiator.getUsername(), initiator.getId());
            throw new SelfDeleteException("Нельзя удалить самого себя");

        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if (user.isDeleted()) {
            log.warn("Пользователь ID {} уже деактивирован", userId);
            return;
        }

        user.setDeleted(true);

        log.info("Пользователь {} (ID: {}) успешно удален админом {}", user.getEmail(), userId, initiator.getUsername());
    }

    @Transactional
    public void archiveUser(Long userId, CustomUserDetails initiator){
        if (initiator == null) {
            throw new IllegalArgumentException("Инициатор действия не может быть null");
        }

        log.info("Архивирование пользователя с ID: {} пользователем {}", userId, initiator.getUsername());

        if (Objects.equals(userId,initiator.getId())){
            log.warn("Пользователь {} с id {} пытался удалить себя", initiator.getUsername(), initiator.getId());
            throw new SelfDeleteException("Нельзя удалить самого себя");

        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        ArchivedUser archivedUser = new ArchivedUser(user,initiator.getUsername());
        archivedUserRepository.save(archivedUser);

//        userAvatarService.deleteExistingAvatarFile(user);

        user.setFirstName("Ghost");
        user.setLastName("User");
        user.setEmail("deleted_user_" + user.getId() + "@deleted.local");
        user.setPassword("{noop}DELETED_" + UUID.randomUUID());
        user.getRoles().clear();
        user.setDeleted(true);

        userRepository.save(user);
    }


}
