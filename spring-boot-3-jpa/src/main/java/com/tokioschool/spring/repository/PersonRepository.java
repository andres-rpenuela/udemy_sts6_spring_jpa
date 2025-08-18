package com.tokioschool.spring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tokioschool.spring.dto.PersonDto;
import com.tokioschool.spring.entity.Person;
import com.tokioschool.spring.projections.NameDto;
import com.tokioschool.spring.projections.NameProjection;

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
	
	
	// obtenre campos personalizados part 1
	@Query("select p.name from Person p where p.id =?1")
	public String getNameById(Long id);
	
	@Query("select concat(p.name,' ',p.lastname) from Person p where p.id =?1")
	public String getFullNameById(Long id);
	
	
	@Query("select p.name,p.lastname from Person p where p.id =?1")
	public Object[][] getFieldsById(Long id); // en la parte 2 se simplifica
	
	/** get fields custom part 2 **/
	@Query("select p.id, p.name, p.lastname, p.programingLanguage from Person p where p.id = ?1")
	//Object obtenerPersonDataFullById(Long id); // jpa automacitamente devuelve Object como un "Object[]"
	Optional<Object> obtenerPersonDataFullById(Long id); // jpa automacitamente devuelve Object como un "Object[]"
	
	@Query("select p.name, p.programingLanguage from Person p")
	List<Object[]> obtenerPersonDataList();

	@Query("select p, p.programingLanguage from Person p")
	List<Object[]> findAllMixPerson();
	
	
	@Query("select new Person(p.id,p.name,p.lastname,p.programingLanguage) from Person p")
	List<Person> findAllClassPerson();
	
	/** projections	**/
	// over interface
	@Query("select p from Person p")
	List<NameProjection> getNames();
	
	// se esceifica el paquete, porque no es una clase entity y no esta dentro del contexto
	@Query("select new com.tokioschool.spring.projections.NameDto(p.name,p.lastname) from Person p")
	List<NameDto> getNameDtos();
	
	@Query("select p.name, p.lastname from Person p")
	List<Object[]> getNamesAsObj();
	
	/** recover over dto's **/
	@Query("select new com.tokioschool.spring.dto.PersonDto(p.name,p.lastname) from Person p")
	List<PersonDto> findPersonDtos();
	
	/** use distinc blocked word JPQL / HQL  **/
	@Query("select distinct p.name from Person p")
	List<String> getNamesPersons();
	
	@Query("select distinct(p.programingLanguage,p.name) from Person p")
	List<String[]> getProgamingLanguageAndNameDistint();
	
	@Query("select count( distinct(p.programingLanguage) ) from Person p")
	List<Long> getProgamingLanguageDistintCount();
	
	// example de JPQL / HQL de concat, upper, lower and Like
	@Query("select CONCAT(p.name, ' ',p.lastname) as fullname from Person p")
	List<String> finAllFullNameConcat();
	
	@Query("select p.name || ' ' || p.lastname from Person p")
	//@Query("select (p.name || ' ' || p.lastname) as fullanme from Person p")
	List<String> finAllFullNameConcatWithPipe();
	
	@Query("select UPPER(p.name || ' ' || p.lastname) from Person p")
	List<String> finAllFullNameConcatWithPipeUpper();
	
	@Query("select LOWER( CONCAT(p.name, ' ',p.lastname) ) as fullname from Person p")
	List<String> finAllFullNameConcatLower();
	
	@Query("select p.name from Person p where p.name LIKE (?1) ")
	List<String> findPersonNameLikeNameV0(String name);
	
	@Query("select p.name from Person p where p.name LIKE (:name) ")
	List<String> findPersonNameLikeNameV1(@Param("name") String name);
	
	@Query("select p.name from Person p where p.name like %?1% ")
	List<String> findPersonNameLikeNameV2(String name);

	
	/** beetwen [lower,top), top no se incluye**/
	// query method
	List<Person> findPersonByIdBetween(Long lower,Long top);

	// JPQL / HQL
	@Query("select p from Person p where p.id between ?1 and ?2")
	List<Person> findPersonByIdBetweenHQL(Long lower,Long top);
	
	List<Person> findPersonByNameBetween(String lower,String top);
	
	@Query("select p from Person p where p.name between ?1 and ?2")
	List<Person> findPersonByNameBetweenHQL(String lower,String top);

	/** order by **/
	List<Person> findPersonByIdBetweenOrderByNameDesc(Long lower,Long top);
	List<Person> findPersonByIdBetweenOrderByNameDescLastnameAsc(Long lower,Long top);


	@Query("select p from Person p where p.id between  ?1 and ?2 order by p.name desc")
	List<Person> findPersonByIdBetweenHQLOrderByNameDesc(Long lower,Long top);
	@Query("select p from Person p where p.id between  ?1 and ?2 order by p.name desc, p.lastname asc")
	List<Person> findPersonByIdBetweenHQLOrderByNameDescLastnameAsc(Long lower,Long top);
}
