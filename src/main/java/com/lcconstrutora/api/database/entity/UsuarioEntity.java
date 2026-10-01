package com.lcconstrutora.api.database.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lcconstrutora.api.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity 
@Data 
@Table (name="usuarios")
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class UsuarioEntity {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome; 
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(length =100 )
    @JsonIgnore 
    private String senha;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    
    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro; 


    @PrePersist
    public void prepararData() {
        if (dataCadastro == null) {
            dataCadastro = LocalDateTime.now();
        }
    }


  

}
