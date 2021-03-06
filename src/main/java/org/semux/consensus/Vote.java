/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.consensus;

import org.semux.crypto.EdDSA;
import org.semux.crypto.EdDSA.Signature;
import org.semux.util.Bytes;
import org.semux.util.SimpleDecoder;
import org.semux.util.SimpleEncoder;

public class Vote {
    public static final boolean VALUE_APPROVE = true;
    public static final boolean VALUE_REJECT = false;

    private VoteType type;
    private boolean value;

    private long height;
    private int view;
    private byte[] blockHash;

    private byte[] encoded;
    private Signature signature;

    public Vote(VoteType type, boolean value, long height, int view, byte[] blockHash) {
        this.type = type;
        this.value = value;
        this.height = height;
        this.view = view;
        this.blockHash = blockHash;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeByte(type.toByte());
        enc.writeBoolean(value);
        enc.writeLong(height);
        enc.writeInt(view);
        enc.writeBytes(blockHash);
        this.encoded = enc.toBytes();
    }

    public Vote(byte[] encoded, byte[] signature) {
        this.encoded = encoded;

        SimpleDecoder dec = new SimpleDecoder(encoded);
        this.type = VoteType.of(dec.readByte());
        this.value = dec.readBoolean();
        this.height = dec.readLong();
        this.view = dec.readInt();
        this.blockHash = dec.readBytes();

        this.signature = Signature.fromBytes(signature);
    }

    public static Vote newApprove(VoteType type, long height, int view, byte[] blockHash) {
        return new Vote(type, VALUE_APPROVE, height, view, blockHash);
    }

    public static Vote newReject(VoteType type, long height, int view) {
        return new Vote(type, VALUE_REJECT, height, view, Bytes.EMPTY_HASH);
    }

    /**
     * Sign this vote.
     * 
     * @param key
     * @return
     */
    public Vote sign(EdDSA key) {
        this.signature = key.sign(encoded);
        return this;
    }

    /**
     * validate the vote format and signature.
     * 
     * @return
     */
    public boolean validate() {
        return type != null //
                && height > 0 //
                && view >= 0 //
                && blockHash != null && blockHash.length == 32 //
                && encoded != null //
                && signature != null && EdDSA.verify(encoded, signature);
    }

    public VoteType getType() {
        return type;
    }

    public boolean getValue() {
        return value;
    }

    public long getHeight() {
        return height;
    }

    public int getView() {
        return view;
    }

    public byte[] getBlockHash() {
        return blockHash;
    }

    public byte[] getEncoded() {
        return encoded;
    }

    public Signature getSignature() {
        return signature;
    }

    public byte[] toBytes() {
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeBytes(encoded);
        enc.writeBytes(signature.toBytes());

        return enc.toBytes();
    }

    public static Vote fromBytes(byte[] bytes) {
        SimpleDecoder dec = new SimpleDecoder(bytes);
        byte[] encoded = dec.readBytes();
        byte[] signature = dec.readBytes();

        return new Vote(encoded, signature);
    }

    @Override
    public String toString() {
        return "Vote [" + type + ", " + (value ? "approve" : "reject") + ", height=" + height + ", view=" + view + "]";
    }
}
