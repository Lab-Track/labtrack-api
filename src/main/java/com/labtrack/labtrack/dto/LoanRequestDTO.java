package com.labtrack.labtrack.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para registro de empréstimo")
public class LoanRequestDTO {

    @JsonProperty("alunoMatricula")
    @Size(max = 50, message = "Matrícula deve ter no máximo 50 caracteres")
    @Schema(description = "Matrícula do aluno (opcional; sem matrícula o aluno é cadastrado como novo)",
            example = "2021001")
    private String studentRegistrationNumber;

    @JsonProperty("alunoNome")
    @NotBlank(message = "Nome do aluno é obrigatório")
    @Size(max = 255, message = "Nome do aluno deve ter no máximo 255 caracteres")
    @Schema(description = "Nome do aluno", example = "Ana Souza")
    private String studentName;

    @JsonProperty("alunoEmail")
    @NotBlank(message = "E-mail do aluno é obrigatório")
    @Email(message = "E-mail do aluno inválido")
    @Size(max = 255, message = "E-mail do aluno deve ter no máximo 255 caracteres")
    @Schema(description = "E-mail do aluno", example = "ana@email.com")
    private String studentEmail;

    @JsonProperty("alunoTelefone")
    @Size(max = 20, message = "Telefone do aluno deve ter no máximo 20 caracteres")
    @Schema(description = "Telefone do aluno (opcional)", example = "11999999999")
    private String studentPhone;

    @JsonProperty("professorId")
    @NotNull(message = "Professor responsável é obrigatório")
    @Schema(description = "ID do professor responsável (também autoriza o empréstimo)", example = "1")
    private Long professorId;

    @JsonProperty("dataPrevistaDevolucao")
    @Future(message = "Data prevista de devolução deve estar no futuro")
    @Schema(description = "Previsão de devolução (opcional; padrão: 7 dias após a retirada)",
            example = "2026-10-20T18:00:00")
    private LocalDateTime expectedReturnDate;

    @JsonProperty("itens")
    @NotEmpty(message = "Informe ao menos um equipamento")
    @Valid
    @Schema(description = "Equipamentos retirados, cada um com sua foto e condição")
    private List<@NotNull(message = "Item não pode ser nulo") LoanItemRequestDTO> items;
}
