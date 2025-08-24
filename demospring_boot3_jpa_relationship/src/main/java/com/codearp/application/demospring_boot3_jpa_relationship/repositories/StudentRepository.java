package com.codearp.application.demospring_boot3_jpa_relationship.repositories;

import com.codearp.application.demospring_boot3_jpa_relationship.domains.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {

    //select s1_0.* from STUDENTS s1_0 left join STUDENTS_courses c1_0 on s1_0.id=c1_0.students_id left join COURSES c1_1 on c1_1.id=c1_0.courses_id where c1_0.courses_id=?
    @Query("select s from Student s left join fetch s.courses c where c.id = ?1")
    Optional<Student> findStudentWithCourseId(Long id);


    @Query("select s from Student s left join fetch s.courses c where s.id = ?1")
     Optional<Student> findStudentWithCourseById(Long id);
}
