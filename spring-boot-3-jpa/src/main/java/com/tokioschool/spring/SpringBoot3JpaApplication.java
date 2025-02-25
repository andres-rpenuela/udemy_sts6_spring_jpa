package com.tokioschool.spring;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
		
	}

}
