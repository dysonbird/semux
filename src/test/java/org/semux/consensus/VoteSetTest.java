/**
 * Copyright (c) 2017 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.consensus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.semux.crypto.EdDSA;
import org.semux.util.Bytes;

public class VoteSetTest {

    private long height = 1;
    private int view = 1;

    private EdDSA v1 = new EdDSA();
    private EdDSA v2 = new EdDSA();
    private EdDSA v3 = new EdDSA();
    private EdDSA v4 = new EdDSA();

    private VoteSet vs = null;

    @Before
    public void setup() {
        List<String> list = new ArrayList<>();
        list.add(v1.toAddressString());
        list.add(v2.toAddressString());
        list.add(v3.toAddressString());
        list.add(v4.toAddressString());

        vs = new VoteSet(VoteType.VALIDATE, height, view, list);
    }

    @Test
    public void testAddVote() {
        Vote vote = Vote.newApprove(VoteType.VALIDATE, height, view, Bytes.EMPTY_HASH);
        assertFalse(vs.addVote(vote));
        vote.sign(new EdDSA());
        assertFalse(vs.addVote(vote));
        vote.sign(v1);
        assertTrue(vs.addVote(vote));

        vote = Vote.newApprove(VoteType.VALIDATE, height + 1, view, Bytes.EMPTY_HASH);
        vote.sign(v1);
        assertFalse(vs.addVote(vote));

        vote = Vote.newApprove(VoteType.VALIDATE, height, view + 1, Bytes.EMPTY_HASH);
        vote.sign(v1);
        assertFalse(vs.addVote(vote));
    }

    @Test
    public void testClear() {
        Vote vote = Vote.newApprove(VoteType.VALIDATE, height, view, Bytes.EMPTY_HASH);
        vote.sign(v1);
        assertTrue(vs.addVote(vote));
        Vote vote2 = Vote.newReject(VoteType.VALIDATE, height, view);
        vote2.sign(v1);
        assertTrue(vs.addVote(vote2));

        assertEquals(1, vs.getApprovals(Bytes.EMPTY_HASH).size());
        assertEquals(1, vs.getRejections().size());
        vs.clear();
        assertEquals(0, vs.getApprovals(Bytes.EMPTY_HASH).size());
        assertEquals(0, vs.getRejections().size());
    }

    @Test
    public void testAddVotes() {
        Vote vote = Vote.newApprove(VoteType.VALIDATE, height, view, Bytes.EMPTY_HASH);
        vote.sign(v1);
        Vote vote2 = Vote.newReject(VoteType.VALIDATE, height, view);
        vote2.sign(v1);
        assertEquals(2, vs.addVotes(Arrays.asList(vote, vote2)));
        assertEquals(2, vs.size());

        assertEquals(1, vs.getApprovals(Bytes.EMPTY_HASH).size());
        assertEquals(1, vs.getRejections().size());
    }

    @Test
    public void testTwoThirds() {
        Vote vote = Vote.newApprove(VoteType.VALIDATE, height, view, Bytes.EMPTY_HASH);
        vote.sign(v1);
        assertTrue(vs.addVote(vote));
        assertFalse(vs.anyApproved().isPresent());

        vote.sign(v2);
        assertTrue(vs.addVote(vote));
        assertFalse(vs.anyApproved().isPresent());

        vote.sign(v3);
        assertTrue(vs.addVote(vote));
        assertTrue(vs.anyApproved().isPresent());
        assertTrue(vs.isApproved(Bytes.EMPTY_HASH));
    }

    @Test
    public void testToString() {
        assertTrue(!vs.toString().startsWith("java.lang.Object"));
    }
}
