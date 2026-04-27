package com.example.librarymanage_be.service;

import com.example.librarymanage_be.entity.Borrow;
import com.example.librarymanage_be.entity.BorrowDetail;

import java.io.IOException;
import java.util.List;

public interface BorrowContractService {
    void generateContract(Borrow borrow, List<BorrowDetail> details) throws IOException;

    byte[] getContractContent(Integer borrowId) throws IOException;

    String getContractFileName(Integer borrowId);

    boolean exists(Integer borrowId);
}
