# 📘 Apuntes: Estrategias de Queries en Spring Boot



## 📑 Índice

1. [Introducción](#1-introducción)
2. [NamedQuery](#2-namedquery)
3. [Spring Data Repository](#3-spring-data-repository)
4. [Criteria API](#4-criteria-api)
5. [QueryDSL](#5-querydsl)
6. [JdbcTemplate vs NamedParameterJdbcTemplate](#6-jdbctemplate-vs-namedparameterjdbctemplate)
7. [Tabla Comparativa](#7-tabla-comparativa)
8. [Diagrama de Decisión](#8-diagrama-de-decisión)
9. [Resumen Práctico](#9-resumen-práctico)

---

## 1. Introducción

Spring Boot ofrece varios enfoques para realizar consultas en bases de datos.
La elección depende de:

* La **complejidad** de la query.
* Si la query es **dinámica o estática**.
* El **nivel de abstracción** que se desea.
* El **rendimiento** requerido.

---

## 2. NamedQuery

* Definidas en la **entidad JPA** con `@NamedQuery`.
* Se validan en el arranque (seguras).
* Poco flexibles porque el SQL queda fijado en el código.

```java
@Entity
@NamedQuery(name = "Client.findByCity", query = "SELECT c FROM Client c WHERE c.city = :city")
public class Client {}
```

✔️ Útiles cuando la query es estable y se reutiliza mucho.

---

## 3. Spring Data Repository

* Basadas en **interfaces** que extienden `JpaRepository`, `CrudRepository` o `PagingAndSortingRepository`.
* Genera queries automáticamente a partir de la convención del nombre del método.
* Muy productivo, poco código.
* Limitado para queries complejas.

```java
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByName(String name);
}
```

✔️ Perfecto para CRUD y queries sencillas.

---

## 4. Criteria API

* API **type-safe** basada en builders.
* Ideal para queries **dinámicas** (condiciones opcionales).
* Verboso y menos legible que otros enfoques.

```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Client> query = cb.createQuery(Client.class);
Root<Client> client = query.from(Client.class);
query.where(cb.equal(client.get("city"), city));
```

✔️ Bueno para queries que cambian en tiempo de ejecución.

---

## 5. QueryDSL

* API **fluent** y **type-safe** para JPA.
* Ideal para queries dinámicas y complejas.
* Requiere generar las clases `Q` con un plugin Maven/Gradle.

```java
QClient client = QClient.client;
new JPAQuery<>(em)
    .select(client)
    .from(client)
    .where(client.city.eq(city))
    .fetch();
```

✔️ Más legible que Criteria API para consultas dinámicas avanzadas.

---

## 6. JdbcTemplate vs NamedParameterJdbcTemplate

### 🔹 JdbcTemplate

* Usa **placeholders `?`**.
* Parámetros en **orden posicional**.
* Más rápido de escribir, pero poco legible si hay muchos parámetros.

```java
String sql = "INSERT INTO client (id, name, city) VALUES (?, ?, ?)";
jdbcTemplate.update(sql, client.getId(), client.getName(), client.getCity());
```

---

### 🔹 NamedParameterJdbcTemplate

* Usa **parámetros con nombre (`:param`)**.
* Más claro y mantenible en queries complejas.
* Requiere `MapSqlParameterSource` o un `Map`.

```java
String sql = "INSERT INTO client (id, name, city) VALUES (:id, :name, :city)";
MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("id", client.getId())
        .addValue("name", client.getName())
        .addValue("city", client.getCity());

namedParameterJdbcTemplate.update(sql, params);
```

✔️ Recomendado para queries con muchos parámetros o condiciones opcionales.

---

## 7. Tabla Comparativa

| Enfoque                        | Nivel de Abstracción | Ventajas                            | Desventajas                         | Uso Ideal                         |
| ------------------------------ | -------------------- | ----------------------------------- | ----------------------------------- | --------------------------------- |
| **NamedQuery**                 | Medio                | Reutilización, validación al inicio | Poco flexible                       | Queries comunes y estáticas       |
| **Spring Data Repository**     | Alto                 | Poco código, rápido                 | Limitado en queries complejas       | CRUDs y consultas simples         |
| **Criteria API**               | Medio-bajo           | Dinámico, seguro                    | Verboso, ilegible en queries largas | Filtros condicionales dinámicos   |
| **QueryDSL**                   | Medio                | Fluent, legible, dinámico           | Requiere plugin y Q-classes         | Queries dinámicas grandes         |
| **JdbcTemplate**               | Bajo                 | Flexibilidad total, rápido          | Mapeo manual                        | SQL específico, rendimiento       |
| **NamedParameterJdbcTemplate** | Bajo                 | Legible, dinámico con `:param`      | Más código que repositorios         | Queries SQL con muchos parámetros |

---

## 8. Diagrama de Decisión

```
                         ┌─────────────────────────┐
                         │ ¿Necesitas CRUD simple? │
                         └─────────────┬───────────┘
                                       │
                              ┌────────▼────────┐
                              │ Spring Data Repo │
                              └──────────────────┘
                                       │
          ┌────────────────────────────┴────────────────────────────┐
          │                                                         │
┌─────────▼─────────┐                                 ┌─────────────▼────────────┐
│ ¿Query estática y │                                 │ ¿Query dinámica?         │
│ común?            │                                 └─────────────┬────────────┘
└─────────┬─────────┘                                               │
          │                                      ┌──────────────────▼───────────────────┐
   ┌──────▼───────┐                              │ ¿Prefieres legibilidad fluent?        │
   │ NamedQuery   │                              └───────────────┬──────────────────────┘
   └──────────────┘                                              │
                                              ┌──────────────────▼─────────────┐
                                              │ QueryDSL (mejor que Criteria)  │
                                              └────────────────────────────────┘
                                                                  │
                                         ┌────────────────────────▼───────────────────────┐
                                         │ ¿Necesitas SQL nativo / máximo rendimiento?    │
                                         └──────────────┬─────────────────────────────────┘
                                                        │
                                ┌───────────────────────▼────────────────────────┐
                                │ JdbcTemplate / NamedParameterJdbcTemplate       │
                                └────────────────────────────────────────────────┘
```

---

## 9. Resumen Práctico

* **CRUD rápido →** Spring Data Repositories.
* **Queries predefinidas y estables →** NamedQuery.
* **Queries dinámicas →** Criteria API o **QueryDSL** (preferible QueryDSL por legibilidad).
* **Máxima flexibilidad y rendimiento →** JdbcTemplate / NamedParameterJdbcTemplate.
