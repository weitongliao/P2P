package com.example.p2p;

import org.jgroups.Header;
import org.jgroups.conf.ClassConfigurator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Supplier;

public class FileHeader extends Header {

    private String message;
    private static final short HEADER_ID = 1000;


    public FileHeader(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }



    @Override
    public short getMagicId() {
        return HEADER_ID;
    }

    @Override
    public Supplier<? extends Header> create() {
        return null;
    }

    @Override
    public int serializedSize() {
        return 0;
    }

    @Override
    public void writeTo(DataOutput dataOutput) throws IOException {

    }

    @Override
    public void readFrom(DataInput dataInput) throws IOException, ClassNotFoundException {

    }
}
