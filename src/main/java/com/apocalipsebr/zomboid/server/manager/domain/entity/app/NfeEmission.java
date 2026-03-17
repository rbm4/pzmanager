package com.apocalipsebr.zomboid.server.manager.domain.entity.app;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nfe_emissions")
public class NfeEmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "numero_nota", nullable = false)
    private String numeroNota;

    @Column(name = "serie", nullable = false)
    private String serie;

    @Column(name = "chave_acesso")
    private String chaveAcesso;

    @Column(name = "protocolo")
    private String protocolo;

    @Column(name = "numero_recibo")
    private String numeroRecibo;

    @Column(name = "status", nullable = false)
    private String status; // PENDENTE, AUTORIZADA, REJEITADA, CANCELADA, ERRO

    @Column(name = "status_sefaz")
    private String statusSefaz;

    @Column(name = "motivo_sefaz", columnDefinition = "TEXT")
    private String motivoSefaz;

    @Column(name = "natureza_operacao", nullable = false)
    private String naturezaOperacao;

    // Emitente
    @Column(name = "emitente_cnpj", nullable = false)
    private String emitenteCnpj;

    @Column(name = "emitente_razao_social", nullable = false)
    private String emitenteRazaoSocial;

    @Column(name = "emitente_ie")
    private String emitenteIe;

    // Destinatario
    @Column(name = "destinatario_cpf_cnpj")
    private String destinatarioCpfCnpj;

    @Column(name = "destinatario_nome")
    private String destinatarioNome;

    // Valores
    @Column(name = "valor_total", nullable = false)
    private Double valorTotal;

    @Column(name = "valor_icms")
    private Double valorIcms;

    @Column(name = "valor_pis")
    private Double valorPis;

    @Column(name = "valor_cofins")
    private Double valorCofins;

    // XML
    @Column(name = "xml_envio", columnDefinition = "TEXT")
    private String xmlEnvio;

    @Column(name = "xml_retorno", columnDefinition = "TEXT")
    private String xmlRetorno;

    @Column(name = "xml_nota_processada", columnDefinition = "TEXT")
    private String xmlNotaProcessada;

    // Cancelamento
    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    @Column(name = "cancelada_em", columnDefinition = "TIMESTAMP")
    private LocalDateTime canceladaEm;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt;

    public NfeEmission() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "PENDENTE";
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getNumeroNota() { return numeroNota; }
    public void setNumeroNota(String numeroNota) { this.numeroNota = numeroNota; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public String getChaveAcesso() { return chaveAcesso; }
    public void setChaveAcesso(String chaveAcesso) { this.chaveAcesso = chaveAcesso; }

    public String getProtocolo() { return protocolo; }
    public void setProtocolo(String protocolo) { this.protocolo = protocolo; }

    public String getNumeroRecibo() { return numeroRecibo; }
    public void setNumeroRecibo(String numeroRecibo) { this.numeroRecibo = numeroRecibo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusSefaz() { return statusSefaz; }
    public void setStatusSefaz(String statusSefaz) { this.statusSefaz = statusSefaz; }

    public String getMotivoSefaz() { return motivoSefaz; }
    public void setMotivoSefaz(String motivoSefaz) { this.motivoSefaz = motivoSefaz; }

    public String getNaturezaOperacao() { return naturezaOperacao; }
    public void setNaturezaOperacao(String naturezaOperacao) { this.naturezaOperacao = naturezaOperacao; }

    public String getEmitenteCnpj() { return emitenteCnpj; }
    public void setEmitenteCnpj(String emitenteCnpj) { this.emitenteCnpj = emitenteCnpj; }

    public String getEmitenteRazaoSocial() { return emitenteRazaoSocial; }
    public void setEmitenteRazaoSocial(String emitenteRazaoSocial) { this.emitenteRazaoSocial = emitenteRazaoSocial; }

    public String getEmitenteIe() { return emitenteIe; }
    public void setEmitenteIe(String emitenteIe) { this.emitenteIe = emitenteIe; }

    public String getDestinatarioCpfCnpj() { return destinatarioCpfCnpj; }
    public void setDestinatarioCpfCnpj(String destinatarioCpfCnpj) { this.destinatarioCpfCnpj = destinatarioCpfCnpj; }

    public String getDestinatarioNome() { return destinatarioNome; }
    public void setDestinatarioNome(String destinatarioNome) { this.destinatarioNome = destinatarioNome; }

    public Double getValorTotal() { return valorTotal; }
    public void setValorTotal(Double valorTotal) { this.valorTotal = valorTotal; }

    public Double getValorIcms() { return valorIcms; }
    public void setValorIcms(Double valorIcms) { this.valorIcms = valorIcms; }

    public Double getValorPis() { return valorPis; }
    public void setValorPis(Double valorPis) { this.valorPis = valorPis; }

    public Double getValorCofins() { return valorCofins; }
    public void setValorCofins(Double valorCofins) { this.valorCofins = valorCofins; }

    public String getXmlEnvio() { return xmlEnvio; }
    public void setXmlEnvio(String xmlEnvio) { this.xmlEnvio = xmlEnvio; }

    public String getXmlRetorno() { return xmlRetorno; }
    public void setXmlRetorno(String xmlRetorno) { this.xmlRetorno = xmlRetorno; }

    public String getXmlNotaProcessada() { return xmlNotaProcessada; }
    public void setXmlNotaProcessada(String xmlNotaProcessada) { this.xmlNotaProcessada = xmlNotaProcessada; }

    public String getMotivoCancelamento() { return motivoCancelamento; }
    public void setMotivoCancelamento(String motivoCancelamento) { this.motivoCancelamento = motivoCancelamento; }

    public LocalDateTime getCanceladaEm() { return canceladaEm; }
    public void setCanceladaEm(LocalDateTime canceladaEm) { this.canceladaEm = canceladaEm; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
