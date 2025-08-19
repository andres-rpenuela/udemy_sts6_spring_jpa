# 📘 Apuntes: Consultas dinámicas con Specification en Spring Data JPA

## Índice
1. [Introducción](#introducción)
2. [Definición de Specification](#definición-de-specification)
   - Implementación de la interfaz `Specification<T>`
   - Método `toPredicate()`
3. [Repositorio con JpaSpecificationExecutor](#repositorio-con-jpaspecificationexecutor)
4. [Construcción dinámica de Specification](#construcción-dinámica-de-specification)
   - Uso en servicios
   - Uso en controladores
5. [Ejemplo práctico](#ejemplo-práctico)
   - Entidad
   - Specification
   - Repositorio
   - Servicio/Controlador
6. [Ventajas del uso de Specification](#ventajas-del-uso-de-specification)

---

## 1. Introducción
Cuando necesitamos realizar consultas a bases de datos con **filtros dinámicos**, podemos usar la interfaz `Specification` de JPA.  
Esto permite construir consultas basadas en los parámetros que reciba la aplicación, evitando crear decenas de métodos en el repositorio.

---

## 2. Definición de Specification
La interfaz `Specification<T>` provee el método:

```java
Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
````

Este método define las condiciones (`WHERE`) de la consulta.

Ejemplo de Specification para filtrar por nombre:

```java
public class PersonSpecification {
    public static Specification<Person> hasName(String name) {
        return (root, query, cb) -> cb.equal(root.get("name"), name);
    }
}
```

---

## 3. Repositorio con JpaSpecificationExecutor

El repositorio debe extender **JpaSpecificationExecutor<T>**, además de `JpaRepository`:

```java
@Repository
public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {
}
```

Esto habilita métodos como:

```java
List<Person> findAll(Specification<Person> spec);
```

---

## 4. Construcción dinámica de Specification

### Uso en servicios

En un servicio podemos construir la consulta de forma dinámica según los parámetros recibidos:

```java
Specification<Person> spec = Specification.where(null);

if (name != null) {
    spec = spec.and(PersonSpecification.hasName(name));
}

if (language != null) {
    spec = spec.and(PersonSpecification.hasLanguage(language));
}

return personRepository.findAll(spec);
```

### Uso en controladores

El controlador recibe los parámetros y los pasa al servicio:

```java
@GetMapping("/search")
public List<Person> search(@RequestParam(required = false) String name,
                           @RequestParam(required = false) String language) {
    return personService.search(name, language);
}
```

---

## 5. Ejemplo práctico

### Entidad

```java
@Entity
public class Person {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String programingLanguage;
}
```

### Specification

```java
public class PersonSpecification {
    public static Specification<Person> hasName(String name) {
        return (root, query, cb) -> cb.equal(root.get("name"), name);
    }

    public static Specification<Person> hasLanguage(String language) {
        return (root, query, cb) -> cb.equal(root.get("programingLanguage"), language);
    }
}
```

### Repositorio

```java
public interface PersonRepository 
    extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {
}
```

### Servicio

```java
@Service
public class PersonService {
    @Autowired
    private PersonRepository personRepository;

    public List<Person> search(String name, String language) {
        Specification<Person> spec = Specification.where(null);

        if (name != null) {
            spec = spec.and(PersonSpecification.hasName(name));
        }
        if (language != null) {
            spec = spec.and(PersonSpecification.hasLanguage(language));
        }

        return personRepository.findAll(spec);
    }
}
```

---

## 6. Ventajas del uso de Specification

✅ Consultas **dinámicas** sin necesidad de crear múltiples métodos en el repositorio.
✅ **Reutilización** de Specifications en distintas combinaciones.
✅ Legibilidad y **mantenibilidad** del código.
✅ Compatible con **Spring Data JPA** y soporta paginación/sorting.

---

```

¿Quieres que te genere también un **ejemplo avanzado** donde se combinen varios criterios dinámicos con `AND` y `OR` (por ejemplo, buscar por nombre *o* lenguaje)?
```
