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

<code>
@Entity
public class Person { /*...*/ }
</code>

### 🔹 @Table

Permite personalizar el nombre de la tabla.

<code>
@Table(name = "persons")
</code>

### 🔹 @Id

Define el campo como clave primaria.

<code>
@Id
private Long id;
</code>

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

Una relación @OneToOne indica que una entidad se asocia con otra entidad de forma única.

Cada instancia de la primera entidad tiene exactamente una instancia de la segunda y viceversa.

Por defecto el fecth es EAGER.

> Ejemplo típico: User y UserProfile, Cliente y DireccionPrincipal, Pasaporte


* En JPA, una de las entidades debe ser el “dueño” de la relación.
* La entidad dueña contiene la columna de la clave foránea (@JoinColumn).
* La otra entidad usa mappedBy para indicar que la relación es inversa (_oneToOne_ bidireccional) (opcional)

Ejemplo de relacion Unidireccional

```java
// Una persona tiene un solo pasaporte. Unidireccional
@OneToOne
@JoinColumn(name = "passport_id")
private Passport passport;
```

Ejemplo de relacion Bidireccional

```java
@Entity
public class Client {

    @Id @GeneratedValue
    private Long id;
    private String name;

    @OneToOne(mappedBy = "client")
    private ClientDetails clientDetails
    
}
```

```java
public class ClientDetails {

    @Id   @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private boolean premium;
    private Integer points;

    // Propietario de la relación
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "client_id") // FK en ClientDetails
    private Client client;
}
```
#### Tipos de mapeo
* Relación compartida (Foreign key en una entidad)
    * Como en el ejemplo anterior.
    * Client tiene la FK address_id.
* Tabla intermedia
    * Similar a @ManyToMany, pero rara vez se usa.
    * Se puede crear con @JoinTable si se quiere desacoplar las tablas.

#### Buenas practicas
1. Siempre definir claramente quién es el propietario de la relación.
2. Para evitar errores de null o inconsistencias, siempre mantener consistencia bidireccional:
```java
public void setMainAddress(Address address) {
    this.mainAddress = address;
    if (address != null) {
        address.setClient(this);
    }
}
```
3. Evitar @OneToOne con colecciones (List o Set), para no confundir con @OneToMany.
4. Ideal para entidades que siempre deben existir juntas o con relación 1:1 estricta.

### @OneToMany / @ManyToOne

**Relación uno a muchos / muchos a uno**.

Aquí tienes una redacción más clara y técnica de tu nota, en estilo de documentación:

---

> **Notas sobre relaciones y rendimiento en JPA/Hibernate**
>
> * En las relaciones:
>
>   * **`@OneToMany`** → el *fetch type* por defecto es **LAZY**.
>   * **`@ManyToOne`** → el *fetch type* por defecto es **EAGER**.
> * Cuando las colecciones en una relación **`@OneToMany`** son de tipo `List` (y no `Set`), Hibernate las maneja internamente como **"bags"**.
>
>   * Esto provoca que si se usan varios `JOIN FETCH` sobre colecciones, se produzca la excepción:
>
>     ```
>     org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
>     ```
>   * Para resolverlo se recomienda:
>
>     * Usar `Set` en lugar de `List`.
>     * O bien, anotar las colecciones con `@OrderColumn` o `@IndexColumn` para que Hibernate las trate de forma ordenada y no como "bags".
> * En consultas que usan *fetch LAZY*, si además se aplica un `WHERE ... IN (:ids)` para filtrar entidades principales, Hibernate puede generar el **problema de N+1 queries** en las colecciones asociadas.
>
>   * Ejemplo: una query carga 10 clientes, y luego Hibernate dispara una consulta independiente para cada colección `invoices` o `addresses`.
>   * Esto puede optimizarse con la anotación `@BatchSize(size = N)`, que indica a Hibernate que cargue colecciones en lotes, reduciendo el número total de queries.


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

Este problema es muy común que se de en aplicaiocnes de consola que usan jpa

