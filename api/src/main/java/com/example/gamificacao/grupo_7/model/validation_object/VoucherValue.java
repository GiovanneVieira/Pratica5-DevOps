package com.example.gamificacao.grupo_7.model.validation_object;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class VoucherValue {

    private Double valorEmCentavos;

    public VoucherValue(Double valor){
        this.valorEmCentavos = convertDoubleToCents(valor);
    }

    private Double convertDoubleToCents(Double valor){

        if(valor < 0){
            throw new IllegalArgumentException("O valor do voucher não pode ser negativo");
        }

        return valor * 100;

    }

}
