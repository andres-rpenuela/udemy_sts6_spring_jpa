package com.codearp.application.demospring_boot3_jpa_relationship.runner;

import com.codearp.application.demospring_boot3_jpa_relationship.domains.Address;
import com.codearp.application.demospring_boot3_jpa_relationship.domains.Client;
import com.codearp.application.demospring_boot3_jpa_relationship.domains.Invoice;
import com.codearp.application.demospring_boot3_jpa_relationship.repositories.AddressRepository;
import com.codearp.application.demospring_boot3_jpa_relationship.repositories.ClientRepository;
import com.codearp.application.demospring_boot3_jpa_relationship.repositories.InvoiceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@Slf4j
@AllArgsConstructor
public class InitDataRunner implements CommandLineRunner {

    private final ClientRepository clientRepository;

    private final InvoiceRepository invoiceRepository;
    private final AddressRepository addressRepository;

    @Override
    public void run(String... args) throws Exception {
        // ### EXAMPLE ManyToOne() (Bidireccional) ###
        //manyToOne();
        //manyToOneAboutAClientExist();

        // ### EXAMPLE OneToMany() ###
        oneToMany();
        oneToManyAboutAClientExist();

        // Example remove OneToMany(casacade= Cascade.All )
        removeInOneToMany();
    }

    /**
     * Ejemplo de codificaicon "ManyToOne"
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

        // son el miso objeto, y ambos tiene el id
        // lo que indica que "save(entity)", modifica el parametro de entrada
        System.out.println(invoice);  //Invoice{id=1, description='null', amount=10000, client={id=4, name='Pedro', lastName='sANCHEZ'}}
        System.out.println(invoice2); //Invoice{id=1, description='null', amount=10000, client={id=4, name='Pedro', lastName='sANCHEZ'}}
    }

    /**
     * Ejemplo de codificaicon "ManyToOne"
     * Busca un clinete existente en la BBDD y le asocia una factura nueva
     */
    @Transactional
    protected void manyToOneAboutAClientExist(){
        //1.  Crear el cliente
        Client client = clientRepository.findById(1L).get();

        //2.  Crear la factura y se referencia al cliente.
        Invoice invoice = Invoice.builder().amount(BigDecimal.valueOf(20000)).client(client).build();
        invoiceRepository.save(invoice);

        System.out.println(invoice);  //Invoice{id=1, description='null', amount=10000, client={id=4, name='Pedro', lastName='sANCHEZ'}}
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

    @Transactional
    public void removeInOneToMany(){
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
            client.getAddresses().removeFirst();
            clientRepository.save(client);
        });

        // Ok remove client + address
        //clientRepository.deleteById(3L);

    }

}
