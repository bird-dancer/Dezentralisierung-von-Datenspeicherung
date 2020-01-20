package src;

import java.util.Comparator;

public class SortAddress implements Comparator<Address> {
    @Override
    public int compare(Address o1, Address o2) {
        return o1.getId() - o2.getId();
    }
}