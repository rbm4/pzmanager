package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

public enum SafehouseClaimStatus {
    PENDING_REVIEW("Pendente"),
    APPROVED("Aprovado"),
    DENIED("Negado"),
    CANCELLED("Cancelado");

    private final String displayName;

    SafehouseClaimStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}