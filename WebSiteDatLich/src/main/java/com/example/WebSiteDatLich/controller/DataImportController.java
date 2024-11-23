package com.example.WebSiteDatLich.controller;
import com.example.WebSiteDatLich.service.DataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class DataImportController {
    @Autowired
    private DataImportService dataImportService;

    @GetMapping("/import-data")
    public String importData() {
        dataImportService.importTestData();
        return "Data imported successfully!";
    }
}
