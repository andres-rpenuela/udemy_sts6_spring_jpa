package com.codearp.application.demospring_boot3_jpa_relationship.domains;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name="STUDENTS")
@Builder
@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name="last_name")
    private String lastName;


    // propietario
    // No se recomienda que se maneje en cascada la REMOVE
    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE} )
    // Si no se pone el @JoinTable, hibernate la crea automaticamente
    @JoinTable(
            name="STUDNETS_COURSES",
            joinColumns = @JoinColumn(name="student_id"), // FK de Student
            inverseJoinColumns = @JoinColumn(name="course_id")// FK Course
    )
    private Set<Course> courses;

}