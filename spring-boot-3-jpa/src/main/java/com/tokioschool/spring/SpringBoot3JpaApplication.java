package com.tokioschool.spring;

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
import com.tokioschool.spring.repository.PersonRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class SpringBoot3JpaApplication implements ApplicationRunner {

	private final PersonRepository personRepository;
	private static Scanner sc = new Scanner(System.in);

	public static void main(String[] args) {
		SpringApplication.run(SpringBoot3JpaApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
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
		delete();
		
		sc.close();
		
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
}
