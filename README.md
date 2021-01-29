# synbio-toolkit
Tools for interacting with SynBioHub

## Run the SynBioHub Docker Container Locally

1. Start the requisite Virtuoso DB container:
```
docker run --name my-virtuoso -p 8890:8890 -p 1111:1111 -e DBA_PASSWORD=dba -e SPARQL_UPDATE=true -e DEFAULT_GRAPH=http://www.example.com/my-graph -v './data/virtuoso:/data:rw' -d tenforce/virtuoso:virtuoso7.2.5
```
2. Start the SynBioHub container:
```
docker run -d --name synbiohub --link my-virtuoso:virtuoso -p 7777:7777 --entrypoint /synbiohub/entry.sh -v './data/virtuoso:/virtuoso' synbiohub/synbiohub:1.6.0-standalone
```

### Configure SynBioHub

1. After the Docker container has started, visit http://localhost:7777/ in a browser
2. In the setup form, change the Virtuoso config fields as follows (these correspond to the volumes we mounted in the Virtuoso Docker run command above):
  * virtuoso.ini = /data/virtuoso.ini
  * virtuoso data = /data

## Building the CLI Application

In NetBeans, right-click on the project in the Projects explorer window, and select 'Set Configuration' -> 'cli' from the context menu.

## Launching the CLI Application

```
java -jar target/sbol-toolkit-web-1.0.0-SNAPSHOT.war --collection-url=http://localhost:7777/user/Johnny/a_random_id/a_random_id_collection/1 --username=<email> password=<password> --dir-path=D:\temp\sbol\codA_Km_0081_slr1130.xml --file-ext-filter=xml --overwrite=true
```
To see the CLI help command run `java -jar target/sbol-toolkit-web-1.0.0-SNAPSHOT.war --help`.

