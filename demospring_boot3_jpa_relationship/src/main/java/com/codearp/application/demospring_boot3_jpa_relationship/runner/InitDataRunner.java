package com.codearp.application.demospring_boot3_jpa_relationship.runner;

import com.codearp.application.demospring_boot3_jpa_relationship.domains.Client;
import com.codearp.application.demospring_boot3_jpa_relationship.domains.Invoice;
import com.codearp.application.demospring_boot3_jpa_relationship.repositories.ClientRepository;
import com.codearp.application.demospring_boot3_jpa_relationship.repositories.InvoiceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@Slf4j
@AllArgsConstructor
public class InitDataRunner implements CommandLineRunner {

    private final ClientRepository clientRepository;

    private final InvoiceRepository invoiceRepository;

    @Override
    public void run(String... args) throws Exception {
        manyToOne();
        manyToOneAboutAClientExist();
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
}
