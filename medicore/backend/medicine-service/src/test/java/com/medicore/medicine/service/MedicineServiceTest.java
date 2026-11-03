package com.medicore.medicine.service;

import com.medicore.medicine.repository.MedicineRepository;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MedicineServiceTest { @Test void serviceHasRepositoryBoundary(){assertNotNull(new MedicineService(mock(MedicineRepository.class)));} }
