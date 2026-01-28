package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Car;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.CarRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class CarService {
    
    private static final Logger logger = Logger.getLogger(CarService.class.getName());
    
    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Transactional
    public Car createCar(Car car) {
        logger.info("Creating new car: " + car.getName());
        return carRepository.save(car);
    }

    public List<Car> getAllCars() {
        return carRepository.findAllByOrderByNameAsc();
    }

    public Page<Car> getAllCarsPaginated(String search, Boolean availableOnly, Pageable pageable) {
        logger.info("Getting paginated cars - search: " + search + ", availableOnly: " + availableOnly);
        
        if (availableOnly == null) {
            availableOnly = false;
        }
        
        return carRepository.searchCars(search, availableOnly, pageable);
    }

    public Optional<Car> getCarById(Long id) {
        return carRepository.findById(id);
    }

    public List<Car> getAvailableCars() {
        return carRepository.findByAvailableTrue();
    }

    @Transactional
    public Car updateCar(Long id, Car updatedCar) {
        logger.info("Updating car with id: " + id);
        
        Car car = carRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Car not found with id: " + id));
        
        car.setName(updatedCar.getName());
        car.setModel(updatedCar.getModel());
        car.setValue(updatedCar.getValue());
        car.setDescription(updatedCar.getDescription());
        car.setImages(updatedCar.getImages());
        car.setAvailable(updatedCar.getAvailable());
        
        return carRepository.save(car);
    }

    @Transactional
    public void deleteCar(Long id) {
        logger.info("Deleting car with id: " + id);
        carRepository.deleteById(id);
    }

    @Transactional
    public Car toggleAvailability(Long id) {
        logger.info("Toggling availability for car with id: " + id);
        
        Car car = carRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Car not found with id: " + id));
        
        car.setAvailable(!car.getAvailable());
        return carRepository.save(car);
    }

    public long getTotalCarCount() {
        return carRepository.count();
    }

    public long getAvailableCarCount() {
        return carRepository.findByAvailableTrue().size();
    }
}
