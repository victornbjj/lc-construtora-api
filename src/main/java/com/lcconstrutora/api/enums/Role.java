package com.lcconstrutora.api.enums;

public enum  Role {
    ADMIN("admin");


    private String role; 


    Role(String role){
        this.role = role; 
    }


    public String getPerfil(){
        return  role;
    }

}
