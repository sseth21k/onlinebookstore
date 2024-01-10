# Deploy this web app to Azure App Service

You can deploy this web application to Azure App Service with the instructions below. If you don't have an Azure account, you can [open a free account](https://azure.microsoft.com/en-us/free/). Once everything is configured, the changes you commit to your repo will be automatically deployed to Azure App Service.

## Requisites

* Create an [Azure account](https://azure.microsoft.com/)
* [Install the Azure CLI](https://learn.microsoft.com/cli/azure/install-azure-cli)
* Login in [Azure Portal](https://portal.azure.com)
* [Enable Cloud Shell](https://learn.microsoft.com/azure/cloud-shell/get-started) to your Azure subscription

## Deployment steps

Run the commands below in a new Powershell terminal. Make sure that the Azure CLI is installed by running `az --version`

### MySQL database creation

This simple web app uses MySQL to store the book and users data. The steps in this section will help you create a MySQL server in Azure.

```powershell

# Configuration

$SUFFIX="$(Get-Random -Minimum 100000 -Maximum 999999)"
$RESOURCE_GROUP="onlinebookstore${SUFFIX}-rg"
$LOCATION="eastus2"
$MYSQL_ADMIN_USER="azureuser"
$MYSQL_ADMIN_PASSWORD="adminPass"
$WEBAPP_NAME="onlinebookstore${SUFFIX}"
$MYSQL_HOST="onlinebookstore${SUFFIX}mysql"

# Create resource group for all resources

az group create --name $RESOURCE_GROUP --location $LOCATION

# Create a MySQL flexible server

az mysql flexible-server create --resource-group $RESOURCE_GROUP --name $MYSQL_HOST --location $LOCATION --admin-user $MYSQL_ADMIN_USER --admin-password $MYSQL_ADMIN_PASSWORD --public-access 0.0.0.0 --sku-name Standard_B1ms
```

Once the previous step completes, we'll create the database by logging into Azure Portal and connect to the new MySQL server.

Run the following command in the same terminal as earlier to find the command to use to connect to the database:

```powershell
echo "Connect with: mysql -h ${MYSQL_HOST}.mysql.database.azure.com -u azureuser -p"
```

Now, while logged into Azure Portal, start a Cloud Shell and run the `mysql` command from the previous step. When prompted, enter the password defined in the `Configuration` section.

In a text editor open the file `setup/ExampleData.sql`. Copy the contents of the file and paste it in the Cloud Shell terminal where you are connected to MySQL. The SQL commands should complete successfully. Enter `exit` to exit the MySQL prompt. You can close the Cloud Shell terminal when done.

### Web app creation

Run the following commands to create a new web application running Tomcat 9 with Java 11:

```powershell
az appservice plan create --resource-group $RESOURCE_GROUP --name "${WEBAPP_NAME}-asp" --sku P1V3 --is-linux --location $LOCATION

az webapp create --resource-group $RESOURCE_GROUP --name $WEBAPP_NAME --plan "${WEBAPP_NAME}-asp" --runtime "TOMCAT:9.0-java11"
```

Once the application is created, you can open the website shown by the output of the following command:

```powershell
echo "Open your website at: https://$(az webapp config hostname list --resource-group $RESOURCE_GROUP --webapp-name $WEBAPP_NAME --query "[0].name" -o tsv)"
```

### Configure the database connection properties

In your Azure Portal account, look for your new web app and under Settings look for Configuration and the Environment variables section. Create a new App Setting with the name `JAVA_OPTS`and the following value, all in one line:

```
-Ddb.host=jdbc:mysql://<MYSQL_HOST>.mysql.database.azure.com -Ddb.port=3306 -Ddb.name=onlinebookstore -Ddb.username=<MYSQL_ADMIN_USER> -Ddb.password=<MYSQL_ADMIN_PASSWORD>
```

Make sure to replace the placeholders `<MYSQL_HOST>`, `<MYSQL_ADMIN_USER>` and `<MYSQL_ADMIN_PASSWORD>` with the actual values from the configuration section at the beginning of this document.

Save the changes in Azure Portal.

The properties defined in `JAVA_OPTS` will be passed to the web application during startup and the web application will be able to open connections to the MySQL database.

### Deploy this code to your new webapp using Github Actions

* In Azure Portal, open your new webapp, and under the Deployment options, select Deployment Center. In the Deployment Center blade, click on Manage Publish Profile and click on **Download publish profile**. Open the contents of the file in a text editor.

* Fork this repo in Github
  * This repository is already configured with a deployment workflow using Github actions. In your Github fork, go to the Settings tab, then Environments. If you don't have any environents, create a new one.
  * In the Actions secrets and variables screen, click on the Variables tab and create a new environment variable with the name `AZURE_WEBPP_NAME` and the value set to the name of your webapp (eg. `onlinebookstoreXXXXXX`).
  * Create new Repository Secret with the name `AZURE_WEBAPP_PUBLISH_PROFILE`, and paste the contents of publishing profile file into it

Finally go to your Github fork, click on the Actions tab, and select "Build and deploy JAR app to Azure Web App" from the menu. Finally, from the right panel, click on Run Workflow.

This will run a workflow that will build the application and deploy the resulting WAR file to Azure App Service using the Github Actions **azure/webapp-deploy** action and the credentials in the publishing profile. Once the workflow completes in Github, you should see your copy of the Online Book Store running on Azure App Service.
