# cmis
Connector to execution operations on a CMIS repository

CMIS stand for Content Management Interoperability Services. This protocole is used by different product (Alfresco, Documentum) to store document.

The connector has different function:
* create folder
* upload a document from a FileStorage or a Camunda Document to the CMIS system
* download a document from CMIS to a FileStorage or a Camunda Document
* delete folder
* delete document


# Start a CMIS docker
See [lightCMISServer/README.md](lightCMISserver/README.md)

# Connect

Use a JSON connection. For example

```json
{
  "url":"http://localhost:8099/cmis/browser",
  "userName":"test",
  "password":"test"}
```