<div align="center">
  <h1>Tremz.in — Backend API (Java/Spring Boot)</h1>
  <p><strong>A Highly Scalable & Secure URL Shortener and Image Hosting API</strong></p>

  <p>
    <img alt="Java" src="https://img.shields.io/badge/Java-21-ED8B00?logo=java&logoColor=white&style=for-the-badge" />
    <img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?logo=spring&logoColor=white&style=for-the-badge" />
    <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-15-4169E1?logo=postgresql&logoColor=white&style=for-the-badge" />
    <img alt="Redis" src="https://img.shields.io/badge/Redis-Cache-DC382D?logo=redis&logoColor=white&style=for-the-badge" />
    <img alt="RabbitMQ" src="https://img.shields.io/badge/RabbitMQ-Message_Broker-FF6600?logo=rabbitmq&logoColor=white&style=for-the-badge" />
  </p>
</div>

---

## 📌 Sobre o Projeto

O **Tremz.in Backend** é uma API RESTful moderna e resiliente. O principal desafio deste projeto não foi apenas criar um encurtador de URLs, mas sim desenhar uma **arquitetura elástica** capaz de lidar com picos de tráfego intensos sem degradar a performance do banco de dados, assegurando alto nível de isolamento e segurança.

O sistema é construído sobre os pilares do ecossistema Java moderno e faz uso extenso de mensageria, cache em memória e defesas cibernéticas contra ataques automatizados.

## ⚙️ Funcionalidades em Destaque

*   **Encurtamento Otimizado (Base62)**: Os links curtos são gerados através de um algoritmo Seguro e Base62, permitindo mais de **56 Bilhões** de combinações em apenas 6 caracteres, com detecção automática de colisões e tentativas de recálculo (<i>do-while lock</i>).
*   **Analytics Assíncrono**: O processamento de métricas (cliques) nunca bloqueia a requisição HTTP. Sempre que um link é acessado, o evento é enviado para uma fila no **RabbitMQ**, onde um *consumer* em background grava os dados no PostgreSQL de forma idótona.
*   **Segurança Avançada de Uploads**: Depender da extensão (`.jpg`, `.png`) enviada pelo navegador não é seguro. Este backend implementa o **Apache Tika** para realizar inspeção profunda dos <i>Magic Numbers</i> dos arquivos, barrando qualquer <i>malware</i> ou executável malicioso disfarçado de imagem.
*   **Storage Blindado contra Colisão**: Nomes físicos de imagens em disco utilizam o padrão *UUIDv4* (sempre únicos), prevenindo ataques de substituição de arquivos e mantendo o *shortcode* intacto para o lado do cliente.
*   **Limitação de Requisições (Rate Limiting)**: Integração robusta com o **Bucket4j**, que bloqueia IPs ou usuários maliciosos que tentam derrubar a API executando centenas de chamadas por segundo num ataque de negação de serviço (DoS/DDoS).
*   **Autenticação JWT**: Segurança *Stateless* com Spring Security.

## 🛠️ Tecnologias Utilizadas

A <i>Stack</i> foi escolhida para simular um ambiente de microsserviços maduro:

*   **Java 21 & Spring Boot 3**: Linguagem e framework principais do servidor.
*   **PostgreSQL**: Banco de dados relacional robusto.
*   **Redis**: Sistema de cache em memória para entrega instantânea das rotas de redirecionamento.
*   **RabbitMQ**: Mensageria avançada para desacoplamento de lógicas de tracking/analytics.
*   **Bucket4j**: Rate-Limiter para proteção e mitigação de bots.
*   **Apache Tika**: Biblioteca para extração e detecção de formatos de arquivos.
*   **Docker**: Conteinerização de infraestrutura (Banco, Cache e Mensageria).

## 🚀 Como Executar Localmente

### Pré-requisitos
*   Java 21
*   Maven
*   Docker & Docker Compose (para os serviços de infraestrutura)

### Passos

1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/shortlink-backend.git
   ```

2. Suba a infraestrutura via Docker:
   *O projeto assume que o Postgres (porta 5432), Redis (6379) e RabbitMQ (5672) estejam em execução.*

3. Configure as Variáveis de Ambiente:
   Crie um arquivo `.env` na raiz do projeto, utilizando o `.env.example` como base. É lá que o Spring vai buscar as credenciais via módulo `spring-dotenv`.

4. Execute o servidor:
   ```bash
   ./mvnw spring-boot:run
   ```

5. Acesse o Swagger UI para testar as rotas da API: `http://localhost:8080/swagger-ui.html`

---

## 👨‍💻 Diferenciais e Tomada de Decisão

Este repositório não é apenas um "CRUD". Ao desenvolver o Tremz.in, foquei em antecipar e neutralizar riscos que ocorrem em aplicações empresariais reais:

1.  **DDoS & Botnets**: Encurtadores são alvos frequentes de robôs de <i>spam</i>. O **Bucket4j** previne que nossa API seja sufocada.
2.  **Segurança de Arquivos**: Aceitar <i>Multipart Files</i> sem inspecionar os bytes (via **Apache Tika**) seria uma vulnerabilidade catastrófica de <i>RCE (Remote Code Execution)</i>. 
3.  **Gargalo de Banco de Dados**: A cada "redirect", o banco precisaria somar `+1` na estatística de acesso, causando tráfego intenso em discos. Ao inserir o **RabbitMQ** na jogada, entregamos o Redirect para o usuário em 5ms usando **Redis**, enquanto a contabilização da métrica fica para ser feita de forma passiva no backend.
4.  **Escalabilidade e Integridade**: Os consumidores do RabbitMQ estão preparados para operar sob instâncias concorrentes da API sem pular eventos, garantindo que o relatório de acesso do cliente esteja sempre 100% correto.

---

<p align="center">Desenvolvido com 🩵 por Lucas Brito.</p>
