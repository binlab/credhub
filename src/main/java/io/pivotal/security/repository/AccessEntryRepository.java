package io.pivotal.security.repository;


import io.pivotal.security.entity.AccessEntryData;
import io.pivotal.security.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface AccessEntryRepository extends JpaRepository<AccessEntryData, UUID> {

  List<AccessEntryData> findAllByCredentialUuid(UUID name);
  AccessEntryData findByCredentialAndActor(Credential credential, String actor);

  @Transactional
  long deleteByCredentialAndActor(Credential credential, String actor);
}
