package com.sohu.focus.libandfixtool.build;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.DigestOutputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import Decoder.BASE64Encoder;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

public class SignedJarBuilder
{
    private static final String DIGEST_ALGORITHM = "SHA1";
    private static final String DIGEST_ATTR = "SHA1-Digest";
    private static final String DIGEST_MANIFEST_ATTR = "SHA1-Digest-Manifest";
    private JarOutputStream mOutputJar;
    private PrivateKey mKey;
    private X509Certificate mCertificate;
    private Manifest mManifest;
    private BASE64Encoder mBase64Encoder;
    private MessageDigest mMessageDigest;
    private byte[] mBuffer = new byte[4096];

    public SignedJarBuilder(OutputStream out, PrivateKey key, X509Certificate certificate)
            throws IOException, NoSuchAlgorithmException
    {
        this.mOutputJar = new JarOutputStream(new BufferedOutputStream(out));
        this.mOutputJar.setLevel(9);
        this.mKey = key;
        this.mCertificate = certificate;
        if ((this.mKey != null) && (this.mCertificate != null)) {
            this.mManifest = new Manifest();
            Attributes main = this.mManifest.getMainAttributes();
            main.putValue("Manifest-Version", "1.0");
            main.putValue("Created-By", "1.0 (ApkPatch)");
            this.mBase64Encoder = new BASE64Encoder();
            this.mMessageDigest = MessageDigest.getInstance("SHA1");
        }
    }

    public void writeFile(File inputFile, String jarPath)
            throws IOException
    {
        FileInputStream fis = new FileInputStream(inputFile);
        try
        {
            JarEntry entry = new JarEntry(jarPath);
            entry.setTime(inputFile.lastModified());
            writeEntry(fis, entry);
        }
        finally {
            fis.close();
        }
    }

