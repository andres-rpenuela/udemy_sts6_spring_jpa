package com.codearp.application.demospring_boot3_jpa_relationship.domains;

import jakarta.persistence.*;
import lombok.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name="COURSES")
@Builder @ToString @Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = {"students"})
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;


    // Opcional, para una comunicacion direccional
    // ¡Importante! y para evitar un loop de subcosutlas, excluir en el toString, equals y demas accesos internos en la clase
    // solo debe exitir un acesso o bien Student accede intermanete a Course, o al revés, en este caso, se deja que el acceso
    // sea el propietario de la relación
    @ManyToMany(mappedBy = "courses")
    @ToString.Exclude
    @Builder.Default
    private Set<Student> students = new HashSet<>();

    // Patrón de Encapsulated Collection Pattern
    public Set<Student> getStudents() {
        return Collections.unmodifiableSet(this.students);
    }

    // Llamar desde Student.addCoruse() para mantener consistencia
    public Course addStudent(Student student){
        Optional.ofNullable(student).ifPresent( this.students::add );
        return this;
    }

    public Course removeStudent(Student student) {
        this.students.removeIf(field -> field.equals(student));
        return this;
    }
}
