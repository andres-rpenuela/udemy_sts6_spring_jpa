package com.tokioschool.spring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tokioschool.spring.entity.Person;

@Repository
public interface PersonRepository extends CrudRepository<Person, Long>{
	// Query Methods
	List<Person> findByProgramingLanguage(String progamingLanguage);
		
	// Query JPA
	@Query("select p from Person p where p.programingLanguage=?1")
	List<Person> searchByProgramingLanguage(String programingLanguage);
	
	@Query("select p from Person p where p.programingLanguage=?1 and p.name=?2")
	List<Person> searchByProgramingLanguageAndName(String programingLanguage,String namePerson);
	
	@Query("select p from Person p where UPPER(p.name) like CONCAT('%', UPPER(:name), '%')")
	List<Person> searchLikeName(@Param("name") String namePerson);
	

}
