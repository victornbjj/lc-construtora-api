package com.lcconstrutora.api.enums;

public enum SistuacaoImovel {
    DISPONIVEL("disponivel"),
    VENDIDO("vendido"),
    ALUGADO("alugado");



    private String SistuacaoImovel;


    SistuacaoImovel (String situacaoImovel){
        this.SistuacaoImovel = situacaoImovel;
    }


    public String getSistuacaoImovel() {
        return SistuacaoImovel;
    }

}
