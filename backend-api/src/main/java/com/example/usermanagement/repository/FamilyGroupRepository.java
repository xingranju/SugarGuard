package com.example.usermanagement.repository;

import com.example.usermanagement.entity.FamilyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyGroupRepository extends JpaRepository<FamilyGroup, Long> {
    Optional<FamilyGroup> findByInviteCode(String inviteCode);
    List<FamilyGroup> findByCreatorId(Long creatorId);
}
