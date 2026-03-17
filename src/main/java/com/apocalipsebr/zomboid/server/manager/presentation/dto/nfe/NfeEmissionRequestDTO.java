package com.apocalipsebr.zomboid.server.manager.presentation.dto.nfe;

import java.math.BigDecimal;
import java.util.List;

public class NfeEmissionRequestDTO {

    // Identificacao
    private String naturezaOperacao;
    private String finalidade; // NORMAL, COMPLEMENTAR, AJUSTE, DEVOLUCAO_MERCADORIA
    private String tipoOperacao; // ENTRADA, SAIDA
    private String destinoOperacao; // OPERACAO_INTERNA, OPERACAO_INTERESTADUAL, OPERACAO_COM_EXTERIOR
    private String consumidorFinal; // SIM, NAO
    private String presencaComprador; // NAO_APLICA, OPERACAO_PRESENCIAL, OPERACAO_NAO_PRESENCIAL_INTERNET, etc.

    // Destinatario
    private String destinatarioCpfCnpj;
    private String destinatarioNome;
    private String destinatarioIe;
    private String destinatarioIndicadorIe; // CONTRIBUINTE_ICMS, CONTRIBUINTE_ISENTO, NAO_CONTRIBUINTE
    private String destinatarioEmail;

    // Endereco Destinatario
    private String destinatarioLogradouro;
    private String destinatarioNumero;
    private String destinatarioComplemento;
    private String destinatarioBairro;
    private String destinatarioCodigoMunicipio;
    private String destinatarioMunicipio;
    private String destinatarioUf;
    private String destinatarioCep;
    private String destinatarioTelefone;

    // Itens
    private List<NfeItemDTO> itens;

    // Transporte
    private String modalidadeFrete; // SEM_OCORRENCIA_TRANSPORTE, CONTRATACAO_POR_CONTA_DO_REMETENTE, etc.

    // Pagamento
    private String meioPagamento; // DINHEIRO, PIX_DINAMICO, CARTAO_CREDITO, BOLETO_BANCARIO, etc.
    private String indicadorPagamento; // A_VISTA, A_PRAZO
    private BigDecimal valorPagamento;

    // Informacoes adicionais
    private String informacoesComplementares;

    // Getters and Setters
    public String getNaturezaOperacao() { return naturezaOperacao; }
    public void setNaturezaOperacao(String naturezaOperacao) { this.naturezaOperacao = naturezaOperacao; }

    public String getFinalidade() { return finalidade; }
    public void setFinalidade(String finalidade) { this.finalidade = finalidade; }

    public String getTipoOperacao() { return tipoOperacao; }
    public void setTipoOperacao(String tipoOperacao) { this.tipoOperacao = tipoOperacao; }

    public String getDestinoOperacao() { return destinoOperacao; }
    public void setDestinoOperacao(String destinoOperacao) { this.destinoOperacao = destinoOperacao; }

    public String getConsumidorFinal() { return consumidorFinal; }
    public void setConsumidorFinal(String consumidorFinal) { this.consumidorFinal = consumidorFinal; }

    public String getPresencaComprador() { return presencaComprador; }
    public void setPresencaComprador(String presencaComprador) { this.presencaComprador = presencaComprador; }

    public String getDestinatarioCpfCnpj() { return destinatarioCpfCnpj; }
    public void setDestinatarioCpfCnpj(String destinatarioCpfCnpj) { this.destinatarioCpfCnpj = destinatarioCpfCnpj; }

    public String getDestinatarioNome() { return destinatarioNome; }
    public void setDestinatarioNome(String destinatarioNome) { this.destinatarioNome = destinatarioNome; }

    public String getDestinatarioIe() { return destinatarioIe; }
    public void setDestinatarioIe(String destinatarioIe) { this.destinatarioIe = destinatarioIe; }

    public String getDestinatarioIndicadorIe() { return destinatarioIndicadorIe; }
    public void setDestinatarioIndicadorIe(String destinatarioIndicadorIe) { this.destinatarioIndicadorIe = destinatarioIndicadorIe; }

    public String getDestinatarioEmail() { return destinatarioEmail; }
    public void setDestinatarioEmail(String destinatarioEmail) { this.destinatarioEmail = destinatarioEmail; }

    public String getDestinatarioLogradouro() { return destinatarioLogradouro; }
    public void setDestinatarioLogradouro(String destinatarioLogradouro) { this.destinatarioLogradouro = destinatarioLogradouro; }

    public String getDestinatarioNumero() { return destinatarioNumero; }
    public void setDestinatarioNumero(String destinatarioNumero) { this.destinatarioNumero = destinatarioNumero; }

    public String getDestinatarioComplemento() { return destinatarioComplemento; }
    public void setDestinatarioComplemento(String destinatarioComplemento) { this.destinatarioComplemento = destinatarioComplemento; }

    public String getDestinatarioBairro() { return destinatarioBairro; }
    public void setDestinatarioBairro(String destinatarioBairro) { this.destinatarioBairro = destinatarioBairro; }

    public String getDestinatarioCodigoMunicipio() { return destinatarioCodigoMunicipio; }
    public void setDestinatarioCodigoMunicipio(String destinatarioCodigoMunicipio) { this.destinatarioCodigoMunicipio = destinatarioCodigoMunicipio; }

    public String getDestinatarioMunicipio() { return destinatarioMunicipio; }
    public void setDestinatarioMunicipio(String destinatarioMunicipio) { this.destinatarioMunicipio = destinatarioMunicipio; }

    public String getDestinatarioUf() { return destinatarioUf; }
    public void setDestinatarioUf(String destinatarioUf) { this.destinatarioUf = destinatarioUf; }

    public String getDestinatarioCep() { return destinatarioCep; }
    public void setDestinatarioCep(String destinatarioCep) { this.destinatarioCep = destinatarioCep; }

    public String getDestinatarioTelefone() { return destinatarioTelefone; }
    public void setDestinatarioTelefone(String destinatarioTelefone) { this.destinatarioTelefone = destinatarioTelefone; }

    public List<NfeItemDTO> getItens() { return itens; }
    public void setItens(List<NfeItemDTO> itens) { this.itens = itens; }

    public String getModalidadeFrete() { return modalidadeFrete; }
    public void setModalidadeFrete(String modalidadeFrete) { this.modalidadeFrete = modalidadeFrete; }

    public String getMeioPagamento() { return meioPagamento; }
    public void setMeioPagamento(String meioPagamento) { this.meioPagamento = meioPagamento; }

    public String getIndicadorPagamento() { return indicadorPagamento; }
    public void setIndicadorPagamento(String indicadorPagamento) { this.indicadorPagamento = indicadorPagamento; }

    public BigDecimal getValorPagamento() { return valorPagamento; }
    public void setValorPagamento(BigDecimal valorPagamento) { this.valorPagamento = valorPagamento; }

    public String getInformacoesComplementares() { return informacoesComplementares; }
    public void setInformacoesComplementares(String informacoesComplementares) { this.informacoesComplementares = informacoesComplementares; }
}
