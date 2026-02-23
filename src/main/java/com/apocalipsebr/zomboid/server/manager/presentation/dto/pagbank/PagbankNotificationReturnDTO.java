package com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "transaction")
@XmlAccessorType(XmlAccessType.FIELD)
public class PagbankNotificationReturnDTO {

    private String date;
    private String code;
    private String reference;
    private String type;
    private String status;
    private String lastEventDate;
    private PaymentMethod paymentMethod;
    private Pix pix;
    private String grossAmount;
    private String discountAmount;
    private CreditorFees creditorFees;
    private String netAmount;
    private String extraAmount;
    private String escrowEndDate;
    private String installmentCount;
    private String itemCount;
    @XmlElementWrapper(name = "items")
    @XmlElement(name = "item")
    private List<Item> items;
    private Sender sender;
    private PrimaryReceiver primaryReceiver;

    public PagbankNotificationReturnDTO() {}

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLastEventDate() { return lastEventDate; }
    public void setLastEventDate(String lastEventDate) { this.lastEventDate = lastEventDate; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public Pix getPix() { return pix; }
    public void setPix(Pix pix) { this.pix = pix; }

    public String getGrossAmount() { return grossAmount; }
    public void setGrossAmount(String grossAmount) { this.grossAmount = grossAmount; }

    public String getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(String discountAmount) { this.discountAmount = discountAmount; }

    public CreditorFees getCreditorFees() { return creditorFees; }
    public void setCreditorFees(CreditorFees creditorFees) { this.creditorFees = creditorFees; }

    public String getNetAmount() { return netAmount; }
    public void setNetAmount(String netAmount) { this.netAmount = netAmount; }

    public String getExtraAmount() { return extraAmount; }
    public void setExtraAmount(String extraAmount) { this.extraAmount = extraAmount; }

    public String getEscrowEndDate() { return escrowEndDate; }
    public void setEscrowEndDate(String escrowEndDate) { this.escrowEndDate = escrowEndDate; }

    public String getInstallmentCount() { return installmentCount; }
    public void setInstallmentCount(String installmentCount) { this.installmentCount = installmentCount; }

    public String getItemCount() { return itemCount; }
    public void setItemCount(String itemCount) { this.itemCount = itemCount; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public Sender getSender() { return sender; }
    public void setSender(Sender sender) { this.sender = sender; }

    public PrimaryReceiver getPrimaryReceiver() { return primaryReceiver; }
    public void setPrimaryReceiver(PrimaryReceiver primaryReceiver) { this.primaryReceiver = primaryReceiver; }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PaymentMethod {
        private String type;
        private String code;

        public PaymentMethod() {}

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Pix {
        private String pixDate;
        private String holderName;
        private String personType;
        private String bankName;
        private String bankAgency;
        private String bankAccount;
        private String bankAccountType;

        public Pix() {}

        public String getPixDate() { return pixDate; }
        public void setPixDate(String pixDate) { this.pixDate = pixDate; }

        public String getHolderName() { return holderName; }
        public void setHolderName(String holderName) { this.holderName = holderName; }

        public String getPersonType() { return personType; }
        public void setPersonType(String personType) { this.personType = personType; }

        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }

        public String getBankAgency() { return bankAgency; }
        public void setBankAgency(String bankAgency) { this.bankAgency = bankAgency; }

        public String getBankAccount() { return bankAccount; }
        public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

        public String getBankAccountType() { return bankAccountType; }
        public void setBankAccountType(String bankAccountType) { this.bankAccountType = bankAccountType; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CreditorFees {
        private String intermediationRateAmount;
        private String intermediationFeeAmount;

        public CreditorFees() {}

        public String getIntermediationRateAmount() { return intermediationRateAmount; }
        public void setIntermediationRateAmount(String intermediationRateAmount) { this.intermediationRateAmount = intermediationRateAmount; }

        public String getIntermediationFeeAmount() { return intermediationFeeAmount; }
        public void setIntermediationFeeAmount(String intermediationFeeAmount) { this.intermediationFeeAmount = intermediationFeeAmount; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Item {
        private String id;
        private String description;
        private String quantity;
        private String amount;

        public Item() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }

        public String getAmount() { return amount; }
        public void setAmount(String amount) { this.amount = amount; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Sender {
        private String name;
        private String email;
        @XmlElementWrapper(name = "documents")
        @XmlElement(name = "document")
        private List<Document> documents;

        public Sender() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public List<Document> getDocuments() { return documents; }
        public void setDocuments(List<Document> documents) { this.documents = documents; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Document {
        private String type;
        private String value;

        public Document() {}

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PrimaryReceiver {
        private String publicKey;

        public PrimaryReceiver() {}

        public String getPublicKey() { return publicKey; }
        public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
    }
}
