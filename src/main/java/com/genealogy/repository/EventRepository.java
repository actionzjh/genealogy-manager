package com.genealogy.repository;

import com.genealogy.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    List<Event> findByPersonId(Long personId);
    
    List<Event> findByGenealogyId(Long genealogyId);
    
    List<Event> findByPersonIdAndEventType(Long personId, String eventType);
}
