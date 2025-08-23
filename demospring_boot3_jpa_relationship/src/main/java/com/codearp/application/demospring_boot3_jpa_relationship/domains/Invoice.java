package com.codearp.application.demospring_boot3_jpa_relationship.domains;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name="INVOICES")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(exclude = { "client"})
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private BigDecimal amount;

    // Crea la FK en INVOICES, que es la tabla dueño
    // Este atributo es opcional para crear una relacion bidirecional,
    // es obligatorio que eixta en bbdd para tenga la relación CLIENTS ---* INVOICES
    // INVOICES *--- Client
    @ManyToOne // Muchos libros puede tener un cliente
    @JoinColumn(name="client_id",nullable = false) // FK_client_id, si se omite, crea un campo en Client con el nombre del atributo
    private Client client;


    // En una de las dos relaciones, no debe acceder a la dependencia si no produce un ciclo infinito
    // en este caso el toString muestra client, pero el toStirng de Client, no muestra facutras.
    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", client=" + client +
                '}';
    }
}
