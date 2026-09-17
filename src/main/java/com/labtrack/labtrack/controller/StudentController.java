package com.labtrack.labtrack.controller;

import com.labtrack.labtrack.dto.ActiveLoanDTO;
import com.labtrack.labtrack.dto.StudentCreateRequest;
import com.labtrack.labtrack.dto.StudentResponse;
import com.labtrack.labtrack.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Tag(name = "Student", description = "Endpoints for student management")
public class StudentController {

    private final StudentService studentService;

    @Operation(
            summary = "Get student's active loans (RF06)",
            description = "Returns all active loans of a student by registration number. " +
                    "Includes checkout date, expected return date and equipment list."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active loans list returned successfully"),
            @ApiResponse(responseCode = "404", description = "Student not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @GetMapping("/{registration}/active-loans")
    public ResponseEntity<List<ActiveLoanDTO>> getActiveLoans(
            @PathVariable
            @NotBlank(message = "Registration number cannot be empty")
            @Parameter(description = "Student registration number", example = "2021001")
            String registration) {

        log.info("Request GET /api/students/{}/active-loans", registration);

        List<ActiveLoanDTO> activeLoans = studentService.findActiveLoansByRegistration(registration);
        log.info("Returning {} active loans for registration: {}", activeLoans.size(), registration);

        return ResponseEntity.ok(activeLoans);
    }

    @Operation(
            summary = "Register student manually (RF18)",
            description = "Registers a student by name, registration number, email and phone, without linking to a loan."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Student registered successfully"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid required fields"),
            @ApiResponse(responseCode = "409", description = "Registration number already registered"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentCreateRequest request) {
        log.info("Request POST /api/students, registration: {}", request.registrationNumber());

        StudentResponse response = studentService.createStudent(request);

        log.info("Student registered successfully, id: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
