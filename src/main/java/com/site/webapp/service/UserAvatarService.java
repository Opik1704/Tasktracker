package com.site.webapp.service;

import com.site.webapp.events.AvatarUploadedEvent;
import com.site.webapp.events.UserArchivedEvent;
import com.site.webapp.events.AvatarDeletedEvent;
import com.site.webapp.exception.AvatarNotFoundException;
import com.site.webapp.exception.UserNotFoundException;
import com.site.webapp.models.User;
import com.site.webapp.repo.UserRepository;
import com.site.webapp.validator.FileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class UserAvatarService {
    private static final Logger log = LoggerFactory.getLogger(UserAvatarService.class);

    @Value("#{'${app.avatar.allowed-types}'.split(',')}")
    private Set<String> ALLOWED_MIME_TYPES;

    @Value("#{'${app.avatar.allowed-extensions}'.split(',')}")
    private Set<String> ALLOWED_EXTENSIONS;

    @Value("${app.avatar.min-dimension}")
    private int minDimension;

    @Value("${app.avatar.max-dimension}")

    private int maxDimension;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;
    private final FileValidator fileValidator;

    public UserAvatarService(UserRepository userRepository,
                             FileStorageService fileStorageService,
                             ApplicationEventPublisher eventPublisher,
                             FileValidator fileValidator) {
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.eventPublisher = eventPublisher;
        this.fileValidator = fileValidator;
    }

    @Transactional
    public void uploadAvatar(Long userId, MultipartFile file) {
        log.debug("Запрос на загрузку аватара пользователем ID: {}, файл: '{}', размер: {} байт",
                userId, file.getOriginalFilename(), file.getSize());
        fileValidator.validateNotEmpty(file);
        fileValidator.validateMimeType(file, ALLOWED_MIME_TYPES );
        String cleanExtension = fileValidator.sanitizeExtension(file, ALLOWED_EXTENSIONS);
        fileValidator.validateDimension(file, minDimension, maxDimension);

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        String oldS3Key = user.getAvatarS3Key();
        if (oldS3Key != null && !oldS3Key.isBlank()) {
            eventPublisher.publishEvent(new AvatarDeletedEvent(userId, oldS3Key));
        }

        String s3Key = fileStorageService.uploadFile(file, "avatars", cleanExtension);
        log.info("Файл аватара загружен в S3 с ключом: {}", s3Key);

        String rawFilename = file.getOriginalFilename();
        String safeFilename = rawFilename != null ? StringUtils.cleanPath(rawFilename) : "avatar." + cleanExtension;

        user.setAvatarS3Key(s3Key);
        user.setOriginalAvatarFileName(safeFilename);

        userRepository.save(user);

        eventPublisher.publishEvent(new AvatarUploadedEvent(userId, safeFilename, s3Key));

        log.info("Аватар успешно обновлен для пользователя ID: {}, s3Key: {}", userId, s3Key);

    }

    public InputStream downloadAvatar(Long userId){
        log.debug("Запрос на скачивание аватара для пользователя ID: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Скачивание аватара отклонено: пользователь ID {} не найден", userId);
            return new UserNotFoundException(userId);
        });

        String s3Key = user.getAvatarS3Key();

        if (s3Key == null || s3Key.isBlank()) {
            log.warn("У пользователя ID {} отсутствует аватар для скачивания", userId);
            throw new AvatarNotFoundException(userId);
        }
        log.info("Загрузка аватара из S3 по ключу: {} для пользователя ID: {}", s3Key, userId);
        return fileStorageService.downloadFile(s3Key);
    }


    @Transactional
    public void deleteAvatar(Long userId) {
        log.debug("Запрос на удаление аватара пользователя ID: {}", userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        String s3Key = user.getAvatarS3Key();
        if (s3Key == null || s3Key.isBlank()) {
            log.warn("У пользователя ID {} нет аватара для удаления", userId);
            return;
        }

        user.setAvatarS3Key(null);
        user.setOriginalAvatarFileName(null);
        userRepository.save(user);

        eventPublisher.publishEvent(new AvatarDeletedEvent(userId, s3Key));
        log.info("Данные аватара занулены в БД, событие AvatarDeletedEvent отправлено для userId: {}", userId);
    }

    @Transactional
    @EventListener
    public void handleUserArchive(UserArchivedEvent event) {
        log.info("Получено событие архивации пользователя ID: {}", event.userId());
        if(event.avatarS3Key() != null && !event.avatarS3Key().isBlank()){
            fileStorageService.deleteFile(event.avatarS3Key());
        }
        log.info("Аватар удален для архивированного пользователя ID: {}", event.userId());
    }


}
