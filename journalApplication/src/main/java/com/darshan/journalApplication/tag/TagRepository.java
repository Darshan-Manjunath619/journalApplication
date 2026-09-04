package com.darshan.journalApplication.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllByOwnerIdOrderByNormalizedName(Long ownerId);
    Optional<Tag> findByIdAndOwnerId(Long id, Long ownerId);
    Optional<Tag> findByOwnerIdAndNormalizedName(Long ownerId, String normalizedName);
    List<Tag> findAllByIdInAndOwnerId(Set<Long> ids, Long ownerId);
}
