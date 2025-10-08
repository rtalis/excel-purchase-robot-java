# Excel Purchase Robot Java

A Java application to automate filling purchase order data into Excel spreadsheets, using an automation robot and REST API integration.



## Requirements

- Access to the purchase order API. The backend server for this API can be found at: [rtalis/flask-foccoerp-rest-api](https://github.com/rtalis/flask-foccoerp-rest-api)
- Operating system permissions for automation (keyboard and clipboard control) with GUI.

## Installation

1.  **Clone this repository:**
    ```bash
    git clone https://github.com/rtalis/excel-purchase-robot-java
    cd excel-purchase-robot-java
    ```

2.  **Backend API Setup:**
    Ensure the backend API server ([rtalis/flask-foccoerp-rest-api](https://github.com/rtalis/flask-foccoerp-rest-api)) is running and accessible.

3.  **Create `.env` file:**
    Create a `.env` file in the project root with the following variables, pointing to your API and credentials:
    ```env
    EMAIL=your-api-email@example.com
    PASSWORD=your-api-password
    BASE_URL=http://localhost:5000 # Or your actual API base URL
    ```

4.  **Build the project (optional, if you want to run from JAR):**
    ```bash
    ./mvnw clean install
    ```

## How to Use

1.  **Configure Columns (First Time or When Changes Needed):**
    - Run the [`ColumnManagerWindow`](src/main/java/com/rt/robotexcel/demo/gui/ColumnManagerWindow.java) class.
      (You can run this directly from your IDE or after building, adapt the command if running from a JAR).
    - In the "Column Manager" window:
        - Add columns from the "Available Columns" list to the "Selected Columns" table.
        - Arrange the selected columns in the exact order they appear in your Excel spreadsheet using the "↑" and "↓" buttons.
        - The "PEDIDO" (Order) column is mandatory.
    - Click "Save" to store the configuration in `column_config.json`.

2.  **Run the Main Robot:**
    - Open your Excel spreadsheet.
    - Position the cursor in the cell containing the first purchase order number (this cell should correspond to your "PEDIDO" column).
    - Run the [`ExcelPurchaseRobot`](src/main/java/com/rt/robotexcel/demo/ExcelPurchaseRobot.java) class (from your IDE or the built JAR).
    - The application will prompt: "Posicione o cursor na coluna do pedido e aguarde 5 segundos..." (Position the cursor on the order column and wait 5 seconds...).
    - After 5 seconds, the robot will:
        - Read the purchase order number from the current cell.
        - Fetch data from the API for that order.
        - Fill the corresponding row in Excel based on your saved column configuration.
        - Automatically move to the cell below to process the next order.

3.  **Processing Multiple Orders:**
    - The robot will continue to read the order number from the current cell, fetch data, fill the row, and move down.
    - Ensure each subsequent order number is in the "PEDIDO" column on the next row.
    - The process stops if an invalid order number is read or an error occurs.

## Available Columns for Configuration

You can configure the mapping for columns like:

-   **SOLIC.** (Request No.): Request number (extracted from observation)
-   **PEDIDO** (Order No.): Purchase order number (mandatory)
-   **FORNECEDOR** (Supplier): Supplier name
-   **DT. PED.** (Order Date): Order date
-   **VALOR** (Value): Total order value
-   **NF** (Invoice No.): Invoice number
-   **CHEGADA** (Arrival Date): Arrival date of goods
-   **CONTATO** (Contact): Contact person
-   **GENERO** (Category/Type): Category/type (extracted from the last line of observation)
-   **PAGAMENTO** (Payment Terms): Payment terms
-   **FUNCIONÁRIO** (Employee): Employee name who created the order
-   **POSIÇÃO** (Status): Order status
-   **VALOR LÍQUIDO** (Net Value): Net order value
-   **VALOR LÍQUIDO IPI** (Net Value + IPI): Net order value including IPI tax
-   **EMPRESA** (Company Code): Company code
-   **ID FORNECEDOR** (Supplier ID): Supplier ID
-   **ID PEDIDO** (Order ID): Internal order ID from the system
-   **OBSERVAÇÃO** (Observation): Full observation text
-   **[COLUNA EM BRANCO]** (Blank Column): Use this to skip a column in Excel.

The JSON field mapping for these is handled in the `ColumnManagerWindow` and saved to `column_config.json`.

## Project Structure

-   [`src/main/java/com/rt/robotexcel/demo/`](src/main/java/com/rt/robotexcel/demo/)
    -   [`api/`](src/main/java/com/rt/robotexcel/demo/api/) - Classes for API communication ([`ApiClient`](src/main/java/com/rt/robotexcel/demo/api/ApiClient.java)).
    -   [`config/`](src/main/java/com/rt/robotexcel/demo/config/) - Configuration classes ([`ExcelColumnConfig`](src/main/java/com/rt/robotexcel/demo/config/ExcelColumnConfig.java), [`ConfigurationManager`](src/main/java/com/rt/robotexcel/demo/config/ConfigurationManager.java)).
    -   [`excel/`](src/main/java/com/rt/robotexcel/demo/excel/) - Logic for updating Excel ([`ExcelUpdater`](src/main/java/com/rt/robotexcel/demo/excel/ExcelUpdater.java)).
    -   [`gui/`](src/main/java/com/rt/robotexcel/demo/gui/) - Graphical user interface for column management ([`ColumnManagerWindow`](src/main/java/com/rt/robotexcel/demo/gui/ColumnManagerWindow.java)).
    -   [`robot/`](src/main/java/com/rt/robotexcel/demo/robot/) - Java Robot utilities ([`RobotUtil`](src/main/java/com/rt/robotexcel/demo/robot/RobotUtil.java)).
    -   [`util/`](src/main/java/com/rt/robotexcel/demo/util/) - Utility classes ([`ClipboardManager`](src/main/java/com/rt/robotexcel/demo/util/ClipboardManager.java)).
    -   [`DemoApplication.java`](src/main/java/com/rt/robotexcel/demo/DemoApplication.java) - Main application entry point.
-   [`pom.xml`](pom.xml) - Maven project configuration.
-   [`.env`](.env) - Environment variables (credentials, API URL - **DO NOT COMMIT**).
-   [`column_config.json`](column_config.json) - Saved column configuration (generated by `ColumnManagerWindow`).



## License

This project is licensed under the MIT License.

---
