CREATE TABLE nfe_emissions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id),
    numero_nota TEXT NOT NULL,
    serie TEXT NOT NULL,
    chave_acesso TEXT,
    protocolo TEXT,
    numero_recibo TEXT,
    status TEXT NOT NULL DEFAULT 'PENDENTE',
    status_sefaz TEXT,
    motivo_sefaz TEXT,
    natureza_operacao TEXT NOT NULL,
    emitente_cnpj TEXT NOT NULL,
    emitente_razao_social TEXT NOT NULL,
    emitente_ie TEXT,
    destinatario_cpf_cnpj TEXT,
    destinatario_nome TEXT,
    valor_total REAL NOT NULL,
    valor_icms REAL,
    valor_pis REAL,
    valor_cofins REAL,
    xml_envio TEXT,
    xml_retorno TEXT,
    xml_nota_processada TEXT,
    motivo_cancelamento TEXT,
    cancelada_em TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT (datetime('now')),
    updated_at TIMESTAMP DEFAULT (datetime('now'))
);

CREATE INDEX idx_nfe_emissions_user_id ON nfe_emissions(user_id);
CREATE INDEX idx_nfe_emissions_status ON nfe_emissions(status);
CREATE INDEX idx_nfe_emissions_chave_acesso ON nfe_emissions(chave_acesso);
