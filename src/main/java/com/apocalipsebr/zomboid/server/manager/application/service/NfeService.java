package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.NfeEmission;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.NfeEmissionRepository;
import com.apocalipsebr.zomboid.server.manager.infrastructure.config.NfeConfiguration;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.nfe.NfeEmissionRequestDTO;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.nfe.NfeItemDTO;

import com.fincatto.documentofiscal.DFModelo;
import com.fincatto.documentofiscal.DFUnidadeFederativa;
import com.fincatto.documentofiscal.nfe400.classes.*;
import com.fincatto.documentofiscal.nfe400.classes.lote.consulta.NFLoteConsultaRetorno;
import com.fincatto.documentofiscal.nfe400.classes.lote.envio.NFLoteEnvio;
import com.fincatto.documentofiscal.nfe400.classes.lote.envio.NFLoteEnvioRetornoDados;
import com.fincatto.documentofiscal.nfe400.classes.lote.envio.NFLoteIndicadorProcessamento;
import com.fincatto.documentofiscal.nfe400.classes.nota.*;
import com.fincatto.documentofiscal.nfe400.classes.nota.consulta.NFNotaConsultaRetorno;
import com.fincatto.documentofiscal.nfe400.classes.statusservico.consulta.NFStatusServicoConsultaRetorno;
import com.fincatto.documentofiscal.nfe400.webservices.WSFacade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class NfeService {

    private static final Logger logger = LoggerFactory.getLogger(NfeService.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final NfeConfiguration nfeConfig;
    private final NfeEmissionRepository nfeEmissionRepository;

    @Value("${nfe.emitente.cnpj:}")
    private String emitenteCnpj;

    @Value("${nfe.emitente.razao.social:}")
    private String emitenteRazaoSocial;

    @Value("${nfe.emitente.nome.fantasia:}")
    private String emitenteNomeFantasia;

    @Value("${nfe.emitente.ie:}")
    private String emitenteIe;

    @Value("${nfe.emitente.regime.tributario:SIMPLES_NACIONAL}")
    private String emitenteRegimeTributario;

    @Value("${nfe.emitente.logradouro:}")
    private String emitenteLogradouro;

    @Value("${nfe.emitente.numero:}")
    private String emitenteNumero;

    @Value("${nfe.emitente.bairro:}")
    private String emitenteBairro;

    @Value("${nfe.emitente.codigo.municipio:}")
    private String emitenteCodigoMunicipio;

    @Value("${nfe.emitente.municipio:}")
    private String emitenteMunicipio;

    @Value("${nfe.emitente.uf:}")
    private String emitenteUf;

    @Value("${nfe.emitente.cep:}")
    private String emitenteCep;

    @Value("${nfe.serie:1}")
    private String serie;

    @Value("${nfe.versao.emissor:PZManager 1.0}")
    private String versaoEmissor;

    public NfeService(NfeConfiguration nfeConfig, NfeEmissionRepository nfeEmissionRepository) {
        this.nfeConfig = nfeConfig;
        this.nfeEmissionRepository = nfeEmissionRepository;
    }

    public boolean isConfigured() {
        return nfeConfig.isConfigured()
                && emitenteCnpj != null && !emitenteCnpj.isBlank()
                && emitenteRazaoSocial != null && !emitenteRazaoSocial.isBlank();
    }

    /**
     * Consulta o status do serviço da SEFAZ.
     */
    public String consultarStatusSefaz() {
        try {
            WSFacade facade = new WSFacade(nfeConfig);
            NFStatusServicoConsultaRetorno retorno = facade.consultaStatus(
                    nfeConfig.getCUF(), DFModelo.NFE);
            return retorno.getStatus() + " - " + retorno.getMotivo();
        } catch (Exception e) {
            logger.error("Erro ao consultar status da SEFAZ", e);
            return "ERRO: " + e.getMessage();
        }
    }

    /**
     * Emite uma NF-e com base nos dados do DTO.
     */
    @Transactional
    public NfeEmission emitirNfe(NfeEmissionRequestDTO request, User user) throws Exception {
        if (!isConfigured()) {
            throw new IllegalStateException("NFe não está configurada. Verifique as configurações do certificado e emitente.");
        }

        // Gera proximo numero da nota
        int proximoNumero = nfeEmissionRepository.findMaxNumeroNotaBySerie(serie) + 1;
        String numeroNota = String.format("%09d", proximoNumero);

        // Cria a NF-e
        NFNota nota = buildNota(request, numeroNota);

        // Cria o lote
        NFLoteEnvio lote = new NFLoteEnvio();
        lote.setVersao("4.00");
        lote.setIdLote(String.format("%015d", proximoNumero));
        lote.setIndicadorProcessamento(NFLoteIndicadorProcessamento.PROCESSAMENTO_SINCRONO);
        lote.setNotas(Collections.singletonList(nota));

        // Salva o registro antes do envio
        NfeEmission emission = new NfeEmission();
        emission.setUser(user);
        emission.setNumeroNota(numeroNota);
        emission.setSerie(serie);
        emission.setNaturezaOperacao(request.getNaturezaOperacao());
        emission.setEmitenteCnpj(emitenteCnpj);
        emission.setEmitenteRazaoSocial(emitenteRazaoSocial);
        emission.setEmitenteIe(emitenteIe);
        emission.setDestinatarioCpfCnpj(request.getDestinatarioCpfCnpj());
        emission.setDestinatarioNome(request.getDestinatarioNome());
        emission.setValorTotal(calcularValorTotal(request).doubleValue());
        emission.setXmlEnvio(lote.toString());
        nfeEmissionRepository.save(emission);
        nfeEmissionRepository.flush();

        try {
            // Envia o lote
            WSFacade facade = new WSFacade(nfeConfig);
            NFLoteEnvioRetornoDados retorno = facade.enviaLote(lote);

            // Processa o retorno
            if (retorno != null) {
                emission.setXmlRetorno(retorno.toString());

                NFProtocoloInfo protocoloInfo = retorno.getRetorno().getProtocoloInfo();
                if (protocoloInfo != null) {
                    emission.setProtocolo(protocoloInfo.getNumeroProtocolo());
                    emission.setChaveAcesso(protocoloInfo.getChave());
                    emission.setStatusSefaz(protocoloInfo.getStatus());
                    emission.setMotivoSefaz(protocoloInfo.getMotivo());

                    String status = protocoloInfo.getStatus();
                    if ("100".equals(status)) {
                        emission.setStatus("AUTORIZADA");

                        // Salva a nota processada
                        NFNotaProcessada notaProcessada = new NFNotaProcessada();
                        notaProcessada.setVersao(new BigDecimal("4.00"));
                        NFProtocolo protocolo = new NFProtocolo();
                        protocolo.setProtocoloInfo(protocoloInfo);
                        notaProcessada.setProtocolo(protocolo);
                        notaProcessada.setNota(nota);
                        emission.setXmlNotaProcessada(notaProcessada.toString());
                    } else {
                        emission.setStatus("REJEITADA");
                    }
                }

                if (retorno.getRetorno().getInfoRecebimento() != null) {
                    emission.setNumeroRecibo(retorno.getRetorno().getInfoRecebimento().getRecibo());
                }
            }

        } catch (Exception e) {
            logger.error("Erro ao enviar NF-e para SEFAZ", e);
            emission.setStatus("ERRO");
            emission.setMotivoSefaz(e.getMessage());
        }

        nfeEmissionRepository.save(emission);
        return emission;
    }

    /**
     * Consulta a situação de uma NF-e pela chave de acesso.
     */
    public NFNotaConsultaRetorno consultarNota(String chaveAcesso) throws Exception {
        WSFacade facade = new WSFacade(nfeConfig);
        return facade.consultaNota(chaveAcesso);
    }

    /**
     * Consulta o lote pelo número do recibo.
     */
    public NFLoteConsultaRetorno consultarLote(String numeroRecibo) throws Exception {
        WSFacade facade = new WSFacade(nfeConfig);
        return facade.consultaLote(numeroRecibo, DFModelo.NFE);
    }

    /**
     * Cancela uma NF-e autorizada.
     */
    @Transactional
    public NfeEmission cancelarNota(Long emissionId, String motivo) throws Exception {
        NfeEmission emission = nfeEmissionRepository.findById(emissionId)
                .orElseThrow(() -> new IllegalArgumentException("Emissão não encontrada: " + emissionId));

        if (!"AUTORIZADA".equals(emission.getStatus())) {
            throw new IllegalStateException("Apenas notas autorizadas podem ser canceladas. Status atual: " + emission.getStatus());
        }

        if (emission.getChaveAcesso() == null || emission.getProtocolo() == null) {
            throw new IllegalStateException("Nota sem chave de acesso ou protocolo. Não é possível cancelar.");
        }

        if (motivo == null || motivo.length() < 15) {
            throw new IllegalArgumentException("O motivo do cancelamento deve ter pelo menos 15 caracteres.");
        }

        WSFacade facade = new WSFacade(nfeConfig);
        facade.cancelaNota(emission.getChaveAcesso(), emission.getProtocolo(), motivo);

        emission.setStatus("CANCELADA");
        emission.setMotivoCancelamento(motivo);
        emission.setCanceladaEm(LocalDateTime.now());
        nfeEmissionRepository.save(emission);

        return emission;
    }

    /**
     * Corrige uma NF-e autorizada (Carta de Correção).
     */
    public String corrigirNota(Long emissionId, String textoCorrecao) throws Exception {
        NfeEmission emission = nfeEmissionRepository.findById(emissionId)
                .orElseThrow(() -> new IllegalArgumentException("Emissão não encontrada: " + emissionId));

        if (!"AUTORIZADA".equals(emission.getStatus())) {
            throw new IllegalStateException("Apenas notas autorizadas podem receber carta de correção.");
        }

        WSFacade facade = new WSFacade(nfeConfig);
        var retorno = facade.corrigeNota(emission.getChaveAcesso(), textoCorrecao, 1);
        return retorno.toString();
    }

    /**
     * Inutiliza um range de numeração.
     */
    public String inutilizarNumeracao(int ano, String serieInutilizar, String numeroInicial, String numeroFinal, String justificativa) throws Exception {
        WSFacade facade = new WSFacade(nfeConfig);
        var retorno = facade.inutilizaNota(ano, emitenteCnpj, serieInutilizar, numeroInicial, numeroFinal, justificativa, DFModelo.NFE);
        return retorno.toString();
    }

    public List<NfeEmission> listarEmissoes() {
        return nfeEmissionRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<NfeEmission> listarEmissoesPorUsuario(User user) {
        return nfeEmissionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Optional<NfeEmission> buscarEmissao(Long id) {
        return nfeEmissionRepository.findById(id);
    }

    // ===== Private helper methods =====

    private NFNota buildNota(NfeEmissionRequestDTO request, String numeroNota) {
        NFNota nota = new NFNota();
        NFNotaInfo info = new NFNotaInfo();

        info.setIdentificacao(buildIdentificacao(request, numeroNota));
        info.setEmitente(buildEmitente());

        if (request.getDestinatarioCpfCnpj() != null && !request.getDestinatarioCpfCnpj().isBlank()) {
            info.setDestinatario(buildDestinatario(request));
        }

        info.setItens(buildItens(request));
        info.setTotal(buildTotal(request));
        info.setTransporte(buildTransporte(request));
        info.setPagamento(buildPagamento(request));

        if (request.getInformacoesComplementares() != null && !request.getInformacoesComplementares().isBlank()) {
            NFNotaInfoInformacoesAdicionais infAdic = new NFNotaInfoInformacoesAdicionais();
            infAdic.setInformacoesComplementaresInteresseContribuinte(request.getInformacoesComplementares());
            info.setInformacoesAdicionais(infAdic);
        }

        info.setVersao(new BigDecimal("4.00"));
        nota.setInfo(info);
        return nota;
    }

    private NFNotaInfoIdentificacao buildIdentificacao(NfeEmissionRequestDTO request, String numeroNota) {
        NFNotaInfoIdentificacao ident = new NFNotaInfoIdentificacao();
        ident.setUf(nfeConfig.getCUF());
        ident.setCodigoRandomico(String.format("%08d", RANDOM.nextInt(100000000)));
        ident.setNaturezaOperacao(request.getNaturezaOperacao());
        ident.setModelo(DFModelo.NFE);
        ident.setSerie(serie);
        ident.setNumeroNota(numeroNota);
        ident.setDataHoraEmissao(ZonedDateTime.now());
        ident.setTipo(NFTipo.valueOf(request.getTipoOperacao()));
        ident.setIdentificadorLocalDestinoOperacao(
                NFIdentificadorLocalDestinoOperacao.valueOf(request.getDestinoOperacao()));
        ident.setCodigoMunicipio(emitenteCodigoMunicipio);
        ident.setTipoImpressao(NFTipoImpressao.DANFE_NORMAL_RETRATO);
        ident.setTipoEmissao(nfeConfig.getTipoEmissao());
        ident.setAmbiente(nfeConfig.getAmbiente());
        ident.setFinalidade(NFFinalidade.valueOf(request.getFinalidade()));
        ident.setOperacaoConsumidorFinal(
                NFOperacaoConsumidorFinal.valueOf(request.getConsumidorFinal()));
        ident.setIndicadorPresencaComprador(
                NFIndicadorPresencaComprador.valueOf(request.getPresencaComprador()));
        ident.setProgramaEmissor(NFProcessoEmissor.CONTRIBUINTE);
        ident.setVersaoEmissor(versaoEmissor);
        ident.setDigitoVerificador(0); // Será recalculado pela lib
        return ident;
    }

    private NFNotaInfoEmitente buildEmitente() {
        NFNotaInfoEmitente emitente = new NFNotaInfoEmitente();
        emitente.setCnpj(emitenteCnpj);
        emitente.setRazaoSocial(emitenteRazaoSocial);

        if (emitenteNomeFantasia != null && !emitenteNomeFantasia.isBlank()) {
            emitente.setNomeFantasia(emitenteNomeFantasia);
        }

        emitente.setInscricaoEstadual(emitenteIe);
        emitente.setRegimeTributario(NFRegimeTributario.valueOf(emitenteRegimeTributario));

        NFEndereco endereco = new NFEndereco();
        endereco.setLogradouro(emitenteLogradouro);
        endereco.setNumero(emitenteNumero);
        endereco.setBairro(emitenteBairro);
        endereco.setCodigoMunicipio(emitenteCodigoMunicipio);
        endereco.setDescricaoMunicipio(emitenteMunicipio);
        endereco.setUf(DFUnidadeFederativa.valueOf(emitenteUf));
        endereco.setCep(emitenteCep);
        endereco.setCodigoPais("1058");
        endereco.setDescricaoPais("BRASIL");
        emitente.setEndereco(endereco);

        return emitente;
    }

    private NFNotaInfoDestinatario buildDestinatario(NfeEmissionRequestDTO request) {
        NFNotaInfoDestinatario dest = new NFNotaInfoDestinatario();

        String cpfCnpj = request.getDestinatarioCpfCnpj().replaceAll("\\D", "");
        if (cpfCnpj.length() == 11) {
            dest.setCpf(cpfCnpj);
        } else if (cpfCnpj.length() == 14) {
            dest.setCnpj(cpfCnpj);
        }

        if (request.getDestinatarioNome() != null && !request.getDestinatarioNome().isBlank()) {
            dest.setRazaoSocial(request.getDestinatarioNome());
        }

        dest.setIndicadorIEDestinatario(
                NFIndicadorIEDestinatario.valueOf(request.getDestinatarioIndicadorIe()));

        if (request.getDestinatarioIe() != null && !request.getDestinatarioIe().isBlank()) {
            dest.setInscricaoEstadual(request.getDestinatarioIe());
        }

        if (request.getDestinatarioEmail() != null && !request.getDestinatarioEmail().isBlank()) {
            dest.setEmail(request.getDestinatarioEmail());
        }

        if (request.getDestinatarioLogradouro() != null && !request.getDestinatarioLogradouro().isBlank()) {
            NFEndereco endereco = new NFEndereco();
            endereco.setLogradouro(request.getDestinatarioLogradouro());
            endereco.setNumero(request.getDestinatarioNumero());
            if (request.getDestinatarioComplemento() != null && !request.getDestinatarioComplemento().isBlank()) {
                endereco.setComplemento(request.getDestinatarioComplemento());
            }
            endereco.setBairro(request.getDestinatarioBairro());
            endereco.setCodigoMunicipio(request.getDestinatarioCodigoMunicipio());
            endereco.setDescricaoMunicipio(request.getDestinatarioMunicipio());
            endereco.setUf(DFUnidadeFederativa.valueOf(request.getDestinatarioUf()));
            endereco.setCep(request.getDestinatarioCep());
            endereco.setCodigoPais("1058");
            endereco.setDescricaoPais("BRASIL");
            dest.setEndereco(endereco);
        }

        return dest;
    }

    private List<NFNotaInfoItem> buildItens(NfeEmissionRequestDTO request) {
        List<NFNotaInfoItem> itens = new ArrayList<>();
        int itemNum = 1;

        for (NfeItemDTO itemDto : request.getItens()) {
            NFNotaInfoItem item = new NFNotaInfoItem();
            item.setNumeroItem(itemNum++);

            // Produto
            NFNotaInfoItemProduto produto = new NFNotaInfoItemProduto();
            produto.setCodigo(itemDto.getCodigo());
            produto.setCodigoDeBarrasGtin("SEM GTIN");
            produto.setDescricao(itemDto.getDescricao());
            produto.setNcm(itemDto.getNcm());
            produto.setCfop(itemDto.getCfop());
            produto.setUnidadeComercial(itemDto.getUnidade());
            produto.setQuantidadeComercial(itemDto.getQuantidade().setScale(4, RoundingMode.HALF_UP));
            produto.setValorUnitario(itemDto.getValorUnitario().setScale(10, RoundingMode.HALF_UP));
            produto.setValorTotalBruto(itemDto.getValorTotal().setScale(2, RoundingMode.HALF_UP));
            produto.setCodigoDeBarrasGtinTributavel("SEM GTIN");
            produto.setUnidadeTributavel(itemDto.getUnidade());
            produto.setQuantidadeTributavel(itemDto.getQuantidade().setScale(4, RoundingMode.HALF_UP));
            produto.setValorUnitarioTributavel(itemDto.getValorUnitario().setScale(10, RoundingMode.HALF_UP));
            produto.setCompoeValorNota(NFProdutoCompoeValorNota.SIM);

            if (itemDto.getValorDesconto() != null && itemDto.getValorDesconto().compareTo(BigDecimal.ZERO) > 0) {
                produto.setValorDesconto(itemDto.getValorDesconto().setScale(2, RoundingMode.HALF_UP));
            }

            item.setProduto(produto);

            // Impostos
            item.setImposto(buildImposto(itemDto));

            itens.add(item);
        }

        return itens;
    }

    private NFNotaInfoItemImposto buildImposto(NfeItemDTO itemDto) {
        NFNotaInfoItemImposto imposto = new NFNotaInfoItemImposto();

        // ICMS
        NFNotaInfoItemImpostoICMS icms = new NFNotaInfoItemImpostoICMS();
        buildIcms(icms, itemDto);
        imposto.setIcms(icms);

        // PIS
        NFNotaInfoItemImpostoPIS pis = new NFNotaInfoItemImpostoPIS();
        buildPis(pis, itemDto);
        imposto.setPis(pis);

        // COFINS
        NFNotaInfoItemImpostoCOFINS cofins = new NFNotaInfoItemImpostoCOFINS();
        buildCofins(cofins, itemDto);
        imposto.setCofins(cofins);

        return imposto;
    }

    private void buildIcms(NFNotaInfoItemImpostoICMS icms, NfeItemDTO itemDto) {
        String cst = itemDto.getIcmsCst();
        NFOrigem origem = NFOrigem.valueOf(itemDto.getIcmsOrigem());

        switch (cst) {
            case "00" -> {
                NFNotaInfoItemImpostoICMS00 icms00 = new NFNotaInfoItemImpostoICMS00();
                icms00.setOrigem(origem);
                icms00.setSituacaoTributaria(NFNotaInfoImpostoTributacaoICMS.CST_00);
                icms00.setModalidadeBCICMS(NFNotaInfoItemModalidadeBCICMS.VALOR_OPERACAO);
                icms00.setValorBaseCalculo(itemDto.getIcmsBaseCalculo().setScale(2, RoundingMode.HALF_UP));
                icms00.setPercentualAliquota(itemDto.getIcmsAliquota().setScale(2, RoundingMode.HALF_UP));
                icms00.setValorTributo(itemDto.getIcmsValor().setScale(2, RoundingMode.HALF_UP));
                icms.setIcms00(icms00);
            }
            case "20" -> {
                NFNotaInfoItemImpostoICMS20 icms20 = new NFNotaInfoItemImpostoICMS20();
                icms20.setOrigem(origem);
                icms20.setSituacaoTributaria(NFNotaInfoImpostoTributacaoICMS.CST_20);
                icms20.setModalidadeBCICMS(NFNotaInfoItemModalidadeBCICMS.VALOR_OPERACAO);
                icms20.setValorBCICMS(itemDto.getIcmsBaseCalculo().setScale(2, RoundingMode.HALF_UP));
                icms20.setPercentualAliquota(itemDto.getIcmsAliquota().setScale(2, RoundingMode.HALF_UP));
                icms20.setValorTributo(itemDto.getIcmsValor().setScale(2, RoundingMode.HALF_UP));
                icms.setIcms20(icms20);
            }
            case "40", "41", "50" -> {
                NFNotaInfoItemImpostoICMS40 icms40 = new NFNotaInfoItemImpostoICMS40();
                icms40.setOrigem(origem);
                icms40.setSituacaoTributaria(NFNotaInfoImpostoTributacaoICMS.valueOf("CST_" + cst));
                icms.setIcms40(icms40);
            }
            case "60" -> {
                NFNotaInfoItemImpostoICMS60 icms60 = new NFNotaInfoItemImpostoICMS60();
                icms60.setOrigem(origem);
                icms60.setSituacaoTributaria(NFNotaInfoImpostoTributacaoICMS.CST_60);
                icms.setIcms60(icms60);
            }
            case "102", "103", "300", "400" -> {
                // Simples Nacional - sem crédito
                NFNotaInfoItemImpostoICMSSN102 icmsSn102 = new NFNotaInfoItemImpostoICMSSN102();
                icmsSn102.setOrigem(origem);
                icmsSn102.setSituacaoOperacaoSN(
                        NFNotaSituacaoOperacionalSimplesNacional.valueOf("CSOSN_" + cst));
                icms.setIcmssn102(icmsSn102);
            }
            case "500" -> {
                NFNotaInfoItemImpostoICMSSN500 icmsSn500 = new NFNotaInfoItemImpostoICMSSN500();
                icmsSn500.setOrigem(origem);
                icmsSn500.setSituacaoOperacaoSN(
                        NFNotaSituacaoOperacionalSimplesNacional.CSOSN_500);
                icms.setIcmssn500(icmsSn500);
            }
            default -> {
                // Default: Simples Nacional 102
                NFNotaInfoItemImpostoICMSSN102 icmsSn = new NFNotaInfoItemImpostoICMSSN102();
                icmsSn.setOrigem(origem);
                icmsSn.setSituacaoOperacaoSN(NFNotaSituacaoOperacionalSimplesNacional.CSOSN_102);
                icms.setIcmssn102(icmsSn);
            }
        }
    }

    private void buildPis(NFNotaInfoItemImpostoPIS pis, NfeItemDTO itemDto) {
        String cst = itemDto.getPisCst();

        if (cst != null && ("01".equals(cst) || "02".equals(cst))) {
            // PIS tributado por aliquota
            NFNotaInfoItemImpostoPISAliquota pisAliq = new NFNotaInfoItemImpostoPISAliquota();
            pisAliq.setSituacaoTributaria(
                    NFNotaInfoSituacaoTributariaPIS.valueOf("CST_" + cst));
            pisAliq.setValorBaseCalculo(
                    itemDto.getPisBaseCalculo() != null ? itemDto.getPisBaseCalculo().setScale(2, RoundingMode.HALF_UP) : itemDto.getValorTotal().setScale(2, RoundingMode.HALF_UP));
            pisAliq.setPercentualAliquota(
                    itemDto.getPisAliquota() != null ? itemDto.getPisAliquota().setScale(4, RoundingMode.HALF_UP) : new BigDecimal("1.6500"));
            pisAliq.setValorTributo(
                    itemDto.getPisValor() != null ? itemDto.getPisValor().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2));
            pis.setAliquota(pisAliq);
        } else {
            // PIS não tributado (CST 04, 05, 06, 07, 08, 09)
            NFNotaInfoItemImpostoPISNaoTributado pisNt = new NFNotaInfoItemImpostoPISNaoTributado();
            pisNt.setSituacaoTributaria(
                    NFNotaInfoSituacaoTributariaPIS.valueOf("CST_" + (cst != null ? cst : "07")));
            pis.setNaoTributado(pisNt);
        }
    }

    private void buildCofins(NFNotaInfoItemImpostoCOFINS cofins, NfeItemDTO itemDto) {
        String cst = itemDto.getCofinsCst();

        if (cst != null && ("01".equals(cst) || "02".equals(cst))) {
            // COFINS tributado por aliquota
            NFNotaInfoItemImpostoCOFINSAliquota cofinsAliq = new NFNotaInfoItemImpostoCOFINSAliquota();
            cofinsAliq.setSituacaoTributaria(
                    NFNotaInfoSituacaoTributariaCOFINS.valueOf("CST_" + cst));
            cofinsAliq.setValorBaseCalculo(
                    itemDto.getCofinsBaseCalculo() != null ? itemDto.getCofinsBaseCalculo().setScale(2, RoundingMode.HALF_UP) : itemDto.getValorTotal().setScale(2, RoundingMode.HALF_UP));
            cofinsAliq.setPercentualAliquota(
                    itemDto.getCofinsAliquota() != null ? itemDto.getCofinsAliquota().setScale(4, RoundingMode.HALF_UP) : new BigDecimal("7.6000"));
            cofinsAliq.setValor(
                    itemDto.getCofinsValor() != null ? itemDto.getCofinsValor().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2));
            cofins.setAliquota(cofinsAliq);
        } else {
            // COFINS não tributado
            NFNotaInfoItemImpostoCOFINSNaoTributavel cofinsNt = new NFNotaInfoItemImpostoCOFINSNaoTributavel();
            cofinsNt.setSituacaoTributaria(
                    NFNotaInfoSituacaoTributariaCOFINS.valueOf("CST_" + (cst != null ? cst : "07")));
            cofins.setNaoTributavel(cofinsNt);
        }
    }

    private NFNotaInfoTotal buildTotal(NfeEmissionRequestDTO request) {
        NFNotaInfoTotal total = new NFNotaInfoTotal();
        NFNotaInfoICMSTotal icmsTotal = new NFNotaInfoICMSTotal();

        BigDecimal valorProdutos = BigDecimal.ZERO;
        BigDecimal valorICMS = BigDecimal.ZERO;
        BigDecimal valorPIS = BigDecimal.ZERO;
        BigDecimal valorCOFINS = BigDecimal.ZERO;
        BigDecimal valorDesconto = BigDecimal.ZERO;

        for (NfeItemDTO item : request.getItens()) {
            valorProdutos = valorProdutos.add(item.getValorTotal());
            if (item.getIcmsValor() != null) {
                valorICMS = valorICMS.add(item.getIcmsValor());
            }
            if (item.getPisValor() != null) {
                valorPIS = valorPIS.add(item.getPisValor());
            }
            if (item.getCofinsValor() != null) {
                valorCOFINS = valorCOFINS.add(item.getCofinsValor());
            }
            if (item.getValorDesconto() != null) {
                valorDesconto = valorDesconto.add(item.getValorDesconto());
            }
        }

        BigDecimal icmsBaseCalculo = BigDecimal.ZERO;
        for (NfeItemDTO item : request.getItens()) {
            if (item.getIcmsBaseCalculo() != null) {
                icmsBaseCalculo = icmsBaseCalculo.add(item.getIcmsBaseCalculo());
            }
        }

        icmsTotal.setBaseCalculoICMS(icmsBaseCalculo.setScale(2, RoundingMode.HALF_UP));
        icmsTotal.setValorTotalICMS(valorICMS.setScale(2, RoundingMode.HALF_UP));
        icmsTotal.setValorICMSDesonerado(BigDecimal.ZERO.setScale(2));
        icmsTotal.setBaseCalculoICMSST(BigDecimal.ZERO.setScale(2));
        icmsTotal.setValorTotalICMSST(BigDecimal.ZERO.setScale(2));
        icmsTotal.setValorTotalDosProdutosServicos(valorProdutos.setScale(2, RoundingMode.HALF_UP));
        icmsTotal.setValorTotalFrete(BigDecimal.ZERO.setScale(2));
        icmsTotal.setValorTotalSeguro(BigDecimal.ZERO.setScale(2));
        icmsTotal.setValorTotalDesconto(valorDesconto.setScale(2, RoundingMode.HALF_UP));
        icmsTotal.setValorTotalII(BigDecimal.ZERO.setScale(2));
        icmsTotal.setValorTotalIPI(BigDecimal.ZERO.setScale(2));
        icmsTotal.setValorTotalIPIDevolvido(BigDecimal.ZERO.setScale(2));
        icmsTotal.setValorPIS(valorPIS.setScale(2, RoundingMode.HALF_UP));
        icmsTotal.setValorCOFINS(valorCOFINS.setScale(2, RoundingMode.HALF_UP));
        icmsTotal.setOutrasDespesasAcessorias(BigDecimal.ZERO.setScale(2));

        BigDecimal valorNfe = valorProdutos.subtract(valorDesconto);
        icmsTotal.setValorTotalNFe(valorNfe.setScale(2, RoundingMode.HALF_UP));
        icmsTotal.setValorTotalTributos(valorICMS.add(valorPIS).add(valorCOFINS).setScale(2, RoundingMode.HALF_UP));
        icmsTotal.setValorTotalFundoCombatePobreza(BigDecimal.ZERO.setScale(2));

        total.setIcmsTotal(icmsTotal);
        return total;
    }

    private NFNotaInfoTransporte buildTransporte(NfeEmissionRequestDTO request) {
        NFNotaInfoTransporte transporte = new NFNotaInfoTransporte();
        transporte.setModalidadeFrete(
                NFModalidadeFrete.valueOf(request.getModalidadeFrete()));
        return transporte;
    }

    private NFNotaInfoPagamento buildPagamento(NfeEmissionRequestDTO request) {
        NFNotaInfoPagamento pagamento = new NFNotaInfoPagamento();

        NFNotaInfoFormaPagamento forma = new NFNotaInfoFormaPagamento();
        forma.setMeioPagamento(NFMeioPagamento.valueOf(request.getMeioPagamento()));
        forma.setIndicadorFormaPagamento(
                NFIndicadorFormaPagamento.valueOf(request.getIndicadorPagamento()));

        BigDecimal valorPag = request.getValorPagamento();
        if (valorPag == null) {
            valorPag = calcularValorTotal(request);
        }
        forma.setValorPagamento(valorPag.setScale(2, RoundingMode.HALF_UP));

        pagamento.setDetalhamentoFormasPagamento(Collections.singletonList(forma));
        return pagamento;
    }

    private BigDecimal calcularValorTotal(NfeEmissionRequestDTO request) {
        BigDecimal total = BigDecimal.ZERO;
        if (request.getItens() != null) {
            for (NfeItemDTO item : request.getItens()) {
                total = total.add(item.getValorTotal());
                if (item.getValorDesconto() != null) {
                    total = total.subtract(item.getValorDesconto());
                }
            }
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
