package com.blockchain.mcsblockchain.Utils;

import java.io.*;

public class Time implements Serializable {

        private static final long serialVersionUID =1L;
        double value;

    public Time() {

        this.value = System.currentTimeMillis()/1000.0;
    }

    public Time(double value) {
        this.value = value;
    }

    public String serialize() throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objectOS = new ObjectOutputStream(out);
            objectOS.writeObject(this);
            String res = out.toString("ISO-8859-1");
            return res;
        }

        public Time deserialize(String str) throws IOException, ClassNotFoundException {
            ByteArrayInputStream byteIn = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
            ObjectInputStream objIn = new ObjectInputStream(byteIn);
            Time res =(Time) objIn.readObject();
            return res;
        }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
        public String toString() {
            return "Time{" +
                    "value=" + value +
                    '}';
        }

}
