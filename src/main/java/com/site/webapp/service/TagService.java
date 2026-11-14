package com.site.webapp.service;

import com.site.webapp.models.Tag;
import com.site.webapp.repo.TagRepository;
import com.site.webapp.exception.tag.TagNotFoundException;
import com.site.webapp.exception.tag.TagAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TagService {

    private static final Logger log = LoggerFactory.getLogger(TagService.class);

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    @CacheEvict(value = "allTags", allEntries = true)
    public Tag createTag(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new TagAlreadyExistsException("Название тега не может быть пустым");
        }
        if (name.length() > 50) {
            throw new TagAlreadyExistsException("Название тега не может превышать 50 символов");
        }
        if (tagRepository.existsByName(name)) {
            throw new TagAlreadyExistsException("Тег с названием '" + name + "' уже существует");
        }
        Tag tag = new Tag(name);
        tagRepository.save(tag);
        log.info("Тег создан: {}", tag.getName());
        return tag;
    }

    @Transactional
    @CacheEvict(value = "allTags", allEntries = true)
    public Tag updateTag(Long id, String newName) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Тег с ID " + id + " не найден"));

        if (newName == null || newName.trim().isEmpty()) {
            throw new TagAlreadyExistsException("Название тега не может быть пустым");
        }
        if (newName.length() > 50) {
            throw new TagAlreadyExistsException("Название тега не может превышать 50 символов");
        }
        if (tagRepository.existsByNameExcludingId(id, newName)) {
            throw new TagAlreadyExistsException("Тег с названием '" + newName + "' уже существует");
        }

        tag.setName(newName);
        tagRepository.save(tag);
        log.info("Тег обновлен: ID={}, новое название={}", id, newName);
        return tag;
    }

    @Transactional
    @CacheEvict(value = "allTags", allEntries = true)
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Тег с ID " + id + " не найден"));

        tagRepository.delete(tag);
        log.info("Тег удален: ID={}", id);
    }

    @Cacheable(value = "allTags", key = "'allTags'")
    public List<Tag> getAllTags() {
        return tagRepository.findAllByOrderByNameAsc();
    }

    public Tag getTagById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Тег с ID " + id + " не найден"));
    }
}