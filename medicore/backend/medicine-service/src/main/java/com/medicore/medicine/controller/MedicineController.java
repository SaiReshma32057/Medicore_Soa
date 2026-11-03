package com.medicore.medicine.controller;

import com.medicore.medicine.dto.*;
import com.medicore.medicine.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicines")
public class MedicineController {
    private final MedicineService service;
    public MedicineController(MedicineService service){this.service=service;}
    @GetMapping public Object all(){return service.findAll();}
    @GetMapping("/{id}") public MedicineResponse one(@PathVariable Long id){return service.find(id);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public MedicineResponse create(@Valid @RequestBody MedicineRequest request){return service.create(request);}
    @PutMapping("/{id}") public MedicineResponse update(@PathVariable Long id,@Valid @RequestBody MedicineRequest request){return service.update(id,request);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id){service.delete(id);}
}
