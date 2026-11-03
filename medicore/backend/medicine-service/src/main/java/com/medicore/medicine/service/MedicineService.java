package com.medicore.medicine.service;

import com.medicore.medicine.dto.*;
import com.medicore.medicine.entity.Medicine;
import com.medicore.medicine.repository.MedicineRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MedicineService {
    private final MedicineRepository repository;
    public MedicineService(MedicineRepository repository){this.repository=repository;}
    public List<MedicineResponse> findAll(){return repository.findAll().stream().map(this::response).toList();}
    public MedicineResponse find(Long id){return response(repository.findById(id).orElseThrow(()->new IllegalArgumentException("Medicine not found")));}
    public MedicineResponse create(MedicineRequest request){Medicine medicine=new Medicine(request.name(),request.price());medicine.update(request.name(),request.price(),request.active());return response(repository.save(medicine));}
    public MedicineResponse update(Long id, MedicineRequest request){Medicine m=repository.findById(id).orElseThrow(()->new IllegalArgumentException("Medicine not found"));m.update(request.name(),request.price(),request.active());return response(repository.save(m));}
    public void delete(Long id){repository.deleteById(id);}
    private MedicineResponse response(Medicine m){return new MedicineResponse(m.getId(),m.getName(),m.getPrice(),m.isActive());}
}
