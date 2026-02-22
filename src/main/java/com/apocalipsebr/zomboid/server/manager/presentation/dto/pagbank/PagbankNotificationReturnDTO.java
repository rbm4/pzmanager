package com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "transaction")
@XmlAccessorType(XmlAccessType.FIELD)
public record PagbankNotificationReturnDTO(
        String date,
        String code,
        String reference,
        String type,
        String status,
        String lastEventDate,
        PaymentMethod paymentMethod,
        Pix pix,
        String grossAmount,
        String discountAmount,
        CreditorFees creditorFees,
        String netAmount,
        String extraAmount,
        String escrowEndDate,
        String installmentCount,
        String itemCount,
        @XmlElementWrapper(name = "items")
        @XmlElement(name = "item")
        List<Item> items,
        Sender sender,
        PrimaryReceiver primaryReceiver
) {

    public record PaymentMethod(String type, String code) {}

    public record Pix(
            String pixDate,
            String holderName,
            String personType,
            String bankName,
            String bankAgency,
            String bankAccount,
            String bankAccountType
    ) {}

    public record CreditorFees(
            String intermediationRateAmount,
            String intermediationFeeAmount
    ) {}

    @XmlAccessorType(XmlAccessType.FIELD)
    public record Item(
            String id,
            String description,
            String quantity,
            String amount
    ) {}

    @XmlAccessorType(XmlAccessType.FIELD)
    public record Sender(
            String name,
            String email,
            @XmlElementWrapper(name = "documents")
            @XmlElement(name = "document")
            List<Document> documents
    ) {}

    public record Document(String type, String value) {}

    public record PrimaryReceiver(String publicKey) {}
}
