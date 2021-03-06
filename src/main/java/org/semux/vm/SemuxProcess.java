/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.vm;

import org.semux.Kernel;
import org.semux.vm.exception.InvalidOpCodeException;
import org.semux.vm.exception.OutOfLimitException;
import org.semux.vm.exception.StackOverflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SemuxProcess implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SemuxProcess.class);

    private SemuxRuntime rt;
    private byte[] code;
    private long limit;

    private Status status = Status.INIT;

    private long[] stack;
    private byte[] heap;

    /**
     * Create a process that runs the specified byte code, with a gas limit.
     * 
     * @param kernel
     * @param rt
     * @param code
     * @param limit
     */
    public SemuxProcess(Kernel kernel, SemuxRuntime rt, byte[] code, long limit) {
        this.rt = rt;
        this.code = code;
        this.limit = limit;

        this.stack = new long[kernel.getConfig().vmMaxStackSize()];
        this.heap = new byte[kernel.getConfig().vmInitialHeapSize()];
    }

    @Override
    public void run() {
        if (status != Status.INIT) {
            logger.error("This process has been started, status: {}", status);
            return;
        } else {
            status = Status.RUNNING;
        }

        try {
            int size = 0; // stack size
            int meter = 0; // computation meter

            for (byte c : code) {
                // check gas
                if (meter >= limit) {
                    throw new OutOfLimitException();
                }

                // read opcode
                Opcode op = Opcode.of(0xff & c);
                if (op == null) {
                    throw new InvalidOpCodeException(c);
                }

                // check stack requirements
                if (op.getRequires() > size || op.getProduces() > stack.length - size) {
                    throw new StackOverflowException();
                }

                // TODO: interpret opcode
            }
        } catch (Exception e) {
            logger.debug("VM exception", e);
        } finally {
            status = Status.STOPPED;
        }
    }

    public SemuxRuntime getRt() {
        return rt;
    }

    public byte[] getCode() {
        return code;
    }

    public Status getStatus() {
        return status;
    }

    public long[] getStack() {
        return stack;
    }

    public byte[] getHeap() {
        return heap;
    }

    @Override
    public String toString() {
        return "SemuxProcess [code.length=" + code.length + ", gasLimit=" + limit + ", status=" + status + "]";
    }

    public enum Status {
        INIT, RUNNING, STOPPED
    }
}
