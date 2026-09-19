package com.site.webapp.service;


import com.site.webapp.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserAvatarService {
    private static final Logger log = LoggerFactory.getLogger(UserAvatarService.class);

    private final UserService userService;
    private final FileStorageService fileStorageService;

    public UserAvatarService(UserService userService, FileStorageService fileStorageService) {
        this.userService = userService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public String uploadAvatar(MultipartFile file, Long userId){

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл аватарки не может быть пустым");
        }
        User user = userService.findUserById(userId);

        //deleteExistingAvatar(user);

        String s3Key = fileStorageService.uploadFile(file, "avatars");

        user.setOriginalAvatarFileName(file.getOriginalFilename());
        user.setAvatarS3Key(s3Key);

        log.info("Аватарка для пользователя ID {} успешно обновлена: {}", userId, s3Key);
        return s3Key;
    }
    @Transactional
    public void deleteAvatar(Long userId){
        User user = userService.findUserById(userId);

        //deleteExistingAvatarFile(user);

        user.setOriginalAvatarFileName(null);
        user.setAvatarS3Key(null);
        //userRepository.save(user);
    }
    @Transactional
    public String deleteExistingAvatar(Long userId) {
        return "a";
    }

}
