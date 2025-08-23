# 📘 Apuntes Extendidos: `MultipleBagFetchException`

---

## 📑 Índice

- [📘 Apuntes Extendidos: `MultipleBagFetchException`](#-apuntes-extendidos-multiplebagfetchexception)
  - [📑 Índice](#-índice)
  - [1. Introducción](#1-introducción)
  - [2. Por qué ocurre el error](#2-por-qué-ocurre-el-error)
  - [3. Soluciones](#3-soluciones)
    - [3.1 Usar `Set`](#31-usar-set)
    - [3.2 Usar `@OrderColumn` con `List`](#32-usar-ordercolumn-con-list)
    - [3.3 Dividir en múltiples queries](#33-dividir-en-múltiples-queries)
    - [3.4 Usar `@BatchSize`](#34-usar-batchsize)
  - [4. Comparativa de enfoques](#4-comparativa-de-enfoques)
  - [5. Diagrama de Decisión](#5-diagrama-de-decisión)
  - [6. Resumen práctico](#6-resumen-práctico)
- [Apunte](#apunte)
  - [JOIN y JOIN FETH](#join-y-join-feth)
    - [JOIN](#join)
    - [JOIN FETCH](#join-fetch)
    - [Ejemplo Práctico](#ejemplo-práctico)
    - [Explicación de `BathSize` para consultas con `Join`.](#explicación-de-bathsize-para-consultas-con-join)

---

## 1. Introducción

El error `MultipleBagFetchException` aparece cuando Hibernate intenta **hacer `fetch join` sobre más de una colección tipo `List` sin índice (`Bag`)**.
Este error es común en entidades con varias relaciones `@OneToMany` o `@ManyToMany`.

---

## 2. Por qué ocurre el error

Ejemplo típico:

```java
@Query("select c from Client c left join fetch c.invoices left join fetch c.addresses where c.id = :id")
Optional<Client> findOne(Long id);
```

* `c.invoices` es `List<Invoice>`
* `c.addresses` es `List<Address>`
* Hibernate → ❌ `MultipleBagFetchException`

---

## 3. Soluciones

### 3.1 Usar `Set`
Si no necesitas mantener un orden específico:

```java
@OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
private Set<Invoice> invoices = new HashSet<>();

@OneToMany(fetch = FetchType.LAZY)
private Set<Address> addresses = new HashSet<>();
```

✅ Hibernate permite múltiples `fetch join` sobre `Set`.

---

### 3.2 Usar `@OrderColumn` con `List`
Si necesitas conservar el orden de inserción o un índice explícito:

```java
@OneToMany(mappedBy = "client")
@OrderColumn(name = "invoice_order") // crea una columna en DB para guardar el orden
private List<Invoice> invoices = new ArrayList<>();

@OneToMany
@OrderColumn(name = "address_order")
private List<Address> addresses = new ArrayList<>();
```

✅ Hibernate las trata como **listas ordenadas**, no como `bags`.

---

### 3.3 Dividir en múltiples queries
Cargar cada relación en queries independientes:

```java
@Query("select c from Client c left join fetch c.invoices where c.id = :id")
Optional<Client> findOneWithInvoices(Long id);

@Query("select c from Client c left join fetch c.addresses where c.id = :id")
Optional<Client> findOneWithAddresses(Long id);
```
Luego en el servicio puedes combinar la información.

✅ Más control, pero más consultas.

---

### 3.4 Usar `@BatchSize`
Si no necesitas fetch join, puedes dejar las relaciones en LAZY y usar @BatchSize:

```java
@OneToMany(mappedBy = "client")
@BatchSize(size = 10)
private List<Invoice> invoices;

@OneToMany
@BatchSize(size = 10)
private List<Address> addresses;
```
Cuando se acceden, Hibernate hace IN en lotes, evitando el N+1.

✅ Reduce el `N+1`, carga diferida optimizada.
❌ No es un único query.

---

## 4. Comparativa de enfoques

| Estrategia              | Ventajas                            | Desventajas                    | Uso recomendado                  |
| ----------------------- | ----------------------------------- | ------------------------------ | -------------------------------- |
| **Set**                 | Permite varios `fetch join`, simple | No garantiza orden             | Cuando no importa el orden       |
| **List + @OrderColumn** | Mantiene orden, evita error         | Columna extra, más complejidad | Cuando necesitas orden explícito |
| **Múltiples queries**   | Control fino, sin errores           | Varias consultas a DB          | Relaciones grandes o pesadas     |
| **@BatchSize**          | Evita N+1, lazy optimizado          | No es un solo query            | Acceso gradual a colecciones     |

---

## 5. Diagrama de Decisión

```text
                    ┌───────────────────────────────────┐
                    │ ¿Tienes más de una colección List │
                    │ en fetch join?                    │
                    └───────────────┬───────────────────┘
                                    │
                        ┌───────────▼───────────┐
                        │  Sí, dará error       │
                        └───────────┬───────────┘
                                    │
       ┌────────────────────────────┴──────────────────────────────┐
       │                                                           │
┌──────▼───────┐                                       ┌───────────▼───────────┐
│ ¿Importa el  │                                       │ ¿Necesitas mantener   │
│ orden?       │                                       │ la colección como List│
└──────┬───────┘                                       └───────────┬───────────┘
       │                                                           │
   ┌───▼───────┐                                              ┌───▼─────────────────┐
   │ Usa Set   │                                              │ Usa @OrderColumn    │
   │ (rápido)  │                                              │ (columna extra)     │
   └───────────┘                                              └─────────────────────┘
                                                                │
                                      ┌─────────────────────────▼───────────────────────────┐
                                      │ ¿Las colecciones son grandes y no necesitas join?   │
                                      └───────────────────────┬─────────────────────────────┘
                                                              │
                                          ┌───────────────────▼──────────────────┐
                                          │ Usa @BatchSize (lazy optimizado)     │
                                          └───────────────────┬──────────────────┘
                                                              │
                                       ┌──────────────────────▼───────────────────────┐
                                       │ Si quieres máximo control → Queries separadas│
                                       └──────────────────────────────────────────────┘
```

---

## 6. Resumen práctico

* ✅ **Si no importa el orden →** usar `Set`.
* ✅ **Si importa el orden →** usar `List + @OrderColumn`.
* ✅ **Si las colecciones son grandes →** queries separadas.
* ✅ **Si accedes gradualmente y no necesitas fetch join →** `@BatchSize`.

---

# Apunte

Se puede usar tambien `NameQueries`:

```java
// En la entidad Client.java
@NamedQueries(
        @NamedQuery(
                // si se pone fech da el mismo error: org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
                name = "Client.findWithInvoicesAndAddresses",
                query = "SELECT DISTINCT c FROM Client c " +
                        "LEFT JOIN FETH c.invoices " +
                        "LEFT JOIN FETH c.addresses " +
                        "WHERE c.id = :id"
        )
)
....
```

```java
// En el repositorio ClientRepository
@Repository
public interface ClientRepository extends JpaRepository<Client,Long> {

    @Query(value = "select c from Client c left join fetch c.addresses where c.id = ?1" )
    Optional<Client> finOneWithAddresses(Long id);

    @Query(value = "select c from Client c left join fetch c.invoices where c.id = ?1" )
    Optional<Client> finOneWithInvoices(Long id);

    // org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags (hay varias soluciones)
    //@Query(value = "select c from Client c left join fetch c.invoices left join fetch c.addresses where c.id = ?1")
    //Optional<Client> findOne(Long id);
    // Solucion con name query
    @Query(name = "Client.findWithInvoicesAndAddresses")
    Optional<Client> findOne(@Param("id") Long id);

}
```
Pero también dará el mismo error:

```bash
org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
```

Y es que si usa más de un **join fecth** en una misma consulta sobre colecciones que no son **Set** y no estan anotados con **OrderColumn** para que creé una campo en la base de datos e Hibarnate pueda gestionar la operación, las tratará como una bolsa.

## JOIN y JOIN FETH

En el contexto de **JPA** e **Hibernate**:

**JOIN** en una consulta **JPQL** o ** Criteria API** simplemente une las tablas y permite referenciar las propiedades de las entidades relacionadas, pero no carga los datos asociados inmediatamente, a menos que estén configurados como 'eager' por defecto.

En cambio, **JOIN FETCH** carga explícitamente las entidades o colecciones relacionadas en la misma consulta, evitando la necesidad de consultas posteriores y reduciendo los problemas de carga perezosa (lazy loading), lo que mejora el rendimiento al minimizar los viajes a la base de datos. 

### JOIN


**Propósito:**
Unir tablas basándose en sus relaciones definidas (por ejemplo, one-to-many). 

**Comportamiento:**
Generalmente, no carga las entidades o colecciones asociadas en la consulta inicial. La carga de estas relaciones se maneja de manera perezosa (lazy), lo que significa que los datos se cargan solo cuando son accedidos. 

Es decir:
- Realiza la unión entre entidades, pero no inicializa las colecciones (Lazy por defecto).
- Cuando accedes a la relación, Hibernate dispara otra query. (Problmea N+1)

**Cuándo usarlo:**
Cuando solo necesitas los datos de la tabla principal en la consulta inicial y no necesitas acceder a los datos relacionados de inmediato. También es útil cuando las relaciones están configuradas para ser 'eager' o cuando no quieres cargar datos que no necesitas. 

**Ejemplo:**

```java
@Query("SELECT c FROM Client c LEFT JOIN c.invoices WHERE c.id = :id")
Optional<Client> findOne(Long id);
```

Flujo de ejecución:

1. `findOne(3L)`

```sql
select c.id, c.name, c.last_name
from clients c
left join invoices i on c.id = i.client_id
where c.id = ?
```

2. client.getInvoices()

```sql
select i.client_id, i.id, i.amount, i.description
from invoices i
where i.client_id = ?
```

✅ Ventaja: Evita explosión de resultados cartesianos.
⚠️ Inconveniente: Puede producir **N+1 queries** si accedes a muchas relaciones.

### JOIN FETCH
**Propósito:**
Cargar de forma "eager" (ansiosa) los datos de la entidad o colección relacionada como parte de la consulta principal. 

**Comportamiento:**
Garantiza que las instancias de la entidad relacionada se obtengan en la misma consulta. Esto significa que la relación se resuelve inmediatamente, independientemente de si está configurada como 'eager' o 'lazy' por defecto. 

Es decir:
- Trae al cliente y todas sus relaciones en una sola query.
- Obliga a inicializar colecciones `Lazy`.
- Puede producir duplicados (por eso se suele usar `DISTINCT`).
- **No funciona con múltiples** `List` (**bags**) → genera `MultipleBagFetchException`.
- 
**Cuándo usarlo:**
Para evitar la necesidad de múltiples viajes a la base de datos al acceder a los datos de la relación. 
Para prevenir excepciones como LazyInitializationException que pueden ocurrir si se intenta acceder a datos perezosos después de que la sesión del EntityManager o Hibernate se haya cerrado. 

**Ejemplo:**

```java
@Query("SELECT DISTINCT c FROM Client c " +
       "LEFT JOIN FETCH c.invoices " +
       "LEFT JOIN FETCH c.addresses " +
       "WHERE c.id = :id")
Optional<Client> findOne(Long id);
```

Flujo de ejecución:

1. `findOne(3L)
```sql
select distinct c.id, c.name, c.last_name,
       i.id, i.amount, i.description,
       a.id, a.street, a.number
from clients c
left join invoices i on c.id = i.client_id
left join clients_addresses ca on c.id = ca.client_id
left join address a on ca.address_id = a.id
where c.id = ?
```

✅ Ventaja: Evita el problema N+1.
⚠️ Inconveniente: Si tienes dos colecciones tipo List, lanza MultipleBagFetchException.

---- 
### Ejemplo Práctico 

```java
/**
 * Diferencia entre JOIN y JOIN FETCH:
 *
 * - JOIN: realiza la consulta al cliente, pero las colecciones (invoices, addresses) se cargan
 *   cuando se accede a ellas -> ejecuta queries adicionales.
 *
 * - JOIN FETCH: trae cliente y colecciones en una sola consulta. Evita N+1, pero puede
 *   provocar MultipleBagFetchException si se aplican varios fetch a List.
 */
@Transactional
public void manyToOneAboutAClientExistJoinProperties() {
    // Consulta cliente con joins
    Client client = clientRepository.findOne(3L).get();

    // Acceso a colecciones con JOIN (lazy -> dispara queries adicionales)
    client.getInvoices().forEach(System.out::println);

    // Crear nuevas facturas y asociarlas al cliente
    Invoice invoice3 = Invoice.builder().amount(BigDecimal.valueOf(20000)).build();
    Invoice invoice4 = Invoice.builder().amount(BigDecimal.valueOf(30000)).build();

    // addInvoice hace dos cosas:
    // 1. Agrega la factura a la lista de facturas del cliente
    // 2. Asigna el cliente a la factura (relación bidireccional)
    client.addInvoice(invoice3).addInvoice(invoice4);

    client.getInvoices().forEach(System.out::println);
}
```

---

### Explicación de `BathSize` para consultas con `Join`.

✅ @BatchSize(size = N > 1): Optimiza consultas cargando colecciones en lotes.

❌ @BatchSize(size = 1): No optimiza nada, es casi equivalente a Lazy normal.

⚠️ Usar con cuidado: muy útil cuando esperas acceder a muchas colecciones en memoria (ej. clientes con facturas), porque reduce el número de queries.

El `@BatiSize`, se reflecja sobre operciones con `where field in ( ?,?,.. )` 

- Sin @BatchSize (Lazy por defecto)

```java
```

```sql
select * from clients where id in (1,2,3); 
-- luego para cada cliente:
select * from invoices where client_id = 1;
select * from invoices where client_id = 2;
select * from invoices where client_id = 3;
```

➡️ 1 query por cada cliente → N+1 problem.

- Con @BatchSize(size = 10)
  
```sql
select * from clients where id in (1,2,3);
select * from invoices where client_id in (1,2,3);
```

➡️ Hibernate junta las colecciones en una sola query → mucho más eficiente.


- Con @BatchSize(size = 1)

```sql
select * from clients where id in (1,2,3);
-- luego igual que lazy normal:
select * from invoices where client_id = 1;
select * from invoices where client_id = 2;
select * from invoices where client_id = 3;
```

➡️ No hay batching real, solo "una por una".

