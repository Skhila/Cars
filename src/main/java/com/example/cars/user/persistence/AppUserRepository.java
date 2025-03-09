package com.example.cars.user.persistence;

import com.example.cars.model.CarDTO;
import com.example.cars.user.model.AppUserDTO;
import com.example.cars.user.model.AppUserInfoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    @Query("SELECT NEW com.example.cars.model.CarDTO(" +
            "c.id, c.model, c.year, c.isDriveable, c.priceInCents, " +
            "NEW com.example.cars.model.EngineDTO(e.id, e.horsePower, e.capacity), " +
            "c.imageUrl, c.salesCount) " +
            "FROM AppUser u " +
            "JOIN u.cars c " +
            "JOIN c.engine e " +
            "WHERE u.id = :userId")
    Page<CarDTO> findUserCars(Long userId, Pageable pageable);

    @Query("SELECT NEW com.example.cars.user.model.AppUserInfoDTO(" +
            "u.id, u.username, u.balanceInCents, SIZE(u.cars))" +
            "FROM AppUser u")
    Page<AppUserInfoDTO> getAllUsersInfo(Pageable pageable);
}
