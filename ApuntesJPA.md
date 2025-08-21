# 📘 Apuntes completos: Spring Boot + Spring Data JPA

## Índice
1. [Introducción a Spring Boot y JPA](#introducción-a-spring-boot-y-jpa)
2. [Entidad Person](#entidad-person)
3. [Repositorios en Spring Data JPA](#repositorios-en-spring-data-jpa)
   - CrudRepository
   - JpaRepository
   - JpaSpecificationExecutor
4. [Consultas en Spring Data JPA](#consultas-en-spring-data-jpa)
   - Query Methods (naming convention)
   - Consultas con @Query (JPQL / SQL nativo)
   - LIKE, BETWEEN, ORDER BY
   - Funciones agregadas (COUNT, MIN, MAX, AVG, SUM, LENGTH)
   - Subconsultas (Subqueries)
   - IN y DISTINCT
5. [Proyecciones](#proyecciones)
   - Interfaces
   - DTOs
   - Objetos parciales
6. [Uso de Optional en consultas](#uso-de-optional-en-consultas)
7. [Specifications para consultas dinámicas](#specifications-para-consultas-dinámicas)
   - Definición
   - Construcción dinámica
   - Ejemplo práctico
8. [Configuración del proyecto](#configuración-del-proyecto)
   - application.properties
   - data.sql
   - H2 Console
9. [Buenas prácticas](#buenas-prácticas)

---

## 1. Introducción a Spring Boot y JPA
- **Spring Boot**: framework que simplifica la creación de aplicaciones Java con configuración automática y un servidor embebido (Tomcat, Jetty).
- **JPA (Java Persistence API)**: especificación para mapear objetos Java a tablas en bases de datos relacionales.
- **Hibernate**: implementación más usada de JPA.
- **Spring Data JPA**: módulo de Spring que facilita el acceso a datos con repositorios predefinidos.

---

## 2. Entidad Person
Ejemplo de entidad mapeada a la tabla `persons`:

```java
@Entity
@Table(name="persons")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @ToString
@Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String lastname;
    
    @Column(name = "progaming_language")
    private String programingLanguage;
}
````

🔹 Anotaciones principales:

* `@Entity`: indica que la clase es persistente.
* `@Table`: asigna el nombre de la tabla.
* `@Id` + `@GeneratedValue`: clave primaria autogenerada.
* `@Column`: personaliza el nombre de la columna.
* **Lombok** (`@Getter`, `@Setter`, `@Builder`…): genera automáticamente getters, setters, constructores y métodos utilitarios.

---

## 3. Repositorios en Spring Data JPA

### CrudRepository

Ofrece métodos CRUD básicos:

```java
public interface PersonRepository extends CrudRepository<Person, Long> {}
```

* `save()`, `findById()`, `findAll()`, `deleteById()`

### JpaRepository

Extiende de `CrudRepository`, añade paginación y ordenación:

```java
public interface PersonRepository extends JpaRepository<Person, Long> {}
```

### JpaSpecificationExecutor

Permite usar Specifications para consultas dinámicas:

```java
public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {}
```

---

## 4. Consultas en Spring Data JPA

### Query Methods

Se basan en el nombre del método:

```java
List<Person> findByProgramingLanguage(String progamingLanguage);
List<Person> findByNameContains(String name);
```

### JPQL con @Query

```java
@Query("select p from Person p where p.programingLanguage = ?1")
List<Person> searchByProgramingLanguage(String language);
```

### LIKE

```java
@Query("select p from Person p where p.name like %?1%")
List<Person> findLikeName(String name);
```

### BETWEEN

```java
List<Person> findByIdBetween(Long lower, Long upper);
```

### ORDER BY

```java
List<Person> findByIdBetweenOrderByNameDesc(Long lower, Long upper);
```

### Funciones agregadas

```java
@Query("select count(p) from Person p") Long total();
@Query("select min(p.id), max(p.id) from Person p") Object resumen();
```

### LENGTH

```java
@Query("select p.name, length(p.name) from Person p")
List<Object[]> getPersonNameLength();
```

### Subconsultas

```java
@Query("select p from Person p where length(p.name) = (select max(length(p2.name)) from Person p2)")
List<Person> getPersonWithLongestName();
```

### IN / DISTINCT

```java
@Query("select distinct p.name from Person p") List<String> getDistinctNames();
@Query("select p from Person p where p.id in (?1)") List<Person> getPersonByIds(List<Long> ids);
```

---

## 5. Proyecciones

### Interfaces

```java
public interface NameProjection {
    String getName();
}
```

```java
@Query("select p from Person p")
List<NameProjection> getNames();
```

### DTOs

```java
@Query("select new com.tokioschool.spring.dto.PersonDto(p.name,p.lastname) from Person p")
List<PersonDto> findPersonDtos();
```

### Objetos parciales

```java
@Query("select p.name, p.programingLanguage from Person p")
List<Object[]> obtenerDatos();
```

---

## 6. Uso de Optional en consultas

Se usa cuando el resultado puede no existir:

```java
@Query("select p from Person p where p.name = ?1")
Optional<Person> findOneName(String name);
```

---

## 7. Specifications para consultas dinámicas

### Definición

```java
public class PersonSpecification {
    public static Specification<Person> hasName(String name) {
        return (root, query, cb) -> cb.equal(root.get("name"), name);
    }
}
```

### Repositorio

```java
public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {}
```

### Uso en servicio

```java
Specification<Person> spec = Specification.where(null);
if (name != null) spec = spec.and(PersonSpecification.hasName(name));
return personRepository.findAll(spec);
```

---

## 8. Configuración del proyecto

### application.properties


```properties
spring.application.name=spring-boot-3-jpa-relationship
server.port=8082

############
##  BBDD  ##
############
spring.jpa.properties.hibernate.connection.useUnicode=true
spring.jpa.properties.hibernate.connection.characterEncoding=utf-8
spring.jpa.properties.hibernate.connection.charSet=utf-8

spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DATABASE_TO_UPPER=false;CASE_INSENSITIVE_IDENTIFIERS=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=sa
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# create, create-drop, update, validate
spring.jpa.hibernate.ddl-auto=update

# forzar a Hibernate a respetar el nombre exacto
# Con esto Hibernate usará exactamente lo que pongas en @Table(name="CLIENTS").
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.h2.console.settings.trace=false
spring.h2.console.settings.web-allow-others=false

# show queries in logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.springframework.jdbc.datasource.init.ScriptUtils=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE


# "src/main/resources/*.sql" is load automatically
#This aligns the script-based initialization with other database migration tools such as Flyway and Liquibase
# controla cuándo se cargan los scripts.
spring.jpa.defer-datasource-initialization=true

# Controla si Spring Boot ejecuta los scripts SQL (schema.sql, data.sql, *-mysql.sql, etc.) al iniciar.
# embedded -> Solo si la base de datos es en memoria (Defautl)
# always -> Ejecuta los scripts siempre, sin importar si la BD es embebida o externa (ej: MySQL, Postgres).
# never -> Nunca ejecuta los scripts, aunque existan. (Se usa cuando prefieres gestionar la BD con Flyway/Liquibase o manualmente.)
spring.sql.init.mode=always


# controla qué archivos SQL se buscan *-h2.sql
spring.sql.init.platform=mysql
````

### data.sql

```sql
INSERT INTO PERSONS (LASTNAME, NAME, PROGAMING_LANGUAGE) VALUES ('Ruiz','Andres','Java');
```

### H2 Console

Disponible en: `http://localhost:8082/h2-console`
Permite consultar la BD embebida.

---

## 9. Buenas prácticas

* Usar `Optional` en consultas que pueden devolver nulos.
* Preferir **DTOs o Proyecciones** en vez de devolver `Object[]`.
* Combinar `Specifications` para filtros dinámicos.
* Usar **paginación (`Pageable`)** para consultas grandes.
* Mantener las consultas **legibles y reutilizables**.

```