* Ejemplo
```java
// class que implementa CommandLineRunner

// operation allow, because remove use in client
// but error: ailed to lazily initialize a collection of role: com.codearp.application.demospring_boot3_jpa_relationship.domains.Client.addresses
// NOTA:
// ESTE ERROR NO PASA EN UNA APLICACIÓN WEB SI SE ANOTA CON TRANSANCIONAL Y NO SE CIERRA LA SESIÓN
// PERO EN UNA DE CONSOLA (ESTAMOS EN EL CONTEXTO COMMANLINERUNNER SI PASA (TRATA CADA OPERACIÓN DE FORMA ATÓMICA)
// PARA SOLUCIONARLO HEMOS USADO: spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true
clientRepository.findById(3L).ifPresent( client -> {
    // el get es otra consulta
    client.getAddresses().removeFirst();
    clientRepository.save(client);
});
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

###### 5. Ejemplo práctico de manejo de una colección

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
##### 7. Operaciones de cascada.

> Se aconsjea que las entidades implemente los méotods Equals & hasCode, que son usados para comparar instancias de este tipo en las colleciones.

Ejemplo común:

```java
@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
@JoinTable(
    name = "CLIENTS_ADDRESSES",
    joinColumns = @JoinColumn(name="client_id"),           // FK hacia Client
    inverseJoinColumns = @JoinColumn(name="address_id"),   // FK hacia Address
    uniqueConstraints = @UniqueConstraint(columnNames = {"address_id"}) // evita que la misma Address se asocie a varios clientes
)
@Builder.Default
public List<Address> addresses = new ArrayList<>();
```

Con:
```java
    uniqueConstraints = @UniqueConstraint(columnNames = {"address_id"}) // 👈 culpable
