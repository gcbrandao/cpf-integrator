package br.com.elo.integrator.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultDTO implements Serializable {
    private String cpf;
    private String nome;
    private String email;

}
