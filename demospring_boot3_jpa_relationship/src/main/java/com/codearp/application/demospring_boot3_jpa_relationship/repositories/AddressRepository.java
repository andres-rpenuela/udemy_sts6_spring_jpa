package com.codearp.application.demospring_boot3_jpa_relationship.repositories;

import com.codearp.application.demospring_boot3_jpa_relationship.domains.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address,Long> {
}
