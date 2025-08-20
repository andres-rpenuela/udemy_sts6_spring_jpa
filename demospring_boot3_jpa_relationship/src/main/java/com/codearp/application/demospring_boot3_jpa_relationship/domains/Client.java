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
}
