package com.blockchain.mcsblockchain.pojo.crypto;

import com.blockchain.mcsblockchain.Utils.Cryptography;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import it.unisa.dia.gas.jpbc.Element;

import java.io.*;
import java.security.NoSuchAlgorithmException;
//签名类
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Signature implements Serializable {
    private static final long serialVersionUID = /*6296489857804803990L;*/1L;
    public transient Element value;

    public Signature() {
        this.value = Cryptography.G1.newElement();
    }

    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();

        if(this.value == null)
            this.value = Cryptography.G1.newElement();

        byte[] str2 = this.value.toBytes();

        out.writeObject(str2);
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        //invoke default serialization method
        in.defaultReadObject();

        byte[] str2 = (byte[])in.readObject();
        this.value = Cryptography.G1.newElement();

        this.value.setFromBytes(str2);
    }

    public String serialize() throws IOException
    {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
        objOut.writeObject(this);
        return byteOut.toString("ISO-8859-1");
    }

    public Signature deserialize(String str_input) throws IOException, ClassNotFoundException
    {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(str_input.getBytes("ISO-8859-1"));
        ObjectInputStream objIn = new ObjectInputStream(byteIn);

        return (Signature) objIn.readObject();
    }
}
