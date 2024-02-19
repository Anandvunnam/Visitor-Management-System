package com.java.vms.repos;

import com.java.vms.domain.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VisitorRepository extends JpaRepository<Visitor, Long> {

    boolean existsByPhoneIgnoreCase(String phone);

    boolean existsByUnqIdIgnoreCase(String unqId);

}
