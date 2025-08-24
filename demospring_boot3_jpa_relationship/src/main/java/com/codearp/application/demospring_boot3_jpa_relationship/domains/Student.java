package com.codearp.application.demospring_boot3_jpa_relationship.domains;

import jakarta.persistence.*;
import lombok.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
    // Si los cursos se comparten, quita CascadeType.PERSIST.
    //@ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE} )
    @ManyToMany(cascade = {CascadeType.MERGE} )
    // Si no se pone el @JoinTable, hibernate la crea automaticamente
//    @JoinTable(
//            name="STUDNETS_COURSES",
//            joinColumns = @JoinColumn(name="student_id"), // FK de Student
//            inverseJoinColumns = @JoinColumn(name="course_id")// FK Course
//    )
    @Builder.Default
    private Set<Course> courses = new HashSet<>();


    // Patrón de Encapsulated Collection Pattern
    public Set<Course> getCourses() {
        return Collections.unmodifiableSet(this.courses);
    }

    public Student addSCourse(Course course){
        if(course==null){
            return this;
        }

        // bidireccional (consistencia), rompería la filosofía `Single Responsibility`, se pdoía EnrollmentService
        // que mantenga la consistenica de la relación bidireciconal.
        // course = course.addStudent(this); // en vez de acceder al getter que es inmutable
        this.courses.add(course);

        return this;
    }

    public Student removeCourse(Course course){
        // EnrollmentService se manea para la consistenica
//        if( this.courses.remove( course) ){
//            course.removeStudent(this);
//        }

        if(course==null){
            return this;
        }
        this.courses.remove(course);
        return this;
    }
}