package com.java.vms.repos;

import com.java.vms.domain.User;
import com.java.vms.domain.Visit;
import com.java.vms.domain.Visitor;
import com.java.vms.model.VisitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("SELECT v FROM Visit v WHERE v.visitStatus = ?1 AND v.user = ?2")
    List<Visit> findVisitByVisitStatusAndUser(VisitStatus status, User user);

    @Query("SELECT v FROM Visit v WHERE v.visitStatus = ?1 AND v.visitor = ?2 AND v.user = ?3 AND v.inTime IS NULL")
    Optional<Visit> isPreApprovedExistsForVisitor(VisitStatus status, Visitor visitor, User user);
}
