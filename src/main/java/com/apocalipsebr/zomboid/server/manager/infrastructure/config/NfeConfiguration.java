package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

import com.fincatto.documentofiscal.DFAmbiente;
import com.fincatto.documentofiscal.DFUnidadeFederativa;
import com.fincatto.documentofiscal.nfe.NFeConfig;
import com.fincatto.documentofiscal.nfe.NFTipoEmissao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Configuration
public class NfeConfiguration extends NFeConfig {

    private static final Logger logger = LoggerFactory.getLogger(NfeConfiguration.class);

    @Value("${nfe.ambiente:HOMOLOGACAO}")
    private String ambiente;

    @Value("${nfe.uf:SP}")
    private String uf;

    @Value("${nfe.certificado.caminho:}")
    private String certificadoCaminho;

    @Value("${nfe.certificado.senha:}")
    private String certificadoSenha;

    @Value("${nfe.cadeia.caminho:}")
    private String cadeiaCaminho;

    @Value("${nfe.cadeia.senha:}")
    private String cadeiaSenha;

    @Value("${nfe.tipo.emissao:EMISSAO_NORMAL}")
    private String tipoEmissao;

    private KeyStore keyStoreCertificado;
    private KeyStore keyStoreCadeia;

    @Override
    public DFAmbiente getAmbiente() {
        return DFAmbiente.valueOf(ambiente);
    }

    @Override
    public DFUnidadeFederativa getCUF() {
        return DFUnidadeFederativa.valueOf(uf);
    }

    @Override
    public String getCertificadoSenha() {
        return certificadoSenha;
    }

    @Override
    public String getCadeiaCertificadosSenha() {
        return cadeiaSenha;
    }

    @Override
    public NFTipoEmissao getTipoEmissao() {
        return NFTipoEmissao.valueOf(tipoEmissao);
    }

    @Override
    public KeyStore getCertificadoKeyStore() throws KeyStoreException {
        if (this.keyStoreCertificado == null) {
            if (certificadoCaminho == null || certificadoCaminho.isBlank()) {
                throw new KeyStoreException("Caminho do certificado digital nao configurado (nfe.certificado.caminho)");
            }
            this.keyStoreCertificado = KeyStore.getInstance("PKCS12");
            try (InputStream stream = Files.newInputStream(Path.of(certificadoCaminho))) {
                this.keyStoreCertificado.load(stream, getCertificadoSenha().toCharArray());
            } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
                this.keyStoreCertificado = null;
                throw new KeyStoreException("Nao foi possivel carregar o certificado digital: " + e.getMessage(), e);
            }
        }
        return this.keyStoreCertificado;
    }

    @Override
    public KeyStore getCadeiaCertificadosKeyStore() throws KeyStoreException {
        if (this.keyStoreCadeia == null) {
            if (cadeiaCaminho == null || cadeiaCaminho.isBlank()) {
                throw new KeyStoreException("Caminho da cadeia de certificados nao configurado (nfe.cadeia.caminho)");
            }
            this.keyStoreCadeia = KeyStore.getInstance("JKS");
            try (InputStream stream = Files.newInputStream(Path.of(cadeiaCaminho))) {
                this.keyStoreCadeia.load(stream, getCadeiaCertificadosSenha().toCharArray());
            } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
                this.keyStoreCadeia = null;
                throw new KeyStoreException("Nao foi possivel carregar a cadeia de certificados: " + e.getMessage(), e);
            }
        }
        return this.keyStoreCadeia;
    }

    public boolean isConfigured() {
        return certificadoCaminho != null && !certificadoCaminho.isBlank()
                && certificadoSenha != null && !certificadoSenha.isBlank()
                && cadeiaCaminho != null && !cadeiaCaminho.isBlank()
                && cadeiaSenha != null && !cadeiaSenha.isBlank();
    }
}
