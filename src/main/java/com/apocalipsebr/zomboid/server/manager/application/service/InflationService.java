package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Car;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ZomboidItem;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.CarRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ZomboidItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.logging.Logger;

@Service
public class InflationService {

    private static final Logger logger = Logger.getLogger(InflationService.class.getName());

    private final CarRepository carRepository;
    private final ZomboidItemRepository zomboidItemRepository;

    public InflationService(CarRepository carRepository, ZomboidItemRepository zomboidItemRepository) {
        this.carRepository = carRepository;
        this.zomboidItemRepository = zomboidItemRepository;
    }

    @Transactional
    public InflationResult applyInflation(double percentage) {
        if (percentage <= 0) {
            throw new IllegalArgumentException("Inflation percentage must be greater than 0");
        }

        double multiplier = 1 + (percentage / 100.0);

        // Inflate available car prices
        List<Car> availableCars = carRepository.findByAvailableTrue();
        int carsInflated = 0;
        for (Car car : availableCars) {
            if (car.getValue() != null && car.getValue() > 0) {
                int oldValue = car.getValue();
                int newValue = (int) Math.ceil(oldValue * multiplier);
                car.setValue(newValue);
                carRepository.save(car);
                carsInflated++;
                logger.info("Inflated car '" + car.getName() + "' from " + oldValue + " to " + newValue);
            }
        }

        // Inflate sellable item prices
        List<ZomboidItem> sellableItems = zomboidItemRepository.findBySellableTrue();
        int itemsInflated = 0;
        for (ZomboidItem item : sellableItems) {
            if (item.getValue() != null && item.getValue() > 0) {
                int oldValue = item.getValue();
                int newValue = (int) Math.ceil(oldValue * multiplier);
                item.setValue(newValue);
                zomboidItemRepository.save(item);
                itemsInflated++;
                logger.info("Inflated item '" + item.getName() + "' from " + oldValue + " to " + newValue);
            }
        }

        logger.info("Inflation applied: " + percentage + "% — " + carsInflated + " cars, " + itemsInflated + " items inflated.");
        return new InflationResult(percentage, carsInflated, itemsInflated);
    }

    public static class InflationResult {
        private final double percentage;
        private final int carsInflated;
        private final int itemsInflated;

        public InflationResult(double percentage, int carsInflated, int itemsInflated) {
            this.percentage = percentage;
            this.carsInflated = carsInflated;
            this.itemsInflated = itemsInflated;
        }

        public double getPercentage() {
            return percentage;
        }

        public int getCarsInflated() {
            return carsInflated;
        }

        public int getItemsInflated() {
            return itemsInflated;
        }

        public int getTotalInflated() {
            return carsInflated + itemsInflated;
        }
    }
}
