package com.java.vms.repos;

import com.java.vms.domain.Visit;
import org.springframework.data.jpa.repository.JpaRepository;


public interface VisitRepository extends JpaRepository<Visit, Long> {
}
