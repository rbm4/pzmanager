package com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank;

/**
 * DTO for the donation creation request from the frontend.
 */
public class DonationRequestDTO {
    private int amountCentavos; // amount in BRL centavos (e.g., 500 = R$5.00)
    private String email;
    private String cpf;
    private boolean rememberInfo;

    public DonationRequestDTO() {}

    public int getAmountCentavos() { return amountCentavos; }
    public void setAmountCentavos(int amountCentavos) { this.amountCentavos = amountCentavos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public boolean isRememberInfo() { return rememberInfo; }
    public void setRememberInfo(boolean rememberInfo) { this.rememberInfo = rememberInfo; }
}
