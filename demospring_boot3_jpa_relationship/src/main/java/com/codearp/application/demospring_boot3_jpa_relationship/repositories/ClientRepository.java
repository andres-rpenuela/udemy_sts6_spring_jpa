package com.codearp.application.demospring_boot3_jpa_relationship.repositories;

import com.codearp.application.demospring_boot3_jpa_relationship.domains.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Query(name = "Client.findInWithInvoicesAndAddresses")
    List<Client> findInLazy(@Param("ids") List<Long> id);


}
