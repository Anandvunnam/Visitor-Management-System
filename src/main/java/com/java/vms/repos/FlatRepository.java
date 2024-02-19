package com.java.vms.repos;

import com.java.vms.domain.Flat;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FlatRepository extends JpaRepository<Flat, Long> {

    boolean existsByFlatNumIgnoreCase(String flatNum);

}
