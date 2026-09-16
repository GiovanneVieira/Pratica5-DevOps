package com.example.gamificacao.grupo_7.model.validation_object;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class RA {
    
    @Column(name = "ra", nullable = false)
    public String ra;

    public RA(){
        this.ra = criaRa();
    }

    public RA(String ra){
        this.ra = ra;
    }

    public String criaRa(){

        String anoDaMatricula =String.valueOf(LocalDateTime.now().getYear()).substring(2,4);
        Integer threeDigitRandom = (int) (Math.random() * 9000 + 1000);

        return String.format("%s%d", anoDaMatricula, threeDigitRandom);

    }


}

