¡Perfecto! 😄 Aquí tienes una versión **completa y organizada** de los apuntes sobre transacciones en Spring/JPA, con índice, proxies, tipos de transacciones, propagaciones, ejemplos de *dirty read* y *non-repeatable read*, `@Version` y diagramas conceptuales.

---

# 📚 Apuntes completos sobre Transacciones en Spring + JPA

## Índice

- [📚 Apuntes completos sobre Transacciones en Spring + JPA](#-apuntes-completos-sobre-transacciones-en-spring--jpa)
  - [Índice](#índice)
  - [1. Qué es una transacción](#1-qué-es-una-transacción)
  - [2. El proxy y `@Transactional`](#2-el-proxy-y-transactional)
    - [⚠️ Problema común](#️-problema-común)
      - [Ejemplo que falla](#ejemplo-que-falla)
      - [Soluciones](#soluciones)
  - [3. Tipos de transacciones](#3-tipos-de-transacciones)
  - [4. Propagaciones (`Propagation`)](#4-propagaciones-propagation)
  - [5. Aislamiento (`Isolation`) + Ejemplos](#5-aislamiento-isolation--ejemplos)
    - [5.1 Dirty Read](#51-dirty-read)
      - [Código ejemplo](#código-ejemplo)
      - [Flujo](#flujo)
    - [5.2 Non-Repeatable Read](#52-non-repeatable-read)
      - [Código ejemplo](#código-ejemplo-1)
      - [Flujo](#flujo-1)
    - [5.3 Diagramas conceptuales](#53-diagramas-conceptuales)
  - [6. Uso de `@Version` (concurrencia optimista)](#6-uso-de-version-concurrencia-optimista)
      - [Código ejemplo](#código-ejemplo-2)
  - [7. Buenas prácticas](#7-buenas-prácticas)
  - [Ejemplo de Transaccion + Proxy: 📊 Diagrama de flujo de transacción en `ProductServiceImpl`](#ejemplo-de-transaccion--proxy--diagrama-de-flujo-de-transacción-en-productserviceimpl)
    - [1. Escenario](#1-escenario)
    - [2. Explicación de transacciones y proxy](#2-explicación-de-transacciones-y-proxy)
    - [3. Diagrama conceptual de flujo](#3-diagrama-conceptual-de-flujo)
    - [4. Nota sobre `@Version` (optimistic locking)](#4-nota-sobre-version-optimistic-locking)
    - [5. Consideraciones sobre propagación](#5-consideraciones-sobre-propagación)
    - [6. Resumen de la línea de tiempo](#6-resumen-de-la-línea-de-tiempo)

---

## 1. Qué es una transacción

* Unidad de trabajo **atómica**: todo o nada.
* Garantiza **ACID**:

  * **Atomicidad**, **Consistencia**, **Aislamiento**, **Durabilidad**.
* En Spring Boot + JPA, se gestionan con `@Transactional`.

---

## 2. El proxy y `@Transactional`

* Spring crea un **proxy** del bean anotado con `@Transactional`.
* El proxy intercepta llamadas y gestiona la transacción:

  1. Abrir la transacción antes del método.
  2. Ejecutar el método.
  3. Hacer commit o rollback al terminar.

### ⚠️ Problema común

Si llamas a un método transaccional **desde otro método de la misma clase con `this`**, **no pasa por el proxy** → la transacción no se activa.

#### Ejemplo que falla

```java
@Service
public class ProductService {
    public void save(ProductDto dto) {
        this.persist(dto); // no pasa por el proxy
    }

    @Transactional
    public void persist(ProductDto dto) {
        // la transacción NO se inicia
    }
}
```

#### Soluciones

1. **Inyectar otro bean** que tenga el método transaccional.
2. **Auto-inyección del mismo bean** (`private ProductService self;`).
3. `AopContext.currentProxy()` (menos recomendado).

---

## 3. Tipos de transacciones

* **Lectura (`readOnly = true`)** → consultas optimizadas.
* **Lectura-escritura (default)** → permite modificaciones (`INSERT/UPDATE/DELETE`).
* **Programáticas** → `TransactionTemplate` o `EntityManager.getTransaction()`.

Ejemplo:

```java
@Transactional(readOnly = true)
public List<ProductDto> findAll() { ... }
```

---

## 4. Propagaciones (`Propagation`)

| Propagation          | Comportamiento                                             |
| -------------------- | ---------------------------------------------------------- |
| `REQUIRED` (default) | Usa la transacción actual o crea una nueva si no hay.      |
| `REQUIRES_NEW`       | Suspende la actual y abre una nueva.                       |
| `SUPPORTS`           | Usa transacción si existe, si no ejecuta sin transacción.  |
| `NOT_SUPPORTED`      | Suspende cualquier transacción activa.                     |
| `MANDATORY`          | Exige transacción activa; si no, lanza excepción.          |
| `NEVER`              | Falla si hay transacción activa.                           |
| `NESTED`             | Crea un punto de rollback dentro de la transacción actual. |

---

## 5. Aislamiento (`Isolation`) + Ejemplos

| Isolation                  | Qué evita                                           |
| -------------------------- | --------------------------------------------------- |
| `READ_UNCOMMITTED`         | Ninguno → *dirty reads* posibles.                   |
| `READ_COMMITTED` (default) | Evita *dirty reads*.                                |
| `REPEATABLE_READ`          | Evita *dirty reads* y *non-repeatable reads*.       |
| `SERIALIZABLE`             | Evita todo, máximo aislamiento, menor concurrencia. |

---

### 5.1 Dirty Read

* Una transacción lee datos **no confirmados** de otra transacción.

#### Código ejemplo

```java
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public BigDecimal readPrice(Long id) {
    return repository.findById(id).map(Product::getPrice).orElse(BigDecimal.ZERO);
}

@Transactional
public void updatePrice(Long id, BigDecimal price) {
    repository.findById(id).ifPresent(p -> p.setPrice(price));
}
```

#### Flujo

| Paso | Tx A                                 | Tx B                             |
| ---- | ------------------------------------ | -------------------------------- |
| 1    | Actualiza `price = 200` (sin commit) |                                  |
| 2    |                                      | Lee `price = 200` → *dirty read* |
| 3    | Rollback Tx A                        |                                  |
| 4    |                                      | Precio real vuelve a `100`       |

---

### 5.2 Non-Repeatable Read

* Dentro de la misma transacción, un `SELECT` puede devolver resultados distintos por commit de otra transacción.

#### Código ejemplo

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public BigDecimal readPriceTwice(Long id) {
    BigDecimal firstRead = repository.findById(id).map(Product::getPrice).orElse(BigDecimal.ZERO);
    try { Thread.sleep(5000); } catch (InterruptedException e) {}
    BigDecimal secondRead = repository.findById(id).map(Product::getPrice).orElse(BigDecimal.ZERO);
    System.out.println("First: " + firstRead + ", Second: " + secondRead);
    return secondRead;
}

@Transactional
public void updatePrice(Long id, BigDecimal price) {
    repository.findById(id).ifPresent(p -> p.setPrice(price));
}
```

#### Flujo

| Paso | Tx A                                            | Tx B                            |
| ---- | ----------------------------------------------- | ------------------------------- |
| 1    | Lee `price = 100`                               |                                 |
| 2    | Espera (sleep)                                  | Actualiza `price = 200`, commit |
| 3    | Lee nuevamente → `price = 200` (non-repeatable) |                                 |

---

### 5.3 Diagramas conceptuales

**Dirty Read**

```
Tx A: |--- update price=200 ---X rollback
Tx B:         |--- read price=200 (dirty) ---|
```

**Non-Repeatable Read**

```
Tx A: |--- read price=100 ---|           |--- read price=200 ---|
Tx B:         |--- update price=200 ---| commit
```

---

## 6. Uso de `@Version` (concurrencia optimista)

* Evita que dos transacciones sobrescriban cambios sin querer.
* Se añade un campo `version` a la entidad.
* Hibernate incrementa el `version` en cada `UPDATE`.

#### Código ejemplo

```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal price;

    @Version
    private Integer version; // control de concurrencia
}
```

**Flujo:**

1. Usuario A lee `version=1`.
2. Usuario B lee `version=1`.
3. Usuario A actualiza → `version=2`.
4. Usuario B intenta actualizar → `OptimisticLockException`.

---

## 7. Buenas prácticas

* `@Transactional` en **servicios**, no en repositorios.
* Métodos **públicos**.
* `readOnly = true` para consultas.
* Evitar `@Transactional` en métodos privados/protegidos.
* Cortar la transacción lo más rápido posible.
* `@Version` en entidades críticas.
* Elegir propagación adecuada (`REQUIRES_NEW` para auditorías/logs).
* Mapear entidades a DTOs dentro de la transacción para evitar `LazyInitializationException`.

--- 

## Ejemplo de Transaccion + Proxy: 📊 Diagrama de flujo de transacción en `ProductServiceImpl`

### 1. Escenario

Método principal: `update(id, productDto)`

Flujo de llamadas:

```
update(id, productDto) → mapAndSaveProduct(product, productDto) → productMapper.toDto(product)
```

---

### 2. Explicación de transacciones y proxy

* `update()` está anotado con `@Transactional` → **abre la transacción**.
* `mapAndSaveProduct()` es `protected @Transactional` pero se llama **internamente con this** → no pasa por proxy, **no abre otra transacción**, pero está **dentro de la transacción de `update()`**.
* `productMapper.toDto()` no necesita transacción, solo mapea los campos del `Product` a DTO.

---

### 3. Diagrama conceptual de flujo

```
Tx: [update() - TRANSACTION START]
┌───────────────────────────────────────────┐
│                                           │
│   findById(id)                            │ ← SELECT producto
│                                           │
│   mapAndSaveProduct(product, productDto)  │
│   ┌────────────────────────────────────┐  │
│   │ product.setName(), setDescription()│  │
│   │ product.setPrice()                 │  │
│   │ repository.save(product)           │  │ ← INSERT/UPDATE
│   └────────────────────────────────────┘  │
│                                           │
│   productMapper.toDto(product)            │ ← map -> DTO (sin transacción)
│                                           │
└───────────────────────────────────────────┘
Tx: [update() - COMMIT]
```

---

### 4. Nota sobre `@Version` (optimistic locking)

* Si `Product` tiene `@Version`:

```
Tx A reads Product(version=1)
Tx B reads Product(version=1)

Tx A updates -> version=2
Tx B updates -> OptimisticLockException (version mismatch)
```

* En tu flujo, si dos `update()` concurrentes intentan modificar el mismo producto, **Spring + JPA lanzará `OptimisticLockException` automáticamente** al hacer `repository.save()`.

---

### 5. Consideraciones sobre propagación

* `mapAndSaveProduct()` usa `Propagation.MANDATORY` → significa:

  * Si hay transacción activa, **se une a ella** (lo que pasa aquí).
  * Si no hubiera transacción, lanza una excepción.
* En este caso, **siempre se ejecuta dentro de la transacción de `update()`**, así que no corta la transacción.

---

### 6. Resumen de la línea de tiempo

| Paso | Acción                  | Transacción          | Resultado            |
| ---- | ----------------------- | -------------------- | -------------------- |
| 1    | `update()` inicia       | Tx abierta           | START                |
| 2    | `findById()`            | Tx activa            | Producto cargado     |
| 3    | `mapAndSaveProduct()`   | Tx activa (no nueva) | Producto actualizado |
| 4    | `productMapper.toDto()` | Tx activa            | DTO creado           |
| 5    | `update()` finaliza     | Tx commit            | Cambios persistidos  |

---