package com.lcconstrutora.api.database.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Data 
@Table(name = "tb_contatos")
@AllArgsConstructor 
@NoArgsConstructor 
public class ContatoEntity {
     
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column (nullable = false, length = 50)
    private String name;

    @Column (nullable = false, length =12 )
    private String telefone;

    @Column (nullable = false, length = 255)
    private String motivo;

}