```

* Ese uniqueConstraints = @UniqueConstraint(columnNames = {"address_id"}) le dice a la BD:
    > “cada address_id solo puede aparecer una vez en la tabla intermedia”.

* Es decir, una dirección no puede pertenecer a más de un cliente.
* Pero en tu código estás asociando address1 y address2 tanto al cliente 3L como al cliente 2L.
* Resultado → al segundo save intenta insertar (client_id=2, address_id=1) y la BD lo bloquea porque address_id=1 ya estaba asociado al cliente 3.

En este caso entonces, el `orphanRemoval = true`, tiene sentido para no dejar huerfanos, y no se podrá crear la dirección y luego el cliente, debe gestionarlo Hiberane, al usar: `cascade = CascadeType.ALL`.

---

1. **Relación `@OneToMany` con `@JoinTable`**

* Existe una **tabla intermedia (`CLIENTS_ADDRESSES`)** que une `Client` ↔ `Address`.
* Cada registro en esa tabla representa la relación.
* Con `uniqueConstraints` sobre `address_id`, se asegura que **una dirección solo puede pertenecer a un cliente**.


**Importante**

En JPA, cuando usas** @OneToMany** con **@JoinTable**, semánticamente significa:

> "Un cliente tiene muchas direcciones, y cada dirección pertenece a un único cliente".

O sea, la cardinalidad real es 1:N, no N:M (**@ManyToMany.**).

---

2. **Cascada (`CascadeType.ALL`)**

* Todas las operaciones que hagas sobre `Client` se propagan a `Address`:

  * `persist` → guarda también las direcciones.
  * `merge` → actualiza las direcciones.
  * `remove` → borra las direcciones asociadas (si no están compartidas).

---

3. **`orphanRemoval = true`**

* Cuando eliminas un objeto hijo de la colección (`addresses.remove(...)`), **Hibernate detecta que la dirección quedó huérfana** y la elimina de la BD.
* Aplica tanto si quitas un elemento de la lista como si reasignas la colección completa.

Ejemplo:

```java
client.getAddresses().remove(address1);
// Hibernate genera:
// DELETE FROM CLIENTS_ADDRESSES WHERE client_id = ? AND address_id = ?
// DELETE FROM ADDRESS WHERE id = ?
```

---

4. **Operaciones comunes**

| Operación en la lista `addresses`    | Efecto en la tabla intermedia `CLIENTS_ADDRESSES`     | Efecto en tabla `ADDRESS` (por orphanRemoval)       |
| ------------------------------------ | ----------------------------------------------------- | --------------------------------------------------- |
| `client.getAddresses().add(addr)`    | INSERT en `CLIENTS_ADDRESSES`                         | INSERT en `ADDRESS` (si es nuevo)                   |
| `client.getAddresses().remove(addr)` | DELETE en `CLIENTS_ADDRESSES`                         | DELETE en `ADDRESS`                                 |
| `client.setAddresses(newList)`       | Borra todas las relaciones anteriores y crea nuevas   | Borra las direcciones antiguas que quedan huérfanas |
| `clientRepository.delete(client)`    | Borra cliente y sus relaciones en `CLIENTS_ADDRESSES` | Borra todas las direcciones asociadas               |

---

5. **Diferencia sin `orphanRemoval`**

* Si **no pones `orphanRemoval = true`**:

  * `addresses.remove(...)` solo borra de la tabla intermedia (`CLIENTS_ADDRESSES`), **pero deja la fila en `ADDRESS`**.
  * Es decir, la dirección no desaparece, solo se “desasocia”.

--- 

6. **Que pasa si:** *“una dirección puede estar en dos clientes”*

> 👉 Eso **cambia completamente la recomendación** sobre `orphanRemoval`.
> 

Si:

* `orphanRemoval = true` significa:
  *“si la entidad hija deja de estar en la colección del padre, Hibernate la borra de la BD”*.

* Entonces, si **una `Address` está asociada a varios `Client`** y borras la relación desde un cliente:

  ```java
  client1.getAddresses().remove(addressX);
  ```

* Hibernate intentará **borrar `addressX` de la tabla `ADDRESS`**, incluso aunque todavía esté asociada a `client2`.
* ❌ Resultado: **datos inconsistentes** (te cargas la dirección usada por otro cliente).

Qué se aconseja en este escenario:

Si una `Address` puede estar asociada a **más de un cliente**:

1. **NO uses `orphanRemoval = true`.**

   * En este caso, la dirección **no depende de un solo cliente**, así que no debería borrarse automáticamente al quitarla de la lista.
   * Lo correcto es que solo se elimine la fila de la **tabla intermedia `CLIENTS_ADDRESSES`**.

2. Maneja las **eliminaciones explícitas**:

   * Si realmente quieres borrar una dirección de la tabla `ADDRESS`, primero asegúrate de que ya no está asociada a ningún cliente.
   * Eso lo puedes hacer con lógica de negocio o con una restricción en BD (`ON DELETE CASCADE` / `ON DELETE RESTRICT`).

3. **Ejemplo recomendado:**

   ```java
   @OneToMany(cascade = CascadeType.ALL) // sin orphanRemoval y UniqueConstraint
   @JoinTable(
       name = "CLIENTS_ADDRESSES",
       joinColumns = @JoinColumn(name="client_id"),
       inverseJoinColumns = @JoinColumn(name="address_id")
   )
   private List<Address> addresses;
   ```

   * Quitar `orphanRemoval = true`.
   * Esto asegura que si quitas una dirección de un cliente, **solo se borra la relación**, no la dirección entera.

En resumen

| Caso                                      | ¿Usar `orphanRemoval`? | Justificación                                                                                      |
| ----------------------------------------- | ---------------------- | -------------------------------------------------------------------------------------------------- |
| **Address pertenece solo a un Client**    | ✅ Sí                   | Address no tiene sentido sin su Client → se elimina.                                               |
| **Address puede estar en varios Clients** | ❌ No                   | Una dirección no debe eliminarse al salir de una colección, puede estar asociada a otros clientes. |

--- 
7. **Buenas prácticas**

1. Usa `orphanRemoval = true` cuando el hijo **no debe existir sin el padre** (ej: `Address` siempre pertenece a un `Client`).
2. Evita `orphanRemoval` si las entidades hijas pueden estar **referenciadas por otras entidades**.
3. Recuerda que `uniqueConstraints` ya impide que la misma dirección se comparta → esto refuerza el caso de uso de `orphanRemoval`.
4. Ten cuidado con `setAddresses(new ArrayList<>())`: se borrarán **todas** las direcciones en cascada.

---



--- 

### @ManyToMany

Una relación muchos a muchos implica que múltiples entidades de un lado pueden estar asociadas con múltiples entidades del otro.
Ejemplo clásico:

Un Estudiante puede estar inscrito en varios Cursos.

Un Curso puede tener varios Estudiantes.

```java
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(
        name = "students_courses", // tabla intermedia
        joinColumns = @JoinColumn(name = "student_id"), // FK en la tabla intermedia hacia Student
        inverseJoinColumns = @JoinColumn(name = "course_id") // FK hacia Course
    )
    private Set<Course> courses = new HashSet<>();
}
```

```java
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @ManyToMany(mappedBy = "courses") // lado inverso
    private Set<Student> students = new HashSet<>();
}
```
#### Puntos de interés
- Tabla intermedia: Hibernate crea una tabla de unión (students_courses) con las claves foráneas de ambos lados.
- Propietario de la relación:
- El lado que define el `@JoinTable` es el **dueño** de la relación.
- El otro lado (_con mappedBy_) es el **inverso** y no gestiona las inserciones/updates.
- Colecciones recomendadas:
    - Usar Set → evita duplicados y el problema de bags.
    - Usar List solo si necesitas un orden explícito (@OrderColumn).
- Cascadas y eliminación:
    - cascade = CascadeType.ALL en un @ManyToMany es peligroso ⚠️, porque puede intentar borrar entidades compartidas por otras relaciones.
    - Normalmente no se usa orphanRemoval = true en @ManyToMany, ya que una entidad puede estar referenciada por múltiples padres.
  
#### Fetching y rendimiento
- Por defecto, @ManyToMany es LAZY (mejor para rendimiento).
- Si se necesita cargar con frecuencia, usar JOIN FETCH en queries específicas.
- Para evitar N+1 queries:
    - Usar @BatchSize(size = N) en la colección.
    - O @Fetch(FetchMode.SUBSELECT) para cargar todas las colecciones en una sola subconsulta.

#### Ejemplo de query con `JOIN FETCH

