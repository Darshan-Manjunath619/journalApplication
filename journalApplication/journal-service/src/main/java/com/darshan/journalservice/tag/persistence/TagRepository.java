package com.darshan.journalservice.tag.persistence;

import com.darshan.journalservice.tag.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllByOwnerIdOrderByNormalizedName(Long ownerId);
    Optional<Tag> findByIdAndOwnerId(Long id, Long ownerId);
    Optional<Tag> findByOwnerIdAndNormalizedName(Long ownerId, String normalizedName);
    List<Tag> findAllByIdInAndOwnerId(Set<Long> ids, Long ownerId);
}
