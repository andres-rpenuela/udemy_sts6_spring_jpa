package com.codearp.application.demospring_boot3_jpa_relationship.domains;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name="INVOICES")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
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
    @JoinColumn(name="client_id") // FK_client_id
    private Client client;


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
