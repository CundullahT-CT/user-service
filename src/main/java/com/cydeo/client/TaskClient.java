package com.cydeo.client;

import com.cydeo.dto.TaskResponseDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "task-service")
public interface TaskClient {

    @GetMapping("/api/v1/task/count/employee/{assignedEmployee}")
    @CircuitBreaker(name = "task-service")
    @Retry(name = "task-service")
    ResponseEntity<TaskResponseDTO> getNonCompletedCountByAssignedEmployee(@RequestHeader(value = "Authorization") String authorizationHeader, @PathVariable("assignedEmployee") String assignedEmployee);

}
