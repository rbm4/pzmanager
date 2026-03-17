package com.apocalipsebr.zomboid.server.manager.presentation.dto.nfe;

import java.math.BigDecimal;

public class NfeItemDTO {

    private String codigo;
    private String descricao;
    private String ncm;
    private String cfop;
    private String unidade;
    private BigDecimal quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
    private BigDecimal valorDesconto;

    // ICMS
    private String icmsOrigem; // 0=Nacional, 1=Estrangeira importacao direta, 2=Estrangeira adquirida mercado interno
    private String icmsCst; // 00, 10, 20, 30, 40, 41, 50, 51, 60, 70, 90, 102, 103, 300, 400, 500, 900
    private BigDecimal icmsBaseCalculo;
    private BigDecimal icmsAliquota;
    private BigDecimal icmsValor;

    // PIS
    private String pisCst; // 01, 02, 03, 04, 05, 06, 07, 08, 09, 49, 50-56, 60-67, 70-75, 98, 99
    private BigDecimal pisBaseCalculo;
    private BigDecimal pisAliquota;
    private BigDecimal pisValor;

    // COFINS
    private String cofinsCst; // Same as PIS
    private BigDecimal cofinsBaseCalculo;
    private BigDecimal cofinsAliquota;
    private BigDecimal cofinsValor;

    // Getters and Setters
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getNcm() { return ncm; }
    public void setNcm(String ncm) { this.ncm = ncm; }

    public String getCfop() { return cfop; }
    public void setCfop(String cfop) { this.cfop = cfop; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }

    public BigDecimal getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(BigDecimal valorUnitario) { this.valorUnitario = valorUnitario; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }

    public String getIcmsOrigem() { return icmsOrigem; }
    public void setIcmsOrigem(String icmsOrigem) { this.icmsOrigem = icmsOrigem; }

    public String getIcmsCst() { return icmsCst; }
    public void setIcmsCst(String icmsCst) { this.icmsCst = icmsCst; }

    public BigDecimal getIcmsBaseCalculo() { return icmsBaseCalculo; }
    public void setIcmsBaseCalculo(BigDecimal icmsBaseCalculo) { this.icmsBaseCalculo = icmsBaseCalculo; }

    public BigDecimal getIcmsAliquota() { return icmsAliquota; }
    public void setIcmsAliquota(BigDecimal icmsAliquota) { this.icmsAliquota = icmsAliquota; }

    public BigDecimal getIcmsValor() { return icmsValor; }
    public void setIcmsValor(BigDecimal icmsValor) { this.icmsValor = icmsValor; }

    public String getPisCst() { return pisCst; }
    public void setPisCst(String pisCst) { this.pisCst = pisCst; }

    public BigDecimal getPisBaseCalculo() { return pisBaseCalculo; }
    public void setPisBaseCalculo(BigDecimal pisBaseCalculo) { this.pisBaseCalculo = pisBaseCalculo; }

    public BigDecimal getPisAliquota() { return pisAliquota; }
    public void setPisAliquota(BigDecimal pisAliquota) { this.pisAliquota = pisAliquota; }

    public BigDecimal getPisValor() { return pisValor; }
    public void setPisValor(BigDecimal pisValor) { this.pisValor = pisValor; }

    public String getCofinsCst() { return cofinsCst; }
    public void setCofinsCst(String cofinsCst) { this.cofinsCst = cofinsCst; }

    public BigDecimal getCofinsBaseCalculo() { return cofinsBaseCalculo; }
    public void setCofinsBaseCalculo(BigDecimal cofinsBaseCalculo) { this.cofinsBaseCalculo = cofinsBaseCalculo; }

    public BigDecimal getCofinsAliquota() { return cofinsAliquota; }
    public void setCofinsAliquota(BigDecimal cofinsAliquota) { this.cofinsAliquota = cofinsAliquota; }

    public BigDecimal getCofinsValor() { return cofinsValor; }
    public void setCofinsValor(BigDecimal cofinsValor) { this.cofinsValor = cofinsValor; }
}
