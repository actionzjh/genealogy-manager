package com.genealogy.repository;

import com.genealogy.entity.Surname;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurnameRepository extends JpaRepository<Surname, Long> {

    Optional<Surname> findByNameIgnoreCase(String name);

    List<Surname> findByNameContainingIgnoreCase(String keyword);

    List<Surname> findAllByOrderByGenealogyCountDesc();
}
