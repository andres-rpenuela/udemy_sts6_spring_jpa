package com.codearp.application.demospring_boot3_jpa_relationship.domains;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    @Builder.Default
    public List<Invoice> invoices = new ArrayList<>();

    // si no se mapped a un field de Address de tipo Cliente o no se usa JoinColum (no eixte una fk), entonces, crea una tabla CLIENT_ADDRESS con las relaciones
    // CLIENT ----* ADDRESSES
//    @OneToMany(cascade = {CascadeType.ALL}, orphanRemoval = true) // crea una tabla intermedia gestionada por hiberante
//    //@OneToMany(mappedBy = "client") // debe esiste el Cliente client y debe ser un @ManyToOne
//    @JoinColumn(name = "client_id") // crea una fk client_id en ADDRESSES
//    @Builder.Default
//    public List<Address> addresses = new ArrayList<>();

    // Crea una tabla intermida
    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true) // Crea la direccion si no existe y no existe en otro cliente, Borrar la dirección si se queda huerfana, no se aconseja si una addres puede estar en varios clientes
    //@OneToMany(cascade = CascadeType.ALL) // Crea la direción si no existe y no existe en otro cliente, Borra la relación pero no la direcicón, si se borra desde cliente
    @JoinTable(
            name = "CLIENTS_ADDRESSES",
            joinColumns = @JoinColumn(name="client_id"), // FK_client_id in Address (class target)
            inverseJoinColumns = @JoinColumn(name="address_id") //FK_address_id in Client (this class)
            ,uniqueConstraints = @UniqueConstraint(columnNames = {"address_id"}) // Not allow am Address in more one Client (reforzar el uso de orphanRemoval = true)
    )
    @Builder.Default
    public List<Address> addresses = new ArrayList<>();

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client client)) return false;
        return Objects.equals(id, client.id) && Objects.equals(name, client.name) && Objects.equals(lastName, client.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, lastName);
    }
}
