package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

public enum SoftWipeStatus {
    WAITING_RESTART("Aguardando Restart"),
    WIPE_AT_RESTART("Wipe no Restart"),
    COMPLETED("Concluído"),
    FAILED("Falhou"),
    CANCELLED("Cancelado");

    private final String displayName;

    SoftWipeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
