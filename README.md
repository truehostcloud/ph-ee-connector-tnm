# ph-ee-connector-tnm

This connector incorporates job worker implementations responsible for polling available jobs in Zeebe and communicating with TNM Malawi's endpoint.
It currently facilitates the Paybill flow.


This connector is a Zeebe project that is part of the OAF Payment Hub EE setup. See
the [Payment Hub EE documentation](https://mifos.gitbook.io/docs/payment-hub-ee/overview)
for more information about Zeebe projects and Payment Hub in general.

## Badges

[![Build Status](https://dev.azure.com/OAFDev/prd-pipelines/_apis/build/status/one-acre-fund.ph-ee-connector-tnm?branchName=develop)](https://dev.azure.com/OAFDev/prd-pipelines/_apis/build/status/one-acre-fund.ph-ee-connector-tnm?branchName=develop)

## Tech Stack

- Java 17
- Spring Boot
- Apache Camel
- Zeebe Java Client

## Getting Started

Clone the project

  ```bash
    git clone https://github.com/one-acre-fund/ph-ee-connector-tnm.git
    cd ph-ee-connector-tnm
  ```

This connector is expected to be run alongside other connectors/services. It depends on some of
those services being up and healthy.
For local development, the services that are most critical for running this project
have been included in the `docker-compose.yml` file. The following components are included:

- Zeebe: A workflow engine for microservices orchestration. This must be running in a healthy state
  otherwise errors
  will occur when the services below attempt to connect to it.
- Zeebe-ops: Provides APIs for carrying out certain operations on zeebe such as uploading a bpmn
  file
- Channel-connector: Provides APIs for initiating collection requests
- Fineract-connector: Acts as Account management system for CLIF - any other ams connector can be used
- Pesa-connector: Acts as an account managment system for Roster

A lot more services can be added to the above based on your needs, but to run the tnm-mw
connector locally, the ones listed above are the required minimum.
Please note that the `docker-compose.yml` file in this repository should NOT be used in a production
environment.

## Running with Docker

There are multiple docker compose files in the project:
- `docker-compose.yml`: This file contains the services required to run the project in a local
  environment with both Fineract and Roster AMS up.
- `docker-compose-fineract.yml`: This file contains the services required to run the project in a local
  environment and spin up only containers needed for Fineract AMS .
- `docker-compose-roster.yml`: This file contains the services required to run the project in a local
    environment and spin up only containers needed for Roster AMS .

Some images listed in the `docker-compose.yml` are available on OAF's Azure Container Registry (
ACR). To be able to pull them, certain permissions must be granted to your azure account. Follow the steps below to
successfully run the project:

- Ensure [Docker](https://docs.docker.com/get-docker/) is installed on your machine

- Authenticate with
  azure. [Install the Azure CLI](https://learn.microsoft.com/en-us/cli/azure/install-azure-cli)
  on your machine if it's not already available, and then run the command below

  ```bash
      az acr login -n oaftech # Log in to OAF's ACR through the Docker CLI.
   ```

- Run the project:

  Update `src/main/resources/application.yml` with the appropriate values where necessary, or
  provide the
  values as environment variables in the `services.fineract-connector.environment` section of
  the `docker-compose.yml`
  file, and run the command below:

  ```bash
      docker compose up -d
   ```

## Usage

To initiate a request, follow the steps below:

- Upload the TNM MW Paybill bpmn (found in `src/main/resources/inbound_tnm_fineract-oaf.bpmn`) through **zeebe-ops**
  by sending a POST request to `http://localhost:5001/zeebe/upload` with the file attached.

- Send a validation request through the **tnm-connector** by sending a GET request
  to `http://localhost:5000/paybill/validate/:accountNumber` with msisdn as the client phone number and an optional BusinessShortCode with a value to determine the OAF AMS to use (short code for Roster or Fineract) in the headers,
  with a sample header as shown below:
  ```
  msisdn: 078123457 # The client's phone number
  BusinessShortCode: 123456 # The business short code
  getAccountDetails:true # Optional. Default value is true. If set to false, the connector will not fetch the client name
  
  ```
  The response will contain the client's name and account number if the account number is valid.
  Response sample:
  ```json
  {
    "status": 200,
    "message": "Account exists",
    "oafTransactionReference": "a18bcbe0-0e47-4736-8b97-bd1d87332f02",
    "clientName": "John Doe"
  }
  ```
  - Send a pay request throught the **TNM connector** by sending a POST request to `http://localhost:5000/paybill/pay` with the following body and headers
    ```json
    {
    "msisdn": "0781234567",
    "amount": "60",
    "trans_id": "RKTQDM7W6SAC",
    "account_number": "10000001",
    "oafTransactionReference": "a18bcbe0-0e47-4736-8b97-bd1d87332f02"
    }
    ```
    - Headers
    ```
    BusinessShortCode: 123456 # The business short code
    ```
    Note:
    - the `oafTransactionReference` is a unique identifier for the transaction and its value is returned during the validation call. It is used to link the pay request with the validation. 
    - In case the pay request does not contain the `oafTransactionReference` or does not come within the time define in the environment variable ``, the connector will redo the validation check before processing the pay request.
- Send a confirmation request to get the status of the transaction through the **TNM connector** by sending a GET request to `http://localhost:5000/paybill/confirm/:oafTransactionReference`.
  The response will contain the status of the transaction.
  Response sample:
  ```json
  {
    "status": 200,
    "message": "Payment successful",
    "receipt_number": "a18bcbe0-0e47-4736-8b97-bd1d87332f02"
  }
  ```
## Troubleshooting

If an error occurs while carrying out any of the steps above, check if the zeebe container is in a
healthy state by
either viewing its state through `docker ps` or sending a GET request
to `http://localhost:9600/health`.
If the zeebe container shows state as unhealthy or the health endpoint doesn't return a 204 status
response, restart the
zeebe container.

## Contributing

See `CONTRIBUTING.md` for ways to get started.
