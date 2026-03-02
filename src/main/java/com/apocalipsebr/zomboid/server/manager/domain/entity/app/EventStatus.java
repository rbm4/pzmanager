package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

public enum EventStatus {
    PENDING("Pendente"),
    FUNDED("Financiado"),
    ACTIVE("Ativo"),
    EXPIRED("Expirado"),
    CANCELLED("Cancelado");

    private final String displayName;

    EventStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
