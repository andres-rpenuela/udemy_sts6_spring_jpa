package com.codearp.application.demospring_boot3_jpa_relationship.runner;

import com.codearp.application.demospring_boot3_jpa_relationship.EnrollmentService;
import com.codearp.application.demospring_boot3_jpa_relationship.domains.*;
import com.codearp.application.demospring_boot3_jpa_relationship.repositories.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
@AllArgsConstructor
public class InitDataRunner implements CommandLineRunner {

    private final ClientRepository clientRepository;

    private final InvoiceRepository invoiceRepository;
    private final AddressRepository addressRepository;

    private final ClientDetailsRepository clientDetailsRepository;
    @Override
    public void run(String... args) throws Exception {
        // ### EXAMPLE ManyToOne() (Bidireccional) ###
        //manyToOne();
        //manyToOneAboutAClientExist();
        //manyToOneAboutAClientExistJoinProperties(); // ejemplo de join fecth con order_colum, y join con batchSize

        // ### EXAMPLE OneToMany() (Unidreccional) ###
        //oneToMany();
        //oneToManyAboutAClientExist();

        // ### EXAMPLE remove OneToMany(casacade= Cascade.All )
        //oneToManyAddressesShared(); // Metodo no permteidio, relación 1 <-> N
        //removeInOneToMany();

        // ### EXAMPLE remove OneToMany bidireccional
        //removeInvoiceBidireccionalFindById();

        // ### EXAMPLE oneToOne
        // Comentar en ClientDetails el mappeby a Client
        //exampleOneToOne();
        //exampleOneToOneAboutClientExists();

        // Descomentar en ClientDetails el maapeby a Client
        //exampleOneToOneAboutClientExistsBidireccional();
        //removeClientDetailsOneToOneBidirecciontal();

        // Ejmplo ManyToMany
        manyToMany();
    }

    /**
     * Ejemplo de codificaicon "ManyToOne" (bidireccional)
     * Crea un cliente nuevo y le asocia una factura nueva
     */
    @Transactional
    protected void manyToOne(){
        //1.  Crear el cliente
        Client client = Client.builder().name("Pedro").lastName("Sanchez").build();
        clientRepository.save(client);

        //2.  Crear la factura y se referencia al cliente.
        Invoice invoice = Invoice.builder().amount(BigDecimal.valueOf(10000)).client(client).build();
        Invoice invoice2 = invoiceRepository.save(invoice);

        // ------------------------------------------------------
        // 📘 EJEMPLO: Comportamiento de save() y relaciones OneToMany
        // ------------------------------------------------------

        // ✅ Ambos objetos (invoice e invoice2) son el MISMO objeto en memoria.
        // Esto se debe a que "save(entity)" devuelve la entidad administrada por el contexto de persistencia.
        // Es decir, modifica el objeto pasado como parámetro y le asigna el ID generado.
        System.out.println(invoice);
        // Ejemplo salida: Invoice{id=1, description='null', amount=10000, client={id=4, name='Pedro', lastName='SANCHEZ'}}
        System.out.println("invoice2 == invoice: " + invoice2.equals(invoice));
            // true → porque apuntan a la misma entidad administrada por JPA.


        // ✅ Relación OneToMany entre Client → Invoice
        System.out.println("Facturas del cliente: " + client.getName());
        // IMPORTANTE: no se hace client.setInvoices(invoice).
        // Por eso, el objeto "client" todavía NO tiene las facturas asociadas en memoria,
        // aunque en BBDD sí que se haya guardado la relación.

        System.out.println("Del objeto client en memoria (ANTES de hacer un find): ");
        client.getInvoices().forEach(System.out::println);
        // 🔴 Vacío → no se ha refrescado desde BBDD, la lista está desincronizada.


        // ✅ Ahora consultamos el cliente en la base de datos
        System.out.println("Del objeto client en BBDD (usando find): ");
        clientRepository.findAll().stream()
                .filter(result -> client.getName().equals(result.getName()))
                .findFirst()
                .map(Client::getInvoices)
                .get()
                .forEach(System.out::println);
        // Aquí SÍ aparecen las facturas, porque el objeto viene desde la BBDD con la relación cargada.


    // ✅ Revisamos nuevamente el objeto "client" en memoria (después del find).
        System.out.println("Del objeto client en memoria (DESPUÉS de hacer un find, sin hacer setInvoices()): ");
        client.getInvoices().forEach(System.out::println);
    // 🔴 Sigue vacío → porque nunca se reasignó client.setInvoices().
    // El objeto en memoria está desincronizado respecto al estado en la BBDD.


    // ✅ Finalmente, si asignamos manualmente la relación en memoria:
        client.setInvoices(Arrays.asList(invoice));

        System.out.println("Del objeto client en memoria (tras hacer setInvoices() manualmente): ");
        client.getInvoices().forEach(System.out::println);
    // ✅ Ahora sí muestra la factura, porque se forzó la asignación en la entidad en memoria.

    }

