package com.etour.dto;

/**
 * BRD 3.7 pax/room summary shown before payment. Room assignment assumption
 * (approved): adults are paired 2-per-double room; a leftover adult gets a
 * single room (singlePersonCost). Children/infants share a parent's room and
 * never generate their own room count.
 */
public class RoomSummary {

    private int adults;
    private int children;
    private int infants;
    private int doubleRooms;
    private int singleRooms;
    /** Adults on an extra bed (triple occupancy), BRD 3.7 "No. of extra beds". */
    private int extraBeds;

    public RoomSummary() {
    }

    public RoomSummary(int adults, int children, int infants, int doubleRooms, int singleRooms) {
        this(adults, children, infants, doubleRooms, singleRooms, 0);
    }

    public RoomSummary(int adults, int children, int infants, int doubleRooms, int singleRooms, int extraBeds) {
        this.adults = adults;
        this.children = children;
        this.infants = infants;
        this.doubleRooms = doubleRooms;
        this.singleRooms = singleRooms;
        this.extraBeds = extraBeds;
    }

    public int getExtraBeds() { return extraBeds; }
    public void setExtraBeds(int extraBeds) { this.extraBeds = extraBeds; }

    public int getAdults() { return adults; }
    public void setAdults(int adults) { this.adults = adults; }
    public int getChildren() { return children; }
    public void setChildren(int children) { this.children = children; }
    public int getInfants() { return infants; }
    public void setInfants(int infants) { this.infants = infants; }
    public int getDoubleRooms() { return doubleRooms; }
    public void setDoubleRooms(int doubleRooms) { this.doubleRooms = doubleRooms; }
    public int getSingleRooms() { return singleRooms; }
    public void setSingleRooms(int singleRooms) { this.singleRooms = singleRooms; }
}
