# 📘 Apuntes: Uso de Named Queries en Hibernate/JPA con Spring Boot

---

## 📑 Índice

1. [Introducción a Named Queries](#1-introducción-a-named-queries)
2. [Sintaxis de Named Queries](#2-sintaxis-de-named-queries)

   * [JPQL (`@NamedQuery`)](#jpql-namedquery)
   * [SQL Nativo (`@NamedNativeQuery`)](#sql-nativo-namednativequery)
3. [Uso en Spring Data JPA](#3-uso-en-spring-data-jpa)

   * [Opción 1: Usando `@Query(name=...)`](#opción-1-usando-queryname)
   * [Opción 2: Usando `EntityManager`](#opción-2-usando-entitymanager)
4. [Diferencias con Repositorios de Spring Data JPA](#4-diferencias-con-repositorios-de-spring-data-jpa)
5. [Dependencias necesarias en Spring Boot](#5-dependencias-necesarias-en-spring-boot)
6. [Buenas prácticas](#6-buenas-prácticas)

---

## 1. Introducción a Named Queries

Los **Named Queries** son consultas predefinidas con un nombre asociado. Se declaran en la entidad (`@Entity`) y luego se invocan desde el repositorio o el `EntityManager`.

👉 Ventajas:

* Reutilización de consultas en distintos puntos de la aplicación.
* Evitan duplicar cadenas JPQL/SQL en múltiples clases.
* Se validan al iniciar la aplicación (errores en la query se detectan antes).

---

## 2. Sintaxis de Named Queries

### JPQL (`@NamedQuery`)

```java
@Entity
@NamedQueries({
    @NamedQuery(
        name = "Client.findByName",
        query = "SELECT c FROM Client c WHERE c.name = :name"
    ),
    @NamedQuery(
        name = "Client.findByNameAndCity",
        query = "SELECT c FROM Client c WHERE c.name = :name AND c.city = :city"
    )
})
public class Client {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String city;
}
```

---

### SQL Nativo (`@NamedNativeQuery`)

```java
@Entity
@NamedNativeQuery(
    name = "Client.findNativeByName",
    query = "SELECT * FROM clients c WHERE c.name = :name",
    resultClass = Client.class
)
public class Client { ... }
```

---

## 3. Uso en Spring Data JPA

### Opción 1: Usando `@Query(name=...)`

```java
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    @Query(name = "Client.findByName")
    List<Client> findByNameParam(@Param("name") String name);

    @Query(name = "Client.findByNameAndCity")
    List<Client> findByNameAndCityParam(@Param("name") String name,
                                        @Param("city") String city);

    @Query(name = "Client.findNativeByName", nativeQuery = true)
    List<Client> findNativeByName(@Param("name") String name);
}
```

---

### Opción 2: Usando `EntityManager`

```java
@Service
@RequiredArgsConstructor
public class ClientService {

    private final EntityManager em;

    public List<Client> findClientsByName(String name) {
        return em.createNamedQuery("Client.findByName", Client.class)
                 .setParameter("name", name)
                 .getResultList();
    }

    public List<Client> findClientsByNameAndCity(String name, String city) {
        return em.createNamedQuery("Client.findByNameAndCity", Client.class)
                 .setParameter("name", name)
                 .setParameter("city", city)
                 .getResultList();
    }
}
```

---

## 4. Diferencias con Repositorios de Spring Data JPA

Spring Data JPA ofrece varios tipos de repositorios:

| Repositorio                  | Extiende de                  | Métodos incluidos                                                      | Cuándo usarlo                                      |
| ---------------------------- | ---------------------------- | ---------------------------------------------------------------------- | -------------------------------------------------- |
| `CrudRepository<T, ID>`      | -                            | `save`, `findById`, `findAll`, `deleteById`                            | Cuando necesitas solo operaciones CRUD básicas.    |
| `PagingAndSortingRepository` | `CrudRepository`             | CRUD + paginación (`findAll(Pageable)`) y ordenación                   | Cuando necesitas resultados paginados o ordenados. |
| `JpaRepository<T, ID>`       | `PagingAndSortingRepository` | CRUD + paginación + métodos adicionales (`flush`, `saveAll`, `getOne`) | Es el más completo y el más usado en Spring Boot.  |

👉 **Named Queries** funcionan igual con cualquiera, pero en la práctica siempre se recomienda **`JpaRepository`** porque incluye todo lo anterior.

---

## 5. Dependencias necesarias en Spring Boot

En **Spring Boot Starter Data JPA**, ya viene todo incluido para usar Named Queries.
En tu `pom.xml` solo necesitas:

```xml
<dependencies>
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Driver de Base de Datos (ejemplo: H2 en memoria) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Si usas otra DB: PostgreSQL / MySQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

👉 No necesitas dependencias extra para `@NamedQuery`, ya que **es parte del estándar JPA** incluido en `spring-boot-starter-data-jpa`.

---

## 6. Buenas prácticas

1. **Nombrar bien las queries** con el patrón `Entidad.nombreConsulta`.
   Ejemplo: `Client.findByName`, `Order.findByStatus`.
2. Usar `@Param` en los repositorios para claridad en parámetros.
3. Si una consulta es muy compleja → considerar `@NamedNativeQuery` o un **Repositorio personalizado** con `EntityManager`.
4. Para consultas simples → aprovechar **Spring Data Derived Queries** (`findByNameAndCity`) en lugar de definir NamedQueries.
5. Evitar el uso de `@NamedNativeQuery` salvo que sea necesario por performance o por SQL específico del motor de BD.

