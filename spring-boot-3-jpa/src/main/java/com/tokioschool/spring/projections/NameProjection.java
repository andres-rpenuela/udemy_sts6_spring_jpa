package com.tokioschool.spring.projections;
/**
 * Los paraemtros debe coincidir con los parametos de la entidad a capturar
 * en el resutlado de la query 
 */
public interface NameProjection {
	String getName();
    String getLastname();
}