# Apocalipse BR - Project Zomboid Server Manager

## Descrição

O **Apocalipse BR Manager** é uma aplicação Spring Boot desenvolvida para gerenciar e controlar aspectos da execução de um servidor **Project Zomboid** através de requisições HTTP. Esta aplicação atua como um intermediário entre interfaces web e o servidor de jogo, permitindo automatizar e integrar interações dos jogadores com o servidor de forma direta e indireta.

## Propósito

Este projeto visa integrar o servidor Project Zomboid com uma plataforma de gerenciamento centralizada, habilitando:

- **Interações Diretas de Jogadores**: Eventos disparados por doações, requisições especiais e ações comunitárias
- **Painel Administrativo**: Interface para moderadores e administradores gerenciarem o servidor remotamente
- **Integração Comunitária**: Conexão entre Discord, plataformas de doação e outras ferramentas de comunicação com o servidor
- **Automação de Eventos**: Acionamento automático de eventos no servidor baseado em ações externas
- **Extensibilidade**: Arquitetura preparada para futuras funcionalidades e integrações

## Funcionalidades

- ✅ Envio de comandos para o servidor Project Zomboid via REST API
- ✅ Execução de comandos bash no servidor Linux
- ✅ Configuração flexível de caminhos de controle
- ✅ Tratamento de erros

## Requisitos

- Java 25+
- Spring Boot 4.0.1+
- Sistema operacional Linux Ubuntu (para o servidor Zomboid)
- Maven 3.6+

## Instalação e Configuração

### 1. Clonar o repositório

```bash
git clone https://github.com/apocalipsebr/manager.git
cd manager
```

### 2. Configurar a aplicação

Edite o arquivo `src/main/resources/application.properties`:

```properties
# Caminho do arquivo de controle do servidor Zomboid
zomboid.control.file=/opt/pzserver/zomboid.control

# Porta da aplicação (padrão: 8080)
server.port=8080
```

### 3. Compilar

```bash
mvn clean package
```

### 4. Executar

```bash
java -jar target/manager-0.0.1-SNAPSHOT.jar
```

## Uso da API

### Enviar Comando (JSON)

**POST** `/api/server/command`

```bash
curl -X POST http://localhost:8080/api/server/command \
  -H "Content-Type: application/json" \
  -d '{"command":"say Olá servidor!"}'
```

### Enviar Comando (Query Parameter)

**POST** `/api/server/command/text?command=say%20Olá%20servidor!`

```bash
curl -X POST "http://localhost:8080/api/server/command/text?command=say%20Olá%20servidor!"
```

## Arquitetura

O projeto segue o padrão **Hexagonal Architecture** (Ports & Adapters):

```
src/main/java/com/apocalipsebr/zomboid/server/manager/
├── domain/              # Camada de domínio (lógica de negócio)
│   ├── entity/         # Entidades do domínio
│   ├── port/           # Portas (interfaces)
│   └── exception/      # Exceções de domínio
├── application/        # Camada de aplicação (casos de uso)
│   └── service/        # Serviços de aplicação
├── infrastructure/     # Camada de infraestrutura (adaptadores)
│   └── adapter/        # Implementações concretas
└── presentation/       # Camada de apresentação (REST API)
    └── controller/     # Controladores REST
```

Esta arquitetura garante:
- Independência de frameworks
- Testabilidade
- Flexibilidade para mudanças futuras
- Separação clara de responsabilidades

## Exemplos de Uso

### Exemplo 1: Anúncio no Servidor

```bash
curl -X POST "http://localhost:8080/api/server/command/text?command=say%20Doação%20recebida%20de%20João!"
```

### Exemplo 2: Evento de Doação

```bash
curl -X POST http://localhost:8080/api/server/command \
  -H "Content-Type: application/json" \
  -d '{"command":"startworldsoundevent zvoid_start"}'
```

### Exemplo 3: Integração com Webhook

Seu sistema de doações pode fazer um POST para a API automaticamente:

```javascript
fetch('http://seu-servidor:8080/api/server/command', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({command: `say Jogador ${doador} fez uma doação!`})
});
```

## Estrutura de Diretórios

```
apocalipsebr-manager/manager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/apocalipsebr/zomboid/server/manager/
│   │   │       ├── domain/
│   │   │       ├── application/
│   │   │       ├── infrastructure/
│   │   │       ├── presentation/
│   │   │       └── ManagerApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

## Segurança

⚠️ **IMPORTANTE**: 
- Esta aplicação não implementa autenticação ou autorização por padrão
- Implemente mecanismos de segurança apropriados antes de usar em produção
- Restrinja o acesso à rede ou implemente autenticação (JWT, OAuth2, etc.)
- Valide e sanitize todos os comandos recebidos

## Roadmap

- [ ] Autenticação e Autorização (JWT)
- [ ] Log persistente de comandos
- [ ] Histórico de execuções
- [ ] Fila de comandos com prioridade
- [ ] Integração com Discord
- [ ] Dashboard Web
- [ ] Suporte a múltiplos servidores Zomboid

## Licença e Aviso de Não Responsabilidade

**ESTE SOFTWARE É FORNECIDO "NO ESTADO EM QUE SE ENCONTRA", SEM QUALQUER GARANTIA DE QUALQUER TIPO, EXPRESSA OU IMPLÍCITA, INCLUINDO, MAS NÃO LIMITADO A, GARANTIAS DE COMERCIALIZAÇÃO, ADEQUAÇÃO A UM PROPÓSITO ESPECÍFICO E NÃO VIOLAÇÃO.**

Os desenvolvedores não são responsáveis por:
- Perda de dados
- Interrupção de serviço
- Danos causados pelo uso desta aplicação
- Problemas de segurança resultantes de má configuração

Use por sua conta e risco. Realize testes completos antes de usar em produção.

## Contribuições

Contribuições são bem-vindas! Por favor:

1. Faça um Fork do projeto
2. Crie uma branch para sua feature (`git checkout -b feature/MinhaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona MinhaFeature'`)
4. Push para a branch (`git push origin feature/MinhaFeature`)
5. Abra um Pull Request

## Suporte

Para reportar bugs ou solicitar features, abra uma issue no repositório do GitHub.

## Comunidade

- **Discord**: [Link do Discord da Comunidade Apocalipse BR]
- **Website**: [Link do website oficial]
- **GitHub**: https://github.com/apocalipsebr/manager

---

**Desenvolvido com ❤️ pela comunidade Apocalipse BR**

*Last Updated: Janeiro 2026*
