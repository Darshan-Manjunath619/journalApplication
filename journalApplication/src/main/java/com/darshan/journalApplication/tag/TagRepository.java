package com.darshan.journalApplication.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllByUserUserNameOrderByNormalizedName(String userName);
    Optional<Tag> findByIdAndUserUserName(Long id, String userName);
    Optional<Tag> findByUserUserNameAndNormalizedName(String userName, String normalizedName);
    List<Tag> findAllByIdInAndUserUserName(Set<Long> ids, String userName);
}
