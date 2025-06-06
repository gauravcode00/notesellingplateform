package com.notesapp.notesellingplateform.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private Double walletBalance;

    public UserDto() {
    }

    public UserDto(Long id, String name, String email, Double walletBalance) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.walletBalance = walletBalance;
    }

    // Optional: for convenience, a constructor from User entity
    public UserDto(com.notesapp.notesellingplateform.entity.User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.walletBalance = user.getWalletBalance();
    }

}
