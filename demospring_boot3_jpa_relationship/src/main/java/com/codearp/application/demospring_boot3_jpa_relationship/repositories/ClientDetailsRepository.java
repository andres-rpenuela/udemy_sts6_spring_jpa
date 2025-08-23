package com.codearp.application.demospring_boot3_jpa_relationship.repositories;

import com.codearp.application.demospring_boot3_jpa_relationship.domains.ClientDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientDetailsRepository extends JpaRepository<ClientDetails,Long> {
}