    /**
     * Ejemplo de codificaicon "ManyToOne" (bidireccional)
     * Busca un clinete existente en la BBDD y le asocia una factura nueva
     */
    @Transactional
    protected void manyToOneAboutAClientExist() {
        // 1️⃣ Recuperar un cliente existente de la base de datos
        // Esto devuelve un objeto Managed dentro de la sesión de Hibernate
        Client client = clientRepository.findById(1L).get(); // puede dar problemas de Lazy (estamo en contexto de consola no de web ->"CommandLineRunner")
        //Client client = clientRepository.finOneWithInvoices(1L).get(); // devuevle cliente con facturas
        //Client client = clientRepository.findOne(1L).get(); // devuevle cliente con direcciones y facturas (join fetch con order_column)

        // 2️⃣ Crear facturas y asignarlas directamente al cliente
        // La relación se establece asignando el cliente a la factura
        // NOTA: Aquí aún no se actualiza la colección del cliente automáticamente
        Invoice invoice1 = Invoice.builder()
                .amount(BigDecimal.valueOf(20000))
                .client(client) // asigna el cliente a la factura
                .build();
        Invoice invoice2 = Invoice.builder()
                .amount(BigDecimal.valueOf(30000))
                .client(client)
                .build();

        // Guardar las facturas en la BD
        invoiceRepository.save(invoice1);
        invoiceRepository.save(invoice2);

        // 🔹 Diferencia: la colección client.getInvoices() todavía no refleja invoice1 ni invoice2
        // porque no se agregó manualmente a la lista de facturas del cliente
        System.out.println("Facturas del cliente (antes de usar addInvoice): " + client.getInvoices());

        // 3️⃣ Crear facturas usando el método addInvoice() de la entidad
        // Esto asegura que la relación bidireccional se mantiene en ambos lados
        Invoice invoice3 = Invoice.builder()
                .amount(BigDecimal.valueOf(20000))
                .build(); // No se asigna cliente directamente

        Invoice invoice4 = Invoice.builder()
                .amount(BigDecimal.valueOf(30000))
                .build();

        // addInvoice() hace dos cosas:
        // 1. Agrega la factura a la lista de facturas del cliente
        // 2. Asigna el cliente a la factura
        client.addInvoice(invoice3).addInvoice(invoice4);

        // Guardar estas nuevas facturas en la BD
        invoiceRepository.save(invoice3);
        invoiceRepository.save(invoice4);

        // 4️⃣ Resultados finales
        // invoice1 e invoice2 tienen cliente asignado, pero client.getInvoices() solo tiene invoice3 e invoice4
        // invoice3 e invoice4 están correctamente asociados bidireccionalmente
        // pero al guardar invoice1 y invoice2 dentro de la misma transacción:
        //      Hibernate sincroniza el estado de todas las entidades Managed al hacer flush.
        // esto implica que la colección client.getInvoices() se actualiza automáticamente
        System.out.println("Invoice1: " + invoice1);
        System.out.println("Invoice2: " + invoice2);
        System.out.println("Invoice3: " + invoice3);
        System.out.println("Invoice4: " + invoice4);

        // Las facturas reflejadas en la colección del cliente:
        System.out.println("Facturas del cliente (después de addInvoice): " + client.getInvoices());
    }


    /**
     * Ejempolo de codificaicon "OneToMany"
     * Crea un cliente con direcciones, y se crea el cliente aprovechadno el Cascade.ALL, para crear la direccion.
     */
    @Transactional
    protected void oneToMany(){

        Address address = Address.builder().street("Avd. Canxas").number(3).build();
        Client client = Client.builder().name("Perico").lastName("Rojas").build();

        client.getAddresses().add(address);

        clientRepository.save(client);
        System.out.println(client);
        client.getAddresses().forEach(System.out::println);
    }

