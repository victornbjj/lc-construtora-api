package com.lcconstrutora.api.enums;

public enum TipoImovel {
        CASA("casa"),
        APARTAMENTO("apartamento"),
        TERRENO("terreno");



        private String TipoImovel;


        TipoImovel(String tipoImovel){
            this.TipoImovel = tipoImovel;
        }


        public String getTipoImovel(){
            return  TipoImovel;
        }

    
        
        
    
}
