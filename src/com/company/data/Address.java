package com.company.data;

/**
 * Created by Александр on 16.10.2016.
 */
public class Address {
    int id;
    String address;
    int regionId;

    public Address(int id, String address, int regionId) {
        this.id = id;
        this.address = address;
        this.regionId = regionId;
    }

    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public int getRegionId() {
        return regionId;
    }
}