Esto carga el estudiante y todos sus cursos en una sola consulta.
```java
@NamedQuery(
    name = "Student.findWithCourses",
    query = "SELECT DISTINCT s FROM Student s LEFT JOIN FETCH s.courses WHERE s.id = :id"
)
```

### Excepción "UnSupportedOperationExeption".

En el **contexto de JPA/Hibernate** ocurre algo importante:

1. **Hibernate** necesita** colecciones mutables**
    - Para poder inyectar entidades cargadas desde la BD.
    - Para manejar cambios en cascada (cascade = ALL).
    - Para realizar orphanRemoval (orphanRemoval = true).

2. Si usas *Collecitons.of(...)* o *Collections.unmodifiableList(...)* como atributo de relación, **Hibernate** fallará porque necesita hacer `.add()` o `.remove()` al sincronizar los datos, y lanzará **UnsupportedOperationException**

Ejemplo:
```java
@OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Invoice> invoices = List.of(); // inmutable ❌
```

La estrategia correcta es:

1. Usar colecciones mutables internamente
   ```java
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invoice> invoices = new ArrayList<>();
    ```
2. Exponerlas inmutables en los getters, así proteges la colección de modificaciones externas:
    ```java
    public List<Invoice> getInvoices() {
        return Collections.unmodifiableList(invoices);
    }
    ```
3. Usar métodos de conveniencia dentro de la entidad, que trabajan sobre la colección mutable interna, para añadir elementos en la base de datos:
    ```java
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invoice> invoices = new ArrayList<>();

    // Getter: solo lectura desde fuera
    public List<Invoice> getInvoices() {
        return Collections.unmodifiableList(invoices);
    }

    // Método de conveniencia para añadir
    public Client addInvoice(Invoice invoice) {
        this.invoices.add(invoice);       // modifica la lista interna
        invoice.setClient(this);          // mantiene relación bidireccional
        return this;
    }

    // Método de conveniencia para eliminar
    public Client removeInvoice(Invoice invoice) {
        this.invoices.remove(invoice);
        invoice.setClient(null);
        return this;
    }
    ```
    
Con esto:

* Hibernate puede modificar la colección internamente (_sigue pudiendo mutar la lista cuando sincroniza datos_), al añadir o eliminar elementos a través de métodos explícitos (addInvoice / removeInvoice).
* El resto de la aplicación no puede hacer .add() o .remove() directamente (_desde fuera, el consumidor no puede modificar directamente la colección `unmodifiableList`_)
* Ventajas de colecciones inmutables en general
    * Seguridad → Nadie puede modificar accidentalmente los datos.
    * Hilos → Son seguras para compartir entre hilos (no necesitan sincronización).
    * Simplifican el diseño → Evitan efectos colaterales.
    * Consistencia → Garantiza que las relaciones bidireccionales estén siempre consistentes.

Por tanto:
- ❌ No usar colecciones inmutables directamente en entidades JPA → Hibernate necesita mutarlas.
- ✅ Sí usarlas en DTOs o exposiciones externas → protegen contra modificaciones no deseadas.
- ✅ Patrón recomendado: mutable internamente + inmutable en getters.
- 
---

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


---

### @JoinTable

`@JoinTable` se utiliza para **configurar la tabla intermedia** en relaciones:

- **Muchos a muchos (`@ManyToMany`)**  
- En algunos casos, **unidireccionales `@OneToMany`** si no se quiere poner la FK en la entidad hija.

Permite definir explícitamente:

* **Nombre de la tabla intermedia**.
* **Columnas que actúan como clave foránea** para cada lado de la relación.
* **Restricciones adicionales** como `unique`, `nullable` o `foreignKey`.

---

#### Sintaxis básica: @ManyToMany

