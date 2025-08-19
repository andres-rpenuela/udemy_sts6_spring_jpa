package com.tokioschool.spring.specification;

import com.tokioschool.spring.entity.Person;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonSpecificationRepository extends CrudRepository<Person, Long>, JpaSpecificationExecutor<Person> {

}
