package com.tokioschool.spring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.aspectj.bridge.Message;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

import com.github.javafaker.Faker;
import com.tokioschool.spring.entity.Person;
import com.tokioschool.spring.projections.NameDto;
import com.tokioschool.spring.projections.NameProjection;
import com.tokioschool.spring.repository.PersonRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class SpringBoot3JpaApplication implements ApplicationRunner {

	private final PersonRepository personRepository;
	private  Scanner sc;

	public static void main(String[] args) {
		SpringApplication.run(SpringBoot3JpaApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		
		sc = new Scanner(System.in);
		
		Faker faker = new Faker();
		List<Person> persons = IntStream.range(1, 5).mapToObj(operand -> Person
				.builder()
				.name(faker.name().name())
				.lastname(faker.name().lastName())
				.programingLanguage(faker.programmingLanguage().name())
				.build()
				).collect(Collectors.toList());
		
		personRepository.saveAll(persons);
		
		log.info("Result find all");
		personRepository.findAll().forEach(p -> log.info(p.toString()) );
		
		log.info("Person know Java");
		personRepository.findByProgramingLanguage("Java").forEach(p -> log.info(p.toString()) );
		
		log.info("Person know Java and name Tadeo");
		personRepository.searchByProgramingLanguageAndName("Java","Tadeo").forEach(p -> log.info(p.toString()) );
		
		log.info("Person like name 'A' ");
		personRepository.searchLikeName("a").forEach(p -> log.info(p.toString()) );
		
		log.info("Obtain person data: ");
		personRepository.obtainedPersonData().forEach(personData -> System.out.println(personData[0]+", programing: "+personData[1] ) );
		
		log.info("Obtain person data by name: ");
		personRepository.obtainedPersonData("Tadeo").forEach(personData -> System.out.println(personData[0]+", programing: "+personData[1] ) );	
		
		log.info("Obtain person data by id: ");
		personRepository.findById(1L).ifPresent(System.out::println);
		
		log.info("Obtain person data by name (custom): ");
		personRepository.findOneName("Andres").ifPresent(System.out::println);
		
		log.info("Obtain person data like name (based query): ");
		personRepository.findLikeName("Andres").ifPresent(System.out::println);
		
		log.info("Obtain person data like name (based query method): ");
		personRepository.findByNameContains("Andres").ifPresent(System.out::println);
		
		/*** example not transactional for created ***/
		//createdPerson();
		
		/** exmaple transacion **/
		//Person p =  creatredDinamiPerson();
		//findPersonById(p.getId()).ifPresent(System.out::println);
		
		/** example of update transactional dinamyc **/
		//update();
		
		/** example of remove delete **/
		//delete();
		
		/** example fields custom of queries part 1 **/
		//System.out.println("Nombre with id 1: "+personRepository.getNameById(1L));
		//System.out.println("Nombre with id 11: "+personRepository.getNameById(11L)); // es nulo porque no existe
		
		//System.out.println("Full Nombre with id 1: "+personRepository.getFullNameById(1L));
		//System.out.println("Full Nombre with id 11: "+personRepository.getFullNameById(11L)); // es nulo porque no existe
		
		//System.out.println("Objects with id 1: "+ Arrays.asList( personRepository.getFieldsById(1L)[0] ) );
		//System.out.println("Objects Nombre with id 11: "+  Arrays.asList( personRepository.getFieldsById(11L) )); // es nulo porque no existe
		
		/** example fields custom of queries part 2 **/
		//fieldCustom();
		
		/** example result mixted of queries **/
		//fieldCustomFindMitexd();
		
		/** projections **/
		//projections();
		
		/** recover person as persondto **/
		//System.out.print("Result query's in dto ");
		//personRepository.findPersonDtos().forEach(System.out::println);
		
		
		/** distinc **/
		//System.out.print("Use distinc in Query ");
		//personRepository.getNamesPersons().forEach(System.out::println);
		//System.out.print("Use distinc in Query v2 ");
		//personRepository.getProgamingLanguageAndNameDistint().stream().map(Arrays::asList).forEach(System.out::println);
		//System.out.print("Count Progamming Language: ");
		//personRepository.getProgamingLanguageDistintCount().forEach(System.out::println);
		
		/** concat, upper, lower y operartor like **/
		System.out.print("Exmample Concat: ");
		personRepository.finAllFullNameConcat().forEach(System.out::println);
		
		System.out.print("Exmample Conca WithPipe:  ");
		personRepository.finAllFullNameConcatWithPipe().forEach(System.out::println);
		
		System.out.print("Exmample Concat Lower : ");
		personRepository.finAllFullNameConcatLower().forEach(System.out::println);
		
		System.out.print("Exmample Concat WithPipe Upper:  ");
		personRepository.finAllFullNameConcatWithPipeUpper().forEach(System.out::println);
		// Like, sensitive to %%
		System.out.print("Exmample Like: 'a' and Query ... like %1");
		personRepository.findPersonNameLikeNameV0("a").forEach(System.out::println);
		System.out.print("Exmample Like: %a% and Query ... like %1  ");
		personRepository.findPersonNameLikeNameV0("%a%").forEach(System.out::println);
		
		System.out.print("Exmample Like: 'a' as @Param and Query like :name ");
		personRepository.findPersonNameLikeNameV1("a").forEach(System.out::println);
		System.out.print("Exmample Like: '%a%' as @Param and Query like :name ");
		personRepository.findPersonNameLikeNameV1("%a%").forEach(System.out::println);
		
		System.out.print("Exmample Like: 'a' and Query ... like %1% ");
		personRepository.findPersonNameLikeNameV2("a").forEach(System.out::println);
		
	}

	private Person createdPerson() {
		Person person = Person.builder().name("Margie").lastname("Simpson").programingLanguage("Java").build();
		return personRepository.save(person);
	}
	
	@Transactional
	private Person creatredDinamiPerson() {
		
		System.out.print("Inserte name: ");
		String name = sc.nextLine();
		
		System.out.print("Inserte lastanme: ");
		String lastName = sc.nextLine();
		
		System.out.print("Inserte programing langauge: ");
		String programingLanguage = sc.nextLine();
		
		Person person = Person.builder().name(name).lastname(lastName).programingLanguage(programingLanguage).build();

		return personRepository.save(person);
	}
	
	
	@Transactional(readOnly = true)
	private Optional<Person> findPersonById(Long id){
		return personRepository.findById(id);
	}
	
	@Transactional
	public void update() {
		
		System.out.print("Inserte el id: ");
		Long id = Long.parseLong(sc.nextLine());
		
		// primero se busca el usuario a modificar y luego se persite
		Optional<Person> personOpt = personRepository.findById(id); 
		if(personOpt.isPresent() ) {
			Person person = personOpt.get();
			log.info("Person: {}",person);
			
			System.out.print("Inserte name: ");
			String name = sc.nextLine();
			
			System.out.print("Inserte lastanme: ");
			String lastName = sc.nextLine();
			
			System.out.print("Inserte programing langauge: ");
			String programingLanguage = sc.nextLine();
			
			
			person.setName(name);
			person.setLastname(lastName);
			person.setProgramingLanguage(programingLanguage);
			
			Person personUpdated = personRepository.save(person);
		}else{
			log.info("Person isn't in the system");
		}
	}
	
	 @Transactional
	 public void delete() {
		 // show result before
		 personRepository.findAll().forEach(System.out::println);
		 
		 System.out.print("Inserte el id: ");
		 Long id = Long.parseLong(sc.nextLine());
			
			// option a
			//personRepository.deleteById(id);
			
			// option b
			Optional<Person> personOpt = personRepository.findById(id);
			personOpt.ifPresent(person -> personRepository.delete(person));
			
			// show result after
			personRepository.findAll().forEach(System.out::println);
			
	 }
	 
	 @Transactional(readOnly = true)
	 public void fieldCustom() {
		 System.out.println("Consulta por cambpos personalizada por el id");
		 
		 System.out.print("Inserte el id: ");
		 Long id = Long.parseLong(sc.nextLine());
			
		 //Object[] row = (Object[]) personRepository.obtenerPersonDataFullById(id);
		 //System.out.println("id= %d,name= %s, lastname= %s, progamming= %s".formatted(row));
		 
		 Optional<Object> rowOptional = personRepository.obtenerPersonDataFullById(id);
		 if( rowOptional.isPresent()) {
			 Object[] row = (Object[]) rowOptional.get();
			 System.out.println("id= %d,name= %s, lastname= %s, progamming= %s".formatted(row));
		 }
		 
		 System.out.println();
	 }
	 
	 @Transactional(readOnly = true)
	 public void fieldCustomFindMitexd() {
		 System.out.println("Consulta con resultado de objetos mixtos");
		 
		 // objeto mixto: persona, string
		 List<Object[]> rows = personRepository.findAllMixPerson();
		 rows.stream().forEach(row -> System.out.println("programing: "+row[0]+", person: "+row[1]));
		 
		 // consulta personliza que el resutlado puebla un objeto persona con el constructor
		 List<Person> persons = personRepository.findAllClassPerson();
		 persons.stream().forEach(person -> System.out.println(person));
		 
	 }
	 
	 @Transactional(readOnly = true)
	 public void projections() {
		 System.out.println("Proyecciones on Interface");
		 List<NameProjection> names = personRepository.getNames();
		 names.stream().forEach(t -> System.out.println(t.getName()+", "+t.getLastname()) );
		 
		 System.out.println("Proyecciones on Dto");
		 List<NameDto> names2 = personRepository.getNameDtos();
		 names2.stream().forEach(t -> System.out.println(t.getName()+", "+t.getLastname()) );
		
		 System.out.println("Proyecciones on Collection<Objec[]> ");
		 for (Object[] obj :personRepository.getNamesAsObj()) {
			    String name = (String) obj[0];
			    String lastname = (String) obj[1];
			    System.out.println(name + " " + lastname);
			}
	 }
}
