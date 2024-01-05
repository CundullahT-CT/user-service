package com.cydeo.client;

import com.cydeo.dto.ProjectResponseDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "project-service")
public interface ProjectClient {

    @GetMapping("/api/v1/project/count/manager/{assignedManager}")
    @CircuitBreaker(name = "project-service")
    @Retry(name = "project-service")
    ResponseEntity<ProjectResponseDTO> getNonCompletedCountByAssignedManager(@RequestHeader(value = "Authorization") String authorizationHeader, @PathVariable("assignedManager") String assignedManager);

}
