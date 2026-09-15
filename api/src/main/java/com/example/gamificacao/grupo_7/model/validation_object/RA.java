package com.example.gamificacao.grupo_7.model.validation_object;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Embeddable
@Getter
@Setter
public class RA {

    public String ra;

    public RA(){
        this.ra = criaRa();
    }

    public String criaRa(){

        Integer anoDaMatricula = LocalDateTime.now().getYear();
        Integer threeDigitRandom = (int) (Math.random() * 900 + 100);

        return String.format("%d%d", anoDaMatricula, threeDigitRandom);

    }


}

