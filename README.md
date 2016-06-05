# Play with Samza, NLP and Arthur Conan Doyle

This code is meant to be run in connection with a Kafka producer (see https://github.com/torito1984/kafka-doyle-generator.git).

It includes two Samza examples

- extract-location : It extract locations from the passages published to a kafka topic an publishes the extracted location
from these texts into another topic. Locations are extracted with Stanford NLP.
- count-by-location: Picks the locations detected by the previous job and creates a windowed sum that updates. 
Times can be configured in the properties file of the job.

Examples of use (after building with 'mvn package')

# To extract
./run-job.sh --config-factory=org.apache.samza.config.factories.PropertiesConfigFactory --config-path=./target/samza-locations-1.0-dist/config/extract-location.properties

# Windowed sum
./run-job.sh --config-factory=org.apache.samza.config.factories.PropertiesConfigFactory --config-path=./target/samza-locations-1.0-dist/config/count-by-location.properties

The code has been tested with Kafka 0.9.0.4 included in Hortonworks HDP 2.4.0. It supposes that Kafka is available in localhost:6667 and Zookeeper in localhost:2181. These locations can be configured in the scripts.
