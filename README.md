# MS - account
## Essa API foi elaborado pensando num projeto em ambiente de trabalho 
- Terá padrão de projeto seguindo as camadas
    - Controller
    - Service
    - Repository
    - Entities
    - DTO
    - Enums
    - Exception
    - Mappers
    - Configure
- Terá autenticação simples, mas num projeto teria que avaliar se a API seria publica(sem autorização) ou privada(Com autorição Oaut2, ou outras ...)
- Terá tratamentos de exceção, descrito abaixo
- Terá evidencias de testes e evidencias funcionais dentro do diretório resource
- Testes unitarários, com cobertura acima de 80%, foi evidenciado com print está dentro do diretório resource/evidencias/coverage.png
- Base de dados, foi evidenciado com print está dentro do diretório resource/evidencias/tabelaCriada.png e consultaNaBase.png
- Docker compose dentro do diretório resource

## Subir aplicação local
- Foi adicionado o arquivo settings.xml dentro do diretório resource/setting/settings.xml
- Ter instalado a JDK 21, pra poder conseguir testar local
- Ter o maven mais recente
- Pra baixar as dependencias rodar o comando - mvn clean install
- Subir o docker-compose
- Tendo tudo isso só executar o AccountApplication e testar local
- Pra facitar foi adicionado a collection do postman, com todos os endpoints dentro do diretório resource

## Foi feito todas os items que foi pedido como requisito e outros descrito abaixo
- Cadastrar conta;
- Atualizar conta;
- Alterar a situação da conta;
- Obter a lista de contas a pagar, com filtro de data de vencimento e descrição;
- Obter conta filtrando o id;
- Obter valor total pago por período.
- Implementar mecanismo para importação de contas a pagar via arquivo csv
  - Evidência homologada
  - Dentro do diretório resource/arquivoLote.csv pra poder incluir no endpoint em lote
- Foi feito a autenticaçao por basic auth, tendo que passar usuario(beneficiary) e a senha(123);
- Foi feito testes unitários, tendo a cobertura superior aos 80%
- Foi incluido logs pra poder ter rastreabilidade, se caso der alguma falha, obtendo pelo dynatrace, grafana, ...
- Foi feito com banco de dados PostgreSQL e Flyway;
- Foi feito tratamento de exceção, incluindo alguns erros funcional, descrito abaixo;
- Foi feito evidencias funcionais, descrito abaixo;
- Foi incluido a colection do postman, pra facilitar dentro do diretório resources/collectionPostman;
- Foi utilizado variavel de ambiente na parte SecurityConfig, se caso for deixar num ambiente na nuvem, cofre de senhas quando subir em outros ambientes.
- Foi utilizado também Java 21, com spring 3;
- Foi utilizado pra montar a conversão de DTO x Entidades com mapstruct;
- Utilizei os campos de entrada em inglês e as entidades mantendo o padrão da modelagem em portugues;
- Tem o script da migration
