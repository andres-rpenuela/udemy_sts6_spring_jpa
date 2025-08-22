package com.codearp.application.demospring_boot3_jpa_relationship.domains;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="CLIENTS")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name="LAST_NAME")
    private String lastName;

    // Relacion bidireciconal
    // ClIENT ----* INVOICES
    @OneToMany(mappedBy = "client")
    public List<Invoice> invoices;

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    // si no se mapped a un field de Address de tipo Cliente o no se usa JoinColum (no eixte una fk), entonces, crea una tabla CLIENT_ADDRESS con las relaciones
    // CLIENT ----* ADDRESSES
    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true) // crea una tabla intermedia gestionada por hiberante
    //@OneToMany(mappedBy = "client") // debe esiste el Cliente client y debe ser un @ManyToOne
    @JoinColumn(name = "client_id") // crea una fk client_id en ADDRESSES
    private List<Address> addresses;
}
