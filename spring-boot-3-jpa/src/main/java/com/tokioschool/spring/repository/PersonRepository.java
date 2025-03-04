package com.tokioschool.spring.repository;

import java.util.List;
import java.util.Optional;

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
	
	// obtain properties of person
	@Query("select p.name, p.programingLanguage from Person p")
	public List<Object[]> obtainedPersonData();
	
	@Query("select p.name, p.programingLanguage from Person p where p.name = ?1")
	public List<Object[]> obtainedPersonData(String name);
	
	@Query("select p.name, p.programingLanguage from Person p where p.name = ?1 and p.programingLanguage = ?1")
	public List<Object[]> obtainedPersonData(String name, String programingLanguage);

	@Query("select p.name, p.programingLanguage from Person p where p.programingLanguage = ?1")
	public List<Object[]> obtainedPersonDataByPromaingLanguage(String programingLanguage);
	
	// obtain a maybe element by query custom
	@Query("select p from Person p where p.name = ?1 ")
	public Optional<Person> findOneName(String name);
	
		// basado en query
	@Query("select p from Person p where p.name like %?1% ")
	public Optional<Person> findLikeName(String name);
		// basado en nombre metodo
	public Optional<Person> findByNameContains(String name);
	
	
	// obtenre campos personalizados
	@Query("select p.name from Person p where p.id =?1")
	public String getNameById(Long id);
	
	@Query("select concat(p.name,' ',p.lastname) from Person p where p.id =?1")
	public String getFullNameById(Long id);
	
	
	@Query("select p.name,p.lastname from Person p where p.id =?1")
	public Object[][] getFieldsById(Long id);

}
