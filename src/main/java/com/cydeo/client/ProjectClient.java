package com.cydeo.client;

import com.cydeo.dto.ProjectResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "project-service", url = "http://localhost:8082")
public interface ProjectClient {

    @GetMapping("/api/v1/project/count/manager/{assignedManager}")
    ResponseEntity<ProjectResponseDTO> getCountByAssignedManager(@RequestHeader(value = "Authorization") String authorizationHeader, @PathVariable("assignedManager") String assignedManager);

}
