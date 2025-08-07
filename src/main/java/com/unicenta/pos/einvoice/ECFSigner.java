package com.unicenta.pos.einvoice;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.*;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import javax.xml.transform.TransformerFactory;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.crypto.dsig.CanonicalizationMethod;

public class ECFSigner {
    
    public static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";


    public String sign(String unsignedXmlPath) throws Exception {
        String certPath = ECFConfig.get("certificate.path");
        String certPassword = ECFConfig.get("certificate.pass");

        // Load keystore
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(certPath), certPassword.toCharArray());

        String alias = ks.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) ks.getKey(alias, certPassword.toCharArray());
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

        // Load XML document
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(unsignedXmlPath));

        // Prepare XML Signature Factory
        XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");

        // Create references
        Reference ref = sigFactory.newReference(
            "", // whole document
            sigFactory.newDigestMethod(DigestMethod.SHA256, null),
            Collections.singletonList(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
            null, null
        );

        // Create SignedInfo
        SignedInfo signedInfo = sigFactory.newSignedInfo(
    sigFactory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
    sigFactory.newSignatureMethod(RSA_SHA256, null),
    Collections.singletonList(ref)
);

        // KeyInfo
        KeyInfoFactory kif = sigFactory.getKeyInfoFactory();
        X509Data x509Data = kif.newX509Data(Collections.singletonList(cert));
        KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(x509Data));

        // Sign context
        DOMSignContext domSignContext = new DOMSignContext(privateKey, doc.getDocumentElement());
        XMLSignature signature = sigFactory.newXMLSignature(signedInfo, keyInfo);
        signature.sign(domSignContext);

        // Save signed XML
        String signedPath = unsignedXmlPath.replace(".xml", "_signed.xml");
        TransformerFactory.newInstance().newTransformer()
            .transform(new javax.xml.transform.dom.DOMSource(doc), new javax.xml.transform.stream.StreamResult(new FileOutputStream(signedPath)));

        return signedPath;
    }
}
