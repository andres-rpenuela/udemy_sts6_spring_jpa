# 📘 Apuntes: Criteria API y QueryDSL en Spring Boot

---

## 📑 Índice

- [📘 Apuntes: Criteria API y QueryDSL en Spring Boot](#-apuntes-criteria-api-y-querydsl-en-spring-boot)
  - [📑 Índice](#-índice)
  - [1. Introducción](#1-introducción)
  - [2. Criteria API](#2-criteria-api)
    - [Ventajas y desventajas](#ventajas-y-desventajas)
    - [Ejemplos básicos](#ejemplos-básicos)
  - [3. QueryDSL](#3-querydsl)
    - [¿Qué es QueryDSL?](#qué-es-querydsl)
    - [Dependencias en Spring Boot](#dependencias-en-spring-boot)
    - [Generación de Q-classes](#generación-de-q-classes)
    - [Ejemplos básicos (QueryDSL)](#ejemplos-básicos-querydsl)
    - [Comparativa con Criteria API](#comparativa-con-criteria-api)
  - [4. Buenas prácticas](#4-buenas-prácticas)
- [📘 Integración de QueryDSL con JpaRepository en Spring Boot](#-integración-de-querydsl-con-jparepository-en-spring-boot)
  - [1️⃣ Dependencias necesarias](#1️⃣-dependencias-necesarias)
  - [2️⃣ Entidad de ejemplo](#2️⃣-entidad-de-ejemplo)
  - [3️⃣ Repositorio base](#3️⃣-repositorio-base)
  - [4️⃣ Interfaz personalizada](#4️⃣-interfaz-personalizada)
  - [5️⃣ Implementación con QueryDSL](#5️⃣-implementación-con-querydsl)
  - [6️⃣ Uso en un servicio](#6️⃣-uso-en-un-servicio)
  - [7️⃣ Diferencia con `JpaRepository`](#7️⃣-diferencia-con-jparepository)

---

## 1. Introducción

Spring Boot ofrece varias formas de construir consultas:

* **JPQL/NamedQuery** → consultas estáticas.
* **Criteria API** → consultas dinámicas tipadas.
* **Spring Data JPA Repositories** → abstracción sencilla (`CrudRepository`, `JpaRepository`).
* **QueryDSL** → API fluida y tipada, más expresiva que Criteria API.

👉 **Regla práctica:**

* Para consultas simples → usar `JpaRepository`.
* Para consultas estáticas complejas → usar JPQL/NamedQuery.
* Para consultas dinámicas → preferir **Criteria API** o mejor aún, **QueryDSL**.

---

## 2. Criteria API

### Ventajas y desventajas

✅ Tipada (menos errores de sintaxis).
✅ Perfecta para **consultas dinámicas** con filtros opcionales.
❌ Verbosa, código poco legible.

### Ejemplos básicos

**Búsqueda por atributo:**

```java
public List<Client> findByName(String name) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Client> cq = cb.createQuery(Client.class);
    Root<Client> root = cq.from(Client.class);

    cq.select(root).where(cb.equal(root.get("name"), name));

    return em.createQuery(cq).getResultList();
}
```

**Filtros dinámicos:**

```java
List<Predicate> predicates = new ArrayList<>();
if (name != null) predicates.add(cb.equal(root.get("name"), name));
if (city != null) predicates.add(cb.equal(root.get("city"), city));

cq.where(cb.and(predicates.toArray(new Predicate[0])));
```

---

## 3. QueryDSL

### ¿Qué es QueryDSL?

Es una **librería para construir consultas tipadas** sobre JPA/Hibernate, mucho más **fluida y expresiva** que Criteria API.
Se basa en clases generadas llamadas **Q-classes** (`QClient`, `QAddress`, etc.), que representan tus entidades con atributos tipados.

👉 Ventajas:

* Sintaxis clara y fluida.
* Autocompletado del IDE (atributos tipados).
* Menos verboso que Criteria.

---

### Dependencias en Spring Boot

En `build.gradle`:

```gradle
implementation "com.querydsl:querydsl-jpa"
annotationProcessor "com.querydsl:querydsl-apt:5.0.0:jpa"
```

En `pom.xml` (Maven):

```xml
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-apt</artifactId>
    <version>5.0.0</version>
    <scope>provided</scope>
</dependency>
```

---

### Generación de Q-classes

Se generan automáticamente al compilar (`mvn compile` o `gradle build`).
Ejemplo generado para `Client`:

```java
public class QClient extends EntityPathBase<Client> {
    public static final QClient client = new QClient("client");
    public final StringPath name = createString("name");
    public final NumberPath<Long> id = createNumber("id", Long.class);
}
```

---

### Ejemplos básicos (QueryDSL)

**Consulta simple:**

```java
QClient client = QClient.client;

List<Client> results = new JPAQuery<>(em)
    .select(client)
    .from(client)
    .where(client.name.eq("Juan"))
    .fetch();
```

**Filtros dinámicos:**

```java
QClient client = QClient.client;
BooleanBuilder builder = new BooleanBuilder();

if (name != null) builder.and(client.name.eq(name));
if (city != null) builder.and(client.city.eq(city));

List<Client> results = new JPAQuery<>(em)
    .select(client)
    .from(client)
    .where(builder)
    .fetch();
```

**Join con direcciones:**

```java
QClient client = QClient.client;
QAddress address = QAddress.address;

List<Client> results = new JPAQuery<>(em)
    .select(client)
    .from(client)
    .join(client.addresses, address)
    .where(address.street.eq("Avd. Florida"))
    .fetch();
```

---

### Comparativa con Criteria API

| Característica     | Criteria API             | QueryDSL                               |
| ------------------ | ------------------------ | -------------------------------------- |
| **Legibilidad**    | Verboso, difícil de leer | Sintaxis fluida y clara                |
| **Dinamismo**      | Bueno con `Predicate`    | Excelente con `BooleanBuilder`         |
| **Autocompletado** | Limitado                 | Completo (clases Q generadas)          |
| **Dependencias**   | Incluido en JPA          | Requiere `querydsl-jpa` y `apt`        |
| **Uso típico**     | Proyectos con JPA básico | Proyectos grandes, consultas complejas |

👉 **Conclusión**: QueryDSL es más expresivo y potente, ideal para proyectos medianos y grandes.

---

## 4. Buenas prácticas

1. Si necesitas **filtros dinámicos** → usar QueryDSL con `BooleanBuilder`.
2. Encapsular consultas en **repositorios personalizados** (`CustomRepositoryImpl`).
3. Para consultas simples → mantener `JpaRepository` (más legible).
4. En equipos grandes → preferir QueryDSL sobre Criteria API.

---

✅ Con esto tienes un **manual completo** de Criteria API y QueryDSL en Spring Boot.

---

Perfecto 🙌 Te preparo un ejemplo de cómo integrar **QueryDSL** dentro de un `JpaRepository` en Spring Boot.

---

# 📘 Integración de QueryDSL con JpaRepository en Spring Boot

---

## 1️⃣ Dependencias necesarias

En **Maven**:

```xml
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.querydsl</groupId>
    <artifactId>querydsl-apt</artifactId>
    <version>5.0.0</version>
    <scope>provided</scope>
</dependency>
```

En **Gradle**:

```gradle
implementation "com.querydsl:querydsl-jpa"
annotationProcessor "com.querydsl:querydsl-apt:5.0.0:jpa"
```

👉 Al compilar (`mvn compile` o `gradle build`), se generan las **Q-classes** (`QClient`, `QAddress`, etc.).

---

## 2️⃣ Entidad de ejemplo

```java
@Entity
public class Client {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String city;
    
    @OneToMany
    private List<Address> addresses = new ArrayList<>();
}
```

---

## 3️⃣ Repositorio base

```java
public interface ClientRepository extends JpaRepository<Client, Long>, ClientRepositoryCustom {
}
```

---

## 4️⃣ Interfaz personalizada

```java
public interface ClientRepositoryCustom {
    List<Client> searchClients(String name, String city);
}
```

---

## 5️⃣ Implementación con QueryDSL

```java
public class ClientRepositoryImpl implements ClientRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Client> searchClients(String name, String city) {
        QClient client = QClient.client;
        BooleanBuilder builder = new BooleanBuilder();

        if (name != null) {
            builder.and(client.name.eq(name));
        }
        if (city != null) {
            builder.and(client.city.eq(city));
        }

        return new JPAQuery<>(em)
                .select(client)
                .from(client)
                .where(builder)
                .fetch();
    }
}
```

📌 Notas:

* `ClientRepositoryImpl` debe **terminar en "Impl"** para que Spring la detecte automáticamente como implementación de la interfaz `ClientRepositoryCustom`.
* Usamos `BooleanBuilder` para **filtros dinámicos**.

---

## 6️⃣ Uso en un servicio

```java
@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void demoQueryDSL() {
        List<Client> results = clientRepository.searchClients("Juan", "Madrid");
        results.forEach(c -> System.out.println("Cliente: " + c.getName()));
    }
}
```

---

## 7️⃣ Diferencia con `JpaRepository`

| Característica        | `JpaRepository`                           | `QueryDSL + Custom Repo`                  |
| --------------------- | ----------------------------------------- | ----------------------------------------- |
| Consultas simples     | Métodos mágicos (`findByName`)            | No necesario                              |
| Consultas complejas   | Difícil con `@Query`                      | Muy fácil y dinámico con `BooleanBuilder` |
| Filtros dinámicos     | Limitados (se necesitan `Specifications`) | Naturales con `QueryDSL`                  |
| Autocompletado en IDE | Solo en nombres de métodos                | Completo con Q-classes                    |

---