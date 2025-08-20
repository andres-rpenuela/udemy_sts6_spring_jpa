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

Una persona puede tener varios libros.

```java
@OneToMany(mappedBy = "person")
private List<Book> books;

@ManyToOne
@JoinColumn(name = "person_id")
private Person person;
```

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