```java
@ManyToMany
@JoinTable(
    name = "student_course",                     // Nombre de la tabla intermedia
    joinColumns = @JoinColumn(name = "student_id"),       // FK hacia la entidad actual
    inverseJoinColumns = @JoinColumn(name = "course_id")  // FK hacia la entidad opuesta
)
private List<Course> courses;
````

**Explicación:**

| Elemento             | Significado                                                              |
| -------------------- | ------------------------------------------------------------------------ |
| `name`               | Nombre de la tabla intermedia                                            |
| `joinColumns`        | Columnas que apuntan a la **entidad dueña** (lado actual de la relación) |
| `inverseJoinColumns` | Columnas que apuntan a la **entidad inversa**                            |

* La tabla intermedia normalmente contiene solo **FKs** y opcionalmente **otras columnas adicionales** si quieres atributos extra en la relación.

---

#### Ejemplo completo: @ManyToMany

```java
@Entity
public class Student {
    @Id @GeneratedValue
    private Long id;
    private String name;

    @ManyToMany
    @JoinTable(
        name = "student_course",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses = new ArrayList<>();
}

@Entity
public class Course {
    @Id @GeneratedValue
    private Long id;
    private String title;

    @ManyToMany(mappedBy = "courses")
    private List<Student> students = new ArrayList<>();
}
```

**Resultado en BD:**

| Tabla `student_course` | student\_id | course\_id |
| ---------------------- | ----------- | ---------- |
| 1                      | 1           | 101        |
| 2                      | 1           | 102        |
| 3                      | 2           | 101        |

---

#### Sintaxis básica: @OneToMany unidireccional con @JoinTable

```java
@Entity
public class Client {
    @Id @GeneratedValue
    private Long id;
    private String name;

    // Crea una tabla intermida
    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinTable(
            name = "CLIENTS_ADDRESSES",
            joinColumns = @JoinColumn(name="client_id"), // FK_client_id in Address (class target)
            inverseJoinColumns = @JoinColumn(name="address_id"), //FK_address_id in Client (this class)
            uniqueConstraints = @UniqueConstraint(columnNames = {"address_id"}) // Not allow mult-value in Address
    )
    @Builder.Default
    public List<Address> addresses = new ArrayList<>();
}

@Entity
public class Address {
    @Id @GeneratedValue
    private Long id;
    private String street;
}
```

**Explicación:**

| Elemento             | Significado                                          |
| -------------------- | ---------------------------------------------------- |
| `name`               | Nombre de la tabla intermedia (`client_address`)     |
| `joinColumns`        | Columnas que apuntan al **lado padre** (`client_id`) |
| `inverseJoinColumns` | Columnas que apuntan al **lado hijo** (`address_id`) |

* La tabla `client_address` contendrá pares `(client_id, address_id)` representando la relación.
* Útil para relaciones **unidireccionales** donde no se desea modificar la entidad hija.

---

#### Diferencias clave: @ManyToMany vs @OneToMany + @JoinTable

| Característica         | @ManyToMany                       | @OneToMany + @JoinTable              |
| ---------------------- | --------------------------------- | ------------------------------------ |
| Relación               | Muchos a muchos                   | Uno a muchos                         |
| FK en entidad hija     | No directamente, tabla intermedia | No directamente, tabla intermedia    |
| Bidireccional opcional | Sí (mappedBy)                     | No se usa mappedBy en unidireccional |
| Tabla intermedia       | Obligatoria                       | Opcional (si no se usa mappedBy)     |

---

#### 🔹 Propiedades adicionales de @JoinTable

| Propiedad            | Tipo                | Descripción                                  |
| -------------------- | ------------------- | -------------------------------------------- |
| `name`               | String              | Nombre de la tabla intermedia                |
| `joinColumns`        | JoinColumn\[]       | Columnas de la entidad propietaria           |
| `inverseJoinColumns` | JoinColumn\[]       | Columnas de la entidad inversa               |
| `uniqueConstraints`  | UniqueConstraint\[] | Restricciones de unicidad sobre la tabla     |
| `foreignKey`         | ForeignKey          | Define el nombre y comportamiento de las FKs |

---

#### Buenas prácticas

1. Definir siempre **joinColumns** e **inverseJoinColumns** explícitamente.
2. Usar `mappedBy` en el lado inverso para relaciones bidireccionales y evitar **tabla duplicada**.
3. Para relaciones con atributos extra en la relación, crear una **entidad intermedia** en lugar de depender solo de `@JoinTable`.
4. En **@OneToMany unidireccional**, usar tabla intermedia solo si no se desea FK en la entidad hija; de lo contrario, `mappedBy` con FK es más limpio.

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