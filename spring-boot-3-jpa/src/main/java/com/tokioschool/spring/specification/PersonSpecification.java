package com.tokioschool.spring.specification;

import com.tokioschool.spring.entity.Person;
import org.springframework.data.jpa.domain.Specification;

public class PersonSpecification {
    public static Specification<Person> hasName(String name) {
        return (root, query, cb) -> cb.equal(root.get("name"), name);
    }

    public static Specification<Person> hasLanguage(String language) {
        return (root, query, cb) -> cb.equal(root.get("programingLanguage"), language);
    }
}
