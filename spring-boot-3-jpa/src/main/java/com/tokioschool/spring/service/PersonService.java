package com.tokioschool.spring.service;

import com.tokioschool.spring.entity.Person;
import com.tokioschool.spring.specification.PersonSpecification;
import com.tokioschool.spring.specification.PersonSpecificationRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PersonService {
    private final PersonSpecificationRepository personSpecificationRepository;

    PersonService(PersonSpecificationRepository personSpecificationRepository){
        this.personSpecificationRepository = personSpecificationRepository;
    }


    public List<Person> search(String name, String language) {
        Specification<Person> spec = Specification.where(null);

        if (name != null) {
            spec = spec.and(PersonSpecification.hasName(name));
        }
        if (language != null) {
            spec = spec.and(PersonSpecification.hasLanguage(language));
        }

        return personSpecificationRepository.findAll(spec);
    }

}
