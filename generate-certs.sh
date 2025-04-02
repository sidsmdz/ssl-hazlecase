#!/bin/bash

# Script to generate SSL certificates and keystores for Hazelcast testing

# Get the absolute path of the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_DIR="$( cd "$SCRIPT_DIR/.." && pwd )"

# Set variables with absolute paths
OUTPUT_DIR="$PROJECT_DIR/src/test/resources"
SERVER_KEYSTORE="$OUTPUT_DIR/server.jks"
CLIENT_KEYSTORE="$OUTPUT_DIR/client.jks"
SERVER_CERT="$OUTPUT_DIR/server.cer"
CLIENT_CERT="$OUTPUT_DIR/client.cer"
PASSWORD="password"

echo "Script directory: $SCRIPT_DIR"
echo "Project directory: $PROJECT_DIR"
echo "Output directory: $OUTPUT_DIR"

# Create output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

echo "Generating certificates and keystores in $OUTPUT_DIR..."

# Generate server keystore
keytool -genkeypair -alias server -keyalg RSA \
        -keystore "$SERVER_KEYSTORE" -storepass "$PASSWORD" \
        -validity 365 -keysize 2048 \
        -dname "CN=server" -keypass "$PASSWORD"

# Generate client keystore
keytool -genkeypair -alias client -keyalg RSA \
        -keystore "$CLIENT_KEYSTORE" -storepass "$PASSWORD" \
        -validity 365 -keysize 2048 \
        -dname "CN=client" -keypass "$PASSWORD"

# Export server cert
keytool -exportcert -alias server -keystore "$SERVER_KEYSTORE" \
        -storepass "$PASSWORD" -file "$SERVER_CERT"

# Export client cert
keytool -exportcert -alias client -keystore "$CLIENT_KEYSTORE" \
        -storepass "$PASSWORD" -file "$CLIENT_CERT"

# Import server cert into client truststore
keytool -importcert -noprompt -alias server \
        -keystore "$CLIENT_KEYSTORE" -storepass "$PASSWORD" \
        -file "$SERVER_CERT"

# Import client cert into server truststore
keytool -importcert -noprompt -alias client \
        -keystore "$SERVER_KEYSTORE" -storepass "$PASSWORD" \
        -file "$CLIENT_CERT"

echo "Certificates and keystores generated successfully!"

# Copy to main resources for server use
MAIN_RESOURCES_DIR="$PROJECT_DIR/src/main/resources"
mkdir -p "$MAIN_RESOURCES_DIR"

cp "$SERVER_KEYSTORE" "$MAIN_RESOURCES_DIR/"
cp "$CLIENT_KEYSTORE" "$MAIN_RESOURCES_DIR/"

echo "Keystores copied to main resources directory for server use."

# Create SSL properties files
cat > "$MAIN_RESOURCES_DIR/ssl.properties" << EOF
# Server SSL properties
keyStore=server.jks
keyStorePassword=$PASSWORD
keyStoreType=JKS
trustStore=server.jks
trustStorePassword=$PASSWORD
trustStoreType=JKS
protocol=TLS
keyManagerAlgorithm=SunX509
trustManagerAlgorithm=SunX509
EOF

cat > "$OUTPUT_DIR/client-ssl.properties" << EOF
# Client SSL properties
keyStore=client.jks
keyStorePassword=$PASSWORD
keyStoreType=JKS
trustStore=client.jks
trustStorePassword=$PASSWORD
trustStoreType=JKS
protocol=TLS
keyManagerAlgorithm=SunX509
trustManagerAlgorithm=SunX509
EOF

echo "SSL properties files created."
echo "Done!"

# List the files in the output directory to verify
ls -la "$OUTPUT_DIR"
ls -la "$MAIN_RESOURCES_DIR"