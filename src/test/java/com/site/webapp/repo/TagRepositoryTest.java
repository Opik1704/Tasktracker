package com.site.webapp.repo;

import com.site.webapp.models.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();
    }

    @Test
    void createTag_withValidName_savesToDatabase() {
        Tag tag = new Tag();
        tag.setName("urgent");
        tag = tagRepository.save(tag);

        assertNotNull(tag.getId());
        assertEquals("urgent", tag.getName());
    }

    @Test
    void findByName_withExistingName_returnsTag() {
        Tag tag = new Tag();
        tag.setName("important");
        tagRepository.save(tag);

        Optional<Tag> found = tagRepository.findByName("important");

        assertTrue(found.isPresent());
        assertEquals("important", found.get().getName());
    }

    @Test
    void findByName_withNonExistentName_returnsEmpty() {
        Optional<Tag> found = tagRepository.findByName("nonexistent");

        assertFalse(found.isPresent());
    }

    @Test
    void existsByName_withExistingName_returnsTrue() {
        Tag tag = new Tag();
        tag.setName("urgent");
        tagRepository.save(tag);

        boolean exists = tagRepository.existsByName("urgent");

        assertTrue(exists);
    }

    @Test
    void existsByName_withDifferentCase_returnsTrue() {
        Tag tag = new Tag();
        tag.setName("Urgent");
        tagRepository.save(tag);

        boolean exists = tagRepository.existsByName("urgent");

        assertTrue(exists);
    }

    @Test
    void existsByName_withNonExistentName_returnsFalse() {
        boolean exists = tagRepository.existsByName("nonexistent");

        assertFalse(exists);
    }

    @Test
    void findAllByOrderByNameAsc_returnsSortedTags() {
        Tag tag1 = new Tag();
        tag1.setName("zebra");
        tagRepository.save(tag1);

        Tag tag2 = new Tag();
        tag2.setName("alpha");
        tagRepository.save(tag2);

        Tag tag3 = new Tag();
        tag3.setName("middle");
        tagRepository.save(tag3);

        List<Tag> tags = tagRepository.findAllByOrderByNameAsc();

        assertEquals(3, tags.size());
        assertEquals("alpha", tags.get(0).getName());
        assertEquals("middle", tags.get(1).getName());
        assertEquals("zebra", tags.get(2).getName());
    }

    @Test
    void tagWithEmptyName_failsValidation() {
        Tag tag = new Tag();
        tag.setName("");

        boolean exceptionThrown = false;
        try {
            tagRepository.save(tag);
        } catch (Exception e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown, "Сохранение тега с пустым названием должно вызывать исключение");
    }

    @Test
    void tagWithNameOver50Chars_failsValidation() {
        Tag tag = new Tag();
        tag.setName("A".repeat(51));

        boolean exceptionThrown = false;
        try {
            tagRepository.save(tag);
        } catch (Exception e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown, "Сохранение тега с названием более 50 символов должно вызывать исключение");
    }

    @Test
    void tagWithDuplicateName_caseInsensitive_fails() {
        Tag tag1 = new Tag();
        tag1.setName("urgent");
        tagRepository.save(tag1);

        Tag tag2 = new Tag();
        tag2.setName("URGENT");
        tagRepository.save(tag2);

        List<Tag> tags = tagRepository.findAllByOrderByNameAsc();

        assertEquals(1, tags.size(), "Дубликат (независимо от регистра) должен быть запрещен");
    }
}