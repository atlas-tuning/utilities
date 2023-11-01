package com.github.manevolent.atlas.uds.response;

import com.github.manevolent.atlas.BitReader;
import com.github.manevolent.atlas.Frame;
import com.github.manevolent.atlas.uds.UDSFrameType;
import com.github.manevolent.atlas.uds.UDSResponse;

import java.io.IOException;

public class UDSSecurityAccessResponse extends UDSResponse implements Frame {
    private int seed; // Supposed to be odd values
    private byte[] data; // Vendor-specific key

    @Override
    public UDSFrameType getType() {
        return UDSFrameType.SECURITY_ACCESS;
    }

    @Override
    public void read(BitReader reader) throws IOException {
        this.seed = reader.readByte();

        this.data = new byte[reader.remainingBytes()];
        reader.read(data);
    }

    public int getSeed() {
        return seed;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "seed=" + seed + " key=" + toHexString();
    }

}
