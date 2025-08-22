# 📘 Apuntes: @Entity y anotaciones básicas en JPA/Hibernate

## Índice
1. [¿Qué es @Entity?](#qué-es-entity)  
2. [Requisitos de una entidad JPA](#requisitos-de-una-entidad-jpa)  
3. [Anotaciones básicas](#anotaciones-básicas)  
   - @Entity  
   - @Table  
   - @Id  
   - @GeneratedValue  
   - @Column  
   - @Transient  
   - @Lob  
   - @Enumerated  
   - @Temporal  
4. [Relaciones entre entidades](#relaciones-entre-entidades)  
   - @OneToOne  
   - @OneToMany / @ManyToOne  
   - @ManyToMany  
   - @JoinColumn  
   - @JoinTable  
5. [Estrategias de herencia en JPA](#estrategias-de-herencia-en-jpa)  
6. [Embebidos con @Embeddable y @Embedded](#embebidos-con-embeddable-y-embedded)  

---

## 1. ¿Qué es @Entity?
- `@Entity` es una anotación de **JPA** que indica que una clase Java será **mapeada a una tabla** en la base de datos.  
- Cada instancia de la clase corresponde a un registro (fila) en la tabla.  

Ejemplo:
```java
@Entity
@Table(name = "persons")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String lastname;
}
````

---

## 2. Requisitos de una entidad JPA

✅ Debe tener un **constructor vacío** (por defecto o explícito).
✅ Debe tener un campo anotado con `@Id` (clave primaria).
✅ Debe ser una **clase pública** y **serializable** (recomendado).
✅ Los campos se pueden mapear con **anotaciones JPA**.

---

## 3. Anotaciones básicas

### 🔹 @Entity

Marca la clase como entidad JPA.

```java
@Entity
public class Person { ... }
```

### 🔹 @Table

Permite personalizar el nombre de la tabla.

```java
@Table(name = "persons")
```

### 🔹 @Id

Define el campo como clave primaria.

```java
@Id
private Long id;
```

### 🔹 @GeneratedValue

Especifica cómo se genera la clave primaria.

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

Tipos de estrategias:

* **AUTO**: JPA elige la mejor estrategia según la BD.
* **IDENTITY**: usa auto-incremento de la BD.
* **SEQUENCE**: usa secuencias (Oracle, PostgreSQL).
* **TABLE**: usa una tabla auxiliar para generar IDs.

### 🔹 @Column

Configura detalles de una columna.

```java
@Column(name = "progaming_language", nullable = false, length = 100)
private String programingLanguage;
```

### 🔹 @Transient

Indica que un campo **no se persiste** en la BD.

```java
@Transient
private int edadCalculada;
```

### 🔹 @Lob

Indica que el campo almacena **Large Objects** (texto largo o binarios).

```java
@Lob
private String descripcionLarga;
```

### 🔹 @Enumerated

Mapea un `enum` a una columna.

```java
public enum Rol { ADMIN, USER }

@Enumerated(EnumType.STRING) // guarda "ADMIN" en BD
private Rol rol;
```

### 🔹 @Temporal

Se usa con **tipos antiguos** (`java.util.Date` o `java.util.Calendar`) para especificar cómo se mapearán en la BD:

```java
@Temporal(TemporalType.DATE)   // solo fecha
@Temporal(TemporalType.TIME)   // solo hora
@Temporal(TemporalType.TIMESTAMP) // fecha y hora
private Date fechaNacimiento;
```

> Nota:
> `@Temporal` ya no se usa con Java 8+ porque las nuevas clases (_LocalDate, LocalTime, LocalDateTime_) están soportadas directamente por JPA/Hibernate. 
> En este caso, también podemos usar @DateTimeFormat de Spring para formateo en formularios/web.
---

### 🔹 @DateTimeFormat

Con Java 8, JPA/Hibernate soporta directamente las clases del paquete java.time:

```java
private LocalDate fechaNacimiento;     // YYYY-MM-DD
private LocalTime horaRegistro;        // HH:mm:ss
private LocalDateTime fechaCreacion;   // YYYY-MM-DD HH:mm:ss
```
👉 No necesitan @Temporal, ya que JPA entiende el tipo automáticamente.

La anotación `@DateTimeFormat`, se usa para formatear entrada/salida en controladores Spring MVC (ej. formularios o JSON).
*No cambia cómo se guarda en la BD, solo cómo se interpreta el valor recibido.

```java
@DateTimeFormat(pattern = "yyyy-MM-dd")
private LocalDate fechaNacimiento;
// Así, si un cliente envía "2025-08-19", Spring lo convierte automáticamente a LocalDate.

@DateTimeFormat(pattern = "HH:mm:ss")
private LocalTime horaRegistro;

@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime fechaCreacion;
```

### @CreationTimestamp y @UpdateTimestamp

Sirve para rellenar automáticamente un campo de fecha/hora con el valor actual de la base de datos cuando se hace un INSERT o UPDATE.

Es útil para auditoría: campos como createdAt y updatedAt.

Las anotaciones de Hibernate como @CurrentTimestamp, @CreationTimestamp y @UpdateTimestamp sí funcionan con los tipos de fecha y hora de Java 8 (LocalDate, LocalTime, LocalDateTime) además de java.util.Date o java.sql.Timestamp.

* @CreationTimestamp
  * Asigna la fecha/hora actual solo en el momento del INSERT.
  * Ideal para createdAt.

* @UpdateTimestamp
  * Actualiza la fecha/hora en cada UPDATE.
  * Ideal para updatedAt.

* @CurrentTimestamp
  * Puede usarse tanto en INSERT como en UPDATE.
  * Se comporta similar a @CreationTimestamp pero más general.
  * Requiere más control manual en algunos casos.

```java
   @CurrentTimestamp
   private LocalDateTime createdAt;  // Fecha y hora actual

   @CurrentTimestamp
   private LocalDate createdDate;  // Solo fecha actual

    @CurrentTimestamp
    private LocalTime createdTime;  // Solo la hora

```
> Nota:
> `@CurrentTimestamp` funciona con LocalDate, LocalTime y LocalDateTime.
> Solo guarda la parte que corresponda según el tipo.

 
## 4. Relaciones entre entidades

### @OneToOne

Una persona tiene un solo pasaporte.

```java
@OneToOne
@JoinColumn(name = "passport_id")
private Passport passport;
```

### @OneToMany / @ManyToOne

**Relación uno a muchos / muchos a uno**.

Ejemplo:
- Una Persona puede tener muchos libros.
- Cada Libro pertenece a una persona.

👉 Eso en base de datos se traduce en que la tabla Book lleva una FK (person_id) que apunta a Person.

> Nota: **¿Por qué no en Person?**
> Si intentaras poner la FK en la tabla person para apuntar a muchos Book:
> * Tendrías que meter una lista de IDs (book_ids) en una columna, algo que no existe en SQL relacional normalizado.
> * O tendrías que crear una tabla intermedia, lo cual ya sería otra cosa (@ManyToMany).
> 
> Por eso, en un @OneToMany unidireccional, la FK siempre vive en la tabla de la entidad “muchos” (el hijo).

```java
// en la clase "Inverso (mappedBy)"
@OneToMany(mappedBy = "person")
private List<Book> books;

// en la clase "Dueño (owning side)"
@ManyToOne
@JoinColumn(name = "person_id") // FK_person_id
private Person person;
```

Resumen gráfico:
```
 Person (id) 1 --- * Book (id, person_id)

* Book.person = lado dueño (@ManyToOne).
* Person.books = lado inverso (@OneToMany(mappedBy="person")).
```

En JPA, cuando defines una relación bidireccional, hay un dueño (**owing side**) de la relación (_el que tiene la FK en la tabla_) y un lado inverso (_mappedBy_) que solo refleja la relación.

* Dueño (owning side) → el que tiene la FK (@ManyToOne).
* Inverso (mappedBy) → el otro lado, que solo “mapea” la relación (@OneToMany(mappedBy=...)).


#### ¿Que significa `mappedBy = "person"`?

```java
@Entity
public class Person {
    @Id @GeneratedValue
    private Long id;
    private String name;

    @OneToMany(mappedBy = "person")
    private List<Book> books;
}
```

El signficado desglosado sería:
* **mappedBy** indica qué atributo en la otra entidad es el dueño de la relación.
* _"person"_ se refiere al nombre del campo en _Book_ que tiene la anotación @ManyToOne.

```java
@Entity
public class Book {
    @Id @GeneratedValue
    private Long id;
    private String title;

    @ManyToOne
    @JoinColumn(name = "person_id") // aquí vive la FK, opcional
    private Person person;
}
```
Aquí, _Book.person_ es el dueño de la relación.
Por eso en _Person_ escribimos _mappedBy = "person"_, para indicar que no se genere otra FK en Person.

#### Por qué el @ManyToOne es opcional en el ejemplo?

En el código:
```java
//El mappedBy en Person apunta al atributo person en Book, que es el dueño de la relación.
@OneToMany(mappedBy = "person")
private List<Book> books;

// El @ManyToOne va en la entidad "hija" (Book) porque es donde se guarda la FK (person_id). (Opcional)
@ManyToOne
@JoinColumn(name = "person_id")
private Person person;
```

>En Person el **@OneToMany** solo tiene sentido si en Book existe el campo `private Person person`.
>
>Ese **@ManyToOne** en Book <ins>no es opcional realmente</ins>, es necesario para que exista la relación: sin él, no hay columna _person_id_ en la tabla Book.
> 
>Lo que pasa es que, si solo quieres modelar una relación unidireccional (Person → Book), podrías omitirlo y hacer algo como:
>
> ```java
>   @Entity
>   public class Person {
>       @Id @GeneratedValue
>       private Long id;
>       private String name;
>
>       // ya no se usa `mappedBy`, no existe el cmapo en Book   
>       @OneToMany
>       @JoinColumn(name = "person_id") // FK en BOOK
>       private List<Book> books;
>   }
>   
>   @Entity
>   public class Book {
>       @Id @GeneratedValue
>       private Long id;
>       private String title;
>   }
> ```
> En ese caso, la relación sería unidireccional (solo Person sabe sus Book), pero no puedes navegar desde Book hacia Person, creando Hibernate crea la FK person_id en la tabla BOOK.

#### Ejemplo de codificaicón `ManyToOne`

```java
@Transactional
    protected void manyToOne(){
        //1.  Crear el cliente
        Client client = Client.builder().name("Pedro").lastName("Sanchez").build();
        clientRepository.save(client);

        //2.  Crear la factura y se referencia al cliente.
        Invoice invoice = Invoice.builder().amount(BigDecimal.valueOf(10000)).client(client).build();
        Invoice invoice2 = invoiceRepository.save(invoice);

        // son el miso objeto, y ambos tiene el id
        // lo que indica que "save(entity)", modifica el parametro de entrada
        System.out.println(invoice);  //Invoice{id=1, description='null', amount=10000, client={id=4, name='Pedro', lastName='sANCHEZ'}}
        System.out.println(invoice2); //Invoice{id=1, description='null', amount=10000, client={id=4, name='Pedro', lastName='sANCHEZ'}}
    }
```

#### @OneToMany

La anotación `@OneToMany` se usa cuando **una entidad (padre)** puede estar relacionada con **muchas entidades hijas** (_de uno a muchos_).

| Estrategia                         | Qué crea JPA                                                   |
| ---------------------------------- | -------------------------------------------------------------- |
| **Sin `mappedBy` ni `JoinColumn`** | Una **tabla intermedia** `CLIENT_ADDRESSES`                    |
| **Con `mappedBy`**                 | FK en `Address` → `client_id` (más usado en bidireccional)     |
| **Con `@JoinColumn`**              | FK en `Address`, pero definida desde `Client` (unidireccional) |

> **Importante**: @OneToMany por defecto es LAZY → Hibernate no carga la lista addresses hasta que se accede.


Ejemplo:

```java
// client.java

    // si no se mapped a un field de Address de tipo Cliente o no se usa JoinColum (no eixte una fk), entonces, crea una tabla CLIENT_ADDRESS con las relaciones
    // CLIENT ----* ADDRESSES
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true)`// crea una tabla intermedia
    //@OneToMany(mappedBy = "client") // debe existir el Cliente client y debe ser un @ManyToOne
    //@JoinColumn(name = "client_id") // crea una fk client_id en ADDRESSES, si neceisada de crar client.
    private List<Address> addresses;
```

> Algunas propieades útiles:
> 
> - cascade = CascadeType.ALL
> → Todas las operaciones (persist, merge, remove, etc.) se propagan a las entidades hijas.
> 
> - orphanRemoval = true
> → Si se elimina un objeto de la lista, se elimina también en la BD.
> 
> - mappedBy
> → Indica que la relación se gestiona desde el lado hijo (@ManyToOne).
> 
> - @JoinColumn(name = "...")
> → Define la FK directamente en la tabla hija.

##### 📌 Consieración sobre `@OneToMany` y Lazy Loading en Hibernate/Spring Data JPA

###### 1. Comportamiento por defecto
- En JPA/Hibernate, **`@OneToMany` es LAZY por defecto**:

```java
@OneToMany(mappedBy = "client")
private List<Address> addresses;
````

* La lista `addresses` **no se carga automáticamente** al recuperar un `Client`.
* Hibernate crea un **proxy** que solo se inicializa cuando se accede a la colección **dentro de una sesión activa**.

---

###### 2. Problema típico

* Acceder a una colección lazy **fuera de la sesión** provoca:

```
org.hibernate.LazyInitializationException: could not initialize proxy - no Session
```

* Ejemplo típico:

```java
Client client = clientRepository.findById(3L).orElseThrow();
List<Address> addresses = client.getAddresses(); // Falla si fuera de la sesión
```

---

###### 3. Caso especial: `findById` devuelve detached

* Importante: **el objeto `Client` devuelto por `findById` puede estar DETACHED**, dependiendo de la configuración del repositorio.
* Habitualmente:

  * **La sesión asociada al `Client` ya está cerrada**.
  * Ni `Hibernate.initialize(client.getAddresses())` ni acceder a la colección forzará la carga.
  * Acceder a lazy fuera de sesión genera **`LazyInitializationException`**.

---

###### 4. Soluciones recomendadas

- Opción 1: Habilitar lazy load fuera de la sesión (no recomendada)

```properties
hibernate.enable_lazy_load_no_trans=true
```

> Permite inicializar proxies fuera de la sesión.
 
>**No recomendado**, rompe consistencia y puede generar consultas N+1 inesperadas.

---

- Opción 2: Acceder dentro de un bloque `@Transactional`

```java
@Transactional(readOnly = true)
public void loadClient() {
    Client client = clientRepository.findById(3L).orElseThrow();
    client.getAddresses().size(); // fuerza la carga
}
```

> Garantiza que la colección se inicialice mientras la sesión está activa.

> **No funciona si el objeto está detached**.

---

- Opción 3: Cambiar a `FetchType.EAGER`

```java
@OneToMany(fetch = FetchType.EAGER, mappedBy = "client")
private List<Address> addresses;
```

> Trae siempre la colección al cargar el `Client`.

> Útil solo si siempre se necesita la colección completa.

> Puede afectar rendimiento si la colección es grande.

---

- Opción 4: Usar `JOIN FETCH` en JPQL

```java
@Query("SELECT c FROM Client c LEFT JOIN FETCH c.addresses WHERE c.id = :id")
Optional<Client> findByIdWithAddresses(@Param("id") Long id);
```

> Permite traer `Client` con la colección `addresses` **ya inicializada**.

> Evita problemas de lazy fuera de la sesión.

---

- Opción 5: Inicialización manual con Hibernate (dentro de la sesión activa)

```java
@Transactional
public void initAddresses() {
    Client client = clientRepository.findById(3L).orElseThrow();
    Hibernate.initialize(client.getAddresses()); // funciona solo si la sesión está activa, puede que sea detached
```

> Solo funciona **dentro de la transacción**.

> **No funciona** si el `Client` está detached, como ocurre con `findById` en algunos casos.

---

########### 5. Ejemplo práctico de manejo de una colección

```java
@Transactional
public void oneToManyAboutAClientExist() {
    clientRepository.findById(3L).ifPresent(client -> {
        Address address1 = Address.builder().street("Avd. Canxas").number(3).build();
        Address address2 = Address.builder().street("Avd. Florida").number(3).build();

        // Machar directamente la lista de direcciones evita LazyInitializationException
        client.setAddresses(Arrays.asList(address1, address2));

        // No es necesario save si el cliente ya está en el contexto de persistencia
        clientRepository.save(client);

        log.info("Cliente: {}", client);
        client.getAddresses().forEach(a -> log.info("Address: {}", a));
    });
}
```

**Claves del ejemplo:**

* `findById` devuelve un `Client` detached.
* Se reemplaza la colección (`setAddresses`) en lugar de inicializar el proxy lazy.
* Evita errores de lazy loading.

---

##### 6. Buenas prácticas

1. Mantener acceso a colecciones lazy **dentro de la transacción y sesión activa**.
2. Preferir **JOIN FETCH** en consultas cuando se necesita la colección.
3. Evitar `hibernate.enable_lazy_load_no_trans` en producción.
4. Usar `FetchType.EAGER` solo si la colección se usa siempre.
5. Para relaciones bidireccionales, mantener consistencia de ambos lados:

```java
address.setClient(client);
client.getAddresses().add(address);
```

######################
--- 

### @ManyToMany

Relación muchos a muchos con tabla intermedia.

```java
@ManyToMany
@JoinTable(
  name = "student_course",
  joinColumns = @JoinColumn(name = "student_id"),
  inverseJoinColumns = @JoinColumn(name = "course_id")
)
private List<Course> courses;
```

### @JoinColumn
La anotación @JoinColumn de JPA se usa para definir la columna que actúa como clave foránea en una relación entre entidades. Es muy común en relaciones @OneToOne, @ManyToOne y, en combinación con @OneToMany (_usualmente en el lado inverso_).

```java
@ManyToOne
@JoinColumn(name = "client_id")
private Client client;
```
>Aquí:
>
> - @ManyToOne: indica que muchos registros de la entidad actual apuntan a uno de Client.
>
> - @JoinColumn(name = "client_id"): indica que en la tabla >de la entidad actual existe una columna client_id que >será la FK hacia Client.id.
>

| Propiedad              | Tipo         | Descripción                                                                            | Ejemplo                                                                      |
| ---------------------- | ------------ | -------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------- |
| `name`                 | `String`     | Nombre de la columna de la tabla actual que contiene la FK                             | `@JoinColumn(name = "client_id")`                                            |
| `referencedColumnName` | `String`     | Nombre de la columna en la tabla referenciada a la que apunta la FK (por defecto `id`) | `@JoinColumn(name="client_id", referencedColumnName="id")`                   |
| `unique`               | `boolean`    | Si la columna debe ser única                                                           | `@JoinColumn(name="client_id", unique=true)`                                 |
| `nullable`             | `boolean`    | Si la columna permite `NULL`                                                           | `@JoinColumn(name="client_id", nullable=false)`                              |
| `insertable`           | `boolean`    | Si la columna se incluye en `INSERT`                                                   | `@JoinColumn(name="client_id", insertable=false)`                            |
| `updatable`            | `boolean`    | Si la columna se incluye en `UPDATE`                                                   | `@JoinColumn(name="client_id", updatable=false)`                             |
| `foreignKey`           | `ForeignKey` | Permite definir el comportamiento del FK (nombre, acciones `ON DELETE/UPDATE`)         | `@JoinColumn(name="client_id", foreignKey=@ForeignKey(name="FK_CLIENT_ID"))` |


### @JoinTable
---

## 5. Estrategias de herencia en JPA

Cuando una jerarquía de clases se mapea en BD, JPA permite distintas estrategias con `@Inheritance`:

* **SINGLE\_TABLE** (default): todas las clases en una sola tabla (más simple, pero puede tener muchas columnas nulas).
* **JOINED**: cada clase tiene su propia tabla (más normalizado).
* **TABLE\_PER\_CLASS**: cada subclase tiene su propia tabla (menos usado).

Ejemplo:

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Employee { ... }

@Entity
public class Manager extends Employee { ... }
```

---

## 6. Embebidos con @Embeddable y @Embedded

### 🔹 ¿Qué son?

Son objetos que **no tienen su propia tabla**, sino que sus campos se insertan en la misma tabla de la entidad principal.
Se usan para modelar objetos de valor como direcciones, coordenadas, etc.

### Ejemplo

```java
@Embeddable
public class Address {
    private String street;
    private String city;
    private String zipcode;
}

@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    private Address address; 
}
```

👉 Esto genera la tabla `persons` con columnas: `id`, `name`, `street`, `city`, `zipcode`.

---


# 📘 Apuntes: Ciclo de vida de entidades JPA + Anotaciones de callbacks


## Índice
1. [Ciclo de vida de una entidad en JPA](#ciclo-de-vida-de-una-entidad-en-jpa)  
   - Transient  
   - Managed  
   - Detached  
   - Removed  
2. [Anotaciones de ciclo de vida](#anotaciones-de-ciclo-de-vida)  
   - @PrePersist / @PostPersist  
   - @PreUpdate / @PostUpdate  
   - @PreRemove / @PostRemove  
   - @PostLoad  
3. [Ejemplo completo](#ejemplo-completo)  

---

## 1. Ciclo de vida de una entidad en JPA

Las entidades en JPA pasan por diferentes estados controlados por el **EntityManager**:

1. **Transient (transitoria)**

   * La entidad acaba de crearse con `new`.
   * No está asociada a la BD.
   * Ejemplo: `Person p = new Person();`

2. **Managed (gestionada/persistente)**

   * Se asocia al contexto de persistencia (`EntityManager`).
   * Los cambios se sincronizan automáticamente con la BD.
   * Ejemplo: `em.persist(p);`

3. **Detached (desasociada)**

   * La entidad existió en el contexto pero ahora está fuera.
   * No se sincronizan cambios con la BD.
   * Ejemplo: después de cerrar el `EntityManager`.

4. **Removed (eliminada)**

   * Marcada para borrarse de la BD.
   * Se elimina en el `flush` o `commit`.
   * Ejemplo: `em.remove(p);`

📌 **Resumen gráfico**:

```
new → [persist()] → managed → [remove()] → removed
          ↓
      [detach()] → detached → [merge()] → managed
```

---

## 2. Anotaciones de ciclo de vida

JPA provee anotaciones para ejecutar **métodos automáticamente** en cada estado:

### 🔹 Persistencia

* **@PrePersist** → antes de insertar (`persist`).
* **@PostPersist** → después de insertar.

```java
@PrePersist
public void prePersist() { this.createdAt = LocalDateTime.now(); }

@PostPersist
public void postPersist() { System.out.println("Entidad persistida con ID " + id); }
```

### 🔹 Actualización

* **@PreUpdate** → antes de actualizar.
* **@PostUpdate** → después de actualizar.

```java
@PreUpdate
public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

@PostUpdate
public void postUpdate() { System.out.println("Entidad actualizada"); }
```

### 🔹 Eliminación

* **@PreRemove** → antes de eliminar.
* **@PostRemove** → después de eliminar.

```java
@PreRemove
public void preRemove() { System.out.println("Eliminando entidad " + id); }

@PostRemove
public void postRemove() { System.out.println("Entidad eliminada"); }
```

### 🔹 Carga

* **@PostLoad** → después de cargar desde la BD., muy usado para inicilizar datos que no son persistidos (_aquellos anotados con @Transient_)

```java
@Transient
public String fullName;

@PostLoad
public void postLoad() { 
    this.fullName = this.name + " " + this.lastname;
    System.out.println("Entidad cargada: " + fullName);
}
```

---

## 3. Ejemplo completo

```java
@Entity
@Table(name = "persons")
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        System.out.println("⏳ PrePersist ejecutado");
    }

    @PostPersist
    public void onPostPersist() {
        System.out.println("✅ PostPersist ejecutado con ID " + id);
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
        System.out.println("✏️ PreUpdate ejecutado");
    }

    @PostUpdate
    public void onPostUpdate() {
        System.out.println("🔄 PostUpdate ejecutado");
    }

    @PreRemove
    public void onPreRemove() {
        System.out.println("🗑 PreRemove ejecutado para " + id);
    }

    @PostRemove
    public void onPostRemove() {
        System.out.println("❌ PostRemove ejecutado");
    }

    @PostLoad
    public void onPostLoad() {
        System.out.println("📥 PostLoad ejecutado para " + name);
    }
}
```

---

✅ Con estas anotaciones se puede implementar **auditoría automática** (ej. `createdAt`, `updatedAt`) y gestionar acciones en cada transición del ciclo de vida.

```

---