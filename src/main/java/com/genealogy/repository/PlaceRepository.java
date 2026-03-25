package com.genealogy.repository;

import com.genealogy.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    
    List<Place> findByParentId(Long parentId);
    
    List<Place> findByNameContaining(String name);
}
