package com.example.librarymanage_be.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    @Column(name = "user_name")
    private String username;

    private String email;

    @Column(name = "phone")
    private String phoneNumber;

    private String address;

    private String password;

}
