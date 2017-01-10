# openssl req -newkey rsa:4096 -keyout serverkey.pem -out serverreq.csr
# openssl pkcs8 -topk8 -inform PEM -outform PEM -in serverkey.pem -out serverkeynew.pem -nocrypt


# openssl req -newkey rsa:4096 -keyout serverkey.pem -out serverreq.csr

Ir a https://www.startssl.com/Account
https://www.startssl.com/OTPLogin

Extracting cert & keys from JKS:
# keytool -importkeystore -srckeystore keystore.jks -destkeystore keystore.p12 -deststoretype PKCS12 -srcalias <jkskeyalias> -deststorepass <password> -destkeypass <password>
# openssl pkcs12 -in keystore.p12  -nokeys -out cert.pem
# openssl pkcs12 -in keystore.p12  -nodes -nocerts -out key.pem


Concatenar los PEMs de la CA, de la intermedia y el servercert.pem, todo en servercert.pem
