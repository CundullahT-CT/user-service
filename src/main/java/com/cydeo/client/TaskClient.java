package com.cydeo.client;

import com.cydeo.dto.TaskResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "task-service", url = "http://35.158.18.223:8083")
public interface TaskClient {

    @GetMapping("/api/v1/task/count/employee/{assignedEmployee}")
    ResponseEntity<TaskResponseDTO> getCountByAssignedEmployee(@RequestHeader(value = "Authorization") String authorizationHeader, @PathVariable("assignedEmployee") String assignedEmployee);

}
