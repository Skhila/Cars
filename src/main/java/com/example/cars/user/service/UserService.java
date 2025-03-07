package com.example.cars.user.service;

import com.example.cars.error.InsufficientFundsException;
import com.example.cars.error.NotFoundException;
import com.example.cars.model.CarDTO;
import com.example.cars.persistence.Car;
import com.example.cars.persistence.CarRepository;
import com.example.cars.user.model.AppUserDTO;
import com.example.cars.user.model.UserRequest;
import com.example.cars.user.persistence.AppUser;
import com.example.cars.user.persistence.AppUserRepository;
import com.example.cars.user.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final double DEPRECIATION_FACTOR = 0.8;

    private final UserUtils userUtils;
    private final CarRepository carRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public Page<CarDTO> getUserCars(int page, int pageSize, Long userId) {
        return appUserRepository.findUserCars(userId, PageRequest.of(page, pageSize));
    }

    public void createUser(UserRequest userRequest) {
        AppUser appUser = new AppUser();
        appUser.setUsername(userRequest.getUsername());
        appUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        appUser.setBalanceInCents(userRequest.getBalanceInCents());
        appUser.setRoles(userRequest.getRoleIds().stream()
                .map(roleService::getRole)
                .collect(Collectors.toSet()));

        appUserRepository.save(appUser);
    }

    public void purchaseCar(Long carId) {
        AppUser currentUser = userUtils.getCurrentUser();
        Car carToAdd = carRepository.findById(carId).orElseThrow(() -> new NotFoundException("Car with id " + carId + " not found"));

        if (currentUser.getBalanceInCents() < carToAdd.getPriceInCents()) {
            throw new InsufficientFundsException("Insufficient funds to purchase the car, price: " + carToAdd.getPriceInCents() + "; balance: " + currentUser.getBalanceInCents());
        }

        if (!currentUser.getCars().contains(carToAdd)) {
            currentUser.getCars().add(carToAdd);
            currentUser.setBalanceInCents(currentUser.getBalanceInCents() - carToAdd.getPriceInCents());
            appUserRepository.save(currentUser);
        }
    }

    public void updateBalance(Long userId, Long amountInCents) {
        AppUser userToUpdate = appUserRepository.findById(userId).orElseThrow(() -> new NotFoundException("User with id '" + userId + "' not found"));

        if (amountInCents < 0 && userToUpdate.getBalanceInCents() < Math.abs(amountInCents)) {
            throw new InsufficientFundsException("Insufficient funds to decrease the balance, requested amount: " + Math.abs(amountInCents) + "; balance: " + userToUpdate.getBalanceInCents());
        }

        userToUpdate.setBalanceInCents(userToUpdate.getBalanceInCents() + amountInCents);
        appUserRepository.save(userToUpdate);
    }

    public void sellCar(Long carId) {
        AppUser currentUser = userUtils.getCurrentUser();
        Car carToSell = carRepository.findById(carId).orElseThrow(() -> new NotFoundException("Car with id " + carId + " not found"));

        if (currentUser.getCars().isEmpty() || !currentUser.getCars().contains(carToSell)) {
            throw new NotFoundException("User has no car with id " + carId);
        }

        currentUser.getCars().remove(carToSell);
        currentUser.setBalanceInCents(currentUser.getBalanceInCents() + Math.round(carToSell.getPriceInCents() * DEPRECIATION_FACTOR));
        appUserRepository.save(currentUser);
    }

    public AppUser getUser(String username) {
        return appUserRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User with username '" + username + "' not found"));
    }
}
