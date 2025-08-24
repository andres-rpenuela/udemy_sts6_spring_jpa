package com.codearp.application.demospring_boot3_jpa_relationship;

import com.codearp.application.demospring_boot3_jpa_relationship.domains.Course;
import com.codearp.application.demospring_boot3_jpa_relationship.domains.Student;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * El uso de EnrollmentService es la de utilizarlo para separar la responsabilidad de mantener la consistencia de la relación bidireccional fuera de las entidades
 *
 * Ventajas:
 * ✅ Evitas recursión infinita (student.removeCourse → course.removeStudent → student.removeCourse ...).
 * ✅ Menos acoplamiento entre entidades (Student no necesita saber cómo manipular Course y viceversa).
 * ✅ El dominio queda más intencional: la relación se administra por un servicio especializado (EnrollmentService).
 * ✅ Facilita añadir lógica adicional (ej. validar cupos, fechas, prerequisitos, etc.) sin ensuciar las entidades.
 *
 * Estos métodos modificina los paraemtros de entrada
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class EnrollmentService {

    public void enroll(Student student, Course course){
        student.addSCourse(course);
        course.addStudent(student);
    }

    public void unenroll(Student student,Course course){
        student.removeCourse(course);
        course.removeStudent(student);
    }
}
