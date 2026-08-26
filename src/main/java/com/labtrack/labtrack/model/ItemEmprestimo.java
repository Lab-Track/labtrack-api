package com.labtrack.labtrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item_emprestimo")
@Getter
@Setter
@NoArgsConstructor
public class ItemEmprestimo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item_emprestimo")
    private Long idItemEmprestimo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emprestimo", nullable = false)
    private Emprestimo emprestimo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipamento", nullable = false)
    private Equipamento equipamento;

    @Column(name = "foto_retirada", nullable = false)
    private String fotoRetirada;

    @Column(name = "estado_retirada", nullable = false)
    private String estadoRetirada;

    @Column(name = "status_item", nullable = false)
    private String statusItem;
}