    /**
     * Ejempolo de codificaicon "OneToMany" sobre un Cliente existente
     * Crea un cliente con direcciones, y se crea el cliente aprovechadno el Cascade.ALL, para crear la direccion.
     */
    @Transactional
    public void oneToManyAboutAClientExist() {

        // findById es detached, lo que se recomienda es hacer feth join
        clientRepository.findById(3L).ifPresent(client ->{
                    Address address1 = Address.builder().street("Avd. Canxas").number(3).build();
                    Address address2 = Address.builder().street("Avd. Florida").number(3).build();
                    // como el findById finlaiza la sesión, al hacer un get de Address dará un error de Lazy
                    // por lo que si no se trajo durante la sesión el objeto, el proxy no se podra inizilizar,
                    // sin embargo se puede machar el valor
                    client.setAddresses(Arrays.asList(address1,address2));

                    // save no es necesario si el cliente ya está en el contexto de persistencia
                    clientRepository.save(client);

                    log.info("Cliente: {}", client);
                    client.getAddresses().forEach(a -> log.info("Address: {}", a));
                }
        );

    }

    /**
     * Método no permitido,
     * por que una un cliente puede tener muchas direcciones, pero una direcicón solo un cliente
     * entonces es una relación 1 <-> N, no de N <-> N
     */
//    @Transactional
//    public void oneToManyAddressesShared(){// Requiere comentar:             //,uniqueConstraints = @UniqueConstraint(columnNames = {"address_id"}) // Not allow am Address in more one Client
//
//        Address address1 = Address.builder().street("Avd. Canxas").number(3).build();
//        Address address2 = Address.builder().street("Avd. Florida").number(3).build();
//        Address address3 = Address.builder().street("Avd. Florida").number(4).build();
//
//        // requiere equlas y hasCode, si no en cada save crea objetos diferntes
//        addressRepository.saveAll( Arrays.asList(address1,address2) );
//
//        clientRepository.findById(2L).ifPresent(client ->{
//            client.setAddresses(Arrays.asList(address1,address2));
//
//            // save no es necesario si el cliente ya está en el contexto de persistencia
//            clientRepository.save(client);
//
//            log.info("Cliente: {}", client);
//            client.getAddresses().forEach(a -> log.info("Address: {}", a));
//        });
//
//        clientRepository.findById(3L).ifPresent(client ->{
//            client.setAddresses(Arrays.asList(address1,address3));
//
//            // save no es necesario si el cliente ya está en el contexto de persistencia
//            clientRepository.save(client);
//
//            log.info("Cliente: {}", client);
//            client.getAddresses().forEach(a -> log.info("Address: {}", a));
//        });
//    }

    @Transactional
    public void removeInOneToMany(){
        clientRepository.findById(3L).ifPresent(client ->{
                    Address address1 = Address.builder().street("Avd. Canxas").number(3).build();
                    Address address2 = Address.builder().street("Avd. Florida").number(3).build();
                    // como el findById finlaiza la sesión, al hacer un get de Address dará un error de Lazy
                    // por lo que si no se trajo durante la sesión el objeto, el proxy no se podra inizilizar,
                    // sin embargo se puede machar el valor
                    client.setAddresses(Arrays.asList(address1,address2));

                    // save no es necesario si el cliente ya está en el contexto de persistencia
                    clientRepository.save(client);

                    log.info("Cliente: {}", client);
                    client.getAddresses().forEach(a -> log.info("Address: {}", a));
                }
        );

        // REMOVE

        // JdbcSQLIntegrityConstraintViolationException: Violaci�n de una restricci�n de Integridad Referencial: "FKdotrmv9mkkqiclhp5iv35dggw: PUBLIC.CLIENTS_ADDRESSES FOREIGN KEY(address_id) REFERENCES PUBLIC.ADDRESSES(id) (CAST(1 AS BIGINT))"
        // Referential integrity constraint violation: "FKdotrmv9mkkqiclhp5iv35dggw: PUBLIC.CLIENTS_ADDRESSES FOREIGN KEY(address_id) REFERENCES PUBLIC.ADDRESSES(id) (CAST(1 AS BIGINT))"; SQL statement:
        // addressRepository.deleteById(1L);

        // operation allow, because remove use in client
        // but error: ailed to lazily initialize a collection of role: com.codearp.application.demospring_boot3_jpa_relationship.domains.Client.addresses
        // NOTA:
        // ESTE ERROR NO PASA EN UNA APLICACIÓN WEB SI SE ANOTA CON TRANSANCIONAL Y NO SE CIERRA LA SESIÓN
        // PERO EN UNA DE CONSOLA (ESTAMOS EN EL CONTEXTO COMMANLINERUNNER SI PASA (TRATA CADA OPERACIÓN DE FORMA ATÓMICA)
        // PARA SOLUCIONARLO HEMOS USADO: spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true
        clientRepository.findById(3L).ifPresent( client -> {
            // elimina la relación y si 'orphanRemoval = true' la dirección si no, la deja huerfana
            client.getAddresses().removeFirst();
            clientRepository.save(client);
        });

        // Ok remove client + address
        //clientRepository.deleteById(3L);

    }

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
        Invoice invoice1 = Invoice.builder().amount(BigDecimal.valueOf(20000)).build();
        Invoice invoice2 = Invoice.builder().amount(BigDecimal.valueOf(30000)).build();
        Invoice invoice3 = Invoice.builder().amount(BigDecimal.valueOf(30000)).build();
        Invoice invoice4 = Invoice.builder().amount(BigDecimal.valueOf(30000)).build();
        Invoice invoice5 = Invoice.builder().amount(BigDecimal.valueOf(30000)).build();


