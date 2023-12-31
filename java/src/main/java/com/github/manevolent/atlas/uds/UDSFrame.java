package com.github.manevolent.atlas.uds;

import com.github.manevolent.atlas.*;

import java.io.IOException;

public class UDSFrame implements Frame, Addressed {
    private final UDSProtocol protocol;
    private Address address;
    private UDSBody body;
    private byte[] remaining;

    public UDSFrame(UDSProtocol protocol) {
        this.protocol = protocol;
    }

    public UDSFrame(UDSProtocol protocol, UDSBody body) {
        this.protocol = protocol;
        this.body = body;
    }

    @Override
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public UDSProtocol getProtocol() {
        return protocol;
    }

    public UDSBody getBody() {
        return body;
    }

    public void setBody(UDSBody body) {
        this.body = body;
    }

    public int getServiceId() {
        UDSBody body = getBody();
        if (body == null) {
            throw new NullPointerException("body");
        }

        return protocol.getSid(body.getClass());
    }

    public void write(BitWriter writer) throws IOException {
        int sid = getServiceId();
        writer.write(sid);
        getBody().write(writer);
    }

    public void read(BitReader reader) throws IOException {
        int serviceId = (reader.readByte() & 0xFF);

        Class<? extends UDSBody> clazz;

        try {
            clazz = protocol.getClassBySid(serviceId);
        } catch (IllegalArgumentException unknownSidException) {
            clazz = UDSUnknownBody.class;
        }

        UDSBody body;
        try {
            body = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        try {
            body.read(reader);
        } catch (IOException ex) {
            String frameString;
            try {
                frameString = toString();
            } catch (Exception ex2) {
                ex.addSuppressed(ex2);
                frameString = "(error)";
            }

            throw new IOException("Problem reading frame " + frameString, ex);
        } catch (UnsupportedOperationException ex) {
            String frameString;
            try {
                frameString = toString();
            } catch (Exception ex2) {
                ex.addSuppressed(ex2);
                frameString = "(error)";
            }

            throw new IOException("TODO Implement " + body.getClass().getName()
                    + ": frame " + frameString, ex);
        }

        setBody(body);

        if (reader.remaining() > 0) {
            remaining = reader.readRemaining();
        }
    }

    @Override
    public byte[] getData() {
        if (body == null) {
            return null;
        }

        return body.getData();
    }

    @Override
    public String toString() {
        String sidString;
        try {
            sidString = String.format("0x%02X", getServiceId());
        } catch (Exception ex) {
            sidString = "(unknown)";
        }
        String fullyReadWarning = remaining != null ? " remaining=" + Frame.toHexString(remaining) : "";
        return getAddress().toString() + " " + sidString + " " + body.getClass().getSimpleName()
                + " " + body.toString() + fullyReadWarning;
    }
}
