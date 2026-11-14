package com.site.webapp.repo;

import com.site.webapp.models.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class TagRepositoryImpl extends SimpleJpaRepository<Tag, Long> implements TagRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public TagRepositoryImpl(EntityManager entityManager) {
        super(Tag.class, entityManager);
    }

    @Override
    public Optional<Tag> findByName(String name) {
        Query query = entityManager.createQuery("SELECT t FROM Tag t WHERE t.name = :name", Tag.class);
        query.setParameter("name", name);
        List<Tag> result = query.getResultList();
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<Tag> findAllByOrderByNameAsc() {
        return entityManager.createQuery("SELECT t FROM Tag t ORDER BY t.name ASC", Tag.class)
                .getResultList();
    }

    @Override
    public boolean existsByName(String name) {
        Query query = entityManager.createQuery(
                "SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Tag t WHERE LOWER(t.name) = LOWER(:name)");
        query.setParameter("name", name);
        return (Boolean) query.getSingleResult();
    }

    @Override
    public boolean existsByNameExcludingId(Long id, String name) {
        Query query = entityManager.createQuery(
                "SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Tag t WHERE t.name = :name AND t.id <> :id");
        query.setParameter("id", id);
        query.setParameter("name", name);
        return (Boolean) query.getSingleResult();
    }

    @Transactional
    @Override
    public <S extends Tag> S save(S entity) {
        if (entity.getId() == null) {
            String lowerName = entity.getName().toLowerCase();
            Tag existing = entityManager.createQuery(
                    "SELECT t FROM Tag t WHERE LOWER(t.name) = :lowerName", Tag.class)
                    .setParameter("lowerName", lowerName)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                return (S) existing;
            }
        }
        return super.save(entity);
    }
}
