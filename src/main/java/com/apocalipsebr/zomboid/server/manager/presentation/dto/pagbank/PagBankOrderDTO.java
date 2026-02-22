package com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank;

import java.util.List;

/**
 * DTO for PagBank PIX order response.
 */
public class PagBankOrderDTO {
    private String id;
    private String reference_id;
    private String created_at;
    private String status;
    private Customer customer;
    private List<Item> items;
    private List<QrCode> qr_codes;
    private List<Charge> charges;
    private List<String> notification_urls;
    private List<Link> links;
    private List<ErrorMessage> error_messages;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReference_id() {
        return reference_id;
    }

    public void setReference_id(String reference_id) {
        this.reference_id = reference_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<QrCode> getQr_codes() {
        return qr_codes;
    }

    public void setQr_codes(List<QrCode> qr_codes) {
        this.qr_codes = qr_codes;
    }

    public List<Charge> getCharges() {
        return charges;
    }

    public void setCharges(List<Charge> charges) {
        this.charges = charges;
    }

    public List<String> getNotification_urls() {
        return notification_urls;
    }

    public void setNotification_urls(List<String> notification_urls) {
        this.notification_urls = notification_urls;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public List<ErrorMessage> getError_messages() {
        return error_messages;
    }

    public void setError_messages(List<ErrorMessage> error_messages) {
        this.error_messages = error_messages;
    }

    /**
     * Checks if the order has been paid by looking at the charges status.
     */
    public boolean isPaid() {
        if (charges != null && !charges.isEmpty()) {
            return charges.stream().anyMatch(c -> "PAID".equalsIgnoreCase(c.getStatus()));
        }
        // Also check qr_codes for payment arrangements
        if (qr_codes != null && !qr_codes.isEmpty()) {
            return qr_codes.stream().anyMatch(qr -> qr.getArrangements() != null && !qr.getArrangements().isEmpty());
        }
        return "PAID".equalsIgnoreCase(status);
    }

    public static class Customer {
        private String name;
        private String email;
        private String tax_id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getTax_id() {
            return tax_id;
        }

        public void setTax_id(String tax_id) {
            this.tax_id = tax_id;
        }
    }

    public static class Item {
        private String name;
        private int quantity;
        private int unit_amount;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getUnit_amount() {
            return unit_amount;
        }

        public void setUnit_amount(int unit_amount) {
            this.unit_amount = unit_amount;
        }
    }

    public static class QrCode {
        private String id;
        private String expiration_date;
        private String text;
        private Amount amount;
        private List<Link> links;
        private List<String> arrangements;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getExpiration_date() {
            return expiration_date;
        }

        public void setExpiration_date(String expiration_date) {
            this.expiration_date = expiration_date;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Amount getAmount() {
            return amount;
        }

        public void setAmount(Amount amount) {
            this.amount = amount;
        }

        public List<Link> getLinks() {
            return links;
        }

        public void setLinks(List<Link> links) {
            this.links = links;
        }

        public List<String> getArrangements() {
            return arrangements;
        }

        public void setArrangements(List<String> arrangements) {
            this.arrangements = arrangements;
        }

        public static class Amount {
            private int value;

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }
        }
    }

    public static class Charge {
        private String id;
        private String status;
        private Amount amount;
        private PaymentMethod payment_method;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Amount getAmount() {
            return amount;
        }

        public void setAmount(Amount amount) {
            this.amount = amount;
        }

        public PaymentMethod getPayment_method() {
            return payment_method;
        }

        public void setPayment_method(PaymentMethod payment_method) {
            this.payment_method = payment_method;
        }

        public static class Amount {
            private int value;
            private String currency;

            public int getValue() {
                return value;
            }

            public void setValue(int value) {
                this.value = value;
            }

            public String getCurrency() {
                return currency;
            }

            public void setCurrency(String currency) {
                this.currency = currency;
            }
        }

        public static class PaymentMethod {
            private String type;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }
    }

    public static class ErrorMessage {
        private String code;
        private String description;
        private String parameter_name;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getParameter_name() {
            return parameter_name;
        }

        public void setParameter_name(String parameter_name) {
            this.parameter_name = parameter_name;
        }
    }

    public static class Link {
        private String rel;
        private String href;
        private String media;
        private String type;

        public String getRel() {
            return rel;
        }

        public void setRel(String rel) {
            this.rel = rel;
        }

        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getMedia() {
            return media;
        }

        public void setMedia(String media) {
            this.media = media;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
