package com.medicore.inventory.repository;
import com.medicore.inventory.entity.InventoryItem; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import jakarta.persistence.LockModeType; import java.util.Optional;
public interface InventoryRepository extends JpaRepository<InventoryItem,Long>{@Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select i from InventoryItem i where i.medicineId = :medicineId") Optional<InventoryItem> findForUpdate(@Param("medicineId") Long medicineId); Optional<InventoryItem> findByMedicineId(Long medicineId);}
