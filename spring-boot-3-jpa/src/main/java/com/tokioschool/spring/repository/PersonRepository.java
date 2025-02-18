package com.tokioschool.spring.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.tokioschool.spring.entity.Person;

@Repository
public interface PersonRepository extends CrudRepository<Person, Long>{

}
