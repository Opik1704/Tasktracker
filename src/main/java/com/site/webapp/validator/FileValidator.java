package com.site.webapp.validator;

import com.site.webapp.exception.InvalidFileTypeException;
import com.site.webapp.exception.InvalidImageDimensionsException;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

@Component
public class FileValidator {

    private static final Logger log = LoggerFactory.getLogger(FileValidator.class);
    private static final Tika TIKA = new Tika();

    public  void validateMimeType(MultipartFile file, Set<String> allowedTypes){
        String contentType = file.getContentType();
        if(contentType == null || !allowedTypes.contains(contentType.toLowerCase(Locale.ROOT))){
            throw new InvalidFileTypeException("Неподдерживаемый MIME-тип файла: " + contentType);
        }
    }

    public String sanitizeExtension(MultipartFile file, Set<String> allowedExtensions){
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);

        if (!StringUtils.hasText(extension)) {
            throw new InvalidFileTypeException("Не удалось определить расширение файла");
        }

        String normalizedExtension = extension.toLowerCase(Locale.ROOT);

        if (!allowedExtensions.contains(normalizedExtension)) {
            throw new InvalidFileTypeException("Недопустимое расширение файла: ." + normalizedExtension);
        }
        return normalizedExtension;
    }

    public void validateSize(MultipartFile file, long maxSizeBytes) {
        if (file.getSize() > maxSizeBytes) {
            throw new InvalidFileTypeException(String.format(
                    "Размер файла (%.2f МБ) превышает допустимый лимит (%.2f МБ)",
                    file.getSize() / (1024.0 * 1024.0),
                    maxSizeBytes / (1024.0 * 1024.0)
            ));
        }
    }

    public  void validateDimension(MultipartFile file, int minDimension, int maxDimension){
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());

            if (image == null) {
                throw new InvalidImageDimensionsException("Не удалось прочитать изображение");
            }
            int width = image.getWidth();
            int height = image.getHeight();

            if (width < minDimension || height < minDimension) {
                throw new InvalidImageDimensionsException(
                        String.format("Изображение слишком маленькое: %dx%d (минимум %d)", width, height, minDimension)
                );
            }
            if (width > maxDimension || height > maxDimension){
                throw new InvalidImageDimensionsException(
                        String.format("Изображение слишком большое: %dx%d (максимум %d)", width, height, maxDimension)
                );
            }
        } catch (IOException e) {
            throw new InvalidImageDimensionsException("Ошибка при чтении файла изображения", e);
        }
    }

    public void validateMagicBytes(MultipartFile file, String expectedMimeType) {
        try (InputStream inputStream = file.getInputStream()) {
            String detectedMimeType = TIKA.detect(inputStream, file.getOriginalFilename());

            if (!detectedMimeType.equalsIgnoreCase(expectedMimeType)) {
                log.warn("SECURITY ALERT | Попытка подмены файла! Заявлен MIME: {}, Реальный Magic Byte: {}, Файл: {}",
                        file.getContentType(), detectedMimeType, file.getOriginalFilename());
                throw new InvalidFileTypeException("Содержимое файла не соответствует его заявленному типу");
            }
        } catch (IOException e) {
            throw new InvalidFileTypeException("Ошибка при чтении содержимого файла", e);
        }
    }

    public void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException("Файл не может быть пустым");
        }
    }
}
