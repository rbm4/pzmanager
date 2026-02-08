package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Car;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.CarRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.CharacterRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class CarService {
    
    private static final Logger logger = Logger.getLogger(CarService.class.getName());
    
    private final CarRepository carRepository;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;
    private final ServerCommandService serverCommandService;

    public CarService(CarRepository carRepository, 
                     CharacterRepository characterRepository,
                     UserRepository userRepository,
                     ServerCommandService serverCommandService) {
        this.carRepository = carRepository;
        this.characterRepository = characterRepository;
        this.userRepository = userRepository;
        this.serverCommandService = serverCommandService;
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
        car.setVehicleScript(updatedCar.getVehicleScript());
        car.setValue(updatedCar.getValue());
        car.setTrunkSize(updatedCar.getTrunkSize());
        car.setSeats(updatedCar.getSeats());
        car.setDoors(updatedCar.getDoors());
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

    /**
     * Get all characters for the currently authenticated user
     * @param user 
     */
    public List<Character> getUserCharacters(User user) {
        if (user == null) {
            return List.of();
        }
        return characterRepository.findByUserOrderByZombieKillsDesc(user);
    }

    /**
     * Check if a character is currently online (last update within 5 minutes)
     */
    public boolean getCharacterStatus(Long characterId) {
        Optional<Character> character = characterRepository.findById(characterId);
        
        if (character.isEmpty()) {
            return false;
        }
        
        LocalDateTime lastUpdate = character.get().getLastUpdate();
        if (lastUpdate == null) {
            return false;
        }
        
        // Character is online if they updated within last 1 minutes
        return lastUpdate.isAfter(LocalDateTime.now().minusMinutes(1));
    }

    /**
     * Process vehicle purchase: deduct points and spawn vehicle for player
     */
    @Transactional
    public Map<String, Object> purchaseVehicle(Long carId, Long characterId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Get current user
            User user = getCurrentUser();
            if (user == null) {
                result.put("success", false);
                result.put("message", "Usuário não autenticado");
                return result;
            }
            
            // Get the car being purchased
            Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found"));
            
            // Get the target character
            Character targetCharacter = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found"));
            
            // Verify the character belongs to the user
            if (!targetCharacter.getUser().getId().equals(user.getId())) {
                result.put("success", false);
                result.put("message", "Este personagem não pertence a você");
                return result;
            }
            
            // Check if character has enough currency points
            Integer characterPoints = targetCharacter.getCurrencyPoints() != null ? 
                targetCharacter.getCurrencyPoints() : 0;
            
            if (characterPoints < car.getValue()) {
                result.put("success", false);
                result.put("message", "Moeda insuficiente. Você tem " + characterPoints + 
                          " ₳ mas precisa de " + car.getValue() + " ₳");
                return result;
            }
            
            // Deduct points from character
            int newPoints = characterPoints - car.getValue();
            targetCharacter.setCurrencyPoints(newPoints);
            characterRepository.save(targetCharacter);
            logger.info("Deducted " + car.getValue() + " ₳ from character: " + targetCharacter.getPlayerName());
            
            // Execute server command to spawn vehicle
            String vehicleCommand = String.format("addVehicle \"%s\" \"%s\"", 
                car.getVehicleScript(), targetCharacter.getPlayerName());
            
            try {
                serverCommandService.sendCommand(vehicleCommand);
                logger.info("Spawned vehicle " + car.getName() + " (" + car.getVehicleScript() + 
                           ") for player: " + targetCharacter.getPlayerName());
            } catch (Exception e) {
                logger.warning("Failed to spawn vehicle via RCon: " + e.getMessage());
                // Vehicle will be spawned when player reconnects
            }
            
            result.put("success", true);
            result.put("message", "Compra realizada com sucesso! O veículo foi gerado para " + 
                      targetCharacter.getPlayerName());
            result.put("newPoints", newPoints);
            result.put("pointsDeducted", car.getValue());
            
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        } catch (Exception e) {
            logger.severe("Error during vehicle purchase: " + e.getMessage());
            result.put("success", false);
            result.put("message", "Ocorreu um erro durante a compra. Tente novamente.");
        }
        
        return result;
    }

    /**
     * Get the currently authenticated user from Spring Security context
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            
            User username = (User) authentication.getPrincipal();
            return username;
        } catch (Exception e) {
            logger.warning("Error getting current user: " + e.getMessage());
            return null;
        }
    }
}
