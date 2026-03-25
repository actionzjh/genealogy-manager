package com.genealogy.controller;

import com.genealogy.service.VisualizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/visualization")
@CrossOrigin(origins = "*")
public class VisualizationController {

    @Autowired
    private VisualizationService visualizationService;

    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getTree(@RequestParam(required = false) String branchName) {
        return ResponseEntity.ok(visualizationService.buildTree(branchName));
    }
}
