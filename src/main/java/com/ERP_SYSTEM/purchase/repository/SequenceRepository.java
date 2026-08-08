package com.ERP_SYSTEM.purchase.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class SequenceRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Long nextPoNumberSequence() {
        return ((Number) entityManager
                .createNativeQuery("SELECT nextval('seq_po_number')")
                .getSingleResult())
                .longValue();
    }

    public Long nextGrNumberSequence() {
        return ((Number) entityManager
                .createNativeQuery("SELECT nextval('seq_gr_number')")
                .getSingleResult())
                .longValue();
    }

    public Long nextSoNumberSequence() {
        return ((Number) entityManager
                .createNativeQuery("SELECT nextval('seq_so_number')")
                .getSingleResult())
                .longValue();
    }

}