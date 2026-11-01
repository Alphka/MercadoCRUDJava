# MercadoCRUDJava

[English](README.md) | **Português (Brasil)**

Aplicativo Android em Java para gerenciar um pequeno mercado: cadastra clientes, produtos e compras (com os produtos de cada compra) e os lista, armazenando tudo em um banco SQLite local no dispositivo.

## Funcionalidades

- **Clientes**: cadastro, busca por ID, edição e exclusão. Campos: nome, e-mail, telefone, dia de pagamento (1 a 31) e endereço (logradouro, número, complemento, bairro, cidade, estado, CEP). Os dados são validados (tamanho do nome, formato do e-mail, formatos de telefone e CEP brasileiros, estado com duas letras).
- **Produtos**: cadastro, busca por ID, edição e exclusão. Campos: descrição, unidade e preço.
- **Compras**: cadastro de uma compra para um cliente (buscado por ID) com data da compra (`dd/MM/yyyy`), edição e exclusão. A data limite de pagamento é derivada do dia de pagamento do cliente, no mês seguinte à compra.
- **Produtos da compra**: a partir de uma compra, é possível adicionar produtos, alterar a quantidade ou remover; o valor total da compra é recalculado.
- **Listas com busca**: clientes (busca por nome), produtos (busca por descrição ou unidade) e compras (busca por nome do cliente, descrição ou unidade do produto). A lista de clientes sinaliza compras pendentes e em atraso.
- Um cliente que possui compras não pode ser excluído pela tela de cadastro (o botão de exclusão fica desabilitado).

## Telas

| Tela (classe) | Finalidade |
| --- | --- |
| `MainActivity` | Menu inicial; cria as tabelas do banco ao iniciar |
| `CadastroClientes` / `ListarClientes` | Formulário / lista de clientes |
| `CadastroProdutos` / `ListarProdutos` | Formulário / lista de produtos |
| `CadastroCompras` / `ListarCompras` | Formulário / lista de compras |
| `CRUDProduto` | Adicionar, editar ou remover um produto dentro de uma compra |

Os textos da interface estão em português (Brasil), e preços e datas são formatados para o locale `pt-BR`.

## Armazenamento de dados

O app usa diretamente a API SQLite do Android (banco `Mercado`, privado do app) com quatro tabelas: `cliente`, `produto`, `compra` e `compra_produto`. O código não faz chamadas de rede e o manifesto não declara permissões, portanto o app não se comunica com nenhum back-end. A API em Go [Mercadinho-API](https://github.com/Alphka/Mercadinho-API) é um projeto de estudo separado, inspirado neste app.

## Tecnologias

| Item | Valor (dos arquivos Gradle) |
| --- | --- |
| Linguagem | Java (compatibilidade de origem/destino 1.8) |
| Application ID | `com.example.mercado` |
| `minSdk` / `targetSdk` / `compileSdk` | 29 / 34 / 34 |
| Android Gradle Plugin | 8.2.2 |
| Gradle | 8.2 |
| Dependências | `androidx.appcompat:appcompat:1.7.0`, `com.google.android.material:material:1.12.0`, `androidx.constraintlayout:constraintlayout:2.1.4` |
| Tema | Material 3 (`Theme.Material3.Light`) |

## Requisitos

- Android Studio com o Android SDK Platform 34 instalado.
- Um JDK compatível com Gradle 8.2 e AGP 8.2.2 (o JDK embutido do Android Studio serve).
- Um dispositivo ou emulador com Android 10 (API 29) ou superior.

## Instalação e execução

```bash
git clone https://github.com/Alphka/MercadoCRUDJava.git
cd MercadoCRUDJava
```

1. Abra a pasta no Android Studio e aguarde a sincronização do Gradle.
2. Selecione um dispositivo ou emulador e execute a configuração `app`.

Pela linha de comando (com o Android SDK configurado via `local.properties` ou `ANDROID_HOME`):

```bash
./gradlew assembleDebug   # no Windows: gradlew.bat assembleDebug
```

## Estrutura do projeto

```text
app/src/main/
  AndroidManifest.xml
  java/com/example/mercado/   Activities, Helpers (formatação/validação), ColumnInfo
  res/layout/                 Um layout por activity
  res/values/                 Strings, cores, tema Material 3
```

## Licença

[ISC](LICENSE.md) © 2024 Kayo Souza
