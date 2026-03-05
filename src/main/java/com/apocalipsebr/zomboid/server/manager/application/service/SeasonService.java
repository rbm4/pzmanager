package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Season;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.SeasonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SeasonService {

    private final SeasonRepository seasonRepository;

    public SeasonService(SeasonRepository seasonRepository) {
        this.seasonRepository = seasonRepository;
    }

    /**
     * Returns all seasons ordered by start date descending (newest first).
     */
    public List<Season> getAllSeasons() {
        List<Season> seasons = seasonRepository.findAll();
        seasons.sort((a, b) -> b.getStartDate().compareTo(a.getStartDate()));
        return seasons;
    }

    /**
     * Returns a season by its ID.
     */
    public Optional<Season> getSeasonById(Long id) {
        return seasonRepository.findById(id);
    }

    /**
     * Returns the currently active season.
     * There must always be exactly one active season.
     *
     * @return the active Season
     * @throws IllegalStateException if no active season is found
     */
    public Season getCurrentSeason() {
        return seasonRepository.findByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("No active season found. Please create one."));
    }

    /**
     * Creates a new season, deactivating the current one.
     * The new season becomes the active season and all character creation
     * will automatically point to it.
     *
     * @return the newly created active Season
     */
    @Transactional
    public Season createNewSeason() {
        seasonRepository.findByActiveTrue().ifPresent(current -> {
            current.setActive(false);
            current.setEndDate(LocalDateTime.now());
            seasonRepository.save(current);
        });

        long seasonNumber = seasonRepository.count();
        Season newSeason = new Season("Temporada " + seasonNumber, LocalDateTime.now(), true);
        return seasonRepository.save(newSeason);
    }
}
