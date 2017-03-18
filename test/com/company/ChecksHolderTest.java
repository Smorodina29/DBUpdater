package com.company;

import com.company.check.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Александр on 18.03.2017.
 */
public class ChecksHolderTest {
    @Test
    public void checksGetForUpdateAddressAddress() {
        ArrayList<Check> expected = new ArrayList<>();
        expected.add(new UniqueIdCheck());
        expected.add(new UniqueRowsCheck());
        expected.add(new PresentRowsCheck());

        List<Check> actual = ChecksHolder.getInstance().getChecksFor("address", "address");
        assertEquals(expected, actual);
    }


    @Test
    public void checksGetForAddAddress() {
        ArrayList<Check> expected = new ArrayList<>();
        List<Check> actual = ChecksHolder.getInstance().getChecksFor("address");
        assertEquals(expected, actual);
    }
}
