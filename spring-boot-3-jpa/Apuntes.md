
# 📘 Apuntes PersonRepository (Spring Data JPA)

## Índice
1. [Configuración del proyecto](#configuración-del-proyecto)
   - application.properties
   - data.sql
2. [Repositorio PersonRepository](#repositorio-personrepository)
   - Métodos basados en convención
   - Consultas JPQL personalizadas
   - Búsqueda por nombre con LIKE
   - Obtención de campos específicos
   - Uso de Optional en consultas
   - Proyecciones (Interface, DTO)
   - Uso de DISTINCT
   - Ejemplos de CONCAT, UPPER, LOWER, LIKE
   - Uso de BETWEEN
   - Ordenación (ORDER BY)
   - Funciones de agregación (COUNT, MIN, MAX, AVG, SUM)
   - Funciones JPQL (LENGTH)
   - Subconsultas (Subqueries)
   - WHERE IN

---

## 1. Configuración del proyecto

### application.properties
Configuración de **Spring Boot + JPA + H2**:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=update
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console


# show queries in logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.springframework.jdbc.datasource.init.ScriptUtils=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE


# "src/main/resources/*.sql" is load automatically 
#This aligns the script-based initialization with other database migration tools such as Flyway and Liquibase
spring.jpa.defer-datasource-initialization=true
````

👉 Se usa **H2 en memoria** con datos iniciales cargados en `data.sql`.

### data.sql

Datos iniciales para la tabla `PERSONS`:

```sql
INSERT INTO PERSONS (LASTNAME,NAME,PROGAMING_LANGUAGE ) VALUES ('Ruiz','Andres','Java');
INSERT INTO PERSONS (LASTNAME,NAME,PROGAMING_LANGUAGE ) VALUES ('Ramirez','Tadeo','Java');
INSERT INTO PERSONS (LASTNAME,NAME,PROGAMING_LANGUAGE ) VALUES ('Perez','Simon','Java');
```

---

## 2. Repositorio PersonRepository

El repositorio extiende de `CrudRepository<Person, Long>`, lo que da acceso a operaciones CRUD básicas.

### 🔹 Métodos basados en convención

```java
List<Person> findByProgramingLanguage(String progamingLanguage);
List<Person> findPersonByIdBetween(Long lower, Long top);
List<Person> findPersonByNameBetween(String lower, String top);
List<Person> findPersonByIdBetweenOrderByNameDesc(Long lower, Long top);
```

---

### 🔹 Consultas JPQL personalizadas

```java
@Query("select p from Person p where p.programingLanguage=?1")
List<Person> searchByProgramingLanguage(String programingLanguage);
```

---

### 🔹 Búsqueda por nombre con LIKE

```java
@Query("select p from Person p where UPPER(p.name) like CONCAT('%', UPPER(:name), '%')")
List<Person> searchLikeName(@Param("name") String namePerson);

Optional<Person> findByNameContains(String name);
```

---

### 🔹 Obtención de campos específicos

```java
@Query("select p.name, p.programingLanguage from Person p")
List<Object[]> obtainedPersonData();

@Query("select concat(p.name,' ',p.lastname) from Person p where p.id =?1")
String getFullNameById(Long id);
```

---

### 🔹 Uso de Optional en consultas

```java
@Query("select p from Person p where p.name = ?1")
Optional<Person> findOneName(String name);
```

---

### 🔹 Proyecciones (Interface, DTO)

```java
@Query("select p from Person p")
List<NameProjection> getNames();

@Query("select new com.tokioschool.spring.projections.NameDto(p.name,p.lastname) from Person p")
List<NameDto> getNameDtos();

@Query("select new com.tokioschool.spring.dto.PersonDto(p.name,p.lastname) from Person p")
List<PersonDto> findPersonDtos();
```

---

### 🔹 Uso de DISTINCT

```java
@Query("select distinct p.name from Person p")
List<String> getNamesPersons();
```

---

### 🔹 Ejemplos de CONCAT, UPPER, LOWER, LIKE

```java
@Query("select CONCAT(p.name, ' ',p.lastname) as fullname from Person p")
List<String> finAllFullNameConcat();

@Query("select UPPER(p.name || ' ' || p.lastname) from Person p")
List<String> finAllFullNameConcatWithPipeUpper();
```

---

### 🔹 Uso de BETWEEN

```java
@Query("select p from Person p where p.id between ?1 and ?2")
List<Person> findPersonByIdBetweenHQL(Long lower, Long top);
```
> Nota: el intervalo es de tipo [low,top), es decir, el top es excluyente.
---

### 🔹 Ordenación (ORDER BY)

```java
@Query("select p from Person p where p.id between ?1 and ?2 order by p.name desc")
List<Person> findPersonByIdBetweenHQLOrderByNameDesc(Long lower, Long top);
```

---

### 🔹 Funciones de agregación (COUNT, MIN, MAX, AVG, SUM)

```java
@Query("select count(p) from Person p")
Long totalPerson();

@Query("select min(p.id), max(p.id), sum(p.id), avg( length(p.name) ), count(p) from Person p")
Object getResumenAggregationFunction();
```

---

### 🔹 Funciones JPQL (LENGTH)

```java
@Query("select p.name, length(p.name) from Person p")
List<Object[]> getPersonNameLength();
```

---

### 🔹 Subconsultas (Subqueries)

```java
@Query("select p from Person p where length(p.name) = (select max(length(p2.name)) from Person p2)")
List<Person> getPersonWithLongestName();
```

---

### 🔹 WHERE IN

```java
@Query("select p from Person p where p.id in (?1)")
List<Person> getPersonByIds(List<Long> ids);
```