    public void writeZip(InputStream input, IZipEntryFilter filter)
            throws IOException, SignedJarBuilder.IZipEntryFilter.ZipAbortException
    {
        ZipInputStream zis = new ZipInputStream(input);
        try
        {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null)
            {
                String name = entry.getName();

                if ((entry.isDirectory()) || (name.startsWith("META-INF/")))
                {
                    continue;
                }
                if ((filter != null) && (!filter.checkEntry(name)))
                    continue;
                JarEntry newEntry;
                if (entry.getMethod() == ZipEntry.STORED) {
                    newEntry = new JarEntry(entry);
                }
                else {
                    newEntry = new JarEntry(name);
                }
                writeEntry(zis, newEntry);
                zis.closeEntry();
            }
        } finally {
            zis.close();
        }
    }

    public void close()
            throws IOException, GeneralSecurityException
    {
        if (this.mManifest != null)
        {
            this.mOutputJar.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            this.mManifest.write(this.mOutputJar);

            Signature signature = Signature.getInstance("SHA1with" + this.mKey.getAlgorithm());
            signature.initSign(this.mKey);
            this.mOutputJar.putNextEntry(new JarEntry("META-INF/CERT.SF"));
            SignatureOutputStream out = new SignatureOutputStream(this.mOutputJar, signature);
            writeSignatureFile(out);

            this.mOutputJar.putNextEntry(new JarEntry("META-INF/CERT." + this.mKey.getAlgorithm()));
            writeSignatureBlock(signature, this.mCertificate, this.mKey);

            out.close();
        }
        this.mOutputJar.close();
        this.mOutputJar = null;
    }

    public void cleanUp()
    {
        if (this.mOutputJar != null)
            try {
                this.mOutputJar.close();
            }
            catch (IOException localIOException)
            {
            }
    }

    private void writeEntry(InputStream input, JarEntry entry)
            throws IOException
    {
        this.mOutputJar.putNextEntry(entry);
        int count;
        while ((count = input.read(this.mBuffer)) != -1)
        {
            this.mOutputJar.write(this.mBuffer, 0, count);

            if (this.mMessageDigest != null) {
                this.mMessageDigest.update(this.mBuffer, 0, count);
            }
        }

        this.mOutputJar.closeEntry();
        if (this.mManifest != null)
        {
            Attributes attr = this.mManifest.getAttributes(entry.getName());
            if (attr == null) {
                attr = new Attributes();
                this.mManifest.getEntries().put(entry.getName(), attr);
            }
            attr.putValue("SHA1-Digest", this.mBase64Encoder.encode(this.mMessageDigest.digest()));
        }
    }

    private void writeSignatureFile(SignatureOutputStream out) throws IOException, GeneralSecurityException
    {
        Manifest sf = new Manifest();
        Attributes main = sf.getMainAttributes();
        main.putValue("Signature-Version", "1.0");
        main.putValue("Created-By", "1.0 (Android)");
        BASE64Encoder base64 = new BASE64Encoder();
        MessageDigest md = MessageDigest.getInstance("SHA1");
        PrintStream print = new PrintStream(
                new DigestOutputStream(new ByteArrayOutputStream(), md),
                true, "utf-8");

        this.mManifest.write(print);
        print.flush();
        main.putValue("SHA1-Digest-Manifest", base64.encode(md.digest()));
        Map entries = this.mManifest.getEntries();
        Set<Entry> entrySet = entries.entrySet();
        for (Entry entry : entrySet)
        {
            print.print("Name: " + (String)entry.getKey() + "\r\n");
            for (Entry att : ((Attributes)entry.getValue()).entrySet()) {
                print.print(att.getKey() + ": " + att.getValue() + "\r\n");
            }
            print.print("\r\n");
            print.flush();
            Attributes sfAttr = new Attributes();
            sfAttr.putValue("SHA1-Digest", base64.encode(md.digest()));
            sf.getEntries().put((String)entry.getKey(), sfAttr);
        }
        sf.write(out);

        if (out.size() % 1024 == 0) {
            out.write(13);
            out.write(10);
        }
    }

    private void writeSignatureBlock(Signature signature, X509Certificate publicKey, PrivateKey privateKey)
            throws IOException, GeneralSecurityException
    {
        SignerInfo signerInfo = new SignerInfo(
                new X500Name(publicKey.getIssuerX500Principal().getName()),
                publicKey.getSerialNumber(),
                AlgorithmId.get("SHA1"),
                AlgorithmId.get(privateKey.getAlgorithm()),
                signature.sign());
        PKCS7 pkcs7 = new PKCS7(
                new AlgorithmId[] { AlgorithmId.get("SHA1") },
                new ContentInfo(ContentInfo.DATA_OID, null),
                new X509Certificate[] { publicKey },
                new SignerInfo[] { signerInfo });
        pkcs7.encodeSignedData(this.mOutputJar);
    }

    public JarOutputStream getOutputStream() {
        return this.mOutputJar;
    }

    public static abstract interface IZipEntryFilter
    {
        public abstract boolean checkEntry(String paramString)
                throws SignedJarBuilder.IZipEntryFilter.ZipAbortException;

        public static class ZipAbortException extends Exception
        {
            private static final long serialVersionUID = 1L;

            public ZipAbortException()
            {
            }

            public ZipAbortException(String format, Object[] args)
            {
                super();
            }
            public ZipAbortException(Throwable cause, String format, Object[] args) {
                super(cause);
            }
            public ZipAbortException(Throwable cause) {
                super();
            }
        }
    }

    private static class SignatureOutputStream extends FilterOutputStream
    {
        private Signature mSignature;
        private int mCount = 0;

        public SignatureOutputStream(OutputStream out, Signature sig) {
            super(out);
            this.mSignature = sig;
        }

        public void write(int b) throws IOException
        {
            try {
                this.mSignature.update((byte)b);
            } catch (SignatureException e) {
                throw new IOException("SignatureException: " + e);
            }
            super.write(b);
            this.mCount += 1;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            try {
                this.mSignature.update(b, off, len);
            } catch (SignatureException e) {
                throw new IOException("SignatureException: " + e);
            }
            super.write(b, off, len);
            this.mCount += len;
        }
        public int size() {
            return this.mCount;
        }
    }
}
