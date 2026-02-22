package com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank;

import java.util.List;

/**
 * DTO for creating a PagBank PIX order request.
 */
public class PagBankOrderParamsDTO {
    private String reference_id;
    private Customer customer;
    private List<Item> items;
    private List<QrCode> qr_codes;
    private List<String> notification_urls;

    public PagBankOrderParamsDTO() {}

    public String getReference_id() { return reference_id; }
    public void setReference_id(String reference_id) { this.reference_id = reference_id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
    public List<QrCode> getQr_codes() { return qr_codes; }
    public void setQr_codes(List<QrCode> qr_codes) { this.qr_codes = qr_codes; }
    public List<String> getNotification_urls() { return notification_urls; }
    public void setNotification_urls(List<String> notification_urls) { this.notification_urls = notification_urls; }

    public static class Customer {
        private String name;
        private String email;
        private String tax_id;

        public Customer() {}
        public Customer(String name, String email,String taxId) {
            this.name = name;
            this.email = email;
            this.tax_id = taxId;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getTax_id() { return tax_id; }
        public void setTax_id(String tax_id) { this.tax_id = tax_id; }
    }

    public static class Item {
        private String name;
        private int quantity;
        private int unit_amount;

        public Item() {}
        public Item(String name, int quantity, int unit_amount) {
            this.name = name;
            this.quantity = quantity;
            this.unit_amount = unit_amount;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public int getUnit_amount() { return unit_amount; }
        public void setUnit_amount(int unit_amount) { this.unit_amount = unit_amount; }
    }

    public static class QrCode {
        private Amount amount;
        private String expiration_date;

        public QrCode() {}
        public QrCode(Amount amount, String expiration_date) {
            this.amount = amount;
            this.expiration_date = expiration_date;
        }

        public Amount getAmount() { return amount; }
        public void setAmount(Amount amount) { this.amount = amount; }
        public String getExpiration_date() { return expiration_date; }
        public void setExpiration_date(String expiration_date) { this.expiration_date = expiration_date; }

        public static class Amount {
            private int value;

            public Amount() {}
            public Amount(int value) { this.value = value; }

            public int getValue() { return value; }
            public void setValue(int value) { this.value = value; }
        }
    }
}
