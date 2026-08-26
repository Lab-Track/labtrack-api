package com.labtrack.labtrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "devolucao")
@Getter
@Setter
@NoArgsConstructor
public class Devolucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucao")
    private Long idDevolucao;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_item_emprestimo", nullable = false, unique = true)
    private ItemEmprestimo itemEmprestimo;

    @Column(name = "data_hora_devolucao", nullable = false)
    private LocalDateTime dataHoraDevolucao;

    @Column(name = "estado_devolucao", nullable = false)
    private String estadoDevolucao;

    @Column(name = "observacoes")
    private String observacoes;

    @Column(name = "status_verificacao", nullable = false)
    private String statusVerificacao;

    @Column(name = "atraso", nullable = false)
    private boolean atraso;
}
