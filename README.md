# MercadoCRUDJava

**English** | [Português (Brasil)](README.pt-BR.md)

Android app written in Java for managing a small market: it registers customers, products and purchases (with the products of each purchase) and lists them, storing everything in a local SQLite database on the device.

## Features

- **Customers**: create, look up by ID, edit and delete. Fields: name, e-mail, phone, payment day (1-31) and address (street, number, complement, district, city, state, ZIP code). Input is validated (name length, e-mail format, Brazilian phone and ZIP code formats, two-letter state).
- **Products**: create, look up by ID, edit and delete. Fields: description, unit and price.
- **Purchases**: create a purchase for a customer (looked up by ID) with a purchase date (`dd/MM/yyyy`), edit it and delete it. The payment due date is derived from the customer's payment day in the month after the purchase.
- **Purchase products**: from a purchase, add, update the quantity of, or remove products; the purchase total is recalculated.
- **Lists with search**: customers (search by name), products (search by description or unit) and purchases (search by customer name, product description or unit). The customers list flags pending and overdue purchases.
- A customer who has purchases cannot be deleted from the registration screen (the delete button is disabled).

## Screens

| Screen (class) | Purpose |
| --- | --- |
| `MainActivity` | Home menu; creates the database tables on start |
| `CadastroClientes` / `ListarClientes` | Customer form / customer list |
| `CadastroProdutos` / `ListarProdutos` | Product form / product list |
| `CadastroCompras` / `ListarCompras` | Purchase form / purchase list |
| `CRUDProduto` | Add, edit or remove a product within a purchase |

The user interface text is in Portuguese (Brazil), and prices and dates are formatted for the `pt-BR` locale.

## Data storage

The app uses the Android SQLite API directly (database `Mercado`, private to the app) with four tables: `cliente`, `produto`, `compra` and `compra_produto`. The code makes no network calls and the manifest declares no permissions, so it does not talk to any back-end. The Go API [Mercadinho-API](https://github.com/Alphka/Mercadinho-API) is a separate training project inspired by this app.

## Tech stack

| Item | Value (from the Gradle files) |
| --- | --- |
| Language | Java (source/target compatibility 1.8) |
| Application ID | `com.example.mercado` |
| `minSdk` / `targetSdk` / `compileSdk` | 29 / 34 / 34 |
| Android Gradle Plugin | 8.2.2 |
| Gradle | 8.2 |
| Dependencies | `androidx.appcompat:appcompat:1.7.0`, `com.google.android.material:material:1.12.0`, `androidx.constraintlayout:constraintlayout:2.1.4` |
| Theme | Material 3 (`Theme.Material3.Light`) |

## Requirements

- Android Studio with the Android SDK Platform 34 installed.
- A JDK compatible with Gradle 8.2 and AGP 8.2.2 (Android Studio's bundled JDK works).
- An Android 10 (API 29) or newer device or emulator.

## Installation and running

```bash
git clone https://github.com/Alphka/MercadoCRUDJava.git
cd MercadoCRUDJava
```

1. Open the folder in Android Studio and let Gradle sync.
2. Select a device or emulator and run the `app` configuration.

Command line (with the Android SDK configured through `local.properties` or `ANDROID_HOME`):

```bash
./gradlew assembleDebug   # on Windows: gradlew.bat assembleDebug
```

## Project structure

```text
app/src/main/
  AndroidManifest.xml
  java/com/example/mercado/   Activities, Helpers (formatting/validation), ColumnInfo
  res/layout/                 One layout per activity
  res/values/                 Strings, colors, Material 3 theme
```

## License

[ISC](LICENSE.md) © 2024 Kayo Souza