        // addInvoice hace dos cosas:
        // 1. Agrega la factura a la lista de facturas del cliente
        // 2. Asigna el cliente a la factura (relación bidireccional)
        client.addInvoice(invoice1).addInvoice(invoice2).addInvoice(invoice3).addInvoice(invoice4).addInvoice(invoice5);

        clientRepository.save(client);


        client = clientRepository.findOne(3L).get();
        client.getInvoices().forEach(System.out::println);

        // Prueba el @BatSize
        List<Client> clients = clientRepository.findInLazy(List.of(1L,2L,3L));
        clients.stream().map(Client::getInvoices).forEach(System.out::println);
    }


    @Transactional
    public void removeInvoiceBidireccionalFindById(){
        clientRepository.findById(3L).ifPresent(client -> {
            Invoice invoice1 = Invoice.builder().amount(BigDecimal.valueOf(20000)).build();
            Invoice invoice2 = Invoice.builder().amount(BigDecimal.valueOf(30000)).build();
            Invoice invoice3 = Invoice.builder().amount(BigDecimal.valueOf(30000)).build();

            client.addInvoice(invoice1).addInvoice(invoice2).addInvoice(invoice3);

            clientRepository.save(client);
        });

        clientRepository.findById(3L).ifPresent(client -> {
            Invoice invoice = invoiceRepository.findById(1L).get();

//            //1. Elimina la factura de la lista de facturas del cliente:
//            client.getInvoices().removeIf(invoice1 -> invoice1.equals( invoice )); // elimina la factura del cliente
//            //2.Rompe la relación bidireccional:
//            invoice.setClient(null); // elimina la fk
            client.removeInvoice(invoice);


            //3. Guarda el cliente y luego elimina la factura
            // Si Client.invoices tiene orphanRemoval = true y cascade = CascadeType.AL
            // y guardar el cliente, Hibernate eliminará automáticamente la factura de la BD.
            // No es necesario llamar a invoiceRepository.delete(invoice) explícitamente
            clientRepository.save(client);
            invoiceRepository.delete(invoice);
        });

        clientRepository.findById(3L).map(Client::getInvoices).stream().forEach(System.out::println);
    }

    /**
     * Ejemplo OnetOne
     */
    @Transactional
    protected void exampleOneToOne(){
        // ### Ejemplo si:
        //  @OneToOne
        //    @ToString.Exclude
        //    private Client client;
        // Esta en el ClientDetails y no en Client (unidireccional)

        //Client client = Client.builder().name("Test").lastName("lastName").build();
        //ClientDetails clientDetails = ClientDetails.builder().points(1).premium(true).client(client).build();
        //clientRepository.save(client);
        //clientDetailsRepository.save(clientDetails);

        //System.out.println(clientDetails);
        //System.out.println(clientDetails.getClient());

        // ### Ejemplo si:
        //  @OneToOne
        //    @ToString.Exclude
        //    private Client client;
        // Esta en el Client y no en ClientDetails (unidireccional)


        ClientDetails clientDetails = ClientDetails.builder().points(1).premium(true).build();
        Client client = Client.builder().name("Test").lastName("lastName").clientDetails(clientDetails).build();
        //clientDetailsRepository.save(clientDetails); // Si client tiene el CascadeAll esto no se guarda ya
        clientRepository.save(client);

        System.out.println(clientDetails);
        System.out.println(client.getClientDetails());

    }

    @Transactional
    protected void exampleOneToOneAboutClientExists(){

        clientRepository.findById(1L).ifPresent( client -> {
            ClientDetails clientDetails = ClientDetails.builder().points(1).premium(true).build();
            client.setClientDetails(clientDetails);
            clientDetailsRepository.save(clientDetails);
            clientRepository.save(client);
        });

        Client client = clientRepository.findById(1L).get();
        System.out.println(client.getClientDetails());

        /**
         * Si ClientDeatils en CLient es Fetch.Lazy y esta comentado odesactivado la propiedad
         * # ANTI-PATTER (NO RECOMENDABLE PARA PRODUCIÓN)
         * #spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true
         *
         * Dara error :
         * org.hibernate.LazyInitializationException: Could not initialize proxy [com.codearp.application.demospring_boot3_jpa_relationship.domains.ClientDetails#2] - no session
         * Eso si estamos en contexto de Spring y en el bloque transaciconal, no dara error enste meodo, pero al estar
         * en  contexto de CommandLineRunner, el findBYiD cierra la sessión y no se podrá obtener
         */
    }

    @Transactional
    protected void exampleOneToOneAboutClientExistsBidireccional(){

        clientRepository.findById(1L).ifPresent( client -> {
            // si no se usa el Cascade.ALL
//            ClientDetails clientDetails = ClientDetails.builder().points(1).premium(true).build();
//            client.setClientDetails(clientDetails);
//            clientDetails.setClient(client);
//
//            clientDetailsRepository.save(clientDetails);
//            clientRepository.save(client);

            // si se usa el Cascae.ALL
            ClientDetails clientDetails = ClientDetails.builder().points(1).premium(true).build();
            client.addClientDetails(clientDetails);

            clientRepository.save(client);
        });

        Client client = clientRepository.findById(1L).get();
        System.out.println(client.getClientDetails());

        /**
         * Si ClientDeatils en CLient es Fetch.Lazy y esta comentado odesactivado la propiedad
         * # ANTI-PATTER (NO RECOMENDABLE PARA PRODUCIÓN)
         * #spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true
         *
         * Dara error :
         * org.hibernate.LazyInitializationException: Could not initialize proxy [com.codearp.application.demospring_boot3_jpa_relationship.domains.ClientDetails#2] - no session
         * Eso si estamos en contexto de Spring y en el bloque transaciconal, no dara error enste meodo, pero al estar
         * en  contexto de CommandLineRunner, el findBYiD cierra la sessión y no se podrá obtener
         */
    }

    /**
     * Elimina ClientDetial pero No cliente
     */
    @Transactional
    public void removeClientDetailsOneToOneBidirecciontal(){
        clientRepository.findById(1L).ifPresent( client -> {

            // si se usa el Cascae.ALL
            ClientDetails clientDetails = client.getClientDetails();
            client.removeClientDetails(clientDetails);

            clientDetailsRepository.delete(clientDetails);

        });

        clientRepository.findById(1L).ifPresent(System.out::println);

    }

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    private final EnrollmentService enrollmentService;

    @Transactional
    public void manyToMany(){
        Student student1 = Student.builder().name("Jano").lastName("Pura").build();
        Student student2 = Student.builder().name("Erba").lastName("Doe").build();

        Course course1 = Course.builder().name("Curso de java master").description("DEV Andres").build();
        Course course2 = Course.builder().name("Curso de Spring Boot").description("DEV Andres").build();

        courseRepository.saveAll(Arrays.asList(course1,course2));

        enrollmentService.enroll(student1,course1);
        enrollmentService.enroll(student1,course2);

        enrollmentService.enroll(student2,course2);

        studentRepository.save(student1);
        studentRepository.save(student2);

        System.out.println(student1);
        System.out.println(student2);

        System.out.println("Se elimina el Estudiante 1 del curso 1");
        enrollmentService.unenroll(student1,course1);

        System.out.println(student1);
        System.out.println(student2);



    }
}
