package com.example.gamificacao.grupo_7.model;

import com.example.gamificacao.grupo_7.enums.VoucherStatus;
import com.example.gamificacao.grupo_7.model.validation_object.VoucherValue;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "voucher")
@Table(name = "vouchers")
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", updatable = false, nullable = false)
    private String nome;

    @Embedded
    private VoucherValue valor;

    @Column(name = "descricao")
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "aluno_id", referencedColumnName = "id", nullable = false)
    private Aluno aluno;


    @Enumerated(EnumType.STRING)
    private VoucherStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime expiresAt;

}
