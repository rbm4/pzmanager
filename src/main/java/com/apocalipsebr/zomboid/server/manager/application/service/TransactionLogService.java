package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.TransactionLog;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.CharacterRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.TransactionLogRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class TransactionLogService {

    private static final Logger logger = Logger.getLogger(TransactionLogService.class.getName());

    private final TransactionLogRepository transactionLogRepository;
    private final CharacterRepository characterRepository;

    public TransactionLogService(TransactionLogRepository transactionLogRepository,
                                  CharacterRepository characterRepository) {
        this.transactionLogRepository = transactionLogRepository;
        this.characterRepository = characterRepository;
    }

    @Transactional
    public TransactionLog logTransaction(User user, Character character, String transactionType,
                                          String itemName, String itemIdRef, Integer amount,
                                          Integer balanceAfter) {
        TransactionLog log = new TransactionLog(user, character, transactionType, 
                                                 itemName, itemIdRef, amount, balanceAfter);
        logger.info("Logging transaction: " + transactionType + " - " + itemName + 
                     " for " + character.getPlayerName() + " (" + amount + " ₳)");
        return transactionLogRepository.save(log);
    }

    public Page<TransactionLog> getTransactions(String search, String type, Pageable pageable) {
        return transactionLogRepository.search(search, type, pageable);
    }

    public Optional<TransactionLog> getById(Long id) {
        return transactionLogRepository.findById(id);
    }

    public long getTotalCount() {
        return transactionLogRepository.count();
    }

    public long getCashbackCount() {
        return transactionLogRepository.countByCashbackTrue();
    }

    @Transactional
    public TransactionLog cashback(Long transactionId, String adminUsername) {
        TransactionLog log = transactionLogRepository.findById(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found with id: " + transactionId));

        if (log.getCashback()) {
            throw new IllegalStateException("This transaction has already been refunded");
        }

        // Refund the currency to the character
        Character character = characterRepository.findById(log.getCharacter().getId())
            .orElseThrow(() -> new IllegalArgumentException("Character not found"));

        int currentPoints = character.getCurrencyPoints() != null ? character.getCurrencyPoints() : 0;
        character.setCurrencyPoints(currentPoints + log.getAmount());
        characterRepository.save(character);

        // Mark the transaction as refunded
        log.setCashback(true);
        log.setCashbackAt(LocalDateTime.now());
        log.setCashbackBy(adminUsername);

        logger.info("Cashback applied for transaction #" + transactionId + 
                     " by admin " + adminUsername + 
                     " - Refunded " + log.getAmount() + " ₳ to " + character.getPlayerName());

        return transactionLogRepository.save(log);
    }
}
