package com.example.librarymanage_be.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BorrowDetailId implements Serializable {
    private Integer borrow;
    private Integer book;

}
